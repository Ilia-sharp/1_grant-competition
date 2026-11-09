package academy.tochkavhoda.competition.api.dto.responses;

import academy.tochkavhoda.competition.application.results.ParticipantResult;

public class ParticipantResponse {
    public final String firstName;
    public final String lastName;
    public final String companyName;

    public ParticipantResponse(String firstName, String lastName, String companyName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.companyName = companyName;
    }

    public static ParticipantResponse from(ParticipantResult result) {
        return new ParticipantResponse(result.firstName, result.lastName, result.companyName);
    }
}
