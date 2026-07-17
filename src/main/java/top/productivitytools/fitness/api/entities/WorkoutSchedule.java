package top.productivitytools.fitness.api.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "workout_schedule")
@Data
public class WorkoutSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "day_name", nullable = false, length = 15)
    private String dayName;

    @Column(name = "main_activity", nullable = false)
    private String mainActivity;

    @Column(name = "goal_benefits", columnDefinition = "TEXT")
    private String goalBenefits;

    @Column(name = "pull_up_protocol", columnDefinition = "TEXT")
    private String pullUpProtocol;

    @Column(name = "suggested_time", columnDefinition = "TEXT")
    private String suggestedTime;
}
