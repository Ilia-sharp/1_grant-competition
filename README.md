# Конкурс грантов — сервер (данные в оперативной памяти)

Проект построен по Clean Architecture (схема Джейсона Тейлора, [шаблон](https://github.com/jasontaylordev/CleanArchitecture)).
Корневой пакет: `academy.tochkavhoda.competition`. Клиентом сервера выступают JUnit-тесты.

## Структура проекта

```
src/main/java/academy/tochkavhoda/competition
├── api                          СЛОЙ ПРЕЗЕНТАЦИИ (внешний мир: json-строки)
│   ├── Server.java              единственный вход; всегда возвращает ServerResponse
│   └── dto
│       ├── requests             *Request  - что пришло от клиента; toCommand() -> команда
│       └── responses            *Response - что уходит клиенту; static from(Result);
│                                ServerResponse, ErrorResponse, EmptyResponse
├── application                  СЛОЙ ПРИЛОЖЕНИЯ (сценарии использования)
│   ├── commands                 *Command - входные данные сервисов (не зависят от json и api)
│   ├── results                  *Result  - то, что сервисы возвращают наружу
│   ├── services                 UserService, ApplicationService, RatingService,
│   │                            CompetitionService, DebugService - бизнес-логика и валидация
│   ├── dao                      ИНТЕРФЕЙСЫ доступа к данным (UserDao, ApplicationDao, ...)
│   └── exceptions               ApplicationException + ErrorCode (коды и тексты ошибок)
├── domain                       СЛОЙ ДОМЕНА (ядро, ни от чего не зависит)
│   ├── Database.java            хранилище в памяти (синглетон)
│   └── models                   User -> Participant, Expert; GrantApplication, Grant, CompetitionSummary
└── infrastructure               СЛОЙ ИНФРАСТРУКТУРЫ (детали хранения)
    └── daoimpl                  UserDaoImpl, ... - реализации интерфейсов application.dao
```

## Правила зависимостей

Зависимости направлены только внутрь: **api → application → domain**, **infrastructure → application, domain**.

| Слой | Может использовать | Не может использовать |
|---|---|---|
| `domain` | только JDK и библиотеку коллекций | `application`, `api`, `infrastructure` |
| `application` | `domain` и свои пакеты | `api`, `infrastructure`, json/Gson |
| `infrastructure` | `application` (интерфейсы dao), `domain` | `api` |
| `api` | `application` | `domain` |

Единственное место, где `api` «видит» `infrastructure` — конструктор `Server()`: это точка сборки приложения
(какие реализации DAO передать сервисам). Сами сервисы получают только интерфейсы DAO через конструктор.

## Как проходит запрос

```
json-строка
  → Server: parse → XxxRequest                      (api)
  → request.toCommand() → XxxCommand                (api → application)
  → XxxService.метод(command)                       (application; работает через XxxDao и модели)
  → XxxResult                                       (application)
  → XxxResponse.from(result) → json → ServerResponse (api)
```

Сервис **не возвращает `ServerResponse`** и ничего не знает про json: он возвращает структуру `Result`,
а при ошибке выбрасывает `ApplicationException`. Превращает всё это в ответ клиенту только `Server`
(метод `handle`): успех → код `200` и json, `ApplicationException` → код `400` и `{"error": "..."}`,
некорректный json → `400` и `{"error": "Некорректный JSON запроса"}`.

## Что исправлено по замечаниям преподавателя

| Замечание | Что сделано |
|---|---|
| Слой application не должен обращаться к слою api | сервисы больше не импортируют `dto`; вход — `commands`, выход — `results`, оба лежат в `application` |
| Сервис не должен возвращать `ServerResponse` | сервисы возвращают `Result`; `ServerResponse` собирает только `Server` |
| В сервисе не должно быть json | разбор и сборка json перенесены в `Server`; Gson используется только в `api` |
| Из Result в Response — через статический `from` | у каждого `*Response` есть `from(*Result)` (у `*Request` — `toCommand()`) |
| В названиях не должно быть `Dto`, только `Request` / `Response` | `LoginDtoRequest` → `LoginRequest`, `ApplicationDtoResponse` → `ApplicationResponse` и т.д. |
| Структура папок: `api`, `application`, `domain` | `Server` в `api`; `services`, `results` в `application`; `Database`, `models` в `domain` |
| Слои не должны зависеть «вверх» | `domain` ни от чего не зависит; сервисы не создают `*DaoImpl` — им передают интерфейсы |
| Администратора нет в задании | роль `Admin` убрана (см. ниже, кто подводит итоги) |
| Нужны конструкторы | у всех команд, результатов, запросов и ответов есть конструкторы |

Для DAO использована схема с доски: интерфейсы лежат в `application/dao`, реализации — в `infrastructure/daoimpl`.

## Принятые решения и допущения

- **Кто подводит итоги.** Роли администратора в ТЗ нет, поэтому `summarize` может вызвать любой вошедший пользователь
  (нужен только действительный токен). Если потребуется ограничить — достаточно заменить `getUserByToken`
  на `getExpertByToken` в `CompetitionService.summarize`.
- **Валидация** (формат логина и пароля, длины, оценка 1..5 и т.д.) оставлена в сервисах `application` —
  это бизнес-правила, а не часть json-протокола. `Server` проверяет только корректность json.
- **Уникальность логина и названия фирмы и запрет операций после подведения итогов** проверяются в сервисах;
  `Database` только хранит данные.
- Поля у `Request`/`Response`/`Command`/`Result` — публичные `final` (простые структуры данных без геттеров),
  в стиле Java: `firstName`, а не `FirstName`.
- Исключения неотслеживаемые (`RuntimeException`), чтобы не писать `throws` в каждом методе.
- Класс `CompetitionResult` из прежней версии переименован в `CompetitionSummary` (итоги конкурса),
  чтобы цепочка имён была единообразной: `CompetitionSummary` → `CompetitionSummaryResult` → `CompetitionSummaryResponse`.

## Методы Server

| Метод | Кто вызывает | Запрос | Ответ |
|---|---|---|---|
| `registerParticipant(json)` | — | `firstName, lastName, companyName, login, password` | `{}` |
| `registerExpert(json)` | — | `firstName, lastName, directions[], login, password` | `{}` |
| `login(json)` | — | `login, password` | `{"token"}` |
| `logout(token)` | любой | — | `{}` |
| `leaveServer(token)` | участник, эксперт | — | `{}` |
| `getParticipants(token, json)` | любой | `directions[]` (необязательно) | `{"participants": [...]}` |
| `addApplication(token, json)` | участник | `title, description, directions[], requestedSum` | `{"id"}` |
| `addApplications(token, json)` | участник | `applications[]` | `{"ids": [...]}` |
| `removeApplication(token, json)` | участник | `applicationId` | `{}` |
| `getMyApplications(token)` | участник | — | `{"applications": [...]}` |
| `getApplicationsByDirections(token, json)` | эксперт | `directions[]` (необязательно) | `{"applications": [...]}` |
| `rateApplication(token, json)` | эксперт | `applicationId, rating` | `{}` |
| `changeRating(token, json)` | эксперт | `applicationId, rating` | `{}` |
| `deleteRating(token, json)` | эксперт | `applicationId` | `{}` |
| `getRatedApplications(token)` | эксперт | — | `{"applications": [{"application", "rating"}]}` |
| `summarize(token, json)` | любой вошедший | `fund, threshold` | итоги конкурса |
| `getResults(token)` | любой вошедший | — | итоги конкурса |
| `clearDatabase()` | тесты | — | `{}` |

## Требования, принятые при разработке

- Логин: 4–30 символов, латинские буквы, цифры, `_`; уникален среди всех пользователей.
- Пароль: 8–50 символов без пробелов, минимум одна буква, одна цифра и один спецсимвол.
- Имя, фамилия — непустые, до 50 символов; название фирмы — непустое, до 100 символов.
- Фирма не может зарегистрироваться дважды: название фирмы уникально (без учёта регистра).
- Направления хранятся без пробелов по краям и в нижнем регистре, дубли убираются.
- Повторный вход выдаёт новый токен, прежний токен становится недействительным.
- Участник, покинувший сервер, теряет все свои заявки (вместе с оценками к ним); эксперт — все свои оценки.
- Эксперт может оценивать только заявки, у которых есть хотя бы одно из его направлений. Оценка — целое 1..5.
  Поставить оценку повторно нельзя — для этого есть `changeRating`; удалить — `deleteRating`.
- Итоги: общий фонд (> 0) и пороговое значение средней оценки (0..5). Заявка без оценок имеет среднюю оценку 0.
- После подведения итогов подача, отмена и оценка заявок запрещены, повторно итоги не подводятся.

## Алгоритм подведения итогов

1. Заявки сортируются по убыванию средней оценки, при равенстве — по возрастанию суммы, при равных суммах — случайно
   (список перемешивается, затем сортируется устойчивой сортировкой).
2. Заявки просматриваются по порядку: если сумма не превышает остатка фонда — грант выделяется и сумма вычитается, иначе заявка пропускается.
3. Как только встречается заявка со средней оценкой не больше порога — распределение прекращается, остаток не распределяется.

## Запуск тестов

```
mvn test
```

Нужны JDK 17+ и Maven. Тесты (`src/test`) работают как клиент: обращаются только к `Server` и классам из `api.dto`.
