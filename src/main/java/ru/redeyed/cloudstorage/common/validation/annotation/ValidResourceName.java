package ru.redeyed.cloudstorage.common.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.redeyed.cloudstorage.common.validation.validator.ResourceNameValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ResourceNameValidator.class)
public @interface ValidResourceName {

    String parameterName() default "name";

    String message() default "Invalid resource name. Prohibited characters: \\:*?\"<>|.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
