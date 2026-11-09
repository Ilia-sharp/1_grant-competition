package academy.tochkavhoda.competition.api.dto.responses;

import academy.tochkavhoda.competition.application.results.GrantResult;

public class GrantResponse {
    public final int applicationId;
    public final String title;
    public final String companyName;
    public final double averageRating;
    public final long grantSum;

    public GrantResponse(int applicationId, String title, String companyName, double averageRating, long grantSum) {
        this.applicationId = applicationId;
        this.title = title;
        this.companyName = companyName;
        this.averageRating = averageRating;
        this.grantSum = grantSum;
    }

    public static GrantResponse from(GrantResult result) {
        return new GrantResponse(result.applicationId, result.title, result.companyName,
                result.averageRating, result.grantSum);
    }
}
