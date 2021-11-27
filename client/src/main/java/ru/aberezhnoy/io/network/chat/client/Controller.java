package ru.aberezhnoy.io.network.chat.client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {
    @FXML
    TextField msgField, usernameField;

    @FXML
    TextArea msgArea;

    @FXML
    HBox loginPanel, msgPanel;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public void setUsername(String username) {
        this.username = username;
        if (username != null) {
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
        } else {
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            usernameField.clear();
            msgArea.clear();
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
        }
    }

    public void login() {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        if (usernameField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "username can't be empty", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try {
            out.writeUTF("/login " + usernameField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        // authorization cycle
                        String msg = in.readUTF();
                        if (msg.startsWith("/login_ok ")) {
                            setUsername(msg.split("\\s")[1]); // split по пробелу
                            break;
                        }
                        if (msg.startsWith("/login_failed")) {
                            String cause = msg.split("\\s", 2)[1];
                            msgArea.appendText(cause + "\n");
                        }
                    }
                    // communication cycle
                    msgArea.clear();
                    while (true) {
                        String msg = in.readUTF();
                        msgArea.appendText(msg + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            });
            t.start();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to connect to the server [ localhost: 8189 ]", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            if (msgField.getText().equals("/Exit")) {
                disconnect();
            }
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "The message couldn't be sent", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void disconnect() {
        setUsername(null);
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}