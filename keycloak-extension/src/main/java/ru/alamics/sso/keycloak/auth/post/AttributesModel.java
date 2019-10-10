package ru.alamics.sso.keycloak.auth.post;

import lombok.*;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttributesModel {
    private String tomsId;
    private String roleName;


    @Override
    public boolean equals (Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        AttributesModel that = (AttributesModel) o;
        return Objects.equals(getTomsId(), that.getTomsId()) &&
                Objects.equals(getRoleName(), that.getRoleName());
    }

    @Override
    public int hashCode () {
        return Objects.hash(getTomsId(), getRoleName());
    }
}
