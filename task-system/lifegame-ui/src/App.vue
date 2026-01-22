<script setup>
  // 这里写 JS 逻辑
</script>

<template>
  <div id="app">
    <transition name="fade" mode="out-in">
      <router-view />
    </transition>
  </div>
</template>

<style>
  /*全局样式重置*/
  body {margin: 0; padding: 0; background-color: #f0f2f5; font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', sans-serif; }
  
  /* 路由切换过渡动画 */
  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.8s ease;
  }
  
  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }

  /* 添加滑动效果增强体验 */
  .slide-fade-enter-active {
    transition: all 0.8s ease;
  }
  .slide-fade-leave-active {
    transition: all 0.8s cubic-bezier(1.0, 0.5, 0.8, 1.0);
  }
  .slide-fade-enter-from,
  .slide-fade-leave-to {
    transform: translateX(10px);
    opacity: 0;
  }
</style>