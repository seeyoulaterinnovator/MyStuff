package ru.alamics.sso.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "MAIN_REDIRECT_URIS")
public class MainRedirectUri implements Serializable {
    @Id
    @Column(name = "client_id")
    private String clientId;

    @Column(name = "uri")
    private String uri;
}
