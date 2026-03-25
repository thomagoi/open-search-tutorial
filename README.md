# open-search-tutorial

A Quarkus application that demonstrates full-text search using Hibernate Search backed by OpenSearch. Authors and books are stored in PostgreSQL and automatically indexed into OpenSearch on every write.

## Stack

| Component   | Technology                          |
|-------------|-------------------------------------|
| REST API    | Quarkus REST (JAX-RS) + Jackson     |
| Database    | PostgreSQL 16 (via Hibernate Panache)|
| Search      | OpenSearch 2.19 (via Hibernate Search)|
| Runtime     | Java 21, Quarkus 3.32.4             |

---

## Getting started

### 1. Build the application JAR

```bash
cd opensearch-demo
./mvnw package -DskipTests
cd ..
```

### 2. Start all services

```bash
docker-compose up
```

This starts three containers:
- **postgres** on port `5432`
- **opensearch** on port `9200`
- **app** on port `8080` (waits for the other two to be healthy)

---

## Use case walkthrough

### Create an author

```bash
curl -s -X POST http://localhost:8080/authors \
  -H 'Content-Type: application/json' \
  -d '{"name": "Stephen King", "birthdate": "1947-09-21"}' | jq
```

Copy the `id` from the response — you need it in the next step.

### Add books for that author

```bash
curl -s -X POST http://localhost:8080/books \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "The Shining",
    "description": "A family heads to an isolated hotel for the winter where a sinister presence influences the father.",
    "price": 1299,
    "genre": "HORROR",
    "authorId": "<author-id>"
  }' | jq

curl -s -X POST http://localhost:8080/books \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "It",
    "description": "A group of children face an evil shapeshifting entity lurking in the sewers of their town.",
    "price": 1499,
    "genre": "HORROR",
    "authorId": "<author-id>"
  }' | jq
```

> `price` is in cents. Valid genres: `THRILLER`, `HORROR`, `FANTASY`, `SCI_FI`

### Search for books

```bash
# Search by title
curl "http://localhost:8080/search/books?q=shining"

# Search by description keyword
curl "http://localhost:8080/search/books?q=shapeshifting"

# Search across title, description and author name at once
curl "http://localhost:8080/search/books?q=king"
```

### Search for authors

```bash
# Search by name
curl "http://localhost:8080/search/authors?q=stephen"

# Results include the author's full book list embedded
curl "http://localhost:8080/search/authors?q=king" | jq '.[0].books'
```

### Limit results

Both search endpoints accept a `size` parameter (default `20`):

```bash
curl "http://localhost:8080/search/books?q=horror&size=5"
```

---

## Full REST API reference

### Authors — `/authors`

| Method | Path            | Description        |
|--------|-----------------|--------------------|
| GET    | `/authors`      | List all authors   |
| GET    | `/authors/{id}` | Get author by id   |
| POST   | `/authors`      | Create an author   |
| PUT    | `/authors/{id}` | Update an author   |
| DELETE | `/authors/{id}` | Delete an author   |

**Author body:**
```json
{
  "name": "Stephen King",
  "birthdate": "1947-09-21"
}
```

### Books — `/books`

| Method | Path         | Description     |
|--------|--------------|-----------------|
| GET    | `/books`     | List all books  |
| GET    | `/books/{id}`| Get book by id  |
| POST   | `/books`     | Create a book   |
| PUT    | `/books/{id}`| Update a book   |
| DELETE | `/books/{id}`| Delete a book   |

**Book body:**
```json
{
  "title": "The Shining",
  "description": "A family heads to an isolated hotel for the winter.",
  "price": 1299,
  "genre": "HORROR",
  "authorId": "<uuid>"
}
```

### Search — `/search`

| Method | Path              | Query params         |
|--------|-------------------|----------------------|
| GET    | `/search/authors` | `q` (text), `size`   |
| GET    | `/search/books`   | `q` (text), `size`   |

Search is full-text across all relevant fields:
- **Authors:** searches `name` and embedded `books.title`
- **Books:** searches `title`, `description`, and embedded `author.name`
