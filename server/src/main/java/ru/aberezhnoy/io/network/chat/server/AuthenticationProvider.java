package ru.aberezhnoy.io.network.chat.server;

public interface AuthenticationProvider {
    void init ();
    String getNicknameByLoginAndPassword (String login, String password);
    void changeNickname (String oldNickname, String newNickname);
    void shutdown();
    boolean isNickBusy(String nickname);
}
