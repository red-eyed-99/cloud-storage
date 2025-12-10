package ru.redeyed.cloudstorage.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.redeyed.cloudstorage.validation.validator.UsernameValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameValidator.class)
public @interface ValidUsername {

    String message() default """
            Incorrect username. Only latin letters and underscores are allowed.
            And the username must not begin or end with an underscore.
            """;

    int minLength() default 5;

    int maxLength() default 20;

    String pattern() default "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
