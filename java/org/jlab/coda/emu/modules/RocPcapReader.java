/*
 * Copyright (c) 2024, Jefferson Science Associates
 *
 * Thomas Jefferson National Accelerator Facility
 * Data Acquisition Group
 *
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 */

package org.jlab.coda.emu.modules;

import org.jlab.coda.emu.Emu;
import org.jlab.coda.emu.support.codaComponent.CODAState;
import org.jlab.coda.emu.support.configurer.DataNotFoundException;
import org.jlab.coda.emu.support.control.CmdExecException;
import org.jlab.coda.emu.support.transport.DataChannel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class implements a ROC module that reads PCAP files and creates TCP sockets
 * for each unique source IP found in the PCAP file.
 */
public class RocPcapReader extends ModuleAdapter {
    // Configuration parameters
    private String pcapFilePath;
    private int basePort;
    private String defaultTargetIp;
    private int maxConnections;
    private int bufferSize;
    private int connectionTimeout;
    
    private final Map<String, SourceIpInfo> sourceIpMap;
    private final Map<String, SocketManager> socketManagers;
    
    // Thread management
    private ExecutorService executorService;
    private RandomAccessFile pcapFile;
    
    /**
     * Constructor for the RocPcapReader module.
     *
     * @param name       name of the module
     * @param attributes configuration attributes
     * @param emu        parent EMU instance
     */
    public RocPcapReader(String name, Map<String, String> attributes, Emu emu) {
        super(name, attributes, emu);
        this.sourceIpMap = new ConcurrentHashMap<>();
        this.socketManagers = new ConcurrentHashMap<>();
        
        // Parse configuration
        parseConfiguration();
    }
    
    /**
     * Parse the module configuration from attributes.
     */
    private void parseConfiguration() {
        try {
            // Basic configuration
            pcapFilePath = getAttr("pcapFile");
            basePort = getIntAttr("basePort");
            defaultTargetIp = getAttr("defaultTargetIp");
            
            // Socket configuration
            maxConnections = getIntAttr("socketConfig/maxConnections");
            bufferSize = getIntAttr("socketConfig/bufferSize");
            connectionTimeout = getIntAttr("socketConfig/connectionTimeout");
            
            // Parse source IP configurations
            parseSourceIpConfigs();
            
        } catch (DataNotFoundException e) {
            errorMsg.set("Configuration error: " + e.getMessage());
            moduleState = CODAState.ERROR;
        }
    }
    
    /**
     * Parse source IP specific configurations.
     */
    private void parseSourceIpConfigs() {
        try {
            // Get source IP configurations from XML
            Document config = emu.configuration();
            if (config != null) {
                NodeList moduleNodes = config.getElementsByTagName("module");
                for (int i = 0; i < moduleNodes.getLength(); i++) {
                    Node moduleNode = moduleNodes.item(i);
                    NamedNodeMap attrs = moduleNode.getAttributes();
                    if (attrs != null) {
                        Node nameAttr = attrs.getNamedItem("name");
                        if (nameAttr != null && nameAttr.getNodeValue().equals(name)) {
                            // Found our module, now look for sourceIpConfig
                            NodeList children = moduleNode.getChildNodes();
                            for (int j = 0; j < children.getLength(); j++) {
                                Node child = children.item(j);
                                if (child.getNodeName().equals("sourceIpConfig")) {
                                    parseSourceIps(child.getChildNodes());
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            errorMsg.set("Source IP configuration error: " + e.getMessage());
            moduleState = CODAState.ERROR;
        }
    }
    
    /**
     * Parse source IP nodes from the configuration.
     */
    private void parseSourceIps(NodeList sourceIpNodes) {
        for (int i = 0; i < sourceIpNodes.getLength(); i++) {
            Node node = sourceIpNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("sourceIp")) {
                NamedNodeMap attrs = node.getAttributes();
                if (attrs != null) {
                    Node ipAttr = attrs.getNamedItem("ip");
                    Node targetIpAttr = attrs.getNamedItem("targetIp");
                    Node portAttr = attrs.getNamedItem("port");
                    
                    if (ipAttr != null && targetIpAttr != null && portAttr != null) {
                        String ip = ipAttr.getNodeValue();
                        String targetIp = targetIpAttr.getNodeValue();
                        int port = Integer.parseInt(portAttr.getNodeValue());
                        
                        SourceIpInfo info = new SourceIpInfo(ip, targetIp, port);
                        sourceIpMap.put(ip, info);
                    }
                }
            }
        }
    }
    
    @Override
    public void go() throws CmdExecException {
        try {
            super.go();
            
            // Open PCAP file
            pcapFile = new RandomAccessFile(pcapFilePath, "r");
            
            // Start thread pool
            executorService = Executors.newCachedThreadPool();
            
            // Start socket managers
            for (Map.Entry<String, SourceIpInfo> entry : sourceIpMap.entrySet()) {
                SourceIpInfo info = entry.getValue();
                SocketManager manager = new SocketManager(info.sourceIp, info.targetIp, info.port);
                socketManagers.put(info.sourceIp, manager);
                manager.start();
            }
            
            // Start PCAP reading thread
            executorService.submit(this::readPcapFile);
            
        } catch (IOException e) {
            moduleState = CODAState.ERROR;
            throw new CmdExecException("Error starting module: " + e.getMessage());
        }
    }
    
    @Override
    public void end() throws CmdExecException {
        try {
            super.end();
            
            // Stop all socket managers
            for (SocketManager manager : socketManagers.values()) {
                manager.stop();
            }
            socketManagers.clear();
            
            // Shutdown thread pool
            if (executorService != null) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Close PCAP file
            if (pcapFile != null) {
                pcapFile.close();
            }
        } catch (IOException e) {
            moduleState = CODAState.ERROR;
            throw new CmdExecException("Error stopping module: " + e.getMessage());
        }
    }
    
    /**
     * Read packets from the PCAP file and distribute them to socket managers.
     */
    private void readPcapFile() {
        try {
            // TODO: Implement PCAP file reading
            // For now, just read raw bytes and distribute them
            byte[] buffer = new byte[bufferSize];
            while (moduleState == CODAState.ACTIVE) {
                int bytesRead = pcapFile.read(buffer);
                if (bytesRead <= 0) {
                    break;
                }
                
                // TODO: Parse packet to get source IP and distribute to correct socket manager
                // For now, just send to all managers
                byte[] packet = Arrays.copyOf(buffer, bytesRead);
                for (SocketManager manager : socketManagers.values()) {
                    manager.queuePacket(packet);
                }
            }
        } catch (IOException e) {
            if (moduleState == CODAState.ACTIVE) {
                errorMsg.set("Error reading PCAP file: " + e.getMessage());
                moduleState = CODAState.ERROR;
            }
        }
    }
    
    /**
     * Inner class to store information about each source IP.
     */
    private static class SourceIpInfo {
        private final String sourceIp;
        private final String targetIp;
        private final int port;
        private long packetCount;
        private long byteCount;
        
        public SourceIpInfo(String sourceIp, String targetIp, int port) {
            this.sourceIp = sourceIp;
            this.targetIp = targetIp;
            this.port = port;
            this.packetCount = 0;
            this.byteCount = 0;
        }
    }
    
    /**
     * Inner class to manage TCP sockets for a source IP.
     */
    private class SocketManager {
        private final String sourceIp;
        private final String targetIp;
        private final int port;
        private ServerSocket serverSocket;
        private final List<Socket> clientSockets;
        private final BlockingQueue<byte[]> packetQueue;
        private final AtomicBoolean running;
        
        public SocketManager(String sourceIp, String targetIp, int port) {
            this.sourceIp = sourceIp;
            this.targetIp = targetIp;
            this.port = port;
            this.clientSockets = new CopyOnWriteArrayList<>();
            this.packetQueue = new LinkedBlockingQueue<>();
            this.running = new AtomicBoolean(false);
        }
        
        public void queuePacket(byte[] packet) {
            packetQueue.offer(packet);
        }
        
        public void start() throws IOException {
            serverSocket = new ServerSocket(port);
            running.set(true);
            
            // Start accepting connections
            executorService.submit(() -> {
                while (running.get()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        clientSockets.add(clientSocket);
                        // Start handling client
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        if (running.get()) {
                            errorMsg.set("Error accepting connection for " + sourceIp + ": " + e.getMessage());
                        }
                    }
                }
            });
        }
        
        private void handleClient(Socket clientSocket) {
            executorService.submit(() -> {
                try {
                    while (running.get()) {
                        byte[] packet = packetQueue.poll(100, TimeUnit.MILLISECONDS);
                        if (packet != null) {
                            clientSocket.getOutputStream().write(packet);
                            clientSocket.getOutputStream().flush();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    if (running.get()) {
                        errorMsg.set("Error sending packet to client: " + e.getMessage());
                    }
                } finally {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                    clientSockets.remove(clientSocket);
                }
            });
        }
        
        public void stop() {
            running.set(false);
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                // Ignore
            }
            for (Socket socket : clientSockets) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            clientSockets.clear();
        }
    }
} 