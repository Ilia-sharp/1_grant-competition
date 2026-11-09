package academy.tochkavhoda.competition.api.dto.responses;

import academy.tochkavhoda.competition.application.results.AddApplicationResult;

public class AddApplicationResponse {
    public final int id;

    public AddApplicationResponse(int id) {
        this.id = id;
    }

    public static AddApplicationResponse from(AddApplicationResult result) {
        return new AddApplicationResponse(result.id);
    }
}
