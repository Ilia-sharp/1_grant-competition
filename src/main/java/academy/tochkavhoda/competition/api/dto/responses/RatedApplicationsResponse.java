package academy.tochkavhoda.competition.api.dto.responses;

import academy.tochkavhoda.competition.application.results.RatedApplicationResult;

import java.util.ArrayList;
import java.util.List;

public class RatedApplicationsResponse {
    public final List<RatedApplicationResponse> applications;

    public RatedApplicationsResponse(List<RatedApplicationResponse> applications) {
        this.applications = applications;
    }

    public static RatedApplicationsResponse from(List<RatedApplicationResult> results) {
        List<RatedApplicationResponse> applications = new ArrayList<>();
        for (RatedApplicationResult result : results) {
            applications.add(RatedApplicationResponse.from(result));
        }
        return new RatedApplicationsResponse(applications);
    }
}
