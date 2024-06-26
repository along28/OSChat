package server;

import room.Room;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SocketServer {
    private ServerSocket serverSocket;
    private ArrayList<SocketServerThread> socketThreads;
    private ArrayList<Room> rooms;
    private int maxThreads;
    private boolean alive;

    public SocketServer(int maxThreads) {
        alive = true;
        socketThreads = new ArrayList<>();
        rooms = new ArrayList<>();
        this.maxThreads = maxThreads;
    }

    public void initServer(String ipAddress, int port) throws IOException {
        InetAddress inetAddress = InetAddress.getByName(ipAddress);
        serverSocket = new ServerSocket(port, 0, inetAddress);
        System.out.println("Server initialized on IP " + ipAddress + " and port " + port + " but not listening yet.");
    }

    public void listen() throws IOException {
        System.out.println("Server is now listening on IP " + serverSocket.getInetAddress().getHostAddress() + " and port " + serverSocket.getLocalPort() + "\n");
        listenToClients();
    }

    private synchronized void listenToClients() throws IOException {
        Socket socket;
        while (alive) {
            System.out.println("Waiting for a client to connect");
            socket = serverSocket.accept();
            if (!alive) {
                break;
            }
            if (socketThreads.size() < maxThreads) {
                SocketServerThread newThread = new SocketServerThread(socket, socketThreads, rooms);
                socketThreads.add(newThread);
                new Thread(newThread).start();
                System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
            } else {
                System.out.println("Max threads reached, connection refused");
                socket.close();
            }
        }
        for (SocketServerThread thread : socketThreads) {
            thread.killThread();
        }
    }

    public void killServer() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) { // Check if serverSocket is not null and not closed before invoking getInetAddress
            InetAddress serverIP = serverSocket.getInetAddress();
            alive = false;
            new Socket(serverSocket.getInetAddress(), serverSocket.getLocalPort()).close();
            serverSocket.close();
        }
    }
    // Getter method for serverSocket
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public static void main(String[] args) {
        String ipAddress = "127.0.0.1"; // Hardcoded IP address
        int port = 6000; // Hardcoded port number
        int maxThreads = 20; // Specify the maximum number of threads here

        SocketServer server = new SocketServer(maxThreads);
        try {
            server.initServer(ipAddress, port);
            server.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}