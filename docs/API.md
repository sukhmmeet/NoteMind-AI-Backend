# NoteMind API Reference

This document describes the REST endpoints currently exposed by the controllers in
`src/main/java/com/dhaliwal/notemind/controller`.

## Base URL and authentication

The application context path is `/api/v1`. For a local server, use:

```text
http://localhost:8080/api/v1
```

All endpoints under `/auth/**` are public. Every other endpoint requires:

```http
Authorization: Bearer <jwtToken>
```

Successful responses use this wrapper:

```json
{
  "success": true,
  "message": "Success",
  "data": {},
  "timestamp": "2026-01-01T12:00:00Z"
}
```

For message-only operations, `data` is omitted. Error responses use the same
wrapper with `success: false` and an error `message`.

## Endpoint summary

| Method | Path | Auth | Status | Description |
|---|---|---:|---:|---|
| POST | `/auth/signup` | No | 200 | Create a user |
| POST | `/auth/login` | No | 200 | Authenticate a user |
| POST | `/auth/refresh-token` | No | 200 | Exchange a refresh token |
| POST | `/auth/logout` | No | 200 | Revoke a refresh token |
| GET | `/dashboard` | Yes | 200 | Get the current user's dashboard summary |
| POST | `/notes` | Yes | 200 | Create a note |
| GET | `/notes` | Yes | 200 | List notes with pagination |
| GET | `/notes/{id}` | Yes | 200 | Get one note |
| PUT | `/notes/{id}` | Yes | 200 | Update a note |
| DELETE | `/notes/{id}` | Yes | 200 | Delete a note |
| GET | `/notes/search` | Yes | 200 | Search the current user's notes |
| GET | `/notes/refresh-summary/{id}` | Yes | 200 | Regenerate a note summary |
| POST | `/notes/{noteId}/flashcards/generate` | Yes | 201 | Generate flashcards with AI |
| POST | `/notes/{noteId}/flashcards/regenerate` | Yes | 200 | Replace flashcards with a new AI set |
| GET | `/notes/{noteId}/flashcards` | Yes | 200 | List a note's flashcards |
| POST | `/notes/{noteId}/flashcards` | Yes | 201 | Create one flashcard |
| DELETE | `/notes/{noteId}/flashcards` | Yes | 200 | Delete all flashcards for a note |
| GET | `/flashcards/{flashCardId}` | Yes | 200 | Get one flashcard |
| PATCH | `/flashcards/{flashCardId}` | Yes | 200 | Update one flashcard |
| DELETE | `/flashcards/{flashCardId}` | Yes | 200 | Delete one flashcard |
| POST | `/notes/{noteId}/quiz/generate` | Yes | 201 | Generate quiz questions with AI |
| POST | `/folder` | Yes | 200 | Create a folder |
| GET | `/folder` | Yes | 200 | List folders |
| GET | `/folder/{id}` | Yes | 200 | Get one folder |
| PATCH | `/folder/{id}` | Yes | 200 | Update a folder |
| DELETE | `/folder/{id}` | Yes | 200 | Delete a folder |

### `GET /dashboard`

Returns the authenticated user's username, totals for notes, folders,
flashcards, and generated quizzes, plus the four most recently updated notes.
Quiz totals count successful quiz-generation requests made after quiz tracking
was introduced; previously generated quizzes were not persisted.

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "username": "john_doe",
    "totalNotes": 8,
    "totalFolders": 3,
    "totalFlashcards": 12,
    "totalQuizzes": 2,
    "recentNotes": []
  }
}
```

## Common data models

### `AuthRequestDto`

```json
{
  "username": "john_doe",
  "password": "securePass123"
}
```

### `AuthResponseDto`

```json
{
  "username": "john_doe",
  "userId": 1,
  "jwtToken": "<access-token>",
  "refreshToken": "<refresh-token>"
}
```

### `RefreshRequestDto` and `RefreshResponseDto`

```json
{ "refreshToken": "<refresh-token>" }
```

```json
{
  "refreshToken": "<refresh-token>",
  "jwtToken": "<access-token>"
}
```

### `NoteDto`

```json
{
  "id": 1,
  "title": "Quantum Computing",
  "content": "Quantum computers use qubits...",
  "summary": "A generated summary...",
  "summaryType": "DETAILED",
  "imageUrl": "https://...",
  "createdAt": "2026-01-01T12:00:00",
  "updatedAt": "2026-01-01T12:00:00",
  "tags": [
    { "id": 1, "name": "technology" }
  ],
  "folderId": 2
}
```

`summaryType` is one of `SHORT`, `DETAILED`, or `BULLET_POINTS`.

### `FlashCardDto`

```json
{
  "id": 1,
  "question": "What is a qubit?",
  "answer": "The basic unit of quantum information.",
  "noteId": 1,
  "createdAt": "2026-01-01T12:00:00"
}
```

Flashcard request bodies contain `question` and `answer`.

### `FolderDtoWithoutNotes` and `FolderDto`

```json
{ "id": 1, "name": "Study Notes" }
```

When `includeNotes=true`, folders are returned with a `notes` array containing
`NoteDto` objects:

```json
{
  "id": 1,
  "name": "Study Notes",
  "notes": []
}
```

### `QuizQuestionDto`

```json
{
  "question": "What is a qubit?",
  "options": ["A data type", "A quantum bit", "A database", "A protocol"],
  "correctAnswer": "A quantum bit",
  "explanation": "A qubit is the basic unit of quantum information."
}
```

## Authentication

### `POST /auth/signup`

Creates a new user.

```bash
curl -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"securePass123"}'
```

Returns an `AuthResponseDto` inside `data`.

### `POST /auth/login`

Authenticates an existing user.

```bash
curl -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"securePass123"}'
```

Returns an `AuthResponseDto` inside `data`.

### `POST /auth/refresh-token`

Creates a new access token from a refresh token.

```bash
curl -X POST "$BASE_URL/auth/refresh-token" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

Returns a `RefreshResponseDto` inside `data`.

### `POST /auth/logout`

Revokes the supplied refresh token.

```bash
curl -X POST "$BASE_URL/auth/logout" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

Returns a message-only success response.

## Notes

### `POST /notes`

Creates a note using `multipart/form-data`.

Parts:

| Part | Required | Type | Description |
|---|---:|---|---|
| `note` | Yes | JSON string | A `NoteDto` request; normally includes `title`, `content`, `summaryType`, and optionally `folderId` |
| `image` | No | File | Optional image attachment |

```bash
curl -X POST "$BASE_URL/notes" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F 'note={"title":"Quantum Computing","content":"Quantum computers use qubits","summaryType":"DETAILED","folderId":2};type=application/json' \
  -F 'image=@diagram.png'
```

Returns the created `NoteDto` inside `data`.

### `GET /notes`

Lists notes belonging to the authenticated user.

Query parameters are Spring Data pagination parameters:

| Parameter | Default | Description |
|---|---:|---|
| `page` | `0` | Zero-based page number |
| `size` | `10` | Page size |
| `sort` | `createdAt` | Sort expression, for example `createdAt,desc` |

```bash
curl "$BASE_URL/notes?page=0&size=10&sort=createdAt,desc" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

Returns a Spring `Page<NoteDto>` inside `data`.

### `GET /notes/{id}`

Returns one note owned by the authenticated user.

```bash
curl "$BASE_URL/notes/1" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### `PUT /notes/{id}`

Updates a note using the same multipart format as note creation. The `note`
part is required and the `image` part is optional.

```bash
curl -X PUT "$BASE_URL/notes/1" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F 'note={"title":"Updated title","content":"Updated content","summaryType":"SHORT"};type=application/json' \
  -F 'image=@updated-image.jpg'
```

Returns the updated `NoteDto` inside `data`.

### `DELETE /notes/{id}`

Deletes a note and returns a message-only success response.

### `GET /notes/search`

Searches the authenticated user's notes.

| Query parameter | Required | Description |
|---|---:|---|
| `query` | Yes | Search term |

```bash
curl "$BASE_URL/notes/search?query=quantum" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

Returns a JSON array of `NoteDto` objects inside `data`.

### `GET /notes/refresh-summary/{id}`

Regenerates a note's summary.

| Query parameter | Required | Values |
|---|---:|---|
| `type` | Yes | `SHORT`, `DETAILED`, `BULLET_POINTS` |

```bash
curl "$BASE_URL/notes/refresh-summary/1?type=BULLET_POINTS" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

Returns the updated `NoteDto` inside `data`.

## Flashcards

### `POST /notes/{noteId}/flashcards/generate`

Generates AI flashcards for a note.

| Query parameter | Default | Constraints |
|---|---:|---|
| `count` | `5` | Integer from `1` to `50` |

Returns an array of `FlashCardDto` objects.

### `POST /notes/{noteId}/flashcards/regenerate`

Regenerates the note's flashcards. It accepts the same `count` parameter
(default `5`, minimum `1`, maximum `50`) and returns a new array of
`FlashCardDto` objects.

### `GET /notes/{noteId}/flashcards`

Returns all `FlashCardDto` objects for a note.

### `POST /notes/{noteId}/flashcards`

Creates one manual flashcard.

```bash
curl -X POST "$BASE_URL/notes/1/flashcards" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question":"What is a qubit?","answer":"The basic unit of quantum information."}'
```

Returns the created `FlashCardDto`.

### `DELETE /notes/{noteId}/flashcards`

Deletes all flashcards belonging to a note. Returns a message-only success
response.

### `GET /flashcards/{flashCardId}`

Returns one `FlashCardDto`.

### `PATCH /flashcards/{flashCardId}`

Updates a flashcard. The request body contains `question` and `answer`.

```bash
curl -X PATCH "$BASE_URL/flashcards/1" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question":"Updated question","answer":"Updated answer"}'
```

### `DELETE /flashcards/{flashCardId}`

Deletes one flashcard and returns a message-only success response.

## Quizzes

### `POST /notes/{noteId}/quiz/generate`

Generates AI quiz questions for a note.

| Query parameter | Default | Constraints |
|---|---:|---|
| `count` | `5` | Integer from `1` to `20` |

```bash
curl -X POST "$BASE_URL/notes/1/quiz/generate?count=5" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

Returns an array of `QuizQuestionDto` objects.

## Folders

### `POST /folder`

Creates a folder.

```bash
curl -X POST "$BASE_URL/folder" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Study Notes"}'
```

Returns a `FolderDtoWithoutNotes`.

### `GET /folder`

Lists folders for the authenticated user.

| Query parameter | Default | Description |
|---|---:|---|
| `includeNotes` | `false` | Set to `true` to include nested notes |

Without `includeNotes=true`, returns an array of `FolderDtoWithoutNotes`.
With it, returns an array of `FolderDto`.

### `GET /folder/{id}`

Returns one `FolderDtoWithoutNotes`.

### `PATCH /folder/{id}`

Updates a folder name.

```bash
curl -X PATCH "$BASE_URL/folder/1" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Exam Notes"}'
```

Returns the updated `FolderDtoWithoutNotes`.

### `DELETE /folder/{id}`

Deletes a folder and returns a message-only success response.

## Error responses

Errors use the standard wrapper:

```json
{
  "success": false,
  "message": "Note not found",
  "timestamp": "2026-01-01T12:00:00Z"
}
```

The global exception handler currently maps errors as follows:

| Status | Examples |
|---:|---|
| 400 | Invalid note/folder data, empty search term |
| 401 | Invalid credentials, invalid or expired refresh token, unauthenticated request |
| 404 | User, note, folder, or flashcard not found |
| 409 | Duplicate username or folder |
| 413 | Multipart request exceeds the configured 10 MB limit |
| 502 | AI provider failure |
| 500 | Image upload or unexpected server error |

Define `BASE_URL` before using the examples:

```bash
export BASE_URL=http://localhost:8080/api/v1
```
