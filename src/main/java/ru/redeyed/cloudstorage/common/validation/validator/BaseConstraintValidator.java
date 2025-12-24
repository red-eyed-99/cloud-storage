package ru.redeyed.cloudstorage.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.common.validation.ValidationErrorMessageUtil;
import ru.redeyed.cloudstorage.common.validation.ValidationUtil;
import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

public abstract class BaseConstraintValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

    protected String parameterName;

    protected String message;

    protected boolean checkNotBlank(ConstraintValidatorContext context, String value) {
        if (ValidationUtil.isBlank(value)) {
            var message = ValidationErrorMessageUtil.getIsBlankMessage(parameterName);
            setCustomMessage(context, message);
            return false;
        }

        return true;
    }

    protected boolean checkLengthBetween(ConstraintValidatorContext context, String value, int minLength, int maxLength) {
        if (!ValidationUtil.checkLength(value, minLength, maxLength)) {
            var message = ValidationErrorMessageUtil.getMinMaxLengthMessage(parameterName, minLength, maxLength);
            setCustomMessage(context, message);
            return false;
        }

        return true;
    }

    protected boolean checkMaxLength(ConstraintValidatorContext context, String value, int maxLength) {
        if (!ValidationUtil.checkMaxLength(value, maxLength)) {
            var message = ValidationErrorMessageUtil.getMaxLengthMessage(parameterName, maxLength);
            setCustomMessage(context, message);
            return false;
        }

        return true;
    }

    protected boolean checkMaxBytes(ConstraintValidatorContext context, String value, int maxBytes) {
        if (!ValidationUtil.checkMaxBytes(value, maxBytes)) {
            var message = ValidationErrorMessageUtil.getMaxBytesMessage(parameterName, maxBytes);
            setCustomMessage(context, message);
            return false;
        }

        return true;
    }

    protected boolean checkNotStartWith(String value, String prefix, ConstraintValidatorContext context) {
        if (ValidationUtil.isStartWith(prefix, value)) {
            var message = ValidationErrorMessageUtil.getNotStartWithMessage(parameterName, prefix);
            setCustomMessage(context, message);
            return false;
        }

        return true;
    }

    protected boolean checkEndWith(ConstraintValidatorContext context, String value, String suffix) {
        if (!ValidationUtil.isEndWith(suffix, value)) {
            var message = ValidationErrorMessageUtil.getEndWithMessage(parameterName, suffix);
            setCustomMessage(context, message);
            return false;
        }

        return true;
    }

    protected boolean checkNotStartOrEndWith(ConstraintValidatorContext context, String value, String pattern) {
        if (ValidationUtil.isStartOrEndWith(pattern, value)) {
            var message = ValidationErrorMessageUtil.getNotStartOrEndWithMessage(parameterName, pattern);
            setCustomMessage(context, message);
            return false;
        }

        return true;
    }

    protected boolean patternMatches(ConstraintValidatorContext context, String value, Pattern pattern) {
        if (!ValidationUtil.patternMatches(pattern, value)) {
            setCustomMessage(context, message);
            return false;
        }

        return true;
    }

    protected boolean checkExtraSpaces(ConstraintValidatorContext context, String value) {
        if (ValidationUtil.hasExtraSpaces(value)) {
            var message = ValidationErrorMessageUtil.getExtraSpacesMessage(parameterName);
            setCustomMessage(context, message);
            return false;
        }

        return true;
    }

    protected void setCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
