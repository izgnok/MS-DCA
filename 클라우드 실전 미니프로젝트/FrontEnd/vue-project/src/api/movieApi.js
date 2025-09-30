// src/api/movieApi.js
import axios from 'axios'

const api = axios.create({
  baseURL: '/backend/api/movie', // ✅ 컨트롤러 경로와 일치
})

// 연도별 조회
export const getMoviesByYear = (year, page = 0, size = 10) =>
  api.post('/year', { year, page, size })

// 영화 검색
export const searchMovies = (keyword, page = 0, size = 10) =>
  api.post('/search', { keyword, page, size })

// 영화 상세 조회
export const getMovieDetail = (movieSeq) => api.get(`/${movieSeq}`)
