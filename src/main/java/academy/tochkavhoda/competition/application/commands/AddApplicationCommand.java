package academy.tochkavhoda.competition.application.commands;

import java.util.List;

/** Команда: подать заявку на грант. */
public class AddApplicationCommand {
    public final String title;
    public final String description;
    public final List<String> directions;
    public final Long requestedSum;

    public AddApplicationCommand(String title, String description, List<String> directions, Long requestedSum) {
        this.title = title;
        this.description = description;
        this.directions = directions;
        this.requestedSum = requestedSum;
    }
}
