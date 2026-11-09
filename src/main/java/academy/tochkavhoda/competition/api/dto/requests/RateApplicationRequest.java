package academy.tochkavhoda.competition.api.dto.requests;

import academy.tochkavhoda.competition.application.commands.RateApplicationCommand;

/** Оценка заявки: id заявки и оценка 1..5. */
public class RateApplicationRequest {
    public final Integer applicationId;
    public final Integer rating;

    public RateApplicationRequest(Integer applicationId, Integer rating) {
        this.applicationId = applicationId;
        this.rating = rating;
    }

    public RateApplicationCommand toCommand() {
        return new RateApplicationCommand(applicationId, rating);
    }
}
