package dca.backend.dto;

import lombok.Data;

@Data
public class MovieByYearRequest {
    private Integer year;   // 조회할 연도
    private int page = 0;   // 페이지 번호
    private int size = 10;  // 페이지 크기
}