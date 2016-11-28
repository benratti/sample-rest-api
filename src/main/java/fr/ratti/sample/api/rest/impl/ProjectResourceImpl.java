package fr.ratti.sample.api.rest.impl;

import fr.ratti.sample.api.model.Project;
import fr.ratti.sample.api.rest.ProjectResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bratti on 17/08/2016.
 */
public class ProjectResourceImpl extends AbstractSimpleResource<Project> implements ProjectResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectResource.class);

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }


//    public Project getResource(String projectId) {
//        return getResource(projectId, projectService::getResource );
//    }

//    public Response createProject(Project project) {
//
//        if ( project == null ) {
//            throw new WebApplicationException(BAD_REQUEST);
//        }
//
//        Response.Status responseStatus = CREATED;
//        String projectId;
//
//        try {
//            projectId = getResourceManagerService().create(project);
//        } catch (ResourceAllreadyExistsException raee) {
//            projectId = raee.getResourceId();
//            responseStatus = NO_CONTENT;
//        } catch (Exception e) {
//            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
//        }
//
//        Response response = Response
//                .status(responseStatus)
//                .header("Location",baseURI + "/projects/" + projectId)
//                .build();
//
//        return response;
//    }


}
