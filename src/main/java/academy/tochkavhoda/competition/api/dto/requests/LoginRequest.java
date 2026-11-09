package academy.tochkavhoda.competition.api.dto.requests;

import academy.tochkavhoda.competition.application.commands.LoginCommand;

/** Вход на сервер: логин и пароль. */
public class LoginRequest {
    public final String login;
    public final String password;

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public LoginCommand toCommand() {
        return new LoginCommand(login, password);
    }
}
