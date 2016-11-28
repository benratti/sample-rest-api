package fr.ratti.sample.api.services;

import fr.ratti.sample.api.exception.ResourceAllreadyExistsException;
import fr.ratti.sample.api.exception.ResourceNotFoundException;
import fr.ratti.sample.api.model.Project;

/**
 * Created by bratti on 17/08/2016.
 */
public interface ProjectService extends ResourceManagerService<Project> {
    /**
     * Cette methode permet de créer un nouveau projet
     * @param project
     * @throws ResourceAllreadyExistsException cette exception est retourné lorsque la ressource existe déjà
     * @return identifiant technique du projet crée
     */
    String create(Project project) throws ResourceAllreadyExistsException;

    /**
     * Cette methode permet de récupérer un project à partir de son identifiant
     * @param projectId identifiant technique du projet
     * @return
     */
    Project getResource(String projectId) throws ResourceNotFoundException;
}
