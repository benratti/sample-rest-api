package fr.ratti.sample.api.rest.impl;

import fr.ratti.sample.api.exception.ResourceAllreadyExistsException;
import fr.ratti.sample.api.exception.ResourceNotFoundException;
import fr.ratti.sample.api.model.Person;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.easymock.EasyMock.*;
import static org.unitils.easymock.EasyMockUnitils.replay;

/**
 * Created by bratti on 16/08/2016.
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@SpringApplicationContext("/fr/ratti/sample/api/rest/impl/PersonResourceImplTest-context.xml")
public class PersonResourceImplTest {

    @SpringBeanByType
    private PodamFactory factory;

    @TestedObject
    @SpringBeanByType
    private PersonResourceImpl personResource;

    @Mock
    @InjectIntoByType
    private ResourceManagerService<Person> personService;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private HttpHeaders httpHeaders;

    @Mock
    @InjectIntoByType
    private IDService idService;

    @Test
    public void getPerson_should_return_person_when_person_exists() throws Exception {

        String userId = UUID.randomUUID().toString();
        Person person = factory.manufacturePojo(Person.class);
        person.setId(userId);

        expect(personService.get(userId)).andStubReturn(person);
        idService.validate(userId);

        replay();

        Person returnedPerson = personResource.getResource(userId, uriInfo);

        assertThat(returnedPerson).isEqualToComparingFieldByField(person);

    }

    @Test
    public void getPerson_should_throw_HTTP_400_when_id_is_null() throws Exception {

        idService.validate(null);
        expectLastCall().andStubThrow(new IllegalArgumentException());

        replay();

        try {
            personResource.getResource(null, uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae ) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }


    }

    @Test
    public void getPerson_should_throw_HTTP_400_when_id_is_empty() throws Exception {

        idService.validate("");
        expectLastCall().andStubThrow(new IllegalArgumentException());

        replay();

        try {
            personResource.getResource("", uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }

    }


    @Test
    public void getPerson_should_throw_HTTP_500_when_personService_throws_unexpected_exception() throws Exception {

        String userId = UUID.randomUUID().toString();

        idService.validate(anyString());
        expectLastCall().atLeastOnce();

        expect(personService.get(anyString())).andThrow(new RuntimeException());

        replay();

        try {
            personResource.getResource(userId, uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    @Test
    public void getPerson_should_throw_HTTP_400_when_id_is_not_uuid() throws Exception {


        idService.validate(anyString());
        expectLastCall().andStubThrow(new IllegalArgumentException());

        replay();


        try {
            personResource.getResource("4142331231312", uriInfo);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }

    }

    @Test
    public void getPerson_should_throw_HTTP_404_when_person_does_not_exist() throws Exception {

        String userId = UUID.randomUUID().toString();

        idService.validate(anyString());
        expectLastCall().anyTimes();

        expect(personService.get(userId)).andStubThrow(new ResourceNotFoundException());

        replay();

        try {
            personResource.getResource(userId, uriInfo);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        }

    }



    @Test
    public void createPerson_should_return_HTTP_201_when_user_is_correctly_created() throws Exception {

        String userId = UUID.randomUUID().toString();
        Person person = factory.manufacturePojo(Person.class);

        expect(personService.create(person)).andStubReturn(userId);

        replay();

        Response response = personResource.createResource(person);

        assertThat(response.getStatus()).isEqualTo(CREATED.getStatusCode());
        assertThat(response.getHeaderString("Location"))
                .isNotNull()
                .isEqualTo("http://api.url.com/v1/api/people/" + userId);

    }

    @Test
    public void createPerson_should_return_HTTP_400_when_person_is_null() throws Exception {
        try {
            personResource.createResource(null);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }

    }


    @Test
    public void createPerson_should_return_HTTP_400_when_person_has_not_email_address() throws Exception {

        try {
            Person person = factory.manufacturePojo(Person.class);
            person.setMail(null);

            personResource.createResource(person);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException wae) {
            assertThat(wae.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void createPerson_should_return_HTTP_204_when_person_allready_exists() throws Exception {

        String userId = UUID.randomUUID().toString();
        Person person = factory.manufacturePojo(Person.class);

        expect(personService.create(person)).andStubThrow(new ResourceAllreadyExistsException(userId));

        replay();

        Response response = personResource.createResource(person);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        assertThat(response.getHeaderString("Location"))
                .isNotNull()
                .isEqualTo("http://api.url.com/v1/api/people/" + userId);

    }


    @Test
    public void searchPeople_should_return_person_list_when_people_are_found_with_filter_criteria() throws Exception {

        String firstnameFitler = "pa*";
        String lastnameFitler = "dup*";
        String mailFitler = "*@me.com";

        Person person1 = factory.manufacturePojo(Person.class);
        Person person2 = factory.manufacturePojo(Person.class);
        Person person3 = factory.manufacturePojo(Person.class);

        HttpHeaders httpHeaders = factory.manufacturePojo(HttpHeaders.class);

        expect(personService.search(firstnameFitler,lastnameFitler,mailFitler)).andStubReturn(Arrays.asList(person1, person2, person3));

        replay();

        Collection<Person> people = personResource.searchPeople(firstnameFitler, lastnameFitler, mailFitler, httpHeaders);

        assertThat(people)
                .isNotNull()
                .containsExactlyElementsOf(Arrays.asList(person1, person2, person3));
    }


    @Test
    public void searchPeople_should_call_personService_with_star_firstname_filter_when_firstname_filter_is_null() throws Exception {

        String lastnameFitler = "dup*";
        String mailFitler = "*@me.com";

        Person person1 = factory.manufacturePojo(Person.class);
        Person person2 = factory.manufacturePojo(Person.class);
        Person person3 = factory.manufacturePojo(Person.class);

        HttpHeaders httpHeaders = factory.manufacturePojo(HttpHeaders.class);

        expect(personService.search("*",lastnameFitler,mailFitler)).andStubReturn(Arrays.asList(person1, person2, person3));

        replay();

        Collection<Person> people = personResource.searchPeople(null, lastnameFitler, mailFitler, httpHeaders);

        assertThat(people)
                .isNotNull()
                .containsExactlyElementsOf(Arrays.asList(person1, person2, person3));

    }


    @Test
    public void searchPeople_should_call_personService_with_star_firstname_filter_when_firstname_filter_is_empty() throws Exception {

        String lastnameFitler = "dup*";
        String mailFitler = "*@me.com";

        Person person1 = factory.manufacturePojo(Person.class);
        Person person2 = factory.manufacturePojo(Person.class);
        Person person3 = factory.manufacturePojo(Person.class);

        HttpHeaders httpHeaders = factory.manufacturePojo(HttpHeaders.class);

        expect(personService.search("*",lastnameFitler,mailFitler)).andStubReturn(Arrays.asList(person1, person2, person3));

        replay();

        Collection<Person> people = personResource.searchPeople("", lastnameFitler, mailFitler, httpHeaders);

        assertThat(people)
                .isNotNull()
                .containsExactlyElementsOf(Arrays.asList(person1, person2, person3));


    }


    @Test
    public void searchPeople_should_call_personService_with_star_lastname_filter_when_lastname_filter_is_null() throws Exception {

        String firstnameFitler = "dup*";
        String mailFitler = "*@me.com";

        Person person1 = factory.manufacturePojo(Person.class);
        Person person2 = factory.manufacturePojo(Person.class);
        Person person3 = factory.manufacturePojo(Person.class);

        HttpHeaders httpHeaders = factory.manufacturePojo(HttpHeaders.class);

        expect(personService.search(firstnameFitler,"*",mailFitler)).andStubReturn(Arrays.asList(person1, person2, person3));

        replay();

        Collection<Person> people = personResource.searchPeople(firstnameFitler,null, mailFitler, httpHeaders);

        assertThat(people)
                .isNotNull()
                .containsExactlyElementsOf(Arrays.asList(person1, person2, person3));

    }


    @Test
    public void searchPeople_should_call_personService_with_star_lastname_filter_when_lastname_filter_is_empty() throws Exception {

        String firstnameFitler = "dup*";
        String mailFitler = "*@me.com";

        Person person1 = factory.manufacturePojo(Person.class);
        Person person2 = factory.manufacturePojo(Person.class);
        Person person3 = factory.manufacturePojo(Person.class);

        HttpHeaders httpHeaders = factory.manufacturePojo(HttpHeaders.class);

        expect(personService.search(firstnameFitler,"*",mailFitler)).andStubReturn(Arrays.asList(person1, person2, person3));

        replay();

         Collection<Person> people = personResource.searchPeople(firstnameFitler,"", mailFitler, httpHeaders);

        assertThat(people)
                .isNotNull()
                .containsExactlyElementsOf(Arrays.asList(person1, person2, person3));

    }


    @Test
    public void searchPeople_should_call_personService_with_star_mail_filter_when_mail_filter_is_null() throws Exception {

        String firstnameFitler = "paul*";
        String lastnameFitler = "dup*";

        Person person1 = factory.manufacturePojo(Person.class);
        Person person2 = factory.manufacturePojo(Person.class);
        Person person3 = factory.manufacturePojo(Person.class);

        HttpHeaders httpHeaders = factory.manufacturePojo(HttpHeaders.class);

        expect(personService.search(firstnameFitler,lastnameFitler,"*")).andStubReturn(Arrays.asList(person1, person2, person3));

        replay();

        Collection<Person> people = personResource.searchPeople(firstnameFitler,lastnameFitler, null, httpHeaders);

        assertThat(people)
                .isNotNull()
                .containsExactlyElementsOf(Arrays.asList(person1, person2, person3));

    }



}
