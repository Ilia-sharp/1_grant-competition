package academy.tochkavhoda.competition.api.dto.responses;

import academy.tochkavhoda.competition.application.results.CompetitionSummaryResult;
import academy.tochkavhoda.competition.application.results.GrantResult;

import java.util.ArrayList;
import java.util.List;

public class CompetitionSummaryResponse {
    public final long fund;
    public final double threshold;
    public final long distributedSum;
    public final long remainingSum;
    public final List<GrantResponse> grants;

    public CompetitionSummaryResponse(long fund, double threshold, long distributedSum, long remainingSum,
                                      List<GrantResponse> grants) {
        this.fund = fund;
        this.threshold = threshold;
        this.distributedSum = distributedSum;
        this.remainingSum = remainingSum;
        this.grants = grants;
    }

    public static CompetitionSummaryResponse from(CompetitionSummaryResult result) {
        List<GrantResponse> grants = new ArrayList<>();
        for (GrantResult grant : result.grants) {
            grants.add(GrantResponse.from(grant));
        }
        return new CompetitionSummaryResponse(result.fund, result.threshold, result.distributedSum,
                result.remainingSum, grants);
    }
}
