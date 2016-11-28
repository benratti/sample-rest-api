package fr.ratti.sample.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Cette annotation permet de préciser qu'un attribut est obligatoire
 *
 * Created by bratti on 18/08/2016.
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Mandatory {


}
