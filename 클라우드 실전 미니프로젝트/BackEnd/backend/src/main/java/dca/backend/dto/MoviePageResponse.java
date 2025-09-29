package dca.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MoviePageResponse {
    private List<MovieListResponse> content;  // 영화 리스트
    private int totalPages;                   // 전체 페이지 수
    private long totalElements;               // 전체 데이터 개수
    private int page;                         // 현재 페이지 번호
    private int size;                         // 요청한 페이지 크기
}