package dca.backend.moviedb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movie")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_seq")
    private Integer movieSeq;

    @Column(name = "movie_title", nullable = false, length = 255)
    private String movieTitle;

    @Column(name = "director", length = 255)
    private String director;

    @Column(name = "genre", length = 255)
    private String genre;

    @Column(name = "country", length = 255)
    private String country;

    @Column(name = "movie_plot", length = 5000)
    private String moviePlot;

    @Column(name = "audience_rating", length = 50)
    private String audienceRating;

    @Column(name = "movie_year", nullable = false)
    private Integer movieYear;

    @Column(name = "running_time", length = 50)
    private String runningTime;

    @Column(name = "movie_rating", nullable = false)
    private Double movieRating;

    @Column(name = "movie_poster_url", length = 500)
    private String moviePosterUrl;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "background_url", length = 500)
    private String backgroundUrl;

    @Column(name = "del_yn", nullable = false, length = 255)
    private String delYn;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Actor> actors = new ArrayList<>();
}