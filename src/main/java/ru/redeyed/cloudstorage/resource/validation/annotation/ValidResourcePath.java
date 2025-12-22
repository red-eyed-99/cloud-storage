package ru.redeyed.cloudstorage.resource.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.redeyed.cloudstorage.resource.validation.validator.ResourcePathValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ResourcePathValidator.class)
public @interface ValidResourcePath {

    String parameterName() default "path";

    String message() default "Invalid resource path. Prohibited characters: \\:*?\"<>| .";

    boolean onlyDirectory() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
