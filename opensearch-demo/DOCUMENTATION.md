# OpenSearch Demo — Project Documentation

## Overview

This project is a simple book catalog API built with **Quarkus** (a Java framework). It stores authors and books in a **PostgreSQL** database and makes them searchable using **OpenSearch** — a powerful full-text search engine.

The key idea is: PostgreSQL is the source of truth for data, while OpenSearch acts as the search index that makes queries fast and smart.

```
┌───────────┐         ┌──────────────────────────────────────┐
│  Client   │  HTTP   │           Quarkus App                │
│ (Browser, │ ──────▶ │  REST Resources  │  Business Logic   │
│  Tool, …) │         │  (AuthorResource,│  (SearchService,  │
└───────────┘         │  SearchResource) │   Hibernate ORM)  │
                      └──────────────────┴────────┬──────────┘
                                                  │
                                   Read / Write   │
                                                  │
                         ┌────────────────────────┴────────────────────────┐
                         │                                                 │
              ┌──────────────────┐                          ┌──────────────────────┐
              │   PostgreSQL     │                          │     OpenSearch       │
              │  (Source of      │ ◀──── Sync ────────────▶ │  (Search Index,      │
              │   Truth)         │      (Hibernate Search)  │   Full-text Search)  │
              └──────────────────┘                          └──────────────────────┘
```

---

## 1. Project Structure

The application follows a three-layer architecture:

```
HTTP Request
     │
     ▼
┌──────────────┐
│   Resource   │  ← Receives the HTTP request, validates input
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   Service    │  ← Contains the business logic (e.g. search queries)
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Database /  │  ← Stores and retrieves data
│  OpenSearch  │
└──────────────┘
```

### Layer 1 — Resource (HTTP Entry Point)

Resources are the outermost layer. They define the HTTP endpoints that clients call.

**Example:** `AuthorResource.java`

```
GET  /authors          → list all authors
GET  /authors/{id}     → get a single author
POST /authors          → create a new author
PUT  /authors/{id}     → update an author
DELETE /authors/{id}   → delete an author
```

The resource receives the incoming request, calls the appropriate method (either directly on the entity or via a Service), and returns a response.

For standard CRUD operations (create, read, update, delete), the resource talks directly to the database through the entity class.

For search, the resource delegates to the `SearchService`:

```
GET /search/authors?q=tolkien   →  SearchResource  →  SearchService  →  OpenSearch
```

### Layer 2 — Service (Business Logic)

The `SearchService` is where the actual OpenSearch queries are built. It uses **Hibernate Search** — a library that bridges Java objects and OpenSearch — to construct and execute queries.

### Layer 3 — Data (Database + Search Index)

- **PostgreSQL** stores the actual author and book records.
- **OpenSearch** stores a search-optimized copy (the "index") of that data.

Both are kept in sync automatically by Hibernate Search.

---

## 2. How the Application Connects to OpenSearch

### Step 1 — Telling OpenSearch Where to Find the Data (Annotations)

OpenSearch does not automatically know which fields of your Java classes it should index. You tell it using annotations directly on the entity.

Here is the `Author` class:

```java
@Entity
@Indexed                         // ← "Index this class in OpenSearch"
public class Author extends PanacheEntityBase {

    @FullTextField(analyzer = "name_analyzer")   // ← searchable text field
    @KeywordField(name = "name_sort", sortable = Sortable.YES)  // ← sortable version
    public String name;

    public LocalDate birthdate;  // ← NOT annotated → NOT indexed, not searchable

    @IndexedEmbedded(includePaths = {"title", "description", "genre"})
    public List<Book> books;     // ← include selected book fields in the author's index
}
```

Think of the OpenSearch index as a search-ready copy of your data. By annotating fields, you choose exactly which pieces of information land in that copy.

**Annotation cheat sheet:**

| Annotation | What it does |
|---|---|
| `@Indexed` | Marks the whole class as something OpenSearch should track |
| `@FullTextField` | Stores the field as searchable text (split into words, lowercased, etc.) |
| `@KeywordField` | Stores the field as an exact value — useful for sorting and filtering |
| `@GenericField` | Stores a simple value (number, enum) for filtering |
| `@IndexedEmbedded` | Pulls in fields from a related entity into this entity's index |

### Step 2 — How Text is Analyzed (The Analyzer)

When you index `"J.R.R. Tolkien"` into OpenSearch, it does not store it exactly as-is. Instead, a **text analyzer** processes it first — splitting it into tokens, lowercasing them, and normalizing characters:

```
"J.R.R. Tolkien"
       ↓  tokenizer splits on whitespace/punctuation
["J", "R", "R", "Tolkien"]
       ↓  lowercase filter
["j", "r", "r", "tolkien"]
       ↓  asciifolding (e.g. "Müller" → "muller")
["j", "r", "r", "tolkien"]
```

The same transformation happens to your search query, so `"tolkien"`, `"Tolkien"`, and `"TOLKIEN"` all find the same results.

This is configured in `SearchAnalysisConfigurer.java`:

```java
context.analyzer("name_analyzer").custom()
    .tokenizer("standard")
    .tokenFilters("lowercase", "asciifolding");
```

### Step 3 — Indexing Data on Startup

When the application starts, `SearchIndexer.java` runs and pushes all existing database records into the OpenSearch index:

```java
Search.session(entityManager).massIndexer().startAndWait();
```

After that, every time an author or book is saved or updated via Hibernate ORM, Hibernate Search automatically keeps the index up to date.

### Step 4 — Configuration

The connection between the app and OpenSearch is configured in `application.properties`:

```properties
quarkus.hibernate-search-orm.elasticsearch.version=opensearch:2.19
quarkus.hibernate-search-orm.elasticsearch.hosts=localhost:9200
quarkus.hibernate-search-orm.elasticsearch.analysis.configurer=class:org.acme.config.SearchAnalysisConfigurer
```

Hibernate Search is compatible with both Elasticsearch and OpenSearch — the `elasticsearch.version` property tells it which one it is talking to.

---

## 3. Search — From Simple to Complex

### 3.1 Simple Search: Finding an Author by Name

**Endpoint:** `GET /search/authors?q=tolkien`

**Flow:**
```
HTTP Request: GET /search/authors?q=tolkien
       │
       ▼
SearchResource.searchAuthors("tolkien", 20)
       │
       ▼
SearchService.searchAuthors("tolkien", 20)
       │
       ▼
OpenSearch query → returns matching Author records
```

**The query in `SearchService.java`:**

```java
searchSession.search(Author.class)
    .where(f -> f.simpleQueryString()
        .fields("name", "books.title")   // search in these fields
        .matching(query)                 // the user's search term
        .defaultOperator(BooleanOperator.AND))
    .sort(f -> f.field("name_sort"))     // sort results A → Z
    .fetchHits(size);
```

**What happens step by step:**

1. The query `"tolkien"` is sent to OpenSearch.
2. OpenSearch looks at the indexed `name` field of every author.
3. It finds all authors where the name contains the token `"tolkien"`.
4. Results are returned sorted alphabetically by name.

**Example result for `?q=tolkien`:**
```json
[
  { "id": "...", "name": "J.R.R. Tolkien" }
]
```

The search is case-insensitive because both the indexed data and the query go through the same `name_analyzer` (lowercase filter).

---

### 3.2 Complex Search: Finding an Author by Book Title

**Endpoint:** `GET /search/authors?q=rings`

This is the same endpoint — but now the search term matches a *book title*, not the author's name directly.

**Why this works — `@IndexedEmbedded`:**

On the `Author` entity:

```java
@IndexedEmbedded(includePaths = {"title", "description", "genre"})
public List<Book> books;
```

This tells Hibernate Search: "When indexing an author, also pull in the `title`, `description`, and `genre` of each of their books."

So the OpenSearch document for Tolkien does not just contain:
```
name: "J.R.R. Tolkien"
```

It also contains:
```
name:        "J.R.R. Tolkien"
books.title: "The Fellowship of the Ring"
books.title: "The Two Towers"
books.title: "The Return of the King"
```

Everything lives in a single OpenSearch document. Searching across `books.title` is just as fast as searching `name`.

**The query searches both fields at once:**

```java
.fields("name", "books.title")
.matching(query)
```

When you search for `"rings"`, OpenSearch checks both fields for every author document. Because `"The Fellowship of the Ring"` contains the token `"ring"` (close enough after analysis), Tolkien is returned — even though his name contains no such word.

**Step by step for `?q=rings`:**

```
Query: "rings"
       ↓  analyzed to token: "rings"

OpenSearch checks every author document:
  Author: "Stephen King"
    name:        "stephen king"         → no match
    books.title: "the shining", "it"    → no match

  Author: "J.R.R. Tolkien"
    name:        "j.r.r. tolkien"       → no match
    books.title: "the fellowship of the ring", "the two towers", ...  → MATCH ("ring" ≈ "rings"*)

Result: [{ "name": "J.R.R. Tolkien", ... }]
```

> *Note: exact fuzzy matching depends on the query syntax. With `simpleQueryString`, the terms must appear in the text. "rings" matches "rings" exactly — if a book is titled "The Lord of the Rings", the token "rings" is present and matches.

**Why not just use a SQL JOIN?**

A traditional SQL query like:
```sql
SELECT a.* FROM author a
JOIN book b ON b.author_id = a.id
WHERE b.title LIKE '%rings%'
```

...works for exact substring matches. But it does not handle:
- Case differences (`Rings` vs `rings`)
- Accent folding (`Müller` vs `Muller`)
- Multi-word queries with AND/OR logic
- Relevance scoring (ranking better matches higher)

OpenSearch handles all of that automatically, and is much faster at scale.

---

### 3.3 Filtering Books by Genre

**Endpoint:** `GET /search/books?q=darkness&genre=HORROR`

The `genre` parameter is optional. When provided, it narrows results to books of that genre. Valid values match the `Genre` enum: `THRILLER`, `HORROR`, `FANTASY`, `SCI_FI`.

**Why a filter, not a full-text query:**

Genre is stored as a `@KeywordField` — an exact, unanalyzed value. Filtering on it is a yes/no match (not ranked), which is exactly what you want: "only show me HORROR books", not "show me books that are *kind of* horror-ish".

**The query in `SearchService.java`:**

```java
searchSession.search(Book.class)
    .where(f -> f.bool(b -> {
        b.must(f.simpleQueryString()
                .fields("title", "description", "author.name")
                .matching(query)
                .defaultOperator(BooleanOperator.AND));
        if (genre != null) {
            b.filter(f.match().field("genre").matching(genre));
        }
    }))
    .fetchHits(size);
```

The `bool` query has two clauses:

| Clause | Purpose |
|---|---|
| `must` | The full-text search — results must match the query text |
| `filter` | The genre restriction — results must be this exact genre (does not affect relevance scoring) |

If `genre` is omitted from the request, the filter clause is skipped and the query behaves exactly as before.

**Examples:**

```
GET /search/books?q=dark             → all books matching "dark"
GET /search/books?q=dark&genre=HORROR → only HORROR books matching "dark"
GET /search/books?q=&genre=FANTASY   → all FANTASY books (empty text query matches everything)
```

---

## 4. Summary

```
┌─────────────────────────────────────────────────────────┐
│                    What each part does                  │
├────────────────────┬────────────────────────────────────┤
│ AuthorResource     │ HTTP endpoints for CRUD operations │
│ SearchResource     │ HTTP endpoints for search queries  │
│ SearchService      │ Builds and runs OpenSearch queries │
│ Author / Book      │ JPA entities + search annotations  │
│ SearchIndexer      │ Pushes data to OpenSearch on start │
│ SearchAnalysisConf │ Defines how text is processed      │
│ PostgreSQL         │ Source of truth for all data       │
│ OpenSearch         │ Fast, full-text search index       │
└────────────────────┴────────────────────────────────────┘
```

The core insight of this architecture:

> **PostgreSQL stores your data. OpenSearch makes it searchable.**
> Hibernate Search keeps both in sync automatically, and annotations on your Java classes define exactly what gets indexed and how.
