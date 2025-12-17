package ru.redeyed.cloudstorage.common.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.redeyed.cloudstorage.common.validation.validator.BcryptValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BcryptValidator.class)
public @interface BcryptEncoded {

    String parameterName() default "";

    String message() default "must be encoded (bcrypt).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
