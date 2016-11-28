package fr.ratti.sample.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by bratti on 26/08/2016.
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Seachable {

    String value() default "";

}
