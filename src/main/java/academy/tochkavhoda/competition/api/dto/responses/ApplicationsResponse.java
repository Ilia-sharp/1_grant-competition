package academy.tochkavhoda.competition.api.dto.responses;

import academy.tochkavhoda.competition.application.results.ApplicationResult;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsResponse {
    public final List<ApplicationResponse> applications;

    public ApplicationsResponse(List<ApplicationResponse> applications) {
        this.applications = applications;
    }

    public static ApplicationsResponse from(List<ApplicationResult> results) {
        List<ApplicationResponse> applications = new ArrayList<>();
        for (ApplicationResult result : results) {
            applications.add(ApplicationResponse.from(result));
        }
        return new ApplicationsResponse(applications);
    }
}
