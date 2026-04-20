# SkillSwap Market — консолидированная спецификация демо-приложения для Codex

## 1. Цель проекта

`SkillSwap Market` — это демо-приложение для тренировки:
- API-тестирования на `Java + REST Assured`
- UI/E2E-тестирования на `Java + Playwright`

Приложение должно выглядеть как реалистичный продукт, но оставаться достаточно компактным, чтобы его можно было быстро реализовать и использовать как учебный стенд и портфолио-проект.

Главная идея: пользователи публикуют навыки или мини-услуги, открывают временные слоты, а другие пользователи бронируют эти слоты за внутренние кредиты.

Этот файл является рабочей консолидированной версией требований. Он объединяет исходную спецификацию и согласованные уточнения по ролевой модели, lifecycle бронирования, cancellation policy, admin scope и disputes.

---

## 2. Продуктовая концепция

Платформа позволяет:
- зарегистрироваться и войти в систему
- создать профиль
- выбрать продуктовые роли пользователя
- опубликовать навык или услугу
- задать доступные временные слоты
- забронировать слот
- зарезервировать кредиты в кошельке
- отменить бронирование по правилам возврата
- начать и завершить сессию
- оставить отзыв
- при конфликте открыть спор

Примеры навыков:
- Разбор Java interview
- Помощь с SQL-запросами
- Разговорный английский 30 минут
- Ревью резюме
- Подготовка к automation QA interview

---

## 3. Почему этот домен подходит для автотестов

В этом домене естественно возникают:
- роли и разграничение прав
- сложные статусы
- позитивные и негативные сценарии
- гонки данных при бронировании одного слота
- бизнес-правила отмены и возврата
- проверки валидации
- UI-флоу с несколькими экранами
- административные сценарии

Проект полезен одновременно для:
- API automation
- UI automation
- интеграционных тестов
- проверки бизнес-логики

---

## 4. Ролевая модель и доступ

### 4.1 Guest

Неавторизованный пользователь.

Может:
- смотреть публичный каталог
- открывать карточки услуг
- регистрироваться
- логиниться

Не может:
- бронировать
- создавать offers
- управлять слотами
- оставлять отзывы
- пользоваться wallet
- обращаться к приватным и admin endpoint-ам

### 4.2 Authenticated User Roles

У авторизованного пользователя хранится набор ролей:
- `STUDENT`
- `MENTOR`
- `ADMIN`

Правило доступа:
- итоговые права пользователя = объединение грантов всех назначенных ролей
- никакой "старшей роли" нет
- `ADMIN` не заменяет `STUDENT` или `MENTOR`, а добавляет административные полномочия

Допустимые комбинации:
- `STUDENT`
- `MENTOR`
- `MENTOR`
- `ADMIN`
- `ADMIN`
- `ADMIN`
- `ADMIN`

Для MVP:
- при публичной регистрации пользователь обязан выбрать минимум одну роль из `STUDENT` и `MENTOR`
- роль `ADMIN` нельзя выбрать через публичную регистрацию
- роль `ADMIN` назначается только seed-данными или служебным способом

### 4.3 Права роли STUDENT

Может:
- редактировать свой профиль
- пополнять условный баланс кредитов
- бронировать чужие слоты
- отменять свои бронирования
- видеть свои бронирования
- подтверждать факт прохождения сценариев student-side, если это понадобится в UI
- оставлять отзывы после `COMPLETED`
- открывать disputes

Не может:
- создавать offers без роли `MENTOR`
- управлять слотами без роли `MENTOR`
- пользоваться admin endpoint-ами без роли `ADMIN`

### 4.4 Права роли MENTOR

Может:
- создавать и редактировать свои offers
- создавать и удалять свои будущие свободные слоты
- видеть бронирования по своим offers
- видеть свой wallet как read-only зону истории заработка и выплат
- переводить бронирование в `COMPLETED`
- завершать бронирование через `complete`
- участвовать в disputes как владелец offer

Не может:
- пользоваться admin endpoint-ами без роли `ADMIN`
- бронировать без роли `STUDENT`
- вручную пополнять wallet без роли `STUDENT`

### 4.5 Права роли ADMIN

Может:
- просматривать и обновлять пользователей
- просматривать и обновлять offers
- создавать и удалять слоты в административном контуре с соблюдением обычных ограничений по состоянию сущностей
- просматривать, создавать, отменять, запускать, завершать и переводить bookings в `CANCELLED`, если система поддерживает это действие
- просматривать wallet-ы, пополнять кредиты и смотреть историю транзакций в административном контуре
- просматривать и обновлять disputes
- создавать reviews или disputes в административном контуре там, где это явно поддержано системой

Для MVP рекомендуется использовать отдельный seed-аккаунт администратора. Admin рассматривается как superuser-роль для поддержанных административных действий и не ограничивается read-only модерацией.

---

## 5. Границы MVP

### Входит в MVP

- регистрация
- логин
- JWT-аутентификация
- профиль пользователя
- выбор ролей `STUDENT` и `MENTOR` при регистрации
- просмотр каталога услуг
- создание и редактирование offers
- создание и удаление слотов
- бронирование слота
- резервирование кредитов
- отмена бронирования
- запуск сессии
- завершение сессии
- отзывы
- disputes в упрощенном виде
- базовая админ-панель

### Не входит в MVP

- реальная оплата внешними платежными системами
- чат в реальном времени
- видеозвонки
- сложная рекомендательная система
- многоязычность
- мобильная версия
- автоматический перерасчет денег по dispute
- полноценный dispute center с файлами и SLA
- сложный workflow модерации mentor-ов

---

## 6. Основные пользовательские сценарии

### Сценарий 1. Пользователь выступает как mentor

1. Пользователь регистрируется с ролью `MENTOR` или `MENTOR`
2. Логинится
3. Создает offer
4. Добавляет доступные слоты
5. Видит свою услугу в каталоге

### Сценарий 2. Пользователь выступает как student

1. Пользователь логинится с ролью `STUDENT` или `MENTOR`
2. Пополняет баланс кредитов
3. Находит услугу в каталоге
4. Открывает карточку услуги
5. Выбирает свободный слот
6. Бронирует слот
7. Видит бронирование в своем кабинете

### Сценарий 3. Проведение и завершение сессии

1. Mentor переводит booking в `COMPLETED`
2. Mentor завершает booking
3. Booking переходит в `COMPLETED`
4. Система выполняет capture средств
5. Student оставляет отзыв

### Сценарий 4. Отмена

1. Student открывает список своих бронирований
2. Выбирает бронирование
3. Отменяет его
4. Система применяет правило возврата
5. Статус booking и балансы обновляются
6. Если отмена произошла до `startTime`, слот снова становится доступным

### Сценарий 5. Администратор

1. Admin логинится
2. Открывает списки users, offers, bookings, disputes
3. При необходимости меняет статус пользователя
4. При необходимости блокирует или архивирует offer
5. При необходимости переводит dispute в новый статус

---

## 7. Доменная модель

### 7.1 User

Поля:
- `id`
- `email`
- `passwordHash`
- `firstName`
- `lastName`
- `displayName`
- `roles`
- `status`
- `createdAt`
- `updatedAt`

`roles`:
- `STUDENT`
- `MENTOR`
- `ADMIN`

`status`:
- `ACTIVE`
- `INACTIVE`

Правила:
- email уникален
- пользователь не может зарегистрироваться без роли `STUDENT` или `MENTOR`
- `ADMIN` не может быть назначен через публичную регистрацию
- неактивный пользователь не может логиниться

### 7.2 SkillOffer

Поля:
- `id`
- `mentorId`
- `title`
- `description`
- `category`
- `durationMinutes`
- `priceCredits`
- `cancellationPolicyHours`
- `status`
- `createdAt`
- `updatedAt`

`status`:
- `DRAFT`
- `ACTIVE`
- `ARCHIVED`
- `BLOCKED`

Правила:
- владелец c ролью `MENTOR` может редактировать свой offer
- `ADMIN` тоже может редактировать offers там, где система явно поддерживает административное управление offers
- бронировать можно только `ACTIVE` offer
- архивный offer остается в истории, но недоступен для новых бронирований
- `cancellationPolicyHours` сохраняется в модели для будущего расширения, но в MVP используется единый глобальный порог отмены `24` часа и per-offer override не применяется

### 7.3 AvailabilitySlot

Поля:
- `id`
- `offerId`
- `startTime`
- `endTime`
- `status`
- `createdAt`

`status`:
- `OPEN`
- `BOOKED`

Правила:
- слот принадлежит одному offer
- слот не может пересекаться с другим активным слотом того же mentor-а
- слот отвечает только за доступность времени
- коммерческое состояние сделки определяется сущностью `Booking`
- слот можно удалить только если он `OPEN`, находится в будущем и по нему нет booking

### 7.4 Booking

Поля:
- `id`
- `slotId`
- `offerId`
- `studentId`
- `mentorId`
- `status`
- `priceCredits`
- `reservedAmount`
- `cancelledByUserId`
- `noShowSide`
- `createdAt`
- `updatedAt`

`status`:
- `RESERVED`
- `COMPLETED`
- `COMPLETED`
- `CANCELLED`
- `CANCELLED`

`noShowSide`:
- `STUDENT`
- `MENTOR`
- `NULL`

Правила:
- self-booking ???????? ? MVP
- бронирование возможно только для свободного slot
- для бронирования должно хватать доступного баланса
- при бронировании кредиты резервируются
- повторное бронирование того же slot должно завершаться ошибкой
- `Booking` является единственным источником истины по состоянию сделки
- при создании `Booking` со статусом `RESERVED` slot становится `BOOKED`
- если booking отменен до `slot.startTime`, slot снова становится `OPEN`
- если booking перешел в `COMPLETED`, `COMPLETED` или `CANCELLED`, slot больше не переоткрывается

### 7.5 Wallet

Поля:
- `id`
- `userId`
- `balance`
- `reservedBalance`
- `updatedAt`

Правила:
- `balance` не может быть отрицательным
- `reservedBalance` не может быть отрицательным
- доступный баланс = `balance - reservedBalance`
- при бронировании сумма уходит в резерв
- после `COMPLETED` сумма списывается у student и начисляется mentor
- после ранней или поздней отмены выполняется release/refund/payout по правилам cancellation policy
- top-up тестовыми кредитами доступен только в `STUDENT` flow
- wallet для `MENTOR` в UI является read-only зоной истории заработка и выплат

### 7.6 Transaction

Поля:
- `id`
- `walletId`
- `bookingId`
- `type`
- `amount`
- `status`
- `createdAt`

`type`:
- `TOP_UP`
- `RESERVE`
- `RELEASE`
- `CAPTURE`
- `REFUND`
- `PAYOUT`
- `ADJUSTMENT`

`status`:
- `CREATED`
- `COMPLETED`
- `FAILED`

### 7.7 Review

Поля:
- `id`
- `bookingId`
- `authorId`
- `targetUserId`
- `rating`
- `comment`
- `createdAt`

Правила:
- guest может только читать публичные reviews на страницах offer
- student может создать review только для своего `COMPLETED` booking в обычном пользовательском сценарии
- student может редактировать или удалять только свои существующие reviews
- mentor может читать reviews, но не может создавать, редактировать или удалять их в MVP
- в обычном student flow один пользователь может оставить только один review на конкретное бронирование
- self-review ???????? ? MVP
- admin может создавать reviews в поддержанном административном контуре, включая сценарии без требования `COMPLETED` booking
- admin может редактировать или удалять любой review в поддержанном административном контуре

### 7.8 Dispute

Для MVP реализуется упрощенно.

Поля:
- `id`
- `bookingId`
- `createdBy`
- `reason`
- `description`
- `status`
- `resolution`
- `createdAt`
- `updatedAt`

`status`:
- `OPEN`
- `UNDER_REVIEW`
- `RESOLVED`
- `REJECTED`

Правила:
- dispute можно открыть только для существующего booking
- dispute не запускает автоматический перерасчет денег в MVP
- admin может просматривать и обновлять те данные и статусы dispute, которые явно поддержаны системой
- административное создание dispute, где оно поддержано, может быть вынесено в отдельный admin UI flow, но dispute все равно относится к конкретному booking

---

## 8. Бизнес-правила MVP

### 8.1 Регистрация и логин

- email должен быть уникальным
- пароль должен удовлетворять минимальным требованиям
- при регистрации пользователь обязан выбрать минимум одну роль из `STUDENT` и `MENTOR`
- после логина пользователь получает JWT access token

### 8.2 Создание offer

- пользователь с ролью `MENTOR` может создать offer
- admin тоже может создать offer, если система явно поддерживает административное управление offers
- title обязателен
- `priceCredits` должен быть больше 0
- `durationMinutes` должен быть больше 0

### 8.3 Создание slot

- slot нельзя создать в прошлом
- slot не должен пересекаться с существующим активным slot того же mentor-а
- slot можно создать только для своего offer, если только admin не выполняет поддержанное системой административное действие
- slot можно создать только для offer со статусом `ACTIVE`

### 8.4 Бронирование

- пользователь с ролью `STUDENT` может бронировать
- admin тоже может создать booking, если система явно поддерживает административные действия с bookings
- self-booking ???????? ? MVP
- для бронирования нужен достаточный доступный баланс
- нельзя забронировать неактивный offer
- нельзя забронировать уже занятый slot

### 8.5 Отмена

Все даты и сравнения выполняются в UTC.

Порог отмены:
- `24` часа до `slot.startTime`

Правила:
- для MVP cancellation policy глобальная; `cancellationPolicyHours` хранится в модели, но не участвует в расчетах booking
- если отмена сделана за `24` часа или раньше, student получает `100% refund`, mentor получает `0`
- если отмена сделана менее чем за `24` часа, student получает `50% refund`, mentor получает `50% payout`
- если отмена сделана mentor-ом до начала slot, student получает `100% refund`
- admin тоже может отменять поддержанные системой bookings в административном контуре, сохраняя обычные финансовые правила и правила переоткрытия slot
- ровно `24` часа до начала считается ранней отменой
- если отмена произошла до `slot.startTime`, slot снова становится `OPEN`
- если наступило `slot.startTime` или позже, slot не переоткрывается

### 8.6 Проведение и завершение

- mentor переводит booking в `COMPLETED`
- admin тоже может перевести поддержанный booking в `COMPLETED` в административном контуре
- mentor завершает booking через `complete`
- admin тоже может завершить поддержанный booking в административном контуре
- после `COMPLETED` сумма из резерва переводится mentor-у
- после `COMPLETED` доступно создание review

### 8.7 cancellation

- booking можно перевести в `CANCELLED`
- сторона cancellation фиксируется в поле `noShowSide`
- детальная финансовая логика cancellation может быть упрощена для MVP и зафиксирована отдельно при реализации
- в MVP `CANCELLED` не запускает автоматический перерасчет refund или payout

### 8.8 Администрирование

- admin может обновлять профильные данные, роли и статусы пользователей через поддержанные админ-инструменты
- admin может обновлять content offer и поддержанные системой offer statuses
- admin может управлять слотами в административном контуре с соблюдением обычных правил целостности slot
- admin может просматривать и изменять bookings через поддержанные административные действия
- admin может просматривать wallet-ы, пополнять кредиты и смотреть wallet transactions
- admin может просматривать и обновлять disputes
- admin может выполнять другие поддержанные cross-entity административные действия без ownership-ограничений

---

## 9. Состояния и переходы Booking

Базовый lifecycle:

`RESERVED -> COMPLETED`

Терминальные альтернативные ветки:
- `RESERVED -> CANCELLED`
- `RESERVED -> CANCELLED`
- `RESERVED -> CANCELLED` при упрощенном сценарии admin/manual resolution

Ограничения:
- нельзя перейти из `COMPLETED` обратно в предыдущий статус
- нельзя оставить review до `COMPLETED`
- нельзя отменить booking после `COMPLETED`
- `confirm-completion` как отдельный endpoint в MVP не требуется

---

## 10. REST API — предварительный контракт

### 10.1 Auth

#### POST `/api/auth/register`

Request:
- `email`
- `password`
- `firstName`
- `lastName`
- `roles`

Ограничения:
- допустимые роли для public registration: `STUDENT`, `MENTOR`
- `ADMIN` передавать нельзя

Response:
- `userId`
- `email`
- `roles`
- `status`

#### POST `/api/auth/login`

Request:
- `email`
- `password`

Response:
- `accessToken`
- `tokenType`
- `expiresIn`
- `user`

### 10.2 Users

#### GET `/api/users`

Получить профиль текущего пользователя.

#### GET `/api/users/{id}`

Обновить профиль текущего пользователя.

#### PATCH `/api/users/{id}`

Список пользователей для admin.



Изменение статуса пользователя.



Обновление данных профиля пользователя, ролей и статуса в admin scope.

### 10.3 Offers

#### GET `/api/offers`

Публичный список услуг.

Поддержка:
- pagination
- sorting
- filtering by category
- filtering by price range
- search by title

#### POST `/api/offers`

Создать offer. Требует роль `MENTOR` или поддержанные административные права.

#### GET `/api/offers/{id}`

Получить карточку offer.

#### PATCH `/api/offers/{id}`

Обновить свой offer или offer в admin scope.

#### PATCH `/api/offers/{id}/status`

Изменить статус offer.

Правила переходов:
- владелец может переключать `DRAFT <-> ACTIVE`
- владелец может переводить `ACTIVE -> ARCHIVED`
- admin может выполнять любые переходы статусов offer, которые явно поддержаны системой
- блокировка и снятие блокировки должны оставаться явными и аудируемыми

#### GET `/api/offers?scope=all`

Список offers для admin.

#### GET `/api/offers/{id}`

Детали offer для admin.

### 10.4 Slots

#### POST `/api/offers/{offerId}/slots`

Создать slot. Требует роль `MENTOR` или поддержанные административные права.

#### GET `/api/offers/{offerId}/slots`

Получить slot-ы offer.

#### DELETE `/api/slots/{slotId}`

Удалить slot.

Ограничения:
- slot должен быть `OPEN`
- slot должен быть в будущем
- по slot не должно существовать booking
- действие может выполнять владелец или admin, если система поддерживает удаление в этом контуре

### 10.5 Bookings

#### POST `/api/bookings`

Создать booking.

Request:
- `slotId`
- `studentId`

Response:
- `bookingId`
- `status`
- `reservedAmount`

Требует:
- аутентифицированного пользователя

Правила:
- `studentId` обязателен всегда
- `STUDENT` может создать booking только для своего `studentId`
- `ADMIN` может создать booking для любого выбранного пользователя с ролью `STUDENT`
- wallet reserve и последующий settlement применяются к выбранному student user, а не к admin

#### GET `/api/bookings`

Список бронирований текущего пользователя.

#### GET `/api/bookings/{id}`

Детали booking.

Правила доступа:
- student этого booking
- mentor, владелец offer
- admin

#### POST `/api/bookings/{id}/cancel`

Отменить booking.

Правила доступа:
- student может отменить свой booking до `COMPLETED`
- mentor может отменить booking по своему offer до `COMPLETED`
- admin тоже может отменить поддержанный booking в административном контуре

Поведение:
- финансовый результат зависит от того, кто отменяет, и от глобальной UTC cancellation policy

#### POST `/api/bookings/{id}/start`

Перевести booking в `COMPLETED`.

Правила доступа:
- mentor этого booking
- admin

#### POST `/api/bookings/{id}/complete`

Перевести booking в `COMPLETED` и выполнить capture.

Правила доступа:
- mentor этого booking
- admin

#### POST `/api/bookings/{id}/cancel`

Перевести booking в `CANCELLED`.

Правила доступа:
- mentor там, где это поддержано
- admin

#### GET `/api/bookings`

Список bookings для admin.

#### GET `/api/bookings/{id}`

Детали booking для admin.

#### POST `/api/bookings/{id}/cancel`

Отменить booking в admin scope.

#### POST `/api/bookings/{id}/start`

Перевести booking в `COMPLETED` в admin scope.

#### POST `/api/bookings/{id}/complete`

Перевести booking в `COMPLETED` в admin scope.

#### POST `/api/bookings/{id}/cancel`

Перевести booking в `CANCELLED` в admin scope.

### 10.6 Wallet

#### GET `/api/users/{id}/wallet`

Получить баланс текущего пользователя.

#### POST `/api/users/{id}/wallet/top-up`

Пополнить баланс тестовыми кредитами.

Доступ:
- `STUDENT`
- `MENTOR`
- другие `STUDENT` authenticated flows
- недоступно для `MENTOR`

#### GET `/api/users/{id}/wallet/transactions`

История транзакций.

#### GET `/api/users/{id}/wallet`

Получить wallet любого пользователя в admin scope.

#### POST `/api/users/{id}/wallet/top-up`

Пополнить wallet любого пользователя в admin scope.

#### GET `/api/users/{id}/wallet/transactions`

Получить wallet transactions любого пользователя в admin scope.

### 10.7 Reviews

#### POST `/api/bookings/{id}/reviews`

Оставить отзыв.

Доступ:
- student этого booking в обычном сценарии
- admin там, где система явно поддерживает административное создание review

#### PATCH `/api/reviews/{id}`

Обновить существующий review.

Доступ:
- author review для своего review в обычном student flow
- admin в поддержанном административном контуре

#### DELETE `/api/reviews/{id}`

Удалить существующий review.

Доступ:
- author review для своего review в обычном student flow
- admin в поддержанном административном контуре

#### GET `/api/offers/{offerId}/reviews`

Получить отзывы по offer.

#### GET `/api/reviews`

Получить список reviews в административном контуре.

#### GET `/api/reviews/{id}`

Получить детали review в административном контуре.

#### POST `/api/reviews`

Создать review напрямую в административном контуре.

#### PATCH `/api/reviews/{id}`

Обновить любой review в административном контуре.

#### DELETE `/api/reviews/{id}`

Удалить любой review в административном контуре.

### 10.8 Disputes

#### POST `/api/bookings/{id}/disputes`

Открыть dispute.

Доступ:
- участник кейса в обычном сценарии
- admin там, где система явно поддерживает административное создание dispute

Примечание:
- admin UI может открывать создание dispute через отдельную страницу, но dispute все равно должен относиться к конкретному booking

#### GET `/api/disputes/{id}`

Получить dispute по id для участника кейса или admin.

#### GET `/api/disputes`

Список disputes для admin.

#### PATCH `/api/disputes/{id}`

Обновить статус dispute.

---

## 11. HTTP-ошибки и валидации

Централизованно поддержать:
- `400 Bad Request` — неверные данные
- `401 Unauthorized` — неавторизованный запрос
- `403 Forbidden` — недостаточно прав
- `404 Not Found` — сущность не найдена
- `409 Conflict` — конфликт состояния, например slot уже забронирован
- `422 Unprocessable Entity` — бизнес-валидация

Примеры:
- повторное booking уже занятого slot -> `409`
- попытка оставить review до `COMPLETED` -> `422`
- редактирование чужого offer -> `403`
- попытка создать offer без роли `MENTOR` -> `403`
- попытка booking без роли `STUDENT` -> `403`

Единый формат ошибки:
- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `validationErrors`

---

## 12. UI-экраны для frontend

### Публичные

- Login page
- Register page
- Catalog page
- Offer details page

### Приватные

- Dashboard
- My profile
- My wallet / earnings view
- My bookings
- My offers
- Create/Edit offer
- Slot management
- Review submission form
- Dispute submission form

### Админские

- Admin users page
- Admin offers page
- Admin reviews page
- Admin bookings page
- Admin disputes page

UI должен учитывать роли пользователя и скрывать недоступные действия.

---

## 13. Какие API-тесты должны быть обязательно

### Auth

- успешная регистрация со `STUDENT`
- успешная регистрация с `MENTOR`
- успешная регистрация с `MENTOR`
- запрет регистрации без ролей
- запрет регистрации с `ADMIN`
- регистрация с существующим email
- логин с валидными данными
- логин с неверным паролем

### Offers

- создание offer пользователем с ролью `MENTOR`
- запрет создания offer пользователем без роли `MENTOR`
- редактирование своего offer
- запрет редактирования чужого offer
- фильтрация и пагинация каталога
- admin может обновить чужой offer
- admin может выполнять поддержанные системой переходы статусов offer

### Slots

- создание валидного slot
- запрет создания slot в прошлом
- запрет пересечения slot-ов
- удаление будущего свободного slot
- запрет удаления slot с booking
- admin может создать slot в admin scope
- admin может удалить допустимый slot в admin scope

### Bookings

- успешное booking пользователем с ролью `STUDENT`
- запрет booking без роли `STUDENT`
- self-booking ???????? ? MVP
- отказ при недостаточном балансе
- отказ при двойном booking
- отмена с полным возвратом
- отмена с частичным возвратом и payout
- переход в `COMPLETED`
- переход в `COMPLETED`
- admin может отменить чужой booking
- admin может перевести чужой booking в `COMPLETED` и `COMPLETED`
- admin может перевести booking в `CANCELLED`

### Wallet

- пополнение баланса
- корректное резервирование при booking
- корректный capture после завершения
- корректный payout mentor-у при поздней отмене
- запрет top-up для `MENTOR`
- admin может просмотреть чужой wallet
- admin может пополнить чужой wallet
- admin может просмотреть чужие wallet transactions

### Reviews

- успешное создание review после `COMPLETED`
- запрет оставить review до `COMPLETED`
- запрет дублирующего review
- student может редактировать свой review
- student не может редактировать чужой review
- student может удалять свой review
- student не может удалять чужой review
- mentor не может создавать review
- mentor не может редактировать или удалять reviews
- self-booking ???????? ? MVP
- admin может создавать review в административном контуре без обычного ограничения на completed booking
- admin может редактировать любой review
- admin может удалять любой review

### Disputes

- успешное создание dispute
- получение dispute участником кейса
- обновление статуса dispute admin-ом
- admin может создать dispute в административном контуре, если это поддержано системой

### Admin

- блокировка пользователя admin-ом
- обновление профиля пользователя и ролей admin-ом
- запрет admin endpoints обычному пользователю
- просмотр admin users/offers/bookings/disputes
- admin может обновлять чужие offers и поддержанные offer statuses
- admin может изменять чужие bookings через поддержанные admin actions
- admin может просматривать и пополнять чужие wallet-ы

---

## 14. Какие UI-тесты должны быть обязательно

### Happy path

1. Регистрация `STUDENT`
2. Логин
3. Пополнение wallet
4. Поиск offer
5. Booking
6. Просмотр booking в кабинете

### Mentor flow

1. Регистрация или логин `MENTOR`
2. Создание offer
3. Добавление slot
4. Проверка, что slot отображается

### single-role flow

1. Пользователь с `MENTOR` публикует свой offer
2. Тот же пользователь бронирует чужой slot
3. Проверяется доступность обоих сценариев в UI
- self-booking ???????? ? MVP

### Cancel flow

1. Student отменяет booking
2. Проверяется предупреждение о refund policy
3. Проверяется обновление статуса и баланса в UI

### Review flow

1. После `COMPLETED` student оставляет review
2. Review появляется на странице offer

### Authorization / Role UI

- пользователь без `MENTOR` не видит create offer actions
- пользователь без `STUDENT` не видит booking actions
- обычный пользователь не видит admin menu
- admin видит admin menu
- guest не может бронировать без логина
- `MENTOR` видит wallet history, но не видит controls для top-up
- admin может открыть отдельный create-dispute flow там, где он поддержан системой

---

## 15. Предлагаемый технический стек

### Backend

- Java 17+
- Spring Boot 3
- Spring Web
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Lombok
- Validation API

### Frontend

- React
- TypeScript
- Vite
- React Router
- React Query
- MUI

### Testing

- JUnit 5
- REST Assured
- Playwright for Java
- Allure Reports

### Infra

- Docker
- Docker Compose

---

## 16. Структура backend-модулей

Рекомендуемая package-структура:

- `auth`
- `user`
- `offer`
- `slot`
- `booking`
- `wallet`
- `review`
- `dispute`
- `admin`
- `common`
- `security`

Внутри каждого модуля:
- `controller`
- `service`
- `repository`
- `dto`
- `entity`
- `mapper`
- `exception`

---

## 17. Seed data

Для удобства тестов стоит предусмотреть начальные данные:
- `admin@test.com`
- `mentor1@test.com`
- `mentor2@test.com`
- `student1@test.com`
- `student2@test.com`
- `mentor3@test.com`

Рекомендуемые роли:
- `admin@test.com` -> `ADMIN`
- `mentor1@test.com` -> `MENTOR`
- `mentor2@test.com` -> `MENTOR`
- `student1@test.com` -> `STUDENT`
- `student2@test.com` -> `STUDENT`
- `mentor3@test.com` -> `MENTOR`

Также полезно предсоздать:
- 5-10 offers
- 10-20 slot-ов
- несколько `COMPLETED` bookings
- несколько `ARCHIVED` offers
- несколько disputes в разных статусах

Seed data должны быть детерминированными.

---

## 18. Расширения для V2

После MVP можно добавить:
- полноценный dispute workflow
- notifications
- waitlist на занятый slot
- promo codes
- trust score
- auto-cancel scheduler
- audit log
- webhooks/mock integrations
- file attachments in dispute
- report abuse
- moderation flow для mentor verification

---

## 19. Что важно для реализации в Codex

При реализации нужно сделать акцент не на максимальном количестве функций, а на качестве демонстрации тестопригодности.

Ключевые требования:
- понятная и чистая архитектура
- предсказуемые статусы
- реальные бизнес-валидации
- единый формат ошибок
- хорошие seed data
- удобный UI для E2E
- `data-testid` для ключевых элементов
- несколько сильных end-to-end сценариев
- хранение дат в UTC
- защита от double booking на уровне БД и транзакций

---

## 20. Практический план реализации

### Этап 1

- поднять backend skeleton
- настроить PostgreSQL и Flyway
- настроить security и JWT
- реализовать auth + users + roles

### Этап 2

- реализовать offers + slots
- реализовать catalog
- внедрить проверки ownership и role-based access

### Этап 3

- реализовать wallet + ledger
- реализовать bookings
- внедрить reserve/cancel/capture/payout logic
- добавить защиту от double booking

### Этап 4

- реализовать reviews
- реализовать disputes в MVP-объеме
- добавить admin endpoints

### Этап 5

- поднять frontend
- сделать основные экраны
- связать с backend
- добавить role-based UI

### Этап 6

- написать API test suite
- написать Playwright UI suite
- подключить Allure
- подготовить seed data и demo scenarios

---

## 21. Финальная рекомендация

Для демо-проекта не стоит пытаться сделать полноценный маркетплейс. Лучше сделать компактную, но логически насыщенную систему.

`SkillSwap Market` хорошо подходит, потому что в нем есть:
- роли
- статусы
- жизненный цикл booking
- денежная логика через кредиты
- конфликтные сценарии
- естественные API и UI тесты

MVP формулируется так:

`SkillSwap Market` — это marketplace временных slot-ов, где пользователь может иметь одну или несколько ролей (`STUDENT`, `MENTOR`, `ADMIN`), slot отвечает только за доступность времени, booking является единственным источником истины по состоянию сделки, а dispute в MVP нужен для трекинга кейса без автоматического финансового перерасчета.
