<template>
  <div class="github-button-container">
    <a href="https://github.com/Icbitic/TeaTeach/tree/main" target="_blank" rel="noopener noreferrer" class="github-button">
      <div class="github-icon">
        <svg height="32" width="32" viewBox="0 0 16 16" version="1.1" aria-hidden="true">
          <path fill-rule="evenodd" d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"></path>
        </svg>
      </div>
      <span class="github-text">{{ $t('common.viewOnGitHub') }}</span>
    </a>
  </div>
</template>

<script>
export default {
  name: 'GitHubButton'
}
</script>

<style scoped>
.github-button-container {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 9999;
}

.github-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  transition: all 0.3s ease;
}

.github-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 60px;
  height: 60px;
  background-color: #24292e;
  border-radius: 50%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.8);
  margin-bottom: 8px;
  border: 3px solid #fff;
  transition: all 0.3s ease;
}

.github-button:hover .github-icon {
  transform: translateY(-5px) rotate(10deg);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.9);
  background-color: #2b3137;
}

.github-icon svg {
  fill: white;
  width: 32px;
  height: 32px;
}

.github-text {
  color: white;
  font-size: 14px;
  font-weight: 600;
  background-color: #24292e;
  padding: 5px 10px;
  border-radius: 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5);
  white-space: nowrap;
  border: 2px solid #fff;
}

.github-button span {
  vertical-align: middle;
}
</style>