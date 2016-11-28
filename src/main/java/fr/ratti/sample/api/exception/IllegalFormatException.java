package fr.ratti.sample.api.exception;

/**
 * Created by bratti on 22/08/2016.
 */
public class IllegalFormatException extends Exception {

    public IllegalFormatException() {
        super();
    }

    public IllegalFormatException(Exception exception) {
        super(exception);
    }
}
