package top.productivitytools.fitness.api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercise")
@Getter
@Setter
@NoArgsConstructor
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_exercise_id", length = 50)
    private String externalExerciseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private FitnessUser user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "gif_url", length = 500)
    private String gifUrl;

    @Column(name = "equipment_category", length = 50)
    private String equipmentCategory;

    @Column(name = "body_category", length = 50)
    private String bodyCategory;

    @Column(name = "target_muscle", length = 100)
    private String targetMuscle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "secondary_muscles", columnDefinition = "jsonb")
    private List<String> secondaryMuscles = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "instructions", columnDefinition = "jsonb")
    private List<String> instructions = new ArrayList<>();

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public boolean isCustom() {
        return Boolean.FALSE.equals(this.isSystem);
    }
}
