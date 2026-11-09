package academy.tochkavhoda.competition.api.dto.requests;

import academy.tochkavhoda.competition.application.commands.AddApplicationCommand;

import java.util.ArrayList;
import java.util.List;

/** Подача сразу нескольких заявок. */
public class AddApplicationsRequest {
    public final List<AddApplicationRequest> applications;

    public AddApplicationsRequest(List<AddApplicationRequest> applications) {
        this.applications = applications;
    }

    /** Возвращает null, если список не указан в запросе: проверка - дело сервиса. */
    public List<AddApplicationCommand> toCommands() {
        if (applications == null) {
            return null;
        }
        List<AddApplicationCommand> commands = new ArrayList<>();
        for (AddApplicationRequest request : applications) {
            commands.add(request == null ? null : request.toCommand());
        }
        return commands;
    }
}
