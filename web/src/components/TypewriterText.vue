<template>
  <span class="typewriter-text" :class="{ 'typing': isTyping }">
    {{ displayText }}
  </span>
</template>

<script>
import { ref, watch, onMounted } from 'vue'

export default {
  name: 'TypewriterText',
  props: {
    text: {
      type: String,
      required: true
    },
    show: {
      type: Boolean,
      default: true
    },
    speed: {
      type: Number,
      default: 50 // milliseconds per character
    },
    delay: {
      type: Number,
      default: 0 // delay before starting typing
    }
  },
  emits: ['typing-complete'],
  setup(props, { emit }) {
    const displayText = ref('')
    const isTyping = ref(false)
    let typingTimeout = null

    const typeText = () => {
      if (!props.show) {
        displayText.value = ''
        isTyping.value = false
        return
      }

      isTyping.value = true
      displayText.value = ''
      let currentIndex = 0

      const typeNextChar = () => {
        if (currentIndex < props.text.length) {
          displayText.value += props.text[currentIndex]
          currentIndex++
          typingTimeout = setTimeout(typeNextChar, props.speed)
        } else {
          isTyping.value = false
          emit('typing-complete')
        }
      }

      typeNextChar()
    }

    const clearText = () => {
      if (typingTimeout) {
        clearTimeout(typingTimeout)
        typingTimeout = null
      }
      displayText.value = ''
      isTyping.value = false
    }

    watch(() => props.show, (newShow) => {
      if (newShow) {
        // Add delay prop plus base delay when showing
        setTimeout(typeText, 200 + props.delay)
      } else {
        clearText()
      }
    })

    watch(() => props.text, () => {
      if (props.show) {
        clearText()
        setTimeout(typeText, 100) // Small delay before starting new text
      }
    })

    onMounted(() => {
      if (props.show) {
        setTimeout(typeText, props.delay)
      }
    })

    return {
      displayText,
      isTyping
    }
  }
}
</script>

<style scoped>
.typewriter-text {
  display: inline-block;
  min-height: 1em;
  position: relative;
}

.typewriter-text.typing::after {
  content: '|';
  animation: blink 1s infinite;
  margin-left: 2px;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}
</style>