# OpenSearch Demo

A Quarkus application demonstrating full-text search with OpenSearch/Hibernate Search, backed by PostgreSQL.

## Running the application

```bash
./mvnw quarkus:dev
```

The app starts on `http://localhost:8080` and seeds the database with 3 authors and 8 books on startup.

---

## API Examples

### Authors

**List all authors**
```bash
curl http://localhost:8080/authors
```

**Get a specific author**
```bash
curl http://localhost:8080/authors/a1000000-0000-0000-0000-000000000001
```

**Create an author**
```bash
curl -X POST http://localhost:8080/authors \
  -H "Content-Type: application/json" \
  -d '{"name": "George Orwell", "birthdate": "1903-06-25"}'
```

---

### Books

**List all books**
```bash
curl http://localhost:8080/books
```

**Get a specific book**
```bash
curl http://localhost:8080/books/b1000000-0000-0000-0000-000000000001
```

**Create a book**
```bash
curl -X POST http://localhost:8080/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "1984",
    "description": "A dystopian novel about a totalitarian society where Big Brother watches its citizens.",
    "price": 999,
    "genre": "THRILLER",
    "authorId": "a1000000-0000-0000-0000-000000000001"
  }'
```

> Genres: `HORROR`, `THRILLER`, `FANTASY`, `SCI_FI`

---

### Search (OpenSearch)

**Search books by keyword**
```bash
curl "http://localhost:8080/search/books?q=ring"
```

**Search books — horror genre terms**
```bash
curl "http://localhost:8080/search/books?q=evil"
```

**Search books — sci-fi terms**
```bash
curl "http://localhost:8080/search/books?q=android"
```

**Search books — limit results**
```bash
curl "http://localhost:8080/search/books?q=world&size=3"
```

**Search authors by name**
```bash
curl "http://localhost:8080/search/authors?q=king"
```

**Search authors — partial name**
```bash
curl "http://localhost:8080/search/authors?q=tolkien"
```

**Search authors — limit results**
```bash
curl "http://localhost:8080/search/authors?q=philip&size=1"
```

---

## Seed Data

| Author | Books |
|---|---|
| Stephen King | The Shining, It, Misery |
| J.R.R. Tolkien | The Fellowship of the Ring, The Two Towers, The Return of the King |
| Philip K. Dick | Do Androids Dream of Electric Sheep?, The Man in the High Castle |