package academy.tochkavhoda.competition.application.exceptions;

/** Коды ошибок и шаблоны сообщений, которые видит клиент. */
public enum ErrorCode {
    WRONG_FIRST_NAME("Имя должно быть непустым и не длиннее 50 символов"),
    WRONG_LAST_NAME("Фамилия должна быть непустой и не длиннее 50 символов"),
    WRONG_COMPANY_NAME("Название фирмы должно быть непустым и не длиннее 100 символов"),
    WRONG_LOGIN("Логин должен содержать от 4 до 30 символов: латинские буквы, цифры или _"),
    WRONG_PASSWORD("Пароль должен быть не короче 8 символов и содержать хотя бы одну букву, одну цифру и один спецсимвол"),
    WRONG_DIRECTIONS("Список направлений не может быть пустым и не может содержать пустых направлений"),
    LOGIN_ALREADY_EXISTS("Логин %s уже используется"),
    COMPANY_ALREADY_REGISTERED("Фирма «%s» уже зарегистрирована"),
    WRONG_LOGIN_OR_PASSWORD("Неверный логин или пароль"),
    WRONG_TOKEN("Неверный токен: пользователь не вошёл на сервер"),
    NOT_PARTICIPANT("Операция доступна только участникам конкурса"),
    NOT_EXPERT("Операция доступна только экспертам"),
    WRONG_TITLE("Название заявки должно быть непустым и не длиннее 200 символов"),
    WRONG_DESCRIPTION("Описание заявки не может быть пустым"),
    WRONG_REQUESTED_SUM("Запрашиваемая сумма гранта должна быть положительной"),
    EMPTY_APPLICATION_LIST("Список заявок не может быть пустым"),
    EMPTY_APPLICATION("Заявка в списке не может быть пустой"),
    WRONG_APPLICATION_ID("Не указан id заявки"),
    APPLICATION_NOT_FOUND("Заявка с id %d не найдена"),
    NOT_APPLICATION_OWNER("Заявка с id %d принадлежит другому участнику"),
    WRONG_RATING("Оценка должна быть целым числом от 1 до 5"),
    ALREADY_RATED("Заявка с id %d уже оценена вами, используйте изменение оценки"),
    RATING_NOT_FOUND("Вы не оценивали заявку с id %d"),
    DIRECTION_NOT_EXPERT("Вы не являетесь экспертом по направлению «%s»"),
    APPLICATION_OUT_OF_EXPERTISE("Заявка с id %d не относится к вашим направлениям"),
    WRONG_FUND("Общий фонд гранта должен быть положительным"),
    WRONG_THRESHOLD("Пороговое значение оценки должно быть в диапазоне от 0 до 5"),
    COMPETITION_FINISHED("Конкурс завершён, операция невозможна"),
    COMPETITION_NOT_FINISHED("Итоги конкурса ещё не подведены");

    private final String messageTemplate;

    ErrorCode(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }
}
