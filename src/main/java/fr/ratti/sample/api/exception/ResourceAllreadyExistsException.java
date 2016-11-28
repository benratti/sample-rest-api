package fr.ratti.sample.api.exception;

/**
 * Created by bratti on 17/08/2016.
 */
public class ResourceAllreadyExistsException extends Exception {

    private String userId;

    public String getResourceId() {
        return userId;
    }

    public ResourceAllreadyExistsException(String userId) {
        super();
        this.userId = userId;
    }
}
