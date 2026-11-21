# Админ-доступ и метки

Пароль `dima228` нельзя безопасно вшивать в Android-приложение: APK можно распаковать, а пароль извлечь. Поэтому приложение должно показывать админ-панель только аккаунтам с Firebase Custom Claim `admin: true`. Проверка прав и запись меток должны выполняться серверными правилами.

## Настройка первого администратора

1. Создай аккаунт обычным способом через Firebase Authentication (Email/Password).
2. Скопируй UID аккаунта из Firebase Console → Authentication → Users.
3. В доверенной серверной среде (например, Cloud Shell с Firebase Admin SDK) назначь claim:

```javascript
const { initializeApp } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
initializeApp();
await getAuth().setCustomUserClaims('UID_АДМИНА', { admin: true });
```

4. Выйди из приложения и войди снова, чтобы обновился ID token.
5. Примени файл `firestore.rules` в Firebase Console → Firestore Database → Rules.

Не размещай service-account JSON, приватные ключи или Admin SDK в Android-клиенте и публичном GitHub. Если обязательно нужен именно пароль `dima228`, его можно использовать только как секрет серверной функции для первоначального назначения администратора, но не как проверку внутри APK.

## Метки
Поддерживаемые значения: `scam`, `verified`, `fake`. Клиент не должен позволять обычному пользователю изменять эти поля. Серверная проверка claim обязательна.
