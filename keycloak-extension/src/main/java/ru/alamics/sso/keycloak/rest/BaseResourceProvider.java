package ru.alamics.sso.keycloak.rest;

import org.keycloak.services.resource.RealmResourceProvider;

public interface BaseResourceProvider<T> extends RealmResourceProvider {

    @Override
    T getResource ();

    @Override
    default void close () { }
}
