package academy.tochkavhoda.competition.domain.models;

import java.util.ArrayList;
import java.util.List;

/** Итоги конкурса: общий фонд, пороговое значение и список выделенных грантов. */
public class CompetitionSummary {
    private long fund;
    private double threshold;
    private List<Grant> grants;

    public CompetitionSummary(long fund, double threshold, List<Grant> grants) {
        this.fund = fund;
        this.threshold = threshold;
        this.grants = new ArrayList<>(grants);
    }

    public long getFund() {
        return fund;
    }

    public void setFund(long fund) {
        this.fund = fund;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public List<Grant> getGrants() {
        return grants;
    }

    public void setGrants(List<Grant> grants) {
        this.grants = grants;
    }

    /** Сумма всех выделенных грантов. */
    public long getDistributedSum() {
        long sum = 0;
        for (Grant grant : grants) {
            sum += grant.getSum();
        }
        return sum;
    }

    /** Часть фонда, которая осталась нераспределённой. */
    public long getRemainingSum() {
        return fund - getDistributedSum();
    }
}
