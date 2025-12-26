package ru.redeyed.cloudstorage.resource.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.resource.ResourceType;
import ru.redeyed.cloudstorage.resource.validation.annotation.SameResourceType;

@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class SameResourceTypeValidator implements ConstraintValidator<SameResourceType, Object[]> {

    @Override
    public boolean isValid(Object[] values, ConstraintValidatorContext context) {
        var fromParameter = (String) values[1];
        var toParameter = (String) values[2];

        var fromResourceType = PathUtil.isDirectory(fromParameter) ? ResourceType.DIRECTORY : ResourceType.FILE;
        var toResourceType = PathUtil.isDirectory(toParameter) ? ResourceType.DIRECTORY : ResourceType.FILE;

        return fromResourceType == toResourceType;
    }
}
