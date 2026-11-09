package academy.tochkavhoda.competition.api;

import academy.tochkavhoda.competition.api.dto.requests.AddApplicationRequest;
import academy.tochkavhoda.competition.api.dto.requests.AddApplicationsRequest;
import academy.tochkavhoda.competition.api.dto.requests.ApplicationIdRequest;
import academy.tochkavhoda.competition.api.dto.requests.DirectionsRequest;
import academy.tochkavhoda.competition.api.dto.requests.LoginRequest;
import academy.tochkavhoda.competition.api.dto.requests.RateApplicationRequest;
import academy.tochkavhoda.competition.api.dto.requests.RegisterExpertRequest;
import academy.tochkavhoda.competition.api.dto.requests.RegisterParticipantRequest;
import academy.tochkavhoda.competition.api.dto.requests.SummarizeRequest;
import academy.tochkavhoda.competition.api.dto.responses.AddApplicationResponse;
import academy.tochkavhoda.competition.api.dto.responses.AddApplicationsResponse;
import academy.tochkavhoda.competition.api.dto.responses.ApplicationsResponse;
import academy.tochkavhoda.competition.api.dto.responses.CompetitionSummaryResponse;
import academy.tochkavhoda.competition.api.dto.responses.EmptyResponse;
import academy.tochkavhoda.competition.api.dto.responses.ErrorResponse;
import academy.tochkavhoda.competition.api.dto.responses.LoginResponse;
import academy.tochkavhoda.competition.api.dto.responses.ParticipantsResponse;
import academy.tochkavhoda.competition.api.dto.responses.RatedApplicationsResponse;
import academy.tochkavhoda.competition.api.dto.responses.ServerResponse;
import academy.tochkavhoda.competition.application.dao.ApplicationDao;
import academy.tochkavhoda.competition.application.dao.CompetitionDao;
import academy.tochkavhoda.competition.application.dao.RatingDao;
import academy.tochkavhoda.competition.application.dao.UserDao;
import academy.tochkavhoda.competition.application.exceptions.ApplicationException;
import academy.tochkavhoda.competition.application.services.ApplicationService;
import academy.tochkavhoda.competition.application.services.CompetitionService;
import academy.tochkavhoda.competition.application.services.DebugService;
import academy.tochkavhoda.competition.application.services.RatingService;
import academy.tochkavhoda.competition.application.services.UserService;
import academy.tochkavhoda.competition.infrastructure.daoimpl.ApplicationDaoImpl;
import academy.tochkavhoda.competition.infrastructure.daoimpl.CompetitionDaoImpl;
import academy.tochkavhoda.competition.infrastructure.daoimpl.DebugDaoImpl;
import academy.tochkavhoda.competition.infrastructure.daoimpl.RatingDaoImpl;
import academy.tochkavhoda.competition.infrastructure.daoimpl.UserDaoImpl;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.List;
import java.util.function.Supplier;

/**
 * Точка входа в сервер (слой api): общается с внешним миром строками json.
 * Каждый метод делает одно и то же:
 * json -> Request -> Command -> сервис -> Result -> Response -> json -> ServerResponse.
 * Все методы возвращают ServerResponse: 200 и тело ответа либо 400 и {"error": "..."}.
 */
public class Server {
    private static final Gson GSON = new Gson();
    private static final String WRONG_JSON = "Некорректный JSON запроса";

    private final UserService userService;
    private final ApplicationService applicationService;
    private final RatingService ratingService;
    private final CompetitionService competitionService;
    private final DebugService debugService;

    /** Здесь собираются все части приложения: выбираются реализации DAO и передаются сервисам. */
    public Server() {
        UserDao userDao = new UserDaoImpl();
        ApplicationDao applicationDao = new ApplicationDaoImpl();
        RatingDao ratingDao = new RatingDaoImpl();
        CompetitionDao competitionDao = new CompetitionDaoImpl();

        userService = new UserService(userDao, applicationDao);
        applicationService = new ApplicationService(userDao, applicationDao, competitionDao);
        ratingService = new RatingService(userDao, applicationDao, ratingDao, competitionDao);
        competitionService = new CompetitionService(userDao, competitionDao);
        debugService = new DebugService(new DebugDaoImpl());
    }

    // ---------- служебное ----------

    public ServerResponse clearDatabase() {
        return handle(() -> {
            debugService.clearDatabase();
            return new EmptyResponse();
        });
    }

    // ---------- регистрация и вход ----------

    public ServerResponse registerParticipant(String requestJson) {
        return handle(() -> {
            RegisterParticipantRequest request = parse(requestJson, RegisterParticipantRequest.class);
            userService.registerParticipant(request.toCommand());
            return new EmptyResponse();
        });
    }

    public ServerResponse registerExpert(String requestJson) {
        return handle(() -> {
            RegisterExpertRequest request = parse(requestJson, RegisterExpertRequest.class);
            userService.registerExpert(request.toCommand());
            return new EmptyResponse();
        });
    }

    public ServerResponse login(String requestJson) {
        return handle(() -> {
            LoginRequest request = parse(requestJson, LoginRequest.class);
            return LoginResponse.from(userService.login(request.toCommand()));
        });
    }

    public ServerResponse logout(String token) {
        return handle(() -> {
            userService.logout(token);
            return new EmptyResponse();
        });
    }

    public ServerResponse leaveServer(String token) {
        return handle(() -> {
            userService.leaveServer(token);
            return new EmptyResponse();
        });
    }

    public ServerResponse getParticipants(String token, String requestJson) {
        return handle(() -> ParticipantsResponse.from(
                userService.getParticipants(token, parseDirections(requestJson))));
    }

    // ---------- заявки ----------

    public ServerResponse addApplication(String token, String requestJson) {
        return handle(() -> {
            AddApplicationRequest request = parse(requestJson, AddApplicationRequest.class);
            return AddApplicationResponse.from(applicationService.addApplication(token, request.toCommand()));
        });
    }

    public ServerResponse addApplications(String token, String requestJson) {
        return handle(() -> {
            AddApplicationsRequest request = parse(requestJson, AddApplicationsRequest.class);
            return AddApplicationsResponse.from(applicationService.addApplications(token, request.toCommands()));
        });
    }

    public ServerResponse removeApplication(String token, String requestJson) {
        return handle(() -> {
            ApplicationIdRequest request = parse(requestJson, ApplicationIdRequest.class);
            applicationService.removeApplication(token, request.applicationId);
            return new EmptyResponse();
        });
    }

    public ServerResponse getMyApplications(String token) {
        return handle(() -> ApplicationsResponse.from(applicationService.getMyApplications(token)));
    }

    public ServerResponse getApplicationsByDirections(String token, String requestJson) {
        return handle(() -> ApplicationsResponse.from(
                applicationService.getApplicationsByDirections(token, parseDirections(requestJson))));
    }

    // ---------- оценки ----------

    public ServerResponse rateApplication(String token, String requestJson) {
        return handle(() -> {
            RateApplicationRequest request = parse(requestJson, RateApplicationRequest.class);
            ratingService.rateApplication(token, request.toCommand());
            return new EmptyResponse();
        });
    }

    public ServerResponse changeRating(String token, String requestJson) {
        return handle(() -> {
            RateApplicationRequest request = parse(requestJson, RateApplicationRequest.class);
            ratingService.changeRating(token, request.toCommand());
            return new EmptyResponse();
        });
    }

    public ServerResponse deleteRating(String token, String requestJson) {
        return handle(() -> {
            ApplicationIdRequest request = parse(requestJson, ApplicationIdRequest.class);
            ratingService.deleteRating(token, request.applicationId);
            return new EmptyResponse();
        });
    }

    public ServerResponse getRatedApplications(String token) {
        return handle(() -> RatedApplicationsResponse.from(ratingService.getRatedApplications(token)));
    }

    // ---------- итоги ----------

    public ServerResponse summarize(String token, String requestJson) {
        return handle(() -> {
            SummarizeRequest request = parse(requestJson, SummarizeRequest.class);
            return CompetitionSummaryResponse.from(competitionService.summarize(token, request.toCommand()));
        });
    }

    public ServerResponse getResults(String token) {
        return handle(() -> CompetitionSummaryResponse.from(competitionService.getResults(token)));
    }

    // ---------- вспомогательные методы ----------

    /**
     * Выполняет операцию и упаковывает её итог в ServerResponse:
     * тело ответа превращается в json (код 200) или ошибка - в {"error": "..."} (код 400).
     */
    private static ServerResponse handle(Supplier<Object> operation) {
        try {
            return new ServerResponse(ServerResponse.CODE_SUCCESS, GSON.toJson(operation.get()));
        } catch (JsonParseException e) {
            return error(WRONG_JSON);
        } catch (ApplicationException e) {
            return error(e.getMessage());
        }
    }

    private static ServerResponse error(String message) {
        return new ServerResponse(ServerResponse.CODE_ERROR, GSON.toJson(new ErrorResponse(message)));
    }

    private static <T> T parse(String json, Class<T> requestClass) {
        if (json == null || json.isBlank()) {
            throw new JsonParseException("Пустой запрос");
        }
        T request = GSON.fromJson(json, requestClass);
        if (request == null) {
            throw new JsonParseException("Пустой запрос");
        }
        return request;
    }

    /** Список направлений из необязательного запроса; null, если запрос не передан. */
    private static List<String> parseDirections(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return parse(json, DirectionsRequest.class).directions;
    }
}
