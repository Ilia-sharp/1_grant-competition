package academy.tochkavhoda.competition.application.commands;

/** Команда: зарегистрировать участника конкурса. */
public class RegisterParticipantCommand {
    public final String firstName;
    public final String lastName;
    public final String companyName;
    public final String login;
    public final String password;

    public RegisterParticipantCommand(String firstName, String lastName, String companyName,
                                      String login, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.companyName = companyName;
        this.login = login;
        this.password = password;
    }
}
