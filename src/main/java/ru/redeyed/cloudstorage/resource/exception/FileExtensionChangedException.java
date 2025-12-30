package ru.redeyed.cloudstorage.resource.exception;

public class FileExtensionChangedException extends RuntimeException {

    private static final String MESSAGE = "Can't change the files extension.";

    public FileExtensionChangedException() {
        super(MESSAGE);
    }
}
