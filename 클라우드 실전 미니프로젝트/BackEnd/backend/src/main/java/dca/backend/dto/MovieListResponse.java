package dca.backend.dto;

import dca.backend.moviedb.entity.Movie;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieListResponse {
    private Integer movieSeq;
    private String movieTitle;
    private String director;
    private Integer movieYear;
    private String moviePosterUrl;

    public static MovieListResponse fromEntity(Movie movie) {
        return MovieListResponse.builder()
                .movieSeq(movie.getMovieSeq())
                .movieTitle(movie.getMovieTitle())
                .director(movie.getDirector())
                .movieYear(movie.getMovieYear())
                .moviePosterUrl(movie.getMoviePosterUrl())
                .build();
    }
}