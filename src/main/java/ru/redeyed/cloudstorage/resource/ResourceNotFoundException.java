package ru.redeyed.cloudstorage.resource;

public class ResourceNotFoundException extends RuntimeException {

    private static final String MESSAGE_FORMAT = "%s not found.";

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(ResourceType resourceType) {
        this(MESSAGE_FORMAT.formatted(resourceType.getCapitalizedName()));
    }
}
