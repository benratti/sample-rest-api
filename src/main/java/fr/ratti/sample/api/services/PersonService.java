package fr.ratti.sample.api.services;

import fr.ratti.sample.api.exception.ResourceAllreadyExistsException;
import fr.ratti.sample.api.exception.ResourceNotFoundException;
import fr.ratti.sample.api.model.Person;

import java.util.List;

/**
 * Created by bratti on 16/08/2016.
 */
public interface PersonService {

    Person getPerson(String userId) throws ResourceNotFoundException;

    String create(Person person) throws ResourceAllreadyExistsException;

    List<Person> search(String fistnameFilter, String lastnameFilter, String mailFilter);
}
