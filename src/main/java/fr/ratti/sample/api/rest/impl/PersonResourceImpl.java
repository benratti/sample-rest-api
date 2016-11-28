package fr.ratti.sample.api.rest.impl;

import fr.ratti.sample.api.model.Person;
import fr.ratti.sample.api.rest.PersonResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import java.util.Collection;

/**
 * Created by bratti on 16/08/2016.
 */
public class PersonResourceImpl extends AbstractSimpleResource<Person> implements PersonResource {


    private static final Logger LOGGER = LoggerFactory.getLogger(PersonResource.class);

//    private PersonService personService;
//
//    public PersonService getPersonService() {
//        return personService;
//    }
//
//    public void setPersonService(PersonService personService) {
//        this.personService = personService;
//    }



//    public Person getResource(String personId) {
//
//        return getResource(personId, personService::getResource);
//
//        checkIdFormat(id);
//
//        try {
//            return personService.getResource(id);
//        } catch (ResourceNotFoundException e) {
//            throw new WebApplicationException(NOT_FOUND);
//        } catch (Exception e) {
//            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
//        }
//    }

//    public Response createResource(Person resource) {
//
//        if ( resource == null || resource.getMail() == null) {
//            throw new WebApplicationException(Response.Status.BAD_REQUEST);
//        }
//
//        String userId;
//        Response.Status responseStatus = CREATED;
//
//
//        try {
//            userId = personService.create(resource);
//        } catch (ResourceAllreadyExistsException paee) {
//            userId = paee.getResourceId();
//            responseStatus = NO_CONTENT;
//        }
//
//        Response response = Response
//                .status(responseStatus)
//                .header("Location",baseURI + "/people/" + userId)
//                .build();
//
//        return response;
//    }

    public Collection<Person> searchPeople(String firstnameParam, String lastnameParam, String mailFilter, HttpHeaders headers) {

        if ( mailFilter == null ) {
            mailFilter = "*";
        }

        return getResourceManagerService().search(
                buildFilter(firstnameParam),
                buildFilter(lastnameParam),
                mailFilter);

    }

    private String buildFilter(String firstnameParam) {
        String firtstnameFilter;
        firtstnameFilter = firstnameParam;

        if ( firtstnameFilter == null || firtstnameFilter.isEmpty()) {
            firtstnameFilter = "*";
        }
        return firtstnameFilter;
    }


    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
