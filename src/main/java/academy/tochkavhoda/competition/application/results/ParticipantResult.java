package academy.tochkavhoda.competition.application.results;

import academy.tochkavhoda.competition.domain.models.Participant;

/** Данные участника, которые можно показывать другим пользователям (без логина и пароля). */
public class ParticipantResult {
    public final String firstName;
    public final String lastName;
    public final String companyName;

    public ParticipantResult(String firstName, String lastName, String companyName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.companyName = companyName;
    }

    public static ParticipantResult from(Participant participant) {
        return new ParticipantResult(participant.getFirstName(), participant.getLastName(),
                participant.getCompanyName());
    }
}
