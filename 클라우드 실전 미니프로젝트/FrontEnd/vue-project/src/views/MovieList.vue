<template>
  <div class="movie-list">
    <h1>🎬 영화 검색</h1>

    <!-- 검색 컨트롤 -->
    <div class="controls">
      <!-- 검색 모드 선택 -->
      <select v-model="searchMode" class="mode-toggle">
        <option value="keyword">제목/감독/배우</option>
        <option value="year">연도</option>
      </select>

      <!-- 검색 입력 -->
      <input
        v-model="searchValue"
        :placeholder="
          searchMode === 'keyword' ? '영화 제목/감독/배우 입력' : '연도 입력 (예: 1999)'
        "
        class="input-box"
        @keyup.enter="onSearch(0)"
      />

      <button class="btn primary" @click="onSearch(0)">검색</button>
    </div>

    <!-- 영화 카드 리스트 -->
    <div class="movie-grid">
      <div
        v-for="movie in movies"
        :key="movie.movieSeq"
        class="movie-card"
        @click="goDetail(movie.movieSeq)"
      >
        <div class="poster-wrapper">
          <img :src="movie.moviePosterUrl" alt="poster" />
          <div class="overlay-text">
            <h3>{{ movie.movieTitle }}</h3>
          </div>
        </div>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div v-if="totalPages > 1" class="pagination">
      <button :disabled="page === 0" @click="changePage(page - 1)">⟨ 이전</button>
      <span>{{ page + 1 }} / {{ totalPages }}</span>
      <button :disabled="page === totalPages - 1" @click="changePage(page + 1)">다음 ⟩</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getMoviesByYear, searchMovies } from '../api/movieApi'

const movies = ref([])
const searchMode = ref('keyword')
const searchValue = ref('')
const page = ref(0)
const size = ref(16)
const totalPages = ref(0)

const router = useRouter()
const route = useRoute()

// 🔍 검색 실행
const onSearch = async (newPage = 0) => {
  page.value = newPage

  // ✅ 검색 조건을 URL 쿼리에 반영
  router.push({
    path: '/',
    query: {
      mode: searchMode.value,
      value: searchValue.value,
      page: page.value,
    },
  })

  try {
    if (searchMode.value === 'keyword') {
      const res = await searchMovies(searchValue.value, page.value, size.value)
      movies.value = res.data.data.content
      totalPages.value = res.data.data.totalPages
    } else if (searchMode.value === 'year') {
      const year = parseInt(searchValue.value)
      if (!year || isNaN(year)) return
      const res = await getMoviesByYear(year, page.value, size.value)
      movies.value = [...res.data.data.content].sort((a, b) => b.movieYear - a.movieYear)
      totalPages.value = res.data.data.totalPages
    }
  } catch (err) {
    console.error('검색 실패:', err)
  }
}

// 📄 페이지 이동
const changePage = (newPage) => {
  onSearch(newPage)
}

// 🎬 상세 페이지 이동
const goDetail = (id) => {
  router.push({
    path: `/movies/${id}`,
    query: route.query, // ✅ 검색조건 유지
  })
}

// 🔄 마운트 시 URL 쿼리 복원
onMounted(() => {
  if (route.query.mode) {
    searchMode.value = route.query.mode
  }
  if (route.query.value) {
    searchValue.value = route.query.value
  }
  if (route.query.page) {
    page.value = parseInt(route.query.page)
  }

  if (searchValue.value) {
    onSearch(page.value)
  }
})
</script>

<style scoped>
.movie-list {
  padding: 20px;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  color: #333;
  text-align: center;
}

h1 {
  margin-bottom: 20px;
  color: #42b983; /* Vue 포인트 컬러 */
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 30px;
}

.mode-toggle {
  padding: 10px 14px;
  border: 1px solid #ccc;
  border-radius: 8px;
  background: white;
  font-size: 0.95rem;
}

.input-box {
  padding: 10px 14px;
  border: 1px solid #ccc;
  border-radius: 8px;
  min-width: 220px;
  font-size: 0.95rem;
}

.btn {
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  transition:
    transform 0.15s,
    background 0.2s;
}

.btn.primary {
  background-color: #42b983;
  color: white;
}
.btn.primary:hover {
  background-color: #36996d;
  transform: scale(1.05);
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr); /* ✅ 8개 x 2줄 */
  gap: 20px;
}

.movie-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  transition:
    transform 0.25s,
    box-shadow 0.25s;
}

.movie-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.3);
}

.poster-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.poster-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.overlay-text {
  position: absolute;
  bottom: 0;
  width: 100%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  color: white;
  padding: 6px;
  text-align: left;
}

.overlay-text h3 {
  margin: 0;
  font-size: 0.85rem;
}

.pagination {
  margin-top: 30px;
  text-align: center;
}

.pagination button {
  margin: 0 12px;
  padding: 8px 14px;
  border: none;
  border-radius: 50px;
  background-color: #42b983;
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition:
    background 0.2s,
    transform 0.15s;
}

.pagination button:hover:not(:disabled) {
  background-color: #36996d;
  transform: scale(1.05);
}

.pagination button:disabled {
  background-color: #bbb;
  cursor: not-allowed;
}
</style>
