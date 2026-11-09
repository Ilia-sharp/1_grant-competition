package academy.tochkavhoda.competition;

import academy.tochkavhoda.competition.api.dto.requests.RegisterExpertRequest;
import academy.tochkavhoda.competition.api.dto.requests.RegisterParticipantRequest;
import academy.tochkavhoda.competition.api.dto.responses.ServerResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRegistration extends TestBase {
    private static final String WRONG_LOGIN =
            "Логин должен содержать от 4 до 30 символов: латинские буквы, цифры или _";
    private static final String WRONG_PASSWORD =
            "Пароль должен быть не короче 8 символов и содержать хотя бы одну букву, одну цифру и один спецсимвол";
    private static final String WRONG_DIRECTIONS =
            "Список направлений не может быть пустым и не может содержать пустых направлений";

    @Test
    public void testRegisterParticipant() {
        ServerResponse response = registerParticipant("romashka", "ООО Ромашка");
        assertSuccess(response);
        assertEquals("{}", response.getResponseData());
    }

    @Test
    public void testRegisterExpert() {
        ServerResponse response = registerExpert("expert_1", "математика", "информатика");
        assertSuccess(response);
        assertEquals("{}", response.getResponseData());
    }

    @Test
    public void testLoginAlreadyUsed() {
        assertSuccess(registerParticipant("romashka", "ООО Ромашка"));
        assertSuccess(registerExpert("expert1", "математика"));
        assertError(registerParticipant("romashka", "ООО Василёк"), "Логин romashka уже используется");
        assertError(registerExpert("romashka", "математика"), "Логин romashka уже используется");
        assertError(registerParticipant("expert1", "ООО Василёк"), "Логин expert1 уже используется");
        assertError(registerExpert("expert1", "физика"), "Логин expert1 уже используется");
    }

    @Test
    public void testCompanyCannotRegisterTwice() {
        assertSuccess(registerParticipant("romashka", "ООО Ромашка"));
        assertError(registerParticipant("romashka2", "ооо ромашка"), "Фирма «ооо ромашка» уже зарегистрирована");
    }

    @Test
    public void testRegisterParticipantWrongData() {
        assertError(server.registerParticipant(toJson(
                        new RegisterParticipantRequest(null, "Иванов", "Фирма", "login1", PASSWORD))),
                "Имя должно быть непустым и не длиннее 50 символов");
        assertError(server.registerParticipant(toJson(
                        new RegisterParticipantRequest("Иван", "   ", "Фирма", "login1", PASSWORD))),
                "Фамилия должна быть непустой и не длиннее 50 символов");
        assertError(server.registerParticipant(toJson(
                        new RegisterParticipantRequest("Иван", "Иванов", "", "login1", PASSWORD))),
                "Название фирмы должно быть непустым и не длиннее 100 символов");
        assertError(server.registerParticipant(toJson(
                        new RegisterParticipantRequest("Иван", "Иванов", "Фирма", "abc", PASSWORD))),
                WRONG_LOGIN);
        assertError(server.registerParticipant(toJson(
                        new RegisterParticipantRequest("Иван", "Иванов", "Фирма", "логин1", PASSWORD))),
                WRONG_LOGIN);
        assertError(server.registerParticipant(toJson(
                        new RegisterParticipantRequest("Иван", "Иванов", "Фирма", null, PASSWORD))),
                WRONG_LOGIN);
    }

    @Test
    public void testRegisterWrongPassword() {
        for (String password : Arrays.asList(null, "", "Ab#1", "Abcdefgh1", "Abcdefgh#", "12345678#", "Abc def#1")) {
            assertError(server.registerParticipant(toJson(
                            new RegisterParticipantRequest("Иван", "Иванов", "Фирма", "login1", password))),
                    WRONG_PASSWORD);
        }
    }

    @Test
    public void testRegisterExpertWrongDirections() {
        assertError(server.registerExpert(toJson(
                new RegisterExpertRequest("Пётр", "Петров", null, "expert1", PASSWORD))), WRONG_DIRECTIONS);
        assertError(server.registerExpert(toJson(
                        new RegisterExpertRequest("Пётр", "Петров", Collections.emptyList(), "expert1", PASSWORD))),
                WRONG_DIRECTIONS);
        assertError(server.registerExpert(toJson(
                        new RegisterExpertRequest("Пётр", "Петров", Arrays.asList("физика", "  "), "expert1",
                                PASSWORD))),
                WRONG_DIRECTIONS);
    }

    @Test
    public void testRegisterWrongJson() {
        for (String json : Arrays.asList(null, "", "   ", "not a json", "[1, 2]", "null", "{\"firstName\": ")) {
            assertError(server.registerParticipant(json), "Некорректный JSON запроса");
            assertError(server.registerExpert(json), "Некорректный JSON запроса");
        }
    }
}
