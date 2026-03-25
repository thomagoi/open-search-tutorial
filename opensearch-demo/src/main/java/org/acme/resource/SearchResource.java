package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.domain.Author;
import org.acme.domain.Book;
import org.acme.service.SearchService;

import java.util.List;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    @Inject
    SearchService searchService;

    @GET
    @Path("/authors")
    @Transactional
    public List<Author> searchAuthors(
            @QueryParam("q") @DefaultValue("") String query,
            @QueryParam("size") @DefaultValue("20") int size) {
        return searchService.searchAuthors(query, size);
    }

    @GET
    @Path("/books")
    @Transactional
    public List<Book> searchBooks(
            @QueryParam("q") @DefaultValue("") String query,
            @QueryParam("size") @DefaultValue("20") int size) {
        return searchService.searchBooks(query, size);
    }
}
