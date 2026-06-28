package com.jtk.ps.api.model;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "multi_role_weight")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultiRoleWeight {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "participant_id")
    private Integer participantId;

    @Column(name = "job_scope_name")
    private String jobScopeName;

    @Column(name = "proportional_weight")
    private Float proportionalWeight; // e.g. 0.5 for 50%

    @Version
    private Long version;
}
