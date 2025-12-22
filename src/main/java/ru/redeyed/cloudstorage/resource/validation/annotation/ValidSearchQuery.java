package ru.redeyed.cloudstorage.resource.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.redeyed.cloudstorage.resource.validation.validator.SearchQueryValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SearchQueryValidator.class)
public @interface ValidSearchQuery {

    String parameterName() default "query";

    String message() default "Invalid query. Prohibited characters: \\/:*?\"<>| .";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
