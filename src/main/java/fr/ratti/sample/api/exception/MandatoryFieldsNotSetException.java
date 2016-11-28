package fr.ratti.sample.api.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;

/**
 * Created by bratti on 18/08/2016.
 */
public class MandatoryFieldsNotSetException extends Exception {

    private Collection<String> fieldNames;
    private Class clazz;


    public MandatoryFieldsNotSetException(Class clazz, Collection<String> fieldNames) {
        super();
        this.clazz = clazz;
        this.fieldNames = fieldNames;
    }

    public Collection<String> getFieldNames() {
        if ( fieldNames == null) {
            fieldNames = new ArrayList<String>();
        }
        return fieldNames;
    }

    public void setFieldNames(Collection<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return "MandatoryFieldsNotSetException{" +
                "fieldNames=" + fieldNames +
                ", clazz=" + clazz +
                '}';
    }

    public String getStringFieldNames() {

        StringJoiner joiner = new StringJoiner(", ");
        for ( String fieldName : getFieldNames()) {
            joiner.add(fieldName);
        }

        return joiner.toString();

    }


}
