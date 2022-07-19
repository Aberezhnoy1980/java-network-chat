package ru.aberezhnoy.io.network.chat.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {

    private class User {
        String login;
        String password;
        String nickname;

        public User(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }
    private final List<User> users;

    public InMemoryAuthenticationProvider () {
        this.users = new ArrayList<>(Arrays.asList(
                new User("Bob", "111", "Bober"),
                new User ("Max", "222", "Maxut"),
                new User ("Rex", "333", "Rectal")
        ));
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if(u.login.equals(login) && u.password.equals(password)) {
                return u.nickname;
            }
        }
        return null;
    }

    @Override
    public void changeNickname(String oldNickname, String newNickname) {
        for(User u : users) {
            if(u.nickname.equals(oldNickname)) {
                u.nickname = newNickname;
                return;
            }
        }
    }

    @Override
    public boolean isNickBusy(String nickname) {
        for(User u : users) {
            if (u.nickname.equals(nickname)) {
                return true;
            }
        } return false;
    }

    @Override
    public void init() {

    }

    @Override
    public void shutdown() {

    }
}
