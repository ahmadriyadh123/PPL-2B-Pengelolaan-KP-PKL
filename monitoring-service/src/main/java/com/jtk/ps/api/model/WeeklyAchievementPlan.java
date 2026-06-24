package com.jtk.ps.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "weekly_achievement_plan")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyAchievementPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonProperty("id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonBackReference
    private Rpp rpp;

    @Column(name = "achievement_plan", nullable = false)
    @JsonProperty("achievement_plan")
    private String achievementPlan;

    @Column(name = "start_date", nullable = false)
    @JsonProperty("start_date")
    private LocalDate startDate;

    @Column(name = "finish_date", nullable = false)
    @JsonProperty("finish_date")
    private LocalDate finishDate;
}