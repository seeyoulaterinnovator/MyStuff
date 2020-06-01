package ru.alamics.sso.jpa.entity.status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "CHECK_TABLE")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckTableEntity {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "updated")
    private LocalDateTime updateTime;

    @Version
    private Long version;
}
