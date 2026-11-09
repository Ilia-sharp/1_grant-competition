package academy.tochkavhoda.competition.domain.models;

import java.util.LinkedHashSet;
import java.util.Set;

public class GrantApplication {
    private int id;
    private Participant participant;
    private String title;
    private String description;
    private Set<String> directions;
    private long requestedSum;

    public GrantApplication(Participant participant, String title, String description,
                            Set<String> directions, long requestedSum) {
        this.participant = participant;
        this.title = title;
        this.description = description;
        this.directions = new LinkedHashSet<>(directions);
        this.requestedSum = requestedSum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getDirections() {
        return directions;
    }

    public void setDirections(Set<String> directions) {
        this.directions = directions;
    }

    public long getRequestedSum() {
        return requestedSum;
    }

    public void setRequestedSum(long requestedSum) {
        this.requestedSum = requestedSum;
    }

    // id присваивается базой данных и уникален, поэтому заявки сравниваются по id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((GrantApplication) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
