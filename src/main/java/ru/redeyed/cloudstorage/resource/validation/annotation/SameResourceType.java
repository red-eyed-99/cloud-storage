package ru.redeyed.cloudstorage.resource.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.redeyed.cloudstorage.resource.validation.validator.SameResourceTypeValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SameResourceTypeValidator.class)
public @interface SameResourceType {

    String message() default "From and to parameters has different resource type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
