package academy.tochkavhoda.competition.api.dto.requests;

/** Запрос, в котором указана заявка: отмена заявки, удаление оценки. */
public class ApplicationIdRequest {
    public final Integer applicationId;

    public ApplicationIdRequest(Integer applicationId) {
        this.applicationId = applicationId;
    }
}
