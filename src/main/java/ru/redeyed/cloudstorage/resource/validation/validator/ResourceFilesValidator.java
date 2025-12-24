package ru.redeyed.cloudstorage.resource.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;
import org.apache.tomcat.util.http.fileupload.impl.FileCountLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.common.validation.validator.BaseConstraintValidator;
import ru.redeyed.cloudstorage.common.validation.ValidationUtil;
import ru.redeyed.cloudstorage.resource.validation.annotation.ValidResourceFiles;
import java.util.List;
import java.util.regex.Pattern;

public class ResourceFilesValidator extends BaseConstraintValidator<ValidResourceFiles, List<MultipartFile>> {

    @Value("${multipart-files-count-limit}")
    private long filesCountLimit;

    private static final String PATTERN = "^[^\\\\:*?\"<>|]+$";

    private Pattern pattern;

    @Override
    public void initialize(ValidResourceFiles constraintAnnotation) {
        pattern = Pattern.compile(PATTERN);
        message = constraintAnnotation.message();
    }

    @Override
    @SneakyThrows
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files.size() > filesCountLimit) {
            throw new FileCountLimitExceededException("Too many files.", filesCountLimit);
        }

        for (var file : files) {
            var filePath = file.getOriginalFilename();

            if (!isValid(filePath, context)) {
                return false;
            }
        }

        return true;
    }

    private boolean isValid(String filePath, ConstraintValidatorContext context) {
        if (!fileNameIsValid(filePath, context)) {
            return false;
        }

        if (PathUtil.isFileName(filePath)) {
            return true;
        }

        if (ValidationUtil.isStartWith(PathUtil.PATH_DELIMITER, filePath)) {
            setCustomMessage(context, filePath + " must not start with a '" + PathUtil.PATH_DELIMITER + "' .");
            return false;
        }

        if (!ValidationUtil.checkMaxBytes(filePath, ResourcePathValidator.PATH_MAX_BYTES)) {
            setCustomMessage(context, filePath + " is too long.");
            return false;
        }

        if (ValidationUtil.hasExtraSpaces(filePath)) {
            setCustomMessage(context, filePath + " has extra spaces.");
            return false;
        }

        if (!ValidationUtil.patternMatches(pattern, filePath)) {
            setCustomMessage(context, filePath + " contains prohibited characters: \\:*?\"<>| .");
            return false;
        }

        return true;
    }

    private boolean fileNameIsValid(String filePath, ConstraintValidatorContext context) {
        var fileName = PathUtil.extractResourceName(filePath);

        if (!ValidationUtil.checkMaxLength(fileName, ResourcePathValidator.RESOURCE_NAME_MAX_LENGTH)) {
            var message = "%s filename is too long. Max - %d characters.";
            setCustomMessage(context, message.formatted(filePath, ResourcePathValidator.RESOURCE_NAME_MAX_LENGTH));
            return false;
        }

        if (!ValidationUtil.checkMaxBytes(fileName, ResourcePathValidator.RESOURCE_NAME_MAX_BYTES)) {
            var message = "%s filename is too big. Max - %d bytes.";
            setCustomMessage(context, message.formatted(filePath, ResourcePathValidator.RESOURCE_NAME_MAX_BYTES));
            return false;
        }

        return true;
    }
}
