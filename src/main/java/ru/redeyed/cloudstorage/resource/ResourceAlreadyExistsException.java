package ru.redeyed.cloudstorage.resource;

public class ResourceAlreadyExistsException extends RuntimeException {

    private static final String MESSAGE_FORMAT = "%s already exists.";

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(ResourceType resourceType) {
        this(MESSAGE_FORMAT.formatted(resourceType.getCapitalizedName()));
    }
}
