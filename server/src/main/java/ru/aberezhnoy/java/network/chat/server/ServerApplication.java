package ru.aberezhnoy.java.network.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class ServerApplication {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Server started at port 8189. Waiting for the Client connection.");
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Client connected.");
            int msgCounter = 0;
            while (true) {
                String msg = in.readUTF();
                msgCounter++;
                if (msg.equals("/stat")) {
                    System.out.print(msg);
                    out.writeUTF("Message count: " + msgCounter);
                } else out.writeUTF("ECHO " + msg + new Date());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
