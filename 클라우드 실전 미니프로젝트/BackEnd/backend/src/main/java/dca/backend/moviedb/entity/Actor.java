package dca.backend.moviedb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "actor")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "actor_seq")
    private Integer actorSeq;

    @Column(name = "actor_name", nullable = false, length = 255)
    private String actorName;

    @Column(name = "role", length = 255)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_seq", nullable = false)
    private Movie movie;
}