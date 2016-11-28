package fr.ratti.sample.api.services.impl;

import fr.ratti.sample.api.exception.IllegalFormatException;
import fr.ratti.sample.api.services.IDService;

import java.util.UUID;

/**
 * Created by bratti on 17/08/2016.
 */
public class IDServiceUUIDImpl implements IDService{

    public String generateId() {
        return UUID.randomUUID().toString();
    }

    public void validate(String identifiant) throws IllegalFormatException {

        if ( identifiant == null || identifiant.isEmpty()) {
            throw new IllegalFormatException();
        }

        try {
            UUID.fromString(identifiant);
        } catch (IllegalArgumentException exception) {

            throw new IllegalFormatException(exception);
        }

    }
}
