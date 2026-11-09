package academy.tochkavhoda.competition.api.dto.responses;

import academy.tochkavhoda.competition.application.results.LoginResult;

public class LoginResponse {
    public final String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.token);
    }
}
