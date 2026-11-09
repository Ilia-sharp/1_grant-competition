package academy.tochkavhoda.competition.api.dto.requests;

import academy.tochkavhoda.competition.application.commands.RegisterExpertCommand;

import java.util.List;

/** Регистрация эксперта: ФИО, направления экспертизы, логин и пароль. */
public class RegisterExpertRequest {
    public final String firstName;
    public final String lastName;
    public final List<String> directions;
    public final String login;
    public final String password;

    public RegisterExpertRequest(String firstName, String lastName, List<String> directions,
                                 String login, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.directions = directions;
        this.login = login;
        this.password = password;
    }

    public RegisterExpertCommand toCommand() {
        return new RegisterExpertCommand(firstName, lastName, directions, login, password);
    }
}
