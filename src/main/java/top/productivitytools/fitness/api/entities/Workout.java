package top.productivitytools.fitness.api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout")
@Getter
@Setter
@NoArgsConstructor
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private FitnessUser user;

    @Column(nullable = false, length = 150)
    private String title = "Log Workout";

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime = OffsetDateTime.now();

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "duration_seconds")
    private Integer durationSeconds = 0;

    @Column(nullable = false, length = 30)
    private String status = "IN_PROGRESS";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<WorkoutExercise> exercises = new ArrayList<>();

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
