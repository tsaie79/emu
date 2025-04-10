package org.jlab.coda.emu.modules.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Manages TCP sockets for the ROC simulation in PCAP mode.
 */
public class TcpSocketManager {
    private final Map<String, ServerSocket> serverSockets;

    public TcpSocketManager() {
        this.serverSockets = new ConcurrentHashMap<>();
    }

    public void createSocketForIP(String sourceIP, int port, String targetIp, 
                                int maxConnections, int bufferSize, int connectionTimeout) 
                                throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(connectionTimeout);
        serverSockets.put(sourceIP, serverSocket);

        // Start a thread to handle incoming connections
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleConnection(clientSocket);
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) {
                            System.err.println("Error accepting connection: " + e.getMessage());
                        }
                    }
                }
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        }).start();
    }

    private void handleConnection(Socket clientSocket) {
        try {
            // For now, just close the connection
            // In a real implementation, this would handle the data transfer
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
        }
    }

    public void closeAllSockets() {
        for (Map.Entry<String, ServerSocket> entry : serverSockets.entrySet()) {
            try {
                entry.getValue().close();
            } catch (IOException e) {
                System.err.println("Error closing socket for " + entry.getKey() + ": " + e.getMessage());
            }
        }
        serverSockets.clear();
    }
} 