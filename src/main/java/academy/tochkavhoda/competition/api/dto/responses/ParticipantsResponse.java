package academy.tochkavhoda.competition.api.dto.responses;

import academy.tochkavhoda.competition.application.results.ParticipantResult;

import java.util.ArrayList;
import java.util.List;

public class ParticipantsResponse {
    public final List<ParticipantResponse> participants;

    public ParticipantsResponse(List<ParticipantResponse> participants) {
        this.participants = participants;
    }

    public static ParticipantsResponse from(List<ParticipantResult> results) {
        List<ParticipantResponse> participants = new ArrayList<>();
        for (ParticipantResult result : results) {
            participants.add(ParticipantResponse.from(result));
        }
        return new ParticipantsResponse(participants);
    }
}
