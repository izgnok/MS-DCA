<template>
  <div
    class="movie-detail"
    v-if="movie"
    :style="{ backgroundImage: 'url(' + movie.backgroundUrl + ')' }"
  >
    <div class="overlay">
      <!-- 뒤로가기 버튼 -->
      <button class="back-btn" @click="goBack">← 돌아가기</button>

      <!-- 상단 -->
      <div class="header">
        <!-- 포스터 -->
        <img class="poster" :src="movie.moviePosterUrl" alt="poster" />

        <!-- 기본 정보 -->
        <div class="info">
          <h1>{{ movie.movieTitle }}</h1>
          <p><b>감독:</b> {{ movie.director }}</p>
          <p><b>장르:</b> {{ movie.genre }}</p>
          <p><b>국가:</b> {{ movie.country }}</p>
          <p><b>연도:</b> {{ movie.movieYear }}</p>
          <p><b>러닝타임:</b> {{ movie.runningTime }}</p>
        </div>

        <!-- 예고편 (우측 상단 고정) -->
        <div v-if="movie.trailerUrl" class="trailer-float">
          <iframe :src="movie.trailerUrl" frameborder="0" allowfullscreen></iframe>
        </div>
      </div>

      <!-- 줄거리 -->
      <section class="plot">
        <h2>줄거리</h2>
        <p>{{ movie.moviePlot }}</p>
      </section>

      <!-- 배우 -->
      <section class="actors">
        <h2>배우</h2>
        <div class="actor-group">
          <h3>🎬 주연</h3>
          <ul class="actor-list">
            <li v-for="actor in mainActors" :key="actor.actorSeq">{{ actor.actorName }}</li>
          </ul>
        </div>
        <div class="actor-group" v-if="supportActors.length">
          <h3>🎭 조연</h3>
          <ul class="actor-list">
            <li v-for="actor in supportActors" :key="actor.actorSeq">{{ actor.actorName }}</li>
          </ul>
        </div>
        <div class="actor-group" v-if="minorActors.length">
          <h3>👤 단역</h3>
          <ul class="actor-list">
            <li v-for="actor in minorActors" :key="actor.actorSeq">{{ actor.actorName }}</li>
          </ul>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMovieDetail } from '../api/movieApi'

const movie = ref(null)
const route = useRoute()
const router = useRouter()

onMounted(async () => {
  const res = await getMovieDetail(route.params.id)
  movie.value = res.data.data
})

const mainActors = computed(() => movie.value?.actors.filter((a) => a.role === '주연') || [])
const supportActors = computed(() => movie.value?.actors.filter((a) => a.role === '조연') || [])
const minorActors = computed(() => movie.value?.actors.filter((a) => a.role === '단역') || [])

// ✅ 뒤로가기 시 검색조건 유지
const goBack = () => {
  router.push({
    path: '/',
    query: route.query,
  })
}
</script>

<style scoped>
.movie-detail {
  min-height: 100vh;
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  display: flex;
  flex-direction: column;
}

.overlay {
  background: rgba(0, 0, 0, 0.7);
  flex: 1;
  color: white;
  padding: 20px;
}

.back-btn {
  background: transparent;
  border: 1px solid #fff;
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 15px;
  transition: background 0.2s;
}
.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.header {
  position: relative; /* ✅ 트레일러 absolute 배치 */
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 40px;
}

.poster {
  width: 220px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.8);
}

.info {
  max-width: 600px;
}

/* ✅ 예고편 크기 고정 (16:9) */
.trailer-float {
  position: absolute;
  top: 0;
  right: 0;
  width: 560px;
  height: 315px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.6);
}
.trailer-float iframe {
  width: 100%;
  height: 100%;
  border: none;
  border-radius: 10px;
}

.plot,
.actors {
  margin-top: 20px;
}

.actor-group {
  margin-bottom: 15px;
}

.actor-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  list-style: none;
  padding: 0;
  margin: 0;
}

.actor-list li {
  background: rgba(255, 255, 255, 0.15);
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.9rem;
}
</style>
