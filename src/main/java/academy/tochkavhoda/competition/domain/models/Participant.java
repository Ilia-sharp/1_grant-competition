package academy.tochkavhoda.competition.domain.models;

public class Participant extends User {
    private String companyName;

    public Participant(String firstName, String lastName, String companyName, String login, String password) {
        super(firstName, lastName, login, password);
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
