package dca.backend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class MovieSearchRequest {
    private String keyword; // 제목 또는 감독, 배우 LIKE 검색
    private int page = 0;
    private int size = 10;
}