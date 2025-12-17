package ru.redeyed.cloudstorage.common.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.common.validation.annotation.BcryptEncoded;
import java.util.regex.Pattern;

public class BcryptValidator extends BaseConstraintValidator<BcryptEncoded, String> {

    private static final Pattern PATTERN = Pattern.compile("\\A\\$2([ayb])?\\$(\\d\\d)\\$[./0-9A-Za-z]{53}");

    @Override
    public void initialize(BcryptEncoded constraintAnnotation) {
        parameterName = constraintAnnotation.parameterName();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return checkNotBlank(context, password)
                && patternMatches(context, password, PATTERN);
    }
}
