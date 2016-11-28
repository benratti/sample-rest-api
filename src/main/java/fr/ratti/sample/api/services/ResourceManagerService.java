package fr.ratti.sample.api.services;

import fr.ratti.sample.api.exception.ResourceAllreadyExistsException;
import fr.ratti.sample.api.exception.ResourceNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by bratti on 19/08/2016.
 */
public interface ResourceManagerService<T> {

    /**
     * this method create resource and generate id
     * @param resource resource to be created
     * @return generated id of created resource
     * @throws ResourceAllreadyExistsException this exception is thrown when resource allready exists
     */
    String create(T resource) throws ResourceAllreadyExistsException;

    /**
     * this method create resource with specified id
     * @param id id for the created resource
     * @param resource resource to be created
     * @throws ResourceAllreadyExistsException this exception is thrown when resource allready exists
     */
    void create(String id, T resource) throws ResourceAllreadyExistsException;

    /**
     * this method get the resouce with specified id
     * @param id resource id
     * @return resource with specified id
     * @throws ResourceNotFoundException the resource with specified id does not exist
     */
    T get(String id) throws ResourceNotFoundException;

    Collection<T> search(String s, String s1, String mailFilter);

    void update(String sampleId, T resource);


    /**
     * This methode delete the resource with specified id
     * @param sampleId resource id
     * @throws ResourceNotFoundException the resource with specified id does not exist
     */
    void delete(String sampleId) throws ResourceNotFoundException;


    /**
     * This method return all resources entries with attributes equal to attributes map
     * @param attributes
     * @return
     */
    List<T> getAll(Map<String, String> attributes);
}
