package academy.tochkavhoda.competition.application.commands;

import java.util.List;

/** Команда: зарегистрировать эксперта. */
public class RegisterExpertCommand {
    public final String firstName;
    public final String lastName;
    public final List<String> directions;
    public final String login;
    public final String password;

    public RegisterExpertCommand(String firstName, String lastName, List<String> directions,
                                 String login, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.directions = directions;
        this.login = login;
        this.password = password;
    }
}
