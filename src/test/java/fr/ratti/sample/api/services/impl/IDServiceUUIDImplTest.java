package fr.ratti.sample.api.services.impl;

import fr.ratti.sample.api.exception.IllegalFormatException;
import fr.ratti.sample.api.services.IDService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.inject.annotation.TestedObject;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bratti on 17/08/2016.
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@SpringApplicationContext("/fr/ratti/sample/api/services/impl/IDServiceUUIDImplTest-context.xml")
public class IDServiceUUIDImplTest {

    @TestedObject
    @SpringBeanByType
    private IDService idService;

    @Test
    public void validate_should_not_throw_exception_when_id_has_uuid_format() throws Exception {

        String id = UUID.randomUUID().toString();

        idService.validate(id);
    }

    @Test(expected = IllegalFormatException.class)
    public void validate_should_throw_exception_when_id_is_null() throws Exception {

        idService.validate(null);
    }

    @Test(expected = IllegalFormatException.class)
    public void validate_should_throw_exception_when_id_is_empty() throws Exception {

        idService.validate("");
    }


    @Test(expected = IllegalFormatException.class)
    public void validate_should_throw_exception_when_id_has_not_UUID_format() throws Exception {

        idService.validate("4321534423424");
    }

    @Test
    public void generate_should_return_identifiant_with_UUID_format_when_method_is_call() throws Exception {

        String identifiant = idService.generateId();

        // on verifie que l'on peut construire un UUID à partir de l'identifiant genéré
        UUID.fromString(identifiant);
    }

    @Test
    public void generate_should_return_distinct_identifiants_when_method_is_call_many_times() throws Exception {

        int nbIdentifiantGenerated = 100000;

        Set<String> identifiantSet = new HashSet<String>(nbIdentifiantGenerated);
        for( int i = 0; i < nbIdentifiantGenerated; i++) {
            identifiantSet.add(idService.generateId());
        }

        assertThat(identifiantSet).hasSize(nbIdentifiantGenerated);
    }

}
