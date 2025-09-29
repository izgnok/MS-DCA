package dca.backend.moviedb.repository;

import dca.backend.moviedb.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.actors WHERE m.movieSeq = :movieSeq")
    Movie findByIdWithActors(@Param("movieSeq") Integer movieSeq);

    /** 연도별 영화 조회 (Page) */
    Page<Movie> findByMovieYear(Integer movieYear, Pageable pageable);

    /** 영화 검색 (제목, 감독, 배우 LIKE) */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.actors a " +
            "WHERE LOWER(m.movieTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(m.director) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(a.actorName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Movie> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}