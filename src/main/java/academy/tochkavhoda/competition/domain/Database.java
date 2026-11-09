package academy.tochkavhoda.competition.domain;

import academy.tochkavhoda.competition.domain.models.CompetitionSummary;
import academy.tochkavhoda.competition.domain.models.Expert;
import academy.tochkavhoda.competition.domain.models.GrantApplication;
import academy.tochkavhoda.competition.domain.models.Participant;
import academy.tochkavhoda.competition.domain.models.User;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * "База данных" в оперативной памяти. Синглетон.
 * Знает только о классах моделей. Правила (уникальность логина, проверка токена и т.п.)
 * проверяет слой application, а здесь данные просто хранятся.
 */
public final class Database {
    private static final Database INSTANCE = new Database();

    // все пользователи по логину
    private final Map<String, User> usersByLogin = new HashMap<>();
    // названия зарегистрированных фирм (в нижнем регистре)
    private final Set<String> companyNames = new HashSet<>();
    // токен <-> пользователь; у пользователя не больше одного действующего токена
    private final BidiMap<String, User> sessions = new DualHashBidiMap<>();
    // заявки по id (TreeMap - чтобы списки выдавались в порядке подачи)
    private final Map<Integer, GrantApplication> applications = new TreeMap<>();
    // направление -> заявки по этому направлению
    private final MultiValuedMap<String, GrantApplication> applicationsByDirection = new HashSetValuedHashMap<>();
    // заявка -> (эксперт -> оценка)
    private final Map<GrantApplication, Map<Expert, Integer>> ratings = new HashMap<>();

    private int nextApplicationId;
    private CompetitionSummary competitionSummary;

    private Database() {
        clear();
    }

    public static Database getInstance() {
        return INSTANCE;
    }

    public synchronized void clear() {
        usersByLogin.clear();
        companyNames.clear();
        sessions.clear();
        applications.clear();
        applicationsByDirection.clear();
        ratings.clear();
        nextApplicationId = 1;
        competitionSummary = null;
    }

    // ---------- пользователи ----------

    public synchronized void insertUser(User user) {
        usersByLogin.put(user.getLogin(), user);
        if (user instanceof Participant) {
            companyNames.add(companyKey(((Participant) user).getCompanyName()));
        }
    }

    public synchronized User getUserByLogin(String login) {
        return usersByLogin.get(login);
    }

    public synchronized boolean isCompanyRegistered(String companyName) {
        return companyNames.contains(companyKey(companyName));
    }

    public synchronized String login(User user) {
        String token = UUID.randomUUID().toString();
        // BidiMap при повторном входе сам удалит старый токен этого пользователя
        sessions.put(token, user);
        return token;
    }

    public synchronized void logout(String token) {
        sessions.remove(token);
    }

    public synchronized User getUserByToken(String token) {
        return sessions.get(token);
    }

    /** Удаляет пользователя вместе с его заявками (участник) или оценками (эксперт). */
    public synchronized void deleteUser(User user) {
        sessions.removeValue(user);
        usersByLogin.remove(user.getLogin());
        if (user instanceof Participant) {
            companyNames.remove(companyKey(((Participant) user).getCompanyName()));
            for (GrantApplication application : getApplicationsByParticipant((Participant) user)) {
                deleteApplication(application);
            }
        }
        if (user instanceof Expert) {
            for (Map<Expert, Integer> applicationRatings : ratings.values()) {
                applicationRatings.remove(user);
            }
        }
    }

    public synchronized List<Participant> getParticipants() {
        List<Participant> result = new ArrayList<>();
        for (User user : usersByLogin.values()) {
            if (user instanceof Participant) {
                result.add((Participant) user);
            }
        }
        return result;
    }

    // ---------- заявки ----------

    public synchronized List<Integer> insertApplications(List<GrantApplication> newApplications) {
        List<Integer> ids = new ArrayList<>();
        for (GrantApplication application : newApplications) {
            application.setId(nextApplicationId++);
            applications.put(application.getId(), application);
            for (String direction : application.getDirections()) {
                applicationsByDirection.put(direction, application);
            }
            ids.add(application.getId());
        }
        return ids;
    }

    public synchronized GrantApplication getApplicationById(int id) {
        return applications.get(id);
    }

    public synchronized List<GrantApplication> getApplicationsByParticipant(Participant participant) {
        List<GrantApplication> result = new ArrayList<>();
        for (GrantApplication application : applications.values()) {
            if (application.getParticipant().equals(participant)) {
                result.add(application);
            }
        }
        return result;
    }

    public synchronized List<GrantApplication> getApplicationsByDirections(Collection<String> directions) {
        Map<Integer, GrantApplication> result = new TreeMap<>();
        for (String direction : directions) {
            for (GrantApplication application : applicationsByDirection.get(direction)) {
                result.put(application.getId(), application);
            }
        }
        return new ArrayList<>(result.values());
    }

    /** Удаляет заявку вместе со всеми оценками к ней. */
    public synchronized void deleteApplication(GrantApplication application) {
        applications.remove(application.getId());
        for (String direction : application.getDirections()) {
            applicationsByDirection.removeMapping(direction, application);
        }
        ratings.remove(application);
    }

    // ---------- оценки ----------

    public synchronized Integer getRating(Expert expert, GrantApplication application) {
        Map<Expert, Integer> applicationRatings = ratings.get(application);
        return applicationRatings == null ? null : applicationRatings.get(expert);
    }

    public synchronized void setRating(Expert expert, GrantApplication application, int rating) {
        ratings.computeIfAbsent(application, key -> new HashMap<>()).put(expert, rating);
    }

    public synchronized void deleteRating(Expert expert, GrantApplication application) {
        Map<Expert, Integer> applicationRatings = ratings.get(application);
        if (applicationRatings != null) {
            applicationRatings.remove(expert);
        }
    }

    /** Все заявки, оценённые экспертом, в порядке id, с его оценками. */
    public synchronized Map<GrantApplication, Integer> getRatingsByExpert(Expert expert) {
        Map<GrantApplication, Integer> result = new LinkedHashMap<>();
        for (GrantApplication application : applications.values()) {
            Map<Expert, Integer> applicationRatings = ratings.get(application);
            if (applicationRatings != null && applicationRatings.containsKey(expert)) {
                result.put(application, applicationRatings.get(expert));
            }
        }
        return result;
    }

    /** Все заявки в порядке id со списками их оценок (у неоценённых заявок список пустой). */
    public synchronized Map<GrantApplication, List<Integer>> getAllApplicationsWithRatings() {
        Map<GrantApplication, List<Integer>> result = new LinkedHashMap<>();
        for (GrantApplication application : applications.values()) {
            Map<Expert, Integer> applicationRatings = ratings.get(application);
            result.put(application, applicationRatings == null
                    ? new ArrayList<>() : new ArrayList<>(applicationRatings.values()));
        }
        return result;
    }

    // ---------- итоги конкурса ----------

    public synchronized boolean isCompetitionFinished() {
        return competitionSummary != null;
    }

    public synchronized void saveCompetitionSummary(CompetitionSummary summary) {
        competitionSummary = summary;
    }

    public synchronized CompetitionSummary getCompetitionSummary() {
        return competitionSummary;
    }

    // название фирмы сравнивается без учёта регистра
    private static String companyKey(String companyName) {
        return companyName.toLowerCase(Locale.ROOT);
    }
}
