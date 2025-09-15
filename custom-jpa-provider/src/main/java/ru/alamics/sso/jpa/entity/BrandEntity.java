package ru.alamics.sso.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "BRAND")
public class BrandEntity {
    @Id
    @Column(name = "ID", nullable = false, length = 36)
    private String id;

    @Column(name = "REALM", nullable = false, length = 50)
    private String realm;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "IS_DEFAULT", nullable = false)
    private Boolean isDefault;
}
