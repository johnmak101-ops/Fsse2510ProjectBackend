package com.fsse2510.fsse2510_project_backend.exception.showcase;

public class ShowcaseCollectionNotFoundException extends RuntimeException {
    public ShowcaseCollectionNotFoundException(Integer id) {
        super("Showcase collection not found: " + id);
    }
}
