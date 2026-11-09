package academy.tochkavhoda.competition.application.commands;

/** Команда: войти на сервер. */
public class LoginCommand {
    public final String login;
    public final String password;

    public LoginCommand(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
