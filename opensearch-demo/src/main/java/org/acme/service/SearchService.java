package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.Author;
import org.acme.domain.Book;
import org.hibernate.search.mapper.orm.session.SearchSession;

import java.util.List;

@ApplicationScoped
public class SearchService {

    @Inject
    SearchSession searchSession;

    public List<Author> searchAuthors(String query, int size) {
        return searchSession.search(Author.class)
                .where(f -> f.bool()
                        .should(f.simpleQueryString().fields("name", "books.title").matching(query))
                        .should(f.simpleQueryString().fields("name", "books.title").matching(query + "*"))
                        .should(f.match().fields("name", "books.title").matching(query).fuzzy(1)))
                .sort(f -> f.field("name_sort"))
                .fetchHits(size);
    }

    public List<Book> searchBooks(String query, int size) {
        return searchSession.search(Book.class)
                .where(f -> f.bool()
                        .should(f.simpleQueryString().fields("title", "description", "author.name").matching(query))
                        .should(f.simpleQueryString().fields("title", "description", "author.name").matching(query + "*"))
                        .should(f.match().fields("title", "description", "author.name").matching(query).fuzzy(1)))
                .fetchHits(size);
    }
}
