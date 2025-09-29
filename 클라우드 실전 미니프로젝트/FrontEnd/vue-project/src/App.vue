<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup></script>

<style>
/* 전역 초기화 */
html,
body,
#app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}

/* 앱 전체 */
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  color: #fff;
  background: #000; /* 배경 이미지 없는 경우 대비 */
  overflow-x: hidden; /* 좌우 스크롤 제거 */
}
</style>
