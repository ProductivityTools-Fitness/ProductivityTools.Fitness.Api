package top.productivitytools.fitness.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workout_template_exercise")
@Getter
@Setter
@NoArgsConstructor
public class WorkoutTemplateExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @JsonIgnore
    private WorkoutTemplate template;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 1;

    @Column(name = "target_sets", nullable = false)
    private Integer targetSets = 3;

    @Column(name = "target_reps", nullable = false)
    private Integer targetReps = 10;

    @Column(name = "target_rest_seconds")
    private Integer targetRestSeconds = 90;
}
