package academy.tochkavhoda.competition.api.dto.responses;

import academy.tochkavhoda.competition.application.results.AddApplicationsResult;

import java.util.List;

public class AddApplicationsResponse {
    public final List<Integer> ids;

    public AddApplicationsResponse(List<Integer> ids) {
        this.ids = ids;
    }

    public static AddApplicationsResponse from(AddApplicationsResult result) {
        return new AddApplicationsResponse(result.ids);
    }
}
