package academy.tochkavhoda.competition.application.results;

/** Заявка вместе с оценкой, которую ей поставил эксперт. */
public class RatedApplicationResult {
    public final ApplicationResult application;
    public final int rating;

    public RatedApplicationResult(ApplicationResult application, int rating) {
        this.application = application;
        this.rating = rating;
    }
}
