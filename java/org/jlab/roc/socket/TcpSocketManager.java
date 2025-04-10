package org.jlab.roc.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

public class TcpSocketManager {
    private final Map<String, ServerSocket> sockets;

    public TcpSocketManager() {
        this.sockets = new ConcurrentHashMap<>();
    }

    public void createSocketForIP(String sourceIP, int port, String targetIP, int maxConnections, int bufferSize, int connectionTimeout) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(connectionTimeout);
        sockets.put(sourceIP, serverSocket);
        
        // Start a new thread to handle connections
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setReceiveBufferSize(bufferSize);
                    // Handle the connection in a separate thread
                    new Thread(() -> handleConnection(clientSocket)).start();
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    System.err.println("Error accepting connection for " + sourceIP + ": " + e.getMessage());
                }
            }
        }).start();
    }

    private void handleConnection(Socket clientSocket) {
        try {
            // Here you would implement the actual data handling
            // For now, we just close the connection
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
        }
    }

    public void closeAllSockets() {
        for (ServerSocket socket : sockets.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
        sockets.clear();
    }
} 