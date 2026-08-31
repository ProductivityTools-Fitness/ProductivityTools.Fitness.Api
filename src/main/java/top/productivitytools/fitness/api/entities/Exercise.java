package top.productivitytools.fitness.api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "exercise")
@Getter
@Setter
@NoArgsConstructor
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private FitnessUser user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(name = "primary_muscle", length = 50)
    private String primaryMuscle;

    @Column(name = "secondary_muscles", length = 200)
    private String secondaryMuscles;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public boolean isCustom() {
        return Boolean.FALSE.equals(this.isSystem);
    }
}
