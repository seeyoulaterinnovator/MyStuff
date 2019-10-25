/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.alamics.sso.keycloak.create.rest;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;
import ru.alamics.sso.registration.service.UserFindService;

@Slf4j
public class CustomUserRealmResourceProviderFactory implements BaseResourceProviderFactory, BaseResourceProvider {

    public static final String ID = "users-toms";

    private KeycloakSession session;
    private UserFindService userFindService;
    private AdminPermissionEvaluator auth;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        this.auth = this.initAuth(session);
        this.session = session;
        this.userFindService = (UserFindService) Lookup.lookup(UserFindService.class);
        return this;
    }

    @Override
    public Object getResource() {
        return new CustomRestResource(session, userFindService, this.auth);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}