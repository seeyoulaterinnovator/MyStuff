package ru.alamics.sso.jpa.model;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.MappedSuperclass;
import javax.persistence.SqlResultSetMapping;

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
                @ColumnResult(name = "ENABLED", type = Boolean.class)
            })
    })

public abstract class UserSummaryMapper {}
