package com.upc.acusticupc.shared.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resource, Object id) {
        super("%s con id %s no fue encontrado".formatted(resource, id));
    }
}