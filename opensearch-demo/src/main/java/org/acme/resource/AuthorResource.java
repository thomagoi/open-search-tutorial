package org.acme.resource;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import org.acme.view.Views;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.domain.Author;

import java.util.List;
import java.util.UUID;

@Path("/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorResource {

    @GET
    @JsonView(Views.Summary.class)
    public List<Author> listAll() {
        return Author.listAll();
    }

    @GET
    @Path("/{id}")
    @JsonView(Views.Detail.class)
    public Response findById(@PathParam("id") UUID id) {
        Author author = Author.findById(id);
        if (author == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(author).build();
    }

    @POST
    @Transactional
    public Response create(Author author) {
        author.id = null; // let Hibernate generate the UUID
        author.persist();
        return Response.status(Response.Status.CREATED).entity(author).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") UUID id, Author updated) {
        Author author = Author.findById(id);
        if (author == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        author.name = updated.name;
        author.birthdate = updated.birthdate;
        return Response.ok(author).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") UUID id) {
        boolean deleted = Author.deleteById(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }
}
