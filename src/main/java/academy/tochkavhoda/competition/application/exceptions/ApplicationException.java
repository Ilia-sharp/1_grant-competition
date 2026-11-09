package academy.tochkavhoda.competition.application.exceptions;

/**
 * Ошибка выполнения операции (неверные данные, нет прав, не найден объект и т.п.).
 * Сервисы выбрасывают её, а слой api превращает её в ответ клиенту с кодом ошибки.
 */
public class ApplicationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    public ApplicationException(ErrorCode errorCode, Object... args) {
        super(String.format(errorCode.getMessageTemplate(), args));
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
