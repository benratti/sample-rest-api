package fr.ratti.sample.api.rest;

import fr.ratti.sample.api.model.Person;
import org.apache.cxf.jaxrs.ext.PATCH;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Created by bratti on 16/08/2016.
 */
@Path("people")
@Produces({APPLICATION_JSON, APPLICATION_XML})
@Consumes({APPLICATION_JSON, APPLICATION_XML})
public interface PersonResource {

    @GET
    @Path("/{id}")
    public Person getResource(@PathParam("id") String id, @Context UriInfo uriInfo);


    @DELETE
    @Path("/{id")
    public Response deleteResource(@PathParam("id") String id);

    @PUT
    @Path("/{id]")
    public Response updateResource(@PathParam("id") String id, Person resource);

    @PATCH
    @Path("/{id}")
    public Response patchResource(@PathParam("id") String id, Person resource);


    @POST
    public Response createResource(Person person);

    @GET
    @Path("/search")
    public Collection<Person> searchPeople(@QueryParam("firstname") String firstnameFilter,
                                     @QueryParam("lastname") String lastnameFilter,
                                     @QueryParam("mail") String mailFilter,
                                     @Context HttpHeaders headers);


    @GET
    @Path("/")
    public Response getAll(@Context UriInfo uriInfo);

}
