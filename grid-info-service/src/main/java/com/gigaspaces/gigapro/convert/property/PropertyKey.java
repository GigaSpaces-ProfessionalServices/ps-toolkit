package com.gigaspaces.gigapro.convert.property;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * @author Svitlana_Pogrebna
 *
 */
@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface PropertyKey {

    String value();

}
