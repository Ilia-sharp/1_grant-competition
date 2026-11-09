package academy.tochkavhoda.competition.application.commands;

/** Команда: подвести итоги конкурса. */
public class SummarizeCommand {
    public final Long fund;
    public final Double threshold;

    public SummarizeCommand(Long fund, Double threshold) {
        this.fund = fund;
        this.threshold = threshold;
    }
}
