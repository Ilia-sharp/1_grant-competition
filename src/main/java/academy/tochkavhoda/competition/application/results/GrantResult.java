package academy.tochkavhoda.competition.application.results;

import academy.tochkavhoda.competition.domain.models.Grant;
import academy.tochkavhoda.competition.domain.models.GrantApplication;

/** Грант, выделенный по итогам конкурса. */
public class GrantResult {
    public final int applicationId;
    public final String title;
    public final String companyName;
    public final double averageRating;
    public final long grantSum;

    public GrantResult(int applicationId, String title, String companyName, double averageRating, long grantSum) {
        this.applicationId = applicationId;
        this.title = title;
        this.companyName = companyName;
        this.averageRating = averageRating;
        this.grantSum = grantSum;
    }

    public static GrantResult from(Grant grant) {
        GrantApplication application = grant.getApplication();
        return new GrantResult(application.getId(), application.getTitle(),
                application.getParticipant().getCompanyName(), grant.getAverageRating(), grant.getSum());
    }
}
