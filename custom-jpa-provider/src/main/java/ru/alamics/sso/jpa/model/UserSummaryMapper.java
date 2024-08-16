package ru.alamics.sso.jpa.model;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SqlResultSetMapping;

@MappedSuperclass
@SqlResultSetMapping(name="UserSummaryMapper",
    classes = {
        @ConstructorResult(
            targetClass = UserSummaryView.class,
            columns = {
                @ColumnResult(name = "ID", type = String.class),
                @ColumnResult(name = "USERNAME", type = String.class),
                @ColumnResult(name = "FIRST_NAME", type = String.class),
                @ColumnResult(name = "LAST_NAME", type = String.class),
                @ColumnResult(name = "EMAIL", type = String.class),
                @ColumnResult(name = "PHONE", type = String.class),
                @ColumnResult(name = "ENABLED", type = Boolean.class),
                @ColumnResult(name = "EMAIL_VERIFIED", type = Boolean.class)
            })
    })

public abstract class UserSummaryMapper {}
