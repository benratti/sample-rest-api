package fr.ratti.sample.api.services;


import fr.ratti.sample.api.exception.IllegalFormatException;

/**
 * Created by bratti on 17/08/2016.
 */
public interface IDService {

    /**
     * Cette methode permet de g�n�rer un identifiant unique
     * @return identifiant unique
     */
    String generateId();

    /**
     * Cette methode permet de valider que le format de l'identifiant est correcte
     * @param identifiant identifiat � verifier
     * @throws IllegalArgumentException cette exception est lev�e lorque le format est incorrect
     */
    void validate(String identifiant) throws IllegalFormatException;
}
