package fr.ratti.sample.api.rest.impl;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * Created by bratti on 20/08/2016.
 */
@Consumes({APPLICATION_JSON, APPLICATION_XML})
@Produces({APPLICATION_JSON, APPLICATION_XML})
@Path("/samples")
public interface SampleResource  {

    @GET
    Response getAll(@Context UriInfo uriInfo);

    @Path("{id}")
    @GET
    SampleData getResource(@PathParam("id")String resourceId, @Context UriInfo uriInfo);
}
