package academy.tochkavhoda.competition.application.services;

import academy.tochkavhoda.competition.application.dao.DebugDao;

/** Служебный сервис: очистка базы данных (используется тестами, чтобы каждый тест начинался "с нуля"). */
public class DebugService {
    private final DebugDao debugDao;

    public DebugService(DebugDao debugDao) {
        this.debugDao = debugDao;
    }

    public void clearDatabase() {
        debugDao.clear();
    }
}
