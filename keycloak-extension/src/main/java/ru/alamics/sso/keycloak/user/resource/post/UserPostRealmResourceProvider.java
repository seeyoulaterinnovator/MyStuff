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

package ru.alamics.sso.keycloak.user.resource.post;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;


public class UserPostRealmResourceProvider implements BaseResourceProvider<UserPostResource> {

    private final KeycloakSession session;

    public UserPostRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public UserPostResource getResource() {
        AdminPermissionEvaluator auth = initAuthByWorkingRealm(this.session);

        auth.users().requireView();

        return new UserPostResource(session, auth);
    }

    @Override
    public void close() {
    }
}
