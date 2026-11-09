package academy.tochkavhoda.competition.api.dto.responses;

import academy.tochkavhoda.competition.application.results.ApplicationResult;

import java.util.List;

public class ApplicationResponse {
    public final int id;
    public final String title;
    public final String description;
    public final List<String> directions;
    public final long requestedSum;
    public final String companyName;

    public ApplicationResponse(int id, String title, String description, List<String> directions,
                               long requestedSum, String companyName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.directions = directions;
        this.requestedSum = requestedSum;
        this.companyName = companyName;
    }

    public static ApplicationResponse from(ApplicationResult result) {
        return new ApplicationResponse(result.id, result.title, result.description, result.directions,
                result.requestedSum, result.companyName);
    }
}
