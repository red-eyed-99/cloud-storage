package ru.redeyed.cloudstorage.user.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.redeyed.cloudstorage.user.validation.validator.PasswordValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword {

    String parameterName() default "password";

    String message() default "Incorrect password. Allowed characters: a-zA-Z0-9!@#$%^&*(),.?\":{}|<>[]/`~+=-_';";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
