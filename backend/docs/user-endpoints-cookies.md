# User Profile Endpoints (Cookie-based Authentication)

Questa documentazione descrive gli endpoint disponibili per la gestione del profilo utente utilizzando l'autenticazione basata su cookie.

## Autenticazione

Il sistema utilizza cookie httpOnly per l'autenticazione, che è più sicuro rispetto ai token nel body. I cookie vengono impostati automaticamente durante il login.

### Login per ottenere i cookie

```bash
POST /auth/login
Content-Type: application/json
User-Agent: my-app

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Risposta:**
- Status: 200
- Cookie: `accessToken` e `refreshToken` impostati automaticamente
- Body: `{ "user": {...} }`

## Endpoints

### 1. GET /users/me

Ottiene il profilo dell'utente corrente.

**Headers richiesti:**
- Cookie: `accessToken` (impostato automaticamente dal login)

**Risposta di successo (200):**
```json
{
  "userId": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "profilePic": "/uploads/profile-pics/1/avatar.jpg",
  "role": "USER",
  "createdAt": "2024-01-01T00:00:00.000Z",
  "updatedAt": "2024-01-01T00:00:00.000Z"
}
```

**Risposta di errore (401):**
```json
{
  "statusCode": 401,
  "message": "Unauthorized",
  "error": "INVALID_ACCESS_TOKEN"
}
```

### 2. PATCH /users/me

Aggiorna i dati del profilo dell'utente corrente.

**Headers richiesti:**
- Cookie: `accessToken` (impostato automaticamente dal login)
- Content-Type: application/json

**Body richiesto:**
```json
{
  "firstName": "John",        // opzionale
  "lastName": "Doe",          // opzionale
  "email": "new@example.com", // opzionale
  "phone": "+1234567890"      // opzionale
}
```

**Risposta di successo (200):**
```json
{
  "userId": 1,
  "email": "new@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "profilePic": "/uploads/profile-pics/1/avatar.jpg",
  "role": "USER",
  "createdAt": "2024-01-01T00:00:00.000Z",
  "updatedAt": "2024-01-01T00:00:00.000Z"
}
```

**Risposta di errore (400) - Email già esistente:**
```json
{
  "statusCode": 400,
  "message": "Email already exists",
  "error": "Bad Request"
}
```

### 3. PATCH /users/me/profile-pic

Aggiorna la foto del profilo dell'utente corrente.

**Headers richiesti:**
- Cookie: `accessToken` (impostato automaticamente dal login)

**Body richiesto:**
- `multipart/form-data` con campo `file` contenente l'immagine

**Risposta di successo (200):**
```json
{
  "profilePic": "/uploads/profile-pics/1/new-avatar.jpg"
}
```

## Esempi di utilizzo

### JavaScript/Node.js (con cookie)
```javascript
const axios = require('axios');

// Login per ottenere i cookie
const loginResponse = await axios.post('http://localhost:3000/auth/login', {
  email: 'user@example.com',
  password: 'password123'
}, {
  headers: { 'User-Agent': 'my-app' },
  withCredentials: true
});

// I cookie vengono impostati automaticamente per le richieste successive
// Ottieni profilo
const profile = await axios.get('http://localhost:3000/users/me', {
  withCredentials: true
});

// Aggiorna profilo
const updatedProfile = await axios.patch('http://localhost:3000/users/me', {
  firstName: 'Updated',
  lastName: 'Name',
  phone: '+1234567890'
}, {
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true
});
```

### cURL (con cookie)
```bash
# Login (i cookie vengono salvati automaticamente)
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -H "User-Agent: my-app" \
  -d '{"email":"user@example.com","password":"password123"}' \
  -c cookies.txt

# Ottieni profilo (usa i cookie salvati)
curl -X GET http://localhost:3000/users/me \
  -b cookies.txt

# Aggiorna profilo (usa i cookie salvati)
curl -X PATCH http://localhost:3000/users/me \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"firstName":"Updated","lastName":"Name"}'
```

### Browser (JavaScript)
```javascript
// Login
const loginResponse = await fetch('/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'User-Agent': 'my-app'
  },
  credentials: 'include', // Importante per i cookie
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password123'
  })
});

// Ottieni profilo (i cookie vengono inviati automaticamente)
const profile = await fetch('/users/me', {
  credentials: 'include'
});

// Aggiorna profilo
const updatedProfile = await fetch('/users/me', {
  method: 'PATCH',
  headers: {
    'Content-Type': 'application/json'
  },
  credentials: 'include',
  body: JSON.stringify({
    firstName: 'Updated',
    lastName: 'Name'
  })
});
```

## Vantaggi dell'autenticazione basata su cookie

1. **Sicurezza**: I cookie httpOnly non sono accessibili via JavaScript
2. **Automatico**: I cookie vengono inviati automaticamente con ogni richiesta
3. **CSRF Protection**: I cookie possono essere protetti da attacchi CSRF
4. **Scadenza**: I cookie possono avere scadenze automatiche
5. **SameSite**: I cookie possono essere configurati per prevenire attacchi cross-site

## Note

- I cookie `accessToken` hanno una durata di 15 minuti
- I cookie `refreshToken` hanno una durata di 30 giorni
- Tutti i campi nel body del PATCH sono opzionali
- L'email deve essere unica nel sistema
- La password non viene mai restituita nelle risposte
- Le immagini del profilo vengono salvate in `/uploads/profile-pics/{userId}/`

## Test

Per testare gli endpoint:

```bash
# Creare utente di test
npm run create-test-user

# Testare endpoint con cookie
npm run test-user-endpoints-cookies
```
