package academy.tochkavhoda.competition.domain.models;

import java.util.LinkedHashSet;
import java.util.Set;

public class Expert extends User {
    private Set<String> directions;

    public Expert(String firstName, String lastName, Set<String> directions, String login, String password) {
        super(firstName, lastName, login, password);
        this.directions = new LinkedHashSet<>(directions);
    }

    public Set<String> getDirections() {
        return directions;
    }

    public void setDirections(Set<String> directions) {
        this.directions = directions;
    }
}
