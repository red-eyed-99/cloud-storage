package ru.redeyed.cloudstorage.resource;

public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException() {
        super("Resource already exists");
    }
}
