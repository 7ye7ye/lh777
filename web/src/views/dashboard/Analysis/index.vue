<template>
  <Home v-if="indexStyle === 0"></Home>
  <div style="width: 100%; text-align: right; margin-top: 20px">
    首页主题：
    <a-radio-group v-model:value="indexStyle">
      <a-radio :value="0">默认</a-radio>
    </a-radio-group>
  </div>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';
  import Home from './homePage/home.vue';

  const indexStyle = ref(0);
</script>
