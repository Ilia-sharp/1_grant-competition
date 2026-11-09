package academy.tochkavhoda.competition.application.results;

import academy.tochkavhoda.competition.domain.models.CompetitionSummary;
import academy.tochkavhoda.competition.domain.models.Grant;

import java.util.ArrayList;
import java.util.List;

/** Итоги конкурса. */
public class CompetitionSummaryResult {
    public final long fund;
    public final double threshold;
    public final long distributedSum;
    public final long remainingSum;
    public final List<GrantResult> grants;

    public CompetitionSummaryResult(long fund, double threshold, long distributedSum, long remainingSum,
                                    List<GrantResult> grants) {
        this.fund = fund;
        this.threshold = threshold;
        this.distributedSum = distributedSum;
        this.remainingSum = remainingSum;
        this.grants = grants;
    }

    public static CompetitionSummaryResult from(CompetitionSummary summary) {
        List<GrantResult> grants = new ArrayList<>();
        for (Grant grant : summary.getGrants()) {
            grants.add(GrantResult.from(grant));
        }
        return new CompetitionSummaryResult(summary.getFund(), summary.getThreshold(),
                summary.getDistributedSum(), summary.getRemainingSum(), grants);
    }
}
