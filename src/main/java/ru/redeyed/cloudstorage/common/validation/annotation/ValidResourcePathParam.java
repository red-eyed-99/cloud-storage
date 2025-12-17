package ru.redeyed.cloudstorage.common.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.redeyed.cloudstorage.common.validation.validator.ResourcePathParamValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ResourcePathParamValidator.class)
public @interface ValidResourcePathParam {

    String parameterName() default "path";

    String message() default "Invalid resource path. Prohibited characters: \\:*?\"<>|.";

    boolean onlyDirectory() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
