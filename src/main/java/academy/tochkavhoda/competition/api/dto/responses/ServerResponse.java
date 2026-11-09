package academy.tochkavhoda.competition.api.dto.responses;

/**
 * Единственный тип, который возвращает Server.
 * responseCode: 200 - успех, 400 - ошибка.
 * responseData: json с результатом операции либо json с единственным полем "error".
 */
public class ServerResponse {
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_ERROR = 400;

    private final int responseCode;
    private final String responseData;

    public ServerResponse(int responseCode, String responseData) {
        this.responseCode = responseCode;
        this.responseData = responseData;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseData() {
        return responseData;
    }

    @Override
    public String toString() {
        return "ServerResponse{responseCode=" + responseCode + ", responseData=" + responseData + "}";
    }
}
