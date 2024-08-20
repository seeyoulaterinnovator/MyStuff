/*
 *
 *  * Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.services.validation;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.text.MessageFormat;
import java.util.Properties;

@Data
public class ValidationMessage {
    private String fieldId;

    private String message;

    @EqualsAndHashCode.Exclude
    private String localizedMessageKey;

    @EqualsAndHashCode.Exclude
    private Object[] localizedMessageParameters;

    public ValidationMessage(String message) {
        this.message = message;
    }

    public ValidationMessage(String message, String localizedMessageKey, Object... localizedMessageParameters) {
        this.message = message;
        this.localizedMessageKey = localizedMessageKey;
        this.localizedMessageParameters = localizedMessageParameters;
    }

    public String getMessage(Properties localizedMessages) {
        if (getLocalizedMessageKey() != null) {
            return MessageFormat.format(localizedMessages.getProperty(getLocalizedMessageKey(), getMessage()), getLocalizedMessageParameters());
        }
        else {
            return getMessage();
        }
    }
}
