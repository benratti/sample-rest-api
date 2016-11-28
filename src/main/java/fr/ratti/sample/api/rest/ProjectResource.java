package fr.ratti.sample.api.rest;

import fr.ratti.sample.api.model.Project;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Created by bratti on 17/08/2016.
 */
@Path("projects")
@Produces({APPLICATION_JSON, APPLICATION_XML})
@Consumes({APPLICATION_JSON, APPLICATION_XML})
public interface ProjectResource {

    @GET
    @Path("/{id}")
    public Project getResource(@PathParam("id") String projectId, @Context UriInfo uriInfo);


    @POST
    public Response createResource(Project project);


}
