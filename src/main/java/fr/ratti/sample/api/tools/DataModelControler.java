package fr.ratti.sample.api.tools;

import fr.ratti.sample.api.annotation.Mandatory;
import fr.ratti.sample.api.exception.MandatoryFieldsNotSetException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bratti on 18/08/2016.
 */
public class DataModelControler<T> {

    public void checkMandatoryFields(T resource) throws MandatoryFieldsNotSetException {


        // check mandatory field
        Class clazz = resource.getClass();
        List<String> mandatoryFields = new ArrayList<>();

        for(Field field : clazz.getDeclaredFields()) {

            Mandatory mandatoryAnnotation = field.getAnnotation(Mandatory.class);
            if ( mandatoryAnnotation != null) {

                try {

                    field.setAccessible(true);
                    if (field.get(resource) == null) {
                        mandatoryFields.add(field.getName());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if ( !mandatoryFields.isEmpty()) {
            throw new MandatoryFieldsNotSetException(resource.getClass(), mandatoryFields);
        }
    }

}
