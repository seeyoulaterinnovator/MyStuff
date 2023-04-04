package ru.alamics.sso.jpa.entity.antifraud;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "BLACK_LIST")
@Entity
@Data
@NoArgsConstructor
//maybe we need USER ID
public class BlackListEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private UUID id;
    @Column(name = "ip")
    private String ip;
    @Column(name = "user_login")
    private String userLogin;
    @CreationTimestamp
    @Column(name = "created")
    private LocalDateTime createdAt;
    @Column(name = "unblocked")
    private LocalDateTime unblockedAt;
    @Column(name = "block_duration")
    private Long blockDurationSec;
    @Column(name = "block_count")
    private Integer blockCount;
}
