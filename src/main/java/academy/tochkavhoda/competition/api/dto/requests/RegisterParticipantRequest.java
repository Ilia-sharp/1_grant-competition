package academy.tochkavhoda.competition.api.dto.requests;

import academy.tochkavhoda.competition.application.commands.RegisterParticipantCommand;

/** Регистрация участника: ФИО руководителя, название фирмы, логин и пароль. */
public class RegisterParticipantRequest {
    public final String firstName;
    public final String lastName;
    public final String companyName;
    public final String login;
    public final String password;

    public RegisterParticipantRequest(String firstName, String lastName, String companyName,
                                      String login, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.companyName = companyName;
        this.login = login;
        this.password = password;
    }

    public RegisterParticipantCommand toCommand() {
        return new RegisterParticipantCommand(firstName, lastName, companyName, login, password);
    }
}
