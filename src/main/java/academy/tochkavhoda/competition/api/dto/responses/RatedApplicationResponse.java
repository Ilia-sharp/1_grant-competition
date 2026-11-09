package academy.tochkavhoda.competition.api.dto.responses;

import academy.tochkavhoda.competition.application.results.RatedApplicationResult;

/** Заявка вместе с оценкой, которую ей поставил эксперт. */
public class RatedApplicationResponse {
    public final ApplicationResponse application;
    public final int rating;

    public RatedApplicationResponse(ApplicationResponse application, int rating) {
        this.application = application;
        this.rating = rating;
    }

    public static RatedApplicationResponse from(RatedApplicationResult result) {
        return new RatedApplicationResponse(ApplicationResponse.from(result.application), result.rating);
    }
}
