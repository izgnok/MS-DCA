package dca.backend.dto;

import dca.backend.moviedb.entity.Movie;
import dca.backend.moviedb.entity.Actor;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieDetailResponse {
    private Integer movieSeq;
    private String movieTitle;
    private String director;
    private String genre;
    private String country;
    private String moviePlot;
    private String audienceRating;
    private Integer movieYear;
    private String runningTime;
    private Double movieRating;
    private String moviePosterUrl;
    private String trailerUrl;
    private String backgroundUrl;
    private String delYn;

    private List<ActorResponse> actors;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActorResponse {
        private Integer actorSeq;
        private String actorName;
        private String role;

        public static ActorResponse fromEntity(Actor actor) {
            return ActorResponse.builder()
                    .actorSeq(actor.getActorSeq())
                    .actorName(actor.getActorName())
                    .role(actor.getRole())
                    .build();
        }
    }

    public static MovieDetailResponse fromEntity(Movie movie) {
        return MovieDetailResponse.builder()
                .movieSeq(movie.getMovieSeq())
                .movieTitle(movie.getMovieTitle())
                .director(movie.getDirector())
                .genre(movie.getGenre())
                .country(movie.getCountry())
                .moviePlot(movie.getMoviePlot())
                .audienceRating(movie.getAudienceRating())
                .movieYear(movie.getMovieYear())
                .runningTime(movie.getRunningTime())
                .movieRating(movie.getMovieRating())
                .moviePosterUrl(movie.getMoviePosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .backgroundUrl(movie.getBackgroundUrl())
                .delYn(movie.getDelYn())
                .actors(
                        movie.getActors().stream()
                                .map(ActorResponse::fromEntity)
                                .collect(Collectors.toList())
                )
                .build();
    }
}