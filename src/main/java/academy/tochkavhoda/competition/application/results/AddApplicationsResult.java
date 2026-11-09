package academy.tochkavhoda.competition.application.results;

import java.util.List;

/** Результат подачи нескольких заявок: их id в порядке подачи. */
public class AddApplicationsResult {
    public final List<Integer> ids;

    public AddApplicationsResult(List<Integer> ids) {
        this.ids = ids;
    }
}
