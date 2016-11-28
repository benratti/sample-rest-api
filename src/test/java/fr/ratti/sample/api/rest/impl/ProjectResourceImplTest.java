package fr.ratti.sample.api.rest.impl;

import fr.ratti.sample.api.exception.ResourceAllreadyExistsException;
import fr.ratti.sample.api.exception.ResourceNotFoundException;
import fr.ratti.sample.api.model.Project;
import fr.ratti.sample.api.rest.ProjectResource;
import fr.ratti.sample.api.services.IDService;
import fr.ratti.sample.api.services.ResourceManagerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.easymock.annotation.Mock;
import org.unitils.inject.annotation.InjectIntoByType;
import org.unitils.inject.annotation.TestedObject;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;
import uk.co.jemos.podam.api.PodamFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.easymock.EasyMock.*;
import static org.unitils.easymock.EasyMockUnitils.replay;

/**
 * Created by bratti on 17/08/2016.
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@SpringApplicationContext("/fr/ratti/sample/api/rest/impl/ProjectResourceImplTest-context.xml")
public class ProjectResourceImplTest {

    @SpringBeanByType
    private PodamFactory factory;

    @Mock
    @InjectIntoByType
    private ResourceManagerService<Project> projectService;

    @Mock
    @InjectIntoByType
    private IDService idService;

    @SpringBeanByType
    @TestedObject
    private ProjectResource projectResource;

    @Mock
    private UriInfo uriInfo;

    @Test
    public void createProject_should_return_HTTP_201_when_project_is_correctly_created() throws Exception {

        String projectId = UUID.randomUUID().toString();
        Project project = factory.manufacturePojo(Project.class);

        expect(projectService.create(project)).andStubReturn(projectId);

        replay();

        Response response = projectResource.createResource(project);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(CREATED.getStatusCode());

        assertThat(response.getHeaderString("Location"))
                .isNotNull()
                .isEqualTo("http://api.url.com/v1/api/projects/" + projectId);

    }


    @Test
    public void createProject_should_return_HTTP_400_when_project_is_null() throws Exception {

        try {
            projectResource.createResource(null);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void createProject_should_return_HTTP_204_when_project_allready_exists() throws Exception {

        String projectId = UUID.randomUUID().toString();
        Project project = factory.manufacturePojo(Project.class);

        expect(projectService.create(project)).andStubThrow(new ResourceAllreadyExistsException(projectId));

        replay();

        Response response = projectResource.createResource(project);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(NO_CONTENT.getStatusCode());

        assertThat(response.getHeaderString("Location"))
                .isNotNull()
                .isEqualTo("http://api.url.com/v1/api/projects/" + projectId);

    }

    @Test
    public void createProject_should_return_HTTP_500_when_projectService_throws_unexpected_exception() throws Exception {

        Project project = factory.manufacturePojo(Project.class);

        expect(projectService.create(project)).andStubThrow(new RuntimeException());

        replay();

        try {
            projectResource.createResource(project);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }


    @Test
    public void getProject_should_return_project_when_project_exists() throws Exception {


        String projectId = UUID.randomUUID().toString();
        Project project = factory.manufacturePojo(Project.class);

        idService.validate(anyString());
        expectLastCall().atLeastOnce();

        expect(projectService.get(projectId)).andStubReturn(project);

        replay();

        Project returnedProjet = projectResource.getResource(projectId, uriInfo);

        assertThat(returnedProjet).isEqualToComparingFieldByField(project);

    }

    @Test
    public void getProject_should_return_HTTP_404_when_project_does_not_exist() throws Exception {

        String projectId = UUID.randomUUID().toString();

        idService.validate(anyString());
        expectLastCall().atLeastOnce();

        expect(projectService.get(projectId)).andStubThrow(new ResourceNotFoundException());

        replay();

        try {
            projectResource.getResource(projectId, uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void getProject_should_return_HTTP_400_when_projectId_is_null() throws Exception {

        idService.validate(anyString());
        expectLastCall().andStubThrow(new IllegalArgumentException());

        replay();

        try {
            projectResource.getResource(null, uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }

    }

    @Test
    public void getProject_should_return_HTTP_400_when_projectId_is_empty() throws Exception {

        idService.validate(anyString());
        expectLastCall().andStubThrow(new IllegalArgumentException());

        replay();

        try {
            projectResource.getResource("", uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }
    }


    @Test
    public void getProject_should_return_HTTP_400_when_projectId_has_not_uuid_format() throws Exception {

        idService.validate(anyString());
        expectLastCall().andStubThrow(new IllegalArgumentException());

        replay();

        try {
            projectResource.getResource("4336576234423", uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }
    }

}

