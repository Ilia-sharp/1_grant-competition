package academy.tochkavhoda.competition.api.dto.requests;

import academy.tochkavhoda.competition.application.commands.SummarizeCommand;

/** Подведение итогов: общий фонд гранта и пороговое значение средней оценки. */
public class SummarizeRequest {
    public final Long fund;
    public final Double threshold;

    public SummarizeRequest(Long fund, Double threshold) {
        this.fund = fund;
        this.threshold = threshold;
    }

    public SummarizeCommand toCommand() {
        return new SummarizeCommand(fund, threshold);
    }
}
