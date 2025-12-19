package ru.redeyed.cloudstorage.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

public abstract class BaseConstraintValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

    protected String parameterName;

    protected String message;

    protected boolean checkNotBlank(ConstraintValidatorContext context, String value) {
        if (ConstraintValidationUtil.isBlank(value)) {
            setCustomMessage(context, ConstraintValidationUtil.getIsBlankMessage(parameterName));
            return false;
        }

        return true;
    }

    protected boolean checkLengthBetween(ConstraintValidatorContext context, String value, int minLength, int maxLength) {
        if (!ConstraintValidationUtil.checkLength(value, minLength, maxLength)) {
            setCustomMessage(
                    context,
                    ConstraintValidationUtil.getMinMaxLengthMessage(parameterName, minLength, maxLength)
            );
            return false;
        }

        return true;
    }

    protected boolean checkMaxBytes(ConstraintValidatorContext context, String value, int maxLength) {
        if (!ConstraintValidationUtil.checkMaxBytes(value, maxLength)) {
            setCustomMessage(context, ConstraintValidationUtil.getMaxBytesMessage(parameterName, maxLength));
            return false;
        }

        return true;
    }

    protected boolean checkNotStartWith(String value, String prefix, ConstraintValidatorContext context) {
        if (ConstraintValidationUtil.isStartWith(prefix, value)) {
            setCustomMessage(context, ConstraintValidationUtil.getNotStartWithMessage(parameterName, value));
            return false;
        }

        return true;
    }

    protected boolean checkEndWith(ConstraintValidatorContext context, String value, String suffix) {
        if (!ConstraintValidationUtil.isEndWith(suffix, value)) {
            setCustomMessage(context, ConstraintValidationUtil.getEndWithMessage(parameterName, suffix));
            return false;
        }

        return true;
    }

    protected boolean checkNotStartOrEndWith(ConstraintValidatorContext context, String value, String pattern) {

        if (ConstraintValidationUtil.isStartOrEndWith(pattern, value)) {
            setCustomMessage(context, ConstraintValidationUtil.getNotStartOrEndWithMessage(parameterName, pattern));
            return false;
        }

        return true;
    }

    protected boolean patternMatches(ConstraintValidatorContext context, String value, Pattern pattern) {
        if (!ConstraintValidationUtil.patternMatches(pattern, value)) {
            setCustomMessage(context, message);
            return false;
        }

        return true;
    }

    protected boolean checkExtraSpaces(ConstraintValidatorContext context, String value) {
        if (ConstraintValidationUtil.hasExtraSpaces(value)) {
            setCustomMessage(context, ConstraintValidationUtil.getExtraSpacesMessage(parameterName));
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
