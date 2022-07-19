package ru.aberezhnoy.io.network.chat.server;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ru.aberezhnoy.io.network.chat.client.Controller;

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
    private final String COMMANDS = "\"/w [username] [message]\" - lets you to send a private message to the specific user\n \"/current_account\" - shows your current account\n \"/change_nickname [new nickname]\" - allows to change your current nickname\n \"/exit\" - close current session\n \"change_nick [new nickname]\" - lets you to change current nickname to the specified\n";

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
                        String[] tokens = msg.split("\\s",3);
                        if(tokens.length !=3) {
                            sendMessage("/login_failed. PLease enter your login and password");
                            continue;
                        }
                        String login = tokens[1];
                        String password = tokens[2];

                        String userNickname = server.getAuthenticationProvider().getNicknameByLoginAndPassword(login, password);

                        if(userNickname == null) {
                            sendMessage("/login_failed. Please enter your login and password");
                            continue;
                        }
                        if (server.isUserOnline(userNickname)) {
                            sendMessage("/login_failed. Current nickname is already used");
                            continue;
                        }
                        username = userNickname;
                        sendMessage("/login_ok " + login + " " + username);
                        server.subscribe(this);
                        System.out.println("Client " + username + " is logged in");
                        break;
                    }
                }
                while (true) {
                    String msg = in.readUTF();

                    if (msg.startsWith("/")) {
                        executeCommand(msg);
                        continue;
                    }

                    server.broadcastMessage(username + ": " + msg);
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

    public void executeCommand(String cmd) {
        if (cmd.startsWith("/w ")) {
            String[] tokens = cmd.split("\\s+", 3);
            if (tokens.length != 3) {
                sendMessage("Invalid command. PLease try again");
                return;
            }
            server.sendPrivateMessage(this, tokens[1], tokens[2]);
        }
        if (cmd.equals("/exit")) {
            sendMessage("/exit");
            System.out.println("Client " + username + " is logged out");
            disconnect();
        }
        if (cmd.equals("/who_am_i")) {
            sendMessage("Your current nickname is: " + username);
        }
        if (cmd.equals("/info") || cmd.equals("/?")) {
            sendMessage("Please, use the following commands to get additional services:\n" + COMMANDS);
//            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please, use the following commands to get additional services: " + COMMANDS, ButtonType.OK);
//            alert.showAndWait();
        }
        if (cmd.startsWith("/change_nick ")) {
            String [] tokens = cmd.split("\\s+",2);
            if(tokens.length !=2) {
                sendMessage("Invalid or bad command, please try again");
                return;
            }
            String newNickname = tokens[1];
            if (newNickname.isEmpty()) {
                sendMessage("nickname can't be empty");
                return;
            }
            if(server.isUserOnline(newNickname)) {
                sendMessage("The nickname is already exist. Please chase another one");
                return;
            }
//            server.changeNickname(username, newNickname);
            username = newNickname;
            sendMessage("Your new nickname is: " + newNickname);
            server.broadcastClientsList();
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            disconnect();
        }
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
