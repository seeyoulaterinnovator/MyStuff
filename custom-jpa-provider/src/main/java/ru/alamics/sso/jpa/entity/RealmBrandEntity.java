package ru.alamics.sso.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "REALM_BRAND")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealmBrandEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false, updatable = false)
    private String id;

    @Column(name = "REALM_ID", nullable = false, length = 64)
    private String realmId;

    @Column(name = "BRAND_ID", nullable = false, length = 36)
    private String brandId;

    @Column(name = "IS_DEFAULT", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRAND_ID", insertable = false, updatable = false)
    private BrandEntity brand;
}