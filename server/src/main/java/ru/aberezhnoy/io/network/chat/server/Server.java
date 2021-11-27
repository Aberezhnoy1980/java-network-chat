package ru.aberezhnoy.io.network.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server start at port " + port);
            while (true) {
                System.out.println("Waiting for a new client connection..");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        System.out.println("Client subscribed");
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Client unsubscribed");
    }

//    public void sendPrivateMessage(ClientHandler sender, String recipientUsername, String message) throws IOException {
//        for (ClientHandler c : clients) {
//            if (c.getUsername().equals(recipientUsername)) {
//                c.sendMessage("From: " + sender.getUsername() + " message: " + message);
//                sender.sendMessage("To user: " + recipientUsername + " message: " + message);
//                return;
//            }
//        }
//        sender.sendMessage("Can't send the message to user " + recipientUsername + " there is no such user on the network");
//    }

    public void broadcastMessage(String message) throws IOException {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    public boolean isNickBusy(String username) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
