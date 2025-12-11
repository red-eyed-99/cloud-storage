package ru.redeyed.cloudstorage.common.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.redeyed.cloudstorage.common.validation.validator.PasswordBcryptValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordBcryptValidator.class)
public @interface PasswordBcryptEncoded {

    String message() default "Password must be encoded (bcrypt).";

    String pattern() default "\\A\\$2([ayb])?\\$(\\d\\d)\\$[./0-9A-Za-z]{53}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
