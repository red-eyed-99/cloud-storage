package ru.redeyed.cloudstorage.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ResourceType {

    FILE("File"), DIRECTORY("Directory");

    private final String capitalizedName;
}
