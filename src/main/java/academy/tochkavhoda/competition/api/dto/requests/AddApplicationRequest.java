package academy.tochkavhoda.competition.api.dto.requests;

import academy.tochkavhoda.competition.application.commands.AddApplicationCommand;

import java.util.List;

/** Заявка на грант: название, описание, направления и запрашиваемая сумма. */
public class AddApplicationRequest {
    public final String title;
    public final String description;
    public final List<String> directions;
    public final Long requestedSum;

    public AddApplicationRequest(String title, String description, List<String> directions, Long requestedSum) {
        this.title = title;
        this.description = description;
        this.directions = directions;
        this.requestedSum = requestedSum;
    }

    public AddApplicationCommand toCommand() {
        return new AddApplicationCommand(title, description, directions, requestedSum);
    }
}
