package ru.alamics.sso.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "DEV_HTTP_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DevHttpLogEntity {
    @Id
    @Column(name = "ID", nullable = false)
    UUID id;

    @Column(name = "PINNED", nullable = false)
    boolean pinned;

    @Column(name = "NODE", nullable = false)
    String node;

    @Column(name = "REQUESTED_AT", nullable = false)
    Instant requestedAt;

    @Column(name = "URL", nullable = false)
    String url;

    @Column(name = "METHOD", nullable = false)
    String method;

    @Column(name = "REQUEST_HEADERS", nullable = false)
    String requestHeaders;

    @Column(name = "REQUEST_BODY_TEXT")
    @Basic(fetch = FetchType.LAZY)
    @ToString.Exclude
    String requestBodyText;

    @Column(name = "REQUEST_BODY_BINARY")
    @Basic(fetch = FetchType.LAZY)
    @ToString.Exclude
    byte[] requestBodyBinary;

    @Column(name = "RESPONDED_AT")
    Instant respondedAt;

    @Column(name = "STATUS")
    Integer status;

    @Column(name = "RESPONSE_HEADERS")
    String responseHeaders;

    @Column(name = "RESPONSE_BODY_TEXT")
    @Basic(fetch = FetchType.LAZY)
    @ToString.Exclude
    String responseBodyText;

    @Column(name = "RESPONSE_BODY_BINARY")
    @Basic(fetch = FetchType.LAZY)
    @ToString.Exclude
    byte[] responseBodyBinary;
}
