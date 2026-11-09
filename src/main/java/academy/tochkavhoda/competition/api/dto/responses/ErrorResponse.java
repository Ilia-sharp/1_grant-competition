package academy.tochkavhoda.competition.api.dto.responses;

/** Тело ответа при ошибке: {"error": "описание ошибки"}. */
public class ErrorResponse {
    public final String error;

    public ErrorResponse(String error) {
        this.error = error;
    }
}
