package ru.alamics.sso.emailer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailModel {
    private UserModel user;
    private RealmModel realmModel;
    private String subject;
    private String bodyTemplate;
    private List<Object> subjectAttributes;
    private Map<String, Object> bodyAttributes;
}
