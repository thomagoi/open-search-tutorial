package org.acme.resource;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.Author;
import org.acme.domain.Book;

import java.util.List;
import java.util.UUID;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    @GET
    public List<Book> listAll() {
        return Book.listAll();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        Book book = Book.findById(id);
        if (book == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(book).build();
    }

    @POST
    @Transactional
    public Response create(BookRequest request) {
        Author author = Author.findById(request.authorId);
        if (author == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Author not found: " + request.authorId)
                    .build();
        }

        Book book = new Book();
        book.title = request.title;
        book.description = request.description;
        book.price = request.price;
        book.genre = request.genre;
        book.author = author;
        book.persist();

        author.books.add(book);

        return Response.status(Response.Status.CREATED).entity(book).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") UUID id, BookRequest request) {
        Book book = Book.findById(id);
        if (book == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        book.title = request.title;
        book.description = request.description;
        book.price = request.price;
        book.genre = request.genre;

        if (request.authorId != null && !request.authorId.equals(book.author.id)) {
            Author newAuthor = Author.findById(request.authorId);
            if (newAuthor == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Author not found: " + request.authorId)
                        .build();
            }
            book.author.books.remove(book);
            book.author = newAuthor;
            newAuthor.books.add(book);
        }

        return Response.ok(book).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") UUID id) {
        Book book = Book.findById(id);
        if (book == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        book.author.books.remove(book);
        book.delete();
        return Response.noContent().build();
    }

    public static class BookRequest {
        public String title;
        public String description;
        public long price;
        public org.acme.domain.Genre genre;
        public UUID authorId;
    }
}
