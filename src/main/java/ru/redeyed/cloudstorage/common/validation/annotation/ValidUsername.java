package ru.redeyed.cloudstorage.common.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.redeyed.cloudstorage.common.validation.validator.UsernameValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameValidator.class)
public @interface ValidUsername {

    String parameterName() default "username";

    String message() default "Incorrect username. Only latin letters, digits and underscores are allowed.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
