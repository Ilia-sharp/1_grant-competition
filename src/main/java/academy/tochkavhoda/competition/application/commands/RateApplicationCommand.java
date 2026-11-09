package academy.tochkavhoda.competition.application.commands;

/** Команда: поставить (или изменить) оценку заявке. */
public class RateApplicationCommand {
    public final Integer applicationId;
    public final Integer rating;

    public RateApplicationCommand(Integer applicationId, Integer rating) {
        this.applicationId = applicationId;
        this.rating = rating;
    }
}
