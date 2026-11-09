package academy.tochkavhoda.competition.application.services;

import academy.tochkavhoda.competition.application.commands.LoginCommand;
import academy.tochkavhoda.competition.application.commands.RegisterExpertCommand;
import academy.tochkavhoda.competition.application.commands.RegisterParticipantCommand;
import academy.tochkavhoda.competition.application.dao.ApplicationDao;
import academy.tochkavhoda.competition.application.dao.UserDao;
import academy.tochkavhoda.competition.application.exceptions.ApplicationException;
import academy.tochkavhoda.competition.application.exceptions.ErrorCode;
import academy.tochkavhoda.competition.application.results.LoginResult;
import academy.tochkavhoda.competition.application.results.ParticipantResult;
import academy.tochkavhoda.competition.domain.models.Expert;
import academy.tochkavhoda.competition.domain.models.GrantApplication;
import academy.tochkavhoda.competition.domain.models.Participant;
import academy.tochkavhoda.competition.domain.models.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static academy.tochkavhoda.competition.application.services.ServiceUtils.getUserByToken;
import static academy.tochkavhoda.competition.application.services.ServiceUtils.isBlank;
import static academy.tochkavhoda.competition.application.services.ServiceUtils.normalizeDirections;

/** Пользователи: регистрация, вход и выход, список участников. */
public class UserService {
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_COMPANY_NAME_LENGTH = 100;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 50;
    private static final Pattern LOGIN_PATTERN = Pattern.compile("[A-Za-z0-9_]{4,30}");

    private final UserDao userDao;
    private final ApplicationDao applicationDao;

    public UserService(UserDao userDao, ApplicationDao applicationDao) {
        this.userDao = userDao;
        this.applicationDao = applicationDao;
    }

    public void registerParticipant(RegisterParticipantCommand command) {
        validate(command);
        String companyName = command.companyName.trim();
        checkLoginIsFree(command.login);
        if (userDao.isCompanyRegistered(companyName)) {
            throw new ApplicationException(ErrorCode.COMPANY_ALREADY_REGISTERED, companyName);
        }
        userDao.insert(new Participant(command.firstName.trim(), command.lastName.trim(),
                companyName, command.login, command.password));
    }

    public void registerExpert(RegisterExpertCommand command) {
        validate(command);
        checkLoginIsFree(command.login);
        userDao.insert(new Expert(command.firstName.trim(), command.lastName.trim(),
                normalizeDirections(command.directions), command.login, command.password));
    }

    public LoginResult login(LoginCommand command) {
        if (isBlank(command.login) || isBlank(command.password)) {
            throw new ApplicationException(ErrorCode.WRONG_LOGIN_OR_PASSWORD);
        }
        User user = userDao.getByLogin(command.login);
        if (user == null || !user.getPassword().equals(command.password)) {
            throw new ApplicationException(ErrorCode.WRONG_LOGIN_OR_PASSWORD);
        }
        return new LoginResult(userDao.login(user));
    }

    public void logout(String token) {
        getUserByToken(userDao, token);
        userDao.logout(token);
    }

    /** Пользователь покидает сервер: удаляются он сам и его заявки (участник) или его оценки (эксперт). */
    public void leaveServer(String token) {
        User user = getUserByToken(userDao, token);
        userDao.delete(user);
    }

    /**
     * Список участников. Без направлений (null или пустой список) - все участники,
     * иначе - только те, у кого есть заявки хотя бы по одному из указанных направлений.
     */
    public List<ParticipantResult> getParticipants(String token, List<String> directions) {
        getUserByToken(userDao, token);
        List<Participant> participants;
        if (directions == null || directions.isEmpty()) {
            participants = userDao.getAllParticipants();
        } else {
            Set<Participant> selected = new LinkedHashSet<>();
            for (GrantApplication application : applicationDao.getByDirections(normalizeDirections(directions))) {
                selected.add(application.getParticipant());
            }
            participants = new ArrayList<>(selected);
        }
        participants.sort(Comparator.comparing(Participant::getCompanyName));
        List<ParticipantResult> results = new ArrayList<>();
        for (Participant participant : participants) {
            results.add(ParticipantResult.from(participant));
        }
        return results;
    }

    private void checkLoginIsFree(String login) {
        if (userDao.getByLogin(login) != null) {
            throw new ApplicationException(ErrorCode.LOGIN_ALREADY_EXISTS, login);
        }
    }

    private static void validate(RegisterParticipantCommand command) {
        validateName(command.firstName, ErrorCode.WRONG_FIRST_NAME, MAX_NAME_LENGTH);
        validateName(command.lastName, ErrorCode.WRONG_LAST_NAME, MAX_NAME_LENGTH);
        validateName(command.companyName, ErrorCode.WRONG_COMPANY_NAME, MAX_COMPANY_NAME_LENGTH);
        validateCredentials(command.login, command.password);
    }

    private static void validate(RegisterExpertCommand command) {
        validateName(command.firstName, ErrorCode.WRONG_FIRST_NAME, MAX_NAME_LENGTH);
        validateName(command.lastName, ErrorCode.WRONG_LAST_NAME, MAX_NAME_LENGTH);
        normalizeDirections(command.directions);
        validateCredentials(command.login, command.password);
    }

    private static void validateName(String name, ErrorCode errorCode, int maxLength) {
        if (isBlank(name) || name.trim().length() > maxLength) {
            throw new ApplicationException(errorCode);
        }
    }

    private static void validateCredentials(String login, String password) {
        if (login == null || !LOGIN_PATTERN.matcher(login).matches()) {
            throw new ApplicationException(ErrorCode.WRONG_LOGIN);
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new ApplicationException(ErrorCode.WRONG_PASSWORD);
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isWhitespace(c)) {
                throw new ApplicationException(ErrorCode.WRONG_PASSWORD);
            } else if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }
        if (!hasLetter || !hasDigit || !hasSpecial) {
            throw new ApplicationException(ErrorCode.WRONG_PASSWORD);
        }
    }
}
