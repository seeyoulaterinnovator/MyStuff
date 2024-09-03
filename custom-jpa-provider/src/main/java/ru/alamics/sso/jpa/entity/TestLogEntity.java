package ru.alamics.sso.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "TEST_LOG")
public class TestLogEntity {

    @Id
    @Column(name = "id")
//    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(name = "log")
    private String log;

}
