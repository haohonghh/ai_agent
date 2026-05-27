<template>
  <div class="web-widget" :class="{ 'is-open': isOpen }">
    <button class="widget-toggle" @click="toggleWidget">
      <span v-if="!isOpen" class="toggle-icon">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span class="notification-dot" v-if="unreadCount > 0"></span>
      </span>
      <span v-else class="toggle-icon">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </span>
    </button>

    <Transition name="widget-slide">
      <div v-if="isOpen" class="widget-panel">
        <div class="panel-header">
          <div class="header-brand">
            <div class="brand-avatar">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
              </svg>
            </div>
            <div class="brand-info">
              <span class="brand-name">智能客服</span>
              <span class="brand-status">
                <span class="status-dot"></span>
                在线
              </span>
            </div>
          </div>
          <el-button text @click="toggleWidget">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>

        <div class="panel-body" ref="messagesContainer">
          <div class="messages-list">
            <div
              v-for="(msg, index) in messages"
              :key="index"
              class="message-item"
              :class="msg.type"
              :style="{ animationDelay: `${index * 50}ms` }"
            >
              <div class="message-avatar" v-if="msg.type === 'agent'">
                <div class="avatar-circle">客服</div>
              </div>
              <div class="message-content">
                <div class="message-bubble">{{ msg.content }}</div>
                <div class="message-time">{{ msg.time }}</div>
              </div>
            </div>

            <div v-if="isTyping" class="message-item agent">
              <div class="message-avatar">
                <div class="avatar-circle">客服</div>
              </div>
              <div class="message-content">
                <div class="message-bubble typing">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="panel-footer">
          <div class="quick-replies" v-if="messages.length === 1">
            <button
              v-for="reply in quickReplies"
              :key="reply"
              class="quick-reply-btn"
              @click="sendQuickReply(reply)"
            >
              {{ reply }}
            </button>
          </div>
          <div class="input-area">
            <el-input
              v-model="inputText"
              placeholder="输入您的问题..."
              @keyup.enter="sendMessage"
              class="message-input"
            />
            <el-button type="primary" @click="sendMessage" :disabled="!inputText.trim()">
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { Close, Promotion } from '@element-plus/icons-vue'

const isOpen = ref(false)
const inputText = ref('')
const isTyping = ref(false)
const unreadCount = ref(0)
const messagesContainer = ref<HTMLElement>()

interface Message {
  type: 'user' | 'agent' | 'system'
  content: string
  time: string
}

const messages = ref<Message[]>([
  {
    type: 'agent',
    content: '您好！我是您的智能客服助手。请问有什么可以帮助您的？',
    time: formatTime(new Date())
  }
])

const quickReplies = ['常见问题', '人工客服', '联系工作人员']

function toggleWidget() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    unreadCount.value = 0
    nextTick(() => {
      scrollToBottom()
    })
  }
}

function formatTime(date: Date) {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

function sendMessage() {
  if (!inputText.value.trim()) return

  messages.value.push({
    type: 'user',
    content: inputText.value,
    time: formatTime(new Date())
  })

  inputText.value = ''
  scrollToBottom()

  isTyping.value = true
  setTimeout(() => {
    isTyping.value = false
    messages.value.push({
      type: 'agent',
      content: '感谢您的提问！我们的客服团队正在处理您的问题，请稍候。',
      time: formatTime(new Date())
    })
    scrollToBottom()
  }, 1500)
}

function sendQuickReply(reply: string) {
  inputText.value = reply
  sendMessage()
}
</script>

<style scoped>
.web-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.widget-toggle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(79, 70, 229, 0.4);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
}

.widget-toggle:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 28px rgba(79, 70, 229, 0.5);
}

.widget-toggle:active {
  transform: scale(0.95);
}

.toggle-icon {
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 10px;
  height: 10px;
  background: #ef4444;
  border-radius: 50%;
  border: 2px solid white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.2); }
}

.widget-panel {
  width: 380px;
  height: 560px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.panel-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-avatar {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.brand-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-name {
  color: white;
  font-weight: 600;
  font-size: 15px;
}

.brand-status {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 12px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #22c55e;
  border-radius: 50%;
  animation: statusPulse 2s infinite;
}

@keyframes statusPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.5); }
  50% { box-shadow: 0 0 0 4px rgba(34, 197, 94, 0); }
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #fafaf9;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 10px;
  animation: messageSlideIn 0.3s ease-out forwards;
  opacity: 0;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
}

.message-item.user .avatar-circle {
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
}

.message-content {
  max-width: 75%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-item.user .message-content {
  align-items: flex-end;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  color: #1c1917;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  word-break: break-word;
}

.message-item.user .message-bubble {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-item.agent .message-bubble {
  background: white;
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 11px;
  color: #a8a29e;
}

.message-bubble.typing {
  display: flex;
  gap: 4px;
  padding: 14px 18px;
}

.message-bubble.typing span {
  width: 8px;
  height: 8px;
  background: #a8a29e;
  border-radius: 50%;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.message-bubble.typing span:nth-child(1) { animation-delay: 0s; }
.message-bubble.typing span:nth-child(2) { animation-delay: 0.2s; }
.message-bubble.typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

.panel-footer {
  padding: 16px;
  background: white;
  border-top: 1px solid #e7e5e4;
}

.quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.quick-reply-btn {
  padding: 6px 12px;
  background: #fafaf9;
  border: 1px solid #e7e5e4;
  border-radius: 16px;
  font-size: 12px;
  color: #4f46e5;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-reply-btn:hover {
  background: #4f46e5;
  color: white;
  border-color: #4f46e5;
}

.input-area {
  display: flex;
  gap: 8px;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: none;
  border: 1px solid #e7e5e4;
  transition: all 0.2s;
}

.message-input :deep(.el-input__wrapper:hover) {
  border-color: #4f46e5;
}

.message-input :deep(.el-input__wrapper.is-focus) {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.input-area .el-button {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  padding: 0;
}

.widget-slide-enter-active,
.widget-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.widget-slide-enter-from,
.widget-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

.widget-slide-enter-to,
.widget-slide-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}

@media (max-width: 480px) {
  .widget-panel {
    position: fixed;
    inset: 0;
    width: 100%;
    height: 100%;
    border-radius: 0;
  }
}
</style>