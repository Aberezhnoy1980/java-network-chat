package ru.aberezhnoy.io.network.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/login ")) {
                        String usernameFromLogin = msg.split("\\s")[1];
                        if (server.isNickBusy(usernameFromLogin)) {
                            sendMessage("/login_failed. Current nickname is already used");
                            continue;
                        }
                        username = usernameFromLogin;
                        sendMessage("/login_ok " + username);
                        server.subscribe(this);
                        System.out.println("Client " + username + " is logged in");
                        break;
                    }
                }

                while (true) {
                    String msg = in.readUTF();

                    // client commands block
                    if (msg.equals("/Who_am_i")) {
                        sendMessage("Your current nickname is: " + username);
                        continue;
                    }
                    if (msg.startsWith("/w ")) {
                        String[] tokens = msg.split("\\s+", 3);
                        if (tokens.length != 3) {
                            sendMessage("Server: incorrect command");
                            continue;
                        }
                        for (ClientHandler r : server.getClients()) {
                            if (r.getUsername().equals(tokens[1])) {
                                r.sendMessage("Message from " + this.username + ": " + tokens[2]);
                                sendMessage("Message to " + tokens[1] + ": " + tokens[2]);
                                break;
                            }
                            sendMessage("Can't send the message to user " + tokens[1] + " there is no such user on the network");
                        }
                        continue;
                    }

                    server.broadcastMessage(username + ": " + msg);

//                    if(msg.startsWith("/Exit")) {
//                        disconnect();
//                        System.out.println("Client " + username + " is logged out");
//                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
    }

    public void disconnect() {
        if (socket != null) {
            server.unsubscribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
