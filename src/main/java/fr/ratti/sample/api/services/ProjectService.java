package fr.ratti.sample.api.services;

import fr.ratti.sample.api.exception.ResourceAllreadyExistsException;
import fr.ratti.sample.api.exception.ResourceNotFoundException;
import fr.ratti.sample.api.model.Project;

/**
 * Created by bratti on 17/08/2016.
 */
public interface ProjectService extends ResourceManagerService<Project> {
    /**
     * Cette methode permet de cr�er un nouveau projet
     * @param project
     * @throws ResourceAllreadyExistsException cette exception est retourn� lorsque la ressource existe d�j�
     * @return identifiant technique du projet cr�e
     */
    String create(Project project) throws ResourceAllreadyExistsException;

    /**
     * Cette methode permet de r�cup�rer un project � partir de son identifiant
     * @param projectId identifiant technique du projet
     * @return
     */
    Project getResource(String projectId) throws ResourceNotFoundException;
}
