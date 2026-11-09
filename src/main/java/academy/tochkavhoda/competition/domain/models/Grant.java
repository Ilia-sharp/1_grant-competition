package academy.tochkavhoda.competition.domain.models;

/** Выделенный по итогам конкурса грант. */
public class Grant {
    private GrantApplication application;
    private double averageRating;
    private long sum;

    public Grant(GrantApplication application, double averageRating, long sum) {
        this.application = application;
        this.averageRating = averageRating;
        this.sum = sum;
    }

    public GrantApplication getApplication() {
        return application;
    }

    public void setApplication(GrantApplication application) {
        this.application = application;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }
}
