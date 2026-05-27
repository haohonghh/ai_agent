<template>
  <div class="workbench-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">工作台</h2>
        <div class="agent-profile">
          <div class="profile-avatar">客服小花</div>
          <div class="profile-info">
            <span class="profile-name">客服小花</span>
            <span class="profile-status">
              <select v-model="agentStatus" class="status-select">
                <option value="online">在线</option>
                <option value="busy">忙碌</option>
                <option value="away">离开</option>
              </select>
            </span>
          </div>
        </div>
      </div>

      <div class="sidebar-filters">
        <div class="filter-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            :class="['filter-tab', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
            <span class="tab-count" v-if="tab.count">{{ tab.count }}</span>
          </button>
        </div>
        <el-input
          v-model="searchQuery"
          placeholder="搜索访客..."
          prefix-icon="Search"
          clearable
          class="search-input"
        />
      </div>

      <div class="session-list">
        <div
          v-for="session in filteredSessions"
          :key="session.id"
          :class="['session-item', { active: selectedSession?.id === session.id, unread: session.unread > 0 }]"
          @click="selectSession(session)"
        >
          <div class="session-avatar">
            <span class="avatar-name">{{ session.visitorName.slice(0, 1) }}</span>
            <span class="session-status-dot" :class="session.visitorStatus"></span>
          </div>
          <div class="session-info">
            <div class="session-header">
              <span class="visitor-name">{{ session.visitorName }}</span>
              <span class="session-time">{{ session.lastTime }}</span>
            </div>
            <div class="session-preview">
              <span class="last-message">{{ session.lastMessage }}</span>
              <span class="unread-badge" v-if="session.unread > 0">{{ session.unread }}</span>
            </div>
          </div>
        </div>
        <div v-if="filteredSessions.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <p>暂无会话</p>
        </div>
      </div>
    </aside>

    <main class="chat-area" v-if="selectedSession">
      <div class="chat-header">
        <div class="chat-info">
          <div class="visitor-details">
            <span class="visitor-name">{{ selectedSession.visitorName }}</span>
            <span class="visitor-meta">{{ selectedSession.device }} · {{ selectedSession.location }}</span>
          </div>
          <div class="session-tags">
            <el-tag v-for="tag in selectedSession.tags" :key="tag" size="small" closable>
              {{ tag }}
            </el-tag>
            <el-button text size="small" @click="addTag">
              <el-icon><Plus /></el-icon> 添加标签
            </el-button>
          </div>
        </div>
        <div class="chat-actions">
          <el-button>转接</el-button>
          <el-button>访客信息</el-button>
          <el-button type="primary">结束会话</el-button>
        </div>
      </div>

      <div class="chat-messages" ref="chatMessagesRef">
        <div
          v-for="(msg, index) in currentMessages"
          :key="index"
          :class="['message-wrapper', msg.type]"
        >
          <div class="message-time" v-if="showTime(index)">{{ msg.time }}</div>
          <div class="message-row">
            <div class="message-avatar" v-if="msg.type === 'agent'">
              <div class="avatar-inner">小花</div>
            </div>
            <div class="message-bubble-wrap">
              <div class="message-bubble" :class="msg.type">
                <div v-if="msg.type === 'system'" class="system-message">{{ msg.content }}</div>
                <div v-else>{{ msg.content }}</div>
              </div>
            </div>
            <div class="message-avatar" v-if="msg.type === 'visitor'">
              <div class="avatar-inner visitor">{{ selectedSession.visitorName.slice(0, 1) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <div class="quick-toolbar">
          <el-tooltip content="表情">
            <button class="toolbar-btn"><span>😀</span></button>
          </el-tooltip>
          <el-tooltip content="图片">
            <button class="toolbar-btn"><el-icon><Picture /></el-icon></button>
          </el-tooltip>
          <el-tooltip content="截图">
            <button class="toolbar-btn"><el-icon><Monitor /></el-icon></button>
          </el-tooltip>
          <el-tooltip content="文件">
            <button class="toolbar-btn"><el-icon><Folder /></el-icon></button>
          </el-tooltip>
          <el-tooltip content="快捷回复">
            <button class="toolbar-btn"><el-icon><ChatDotSquare /></el-icon></button>
          </el-tooltip>
          <div class="toolbar-divider"></div>
          <el-tooltip content="知识库检索">
            <button class="toolbar-btn"><el-icon><Search /></el-icon></button>
          </el-tooltip>
        </div>
        <div class="input-row">
          <el-input
            v-model="messageInput"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keydown.enter.meta="sendMessage"
            @keydown.enter.ctrl="sendMessage"
            class="message-textarea"
          />
        </div>
        <div class="input-actions">
          <div class="left-tools">
            <el-popover
              placement="top-start"
              :width="320"
              trigger="click"
            >
              <template #reference>
                <el-button text>
                  <el-icon><Tickets /></el-icon>
                  快捷回复
                </el-button>
              </template>
              <div class="quick-reply-panel">
                <div class="quick-reply-header">
                  <span>快捷回复库</span>
                  <el-button text size="small">管理</el-button>
                </div>
                <div class="quick-reply-list">
                  <div
                    v-for="reply in quickReplies"
                    :key="reply.id"
                    class="quick-reply-item"
                    @click="useQuickReply(reply)"
                  >
                    <div class="reply-tag">{{ reply.tag }}</div>
                    <div class="reply-content">{{ reply.content }}</div>
                  </div>
                </div>
              </div>
            </el-popover>
          </div>
          <el-button type="primary" @click="sendMessage" :disabled="!messageInput.trim()">
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </main>

    <main class="chat-area empty" v-else>
      <div class="empty-chat">
        <div class="empty-chat-icon">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
        </div>
        <h3>选择一个会话</h3>
        <p>从左侧列表选择一个访客会话开始对话</p>
      </div>
    </main>

    <aside class="info-panel">
      <div class="info-tabs">
        <button
          v-for="tab in infoTabs"
          :key="tab"
          :class="['info-tab', { active: activeInfoTab === tab }]"
          @click="activeInfoTab = tab"
        >
          {{ tab }}
        </button>
      </div>
      <div class="info-content">
        <div v-if="activeInfoTab === '访客信息'" class="visitor-info-panel">
          <div class="info-section">
            <h4>基本信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <label>姓名</label>
                <span>{{ selectedSession?.visitorName }}</span>
              </div>
              <div class="info-item">
                <label>设备</label>
                <span>{{ selectedSession?.device }}</span>
              </div>
              <div class="info-item">
                <label>位置</label>
                <span>{{ selectedSession?.location }}</span>
              </div>
              <div class="info-item">
                <label>来源</label>
                <span>{{ selectedSession?.source || '直接访问' }}</span>
              </div>
            </div>
          </div>
          <div class="info-section">
            <h4>会话统计</h4>
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-value">{{ selectedSession?.messageCount || 0 }}</span>
                <span class="stat-label">消息数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ selectedSession?.duration || '0分钟' }}</span>
                <span class="stat-label">会话时长</span>
              </div>
            </div>
          </div>
        </div>
        <div v-if="activeInfoTab === '快捷回复'" class="quick-reply-panel-sidebar">
          <el-button type="primary" class="add-reply-btn">
            <el-icon><Plus /></el-icon>
            新建回复
          </el-button>
          <div class="reply-category" v-for="cat in replyCategories" :key="cat.name">
            <div class="category-header">{{ cat.name }}</div>
            <div
              v-for="reply in cat.replies"
              :key="reply.id"
              class="reply-item"
              @click="useQuickReply(reply)"
            >
              <div class="reply-tag">{{ reply.tag }}</div>
              <div class="reply-content">{{ reply.content }}</div>
            </div>
          </div>
        </div>
        <div v-if="activeInfoTab === '会话历史'" class="history-panel">
          <div class="history-item" v-for="n in 5" :key="n">
            <div class="history-date">{{ new Date().toLocaleDateString('zh-CN') }} {{ n }}小时前</div>
            <div class="history-preview">
              访客：这个问题怎么解决...
            </div>
          </div>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus, Picture, Monitor, Folder, ChatDotSquare, Search, Tickets, Promotion } from '@element-plus/icons-vue'

const tabs = [
  { key: 'all', label: '全部', count: 12 },
  { key: 'pending', label: '待接入', count: 3 },
  { key: 'active', label: '进行中', count: 8 },
  { key: 'resolved', label: '已结束', count: 24 }
]

const activeTab = ref('all')
const searchQuery = ref('')
const selectedSession = ref<any>(null)
const messageInput = ref('')
const agentStatus = ref('online')
const activeInfoTab = ref('访客信息')
const chatMessagesRef = ref<HTMLElement>()

interface Message {
  type: 'visitor' | 'agent' | 'system'
  content: string
  time: string
}

const currentMessages = ref<Message[]>([
  { type: 'system', content: '会话已开始', time: '10:30' },
  { type: 'visitor', content: '你好，我想咨询一下产品报价', time: '10:30' },
  { type: 'agent', content: '您好！我是客服小花，很高兴为您服务。请告诉我您想了解的产品类型和用量需求，我会为您提供详细报价。', time: '10:31' },
  { type: 'visitor', content: '我们公司规模大概200人，想了解企业版的价格', time: '10:32' },
  { type: 'agent', content: '企业版适合200人规模的公司，我们提供定制化方案和专属服务。请留下您的联系方式，我们的市场人员会尽快与您联系为您提供详细报价。', time: '10:33' }
])

const sessions = ref([
  {
    id: '1',
    visitorName: '李明',
    visitorStatus: 'online',
    device: 'Windows',
    location: '北京',
    source: '官网',
    lastMessage: '想了解企业版的价格',
    lastTime: '刚刚',
    unread: 2,
    tags: ['潜在客户', '企业咨询'],
    messageCount: 12,
    duration: '15分钟'
  },
  {
    id: '2',
    visitorName: '王芳',
    visitorStatus: 'online',
    device: 'iPhone',
    location: '上海',
    source: 'App',
    lastMessage: '账号登录不了',
    lastTime: '5分钟前',
    unread: 1,
    tags: ['技术问题'],
    messageCount: 5,
    duration: '3分钟'
  },
  {
    id: '3',
    visitorName: '张伟',
    visitorStatus: 'away',
    device: 'MacOS',
    location: '深圳',
    source: '推广链接',
    lastMessage: '好的，谢谢',
    lastTime: '30分钟前',
    unread: 0,
    tags: [],
    messageCount: 20,
    duration: '45分钟'
  }
])

const quickReplies = ref([
  { id: '1', tag: '问候', content: '您好！我是客服，很高兴为您服务，请问有什么可以帮您？' },
  { id: '2', tag: '忙碌', content: '抱歉，当前咨询人数较多，请稍后再次尝试，或留下您的联系方式。' },
  { id: '3', tag: '结束', content: '感谢您的咨询，祝您生活愉快！如有问题随时联系我们。' },
  { id: '4', tag: '转接', content: '正在为您转接人工客服，请稍候...' }
])

const replyCategories = ref([
  {
    name: '通用',
    replies: [
      { id: '1', tag: '问候', content: '您好！我是客服，很高兴为您服务，请问有什么可以帮您？' },
      { id: '3', tag: '结束', content: '感谢您的咨询，祝您生活愉快！如有问题随时联系我们。' }
    ]
  },
  {
    name: '售前',
    replies: [
      { id: '4', tag: '报价', content: '请告诉我您的需求和联系方式，我们的市场人员会尽快为您提供详细报价。' }
    ]
  }
])

const infoTabs = ['访客信息', '快捷回复', '会话历史']

const filteredSessions = computed(() => {
  let list = sessions.value
  if (activeTab.value !== 'all') {
    if (activeTab.value === 'pending') list = list.filter(s => s.visitorStatus === 'online')
    if (activeTab.value === 'active') list = list.filter(s => s.unread > 0)
    if (activeTab.value === 'resolved') list = list.filter(s => s.unread === 0 && s.lastTime.includes('分钟前'))
  }
  if (searchQuery.value) {
    list = list.filter(s => s.visitorName.includes(searchQuery.value))
  }
  return list
})

function selectSession(session: any) {
  selectedSession.value = session
  session.unread = 0
}

function sendMessage() {
  if (!messageInput.value.trim()) return
  currentMessages.value.push({
    type: 'agent',
    content: messageInput.value,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  messageInput.value = ''
}

function useQuickReply(reply: any) {
  messageInput.value = reply.content
}

function addTag() {
  // placeholder
}

function showTime(index: number) {
  if (index === 0) return true
  const curr = currentMessages.value[index].time
  const prev = currentMessages.value[index - 1].time
  return curr !== prev
}
</script>

<style scoped>
.workbench-layout {
  display: grid;
  grid-template-columns: 280px 1fr 300px;
  height: 100vh;
  background: var(--color-surface);
}

.sidebar {
  background: white;
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 400;
  color: var(--color-text-primary);
  margin-bottom: 16px;
}

.agent-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-name {
  font-weight: 600;
  font-size: 14px;
}

.profile-status {
  font-size: 12px;
}

.status-select {
  border: none;
  background: transparent;
  font-size: 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
}

.sidebar-filters {
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  border-bottom: 1px solid var(--color-border);
}

.filter-tabs {
  display: flex;
  gap: 4px;
}

.filter-tab {
  flex: 1;
  padding: 6px 8px;
  border: none;
  background: transparent;
  font-size: 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.filter-tab:hover {
  background: var(--color-surface);
}

.filter-tab.active {
  background: var(--color-primary);
  color: white;
}

.tab-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
}

.filter-tab:not(.active) .tab-count {
  background: var(--color-surface);
  color: var(--color-text-muted);
}

.search-input {
  --el-input-border-radius: 8px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.session-item:hover {
  background: var(--color-surface);
}

.session-item.active {
  background: rgba(79, 70, 229, 0.08);
}

.session-item.unread .visitor-name {
  font-weight: 700;
}

.session-avatar {
  position: relative;
  flex-shrink: 0;
}

.avatar-name {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.session-status-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
}

.session-status-dot.online { background: #22c55e; }
.session-status-dot.away { background: #f59e0b; }
.session-status-dot.offline { background: #a8a29e; }

.session-info {
  flex: 1;
  min-width: 0;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.visitor-name {
  font-weight: 600;
  font-size: 14px;
}

.session-time {
  font-size: 11px;
  color: var(--color-text-muted);
}

.session-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-message {
  font-size: 13px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}

.unread-badge {
  background: var(--color-primary);
  color: white;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 10px;
  flex-shrink: 0;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text-muted);
}

.empty-icon {
  margin-bottom: 12px;
  opacity: 0.5;
}

.chat-area {
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  overflow: hidden;
}

.chat-area.empty {
  align-items: center;
  justify-content: center;
}

.empty-chat {
  text-align: center;
  color: var(--color-text-muted);
}

.empty-chat-icon {
  margin-bottom: 16px;
  opacity: 0.3;
}

.empty-chat h3 {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 400;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.chat-header {
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.chat-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.visitor-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.visitor-details .visitor-name {
  font-size: 16px;
  font-weight: 600;
}

.visitor-meta {
  font-size: 12px;
  color: var(--color-text-muted);
}

.session-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-time {
  text-align: center;
  font-size: 11px;
  color: var(--color-text-muted);
  padding: 8px 0;
}

.message-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.message-wrapper.user .message-row {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar-inner {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 600;
}

.avatar-inner.visitor {
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
}

.message-bubble-wrap {
  max-width: 65%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
}

.message-bubble.visitor {
  background: white;
  box-shadow: var(--shadow-sm);
  border-bottom-left-radius: 4px;
}

.message-bubble.agent {
  background: var(--color-primary);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-bubble.system {
  background: rgba(79, 70, 229, 0.1);
  color: var(--color-text-secondary);
  font-size: 12px;
  text-align: center;
  padding: 8px 16px;
  border-radius: 8px;
  align-self: center;
}

.chat-input-area {
  background: white;
  border-top: 1px solid var(--color-border);
  padding: 12px 24px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: var(--color-text-secondary);
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background: var(--color-surface);
  color: var(--color-primary);
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: var(--color-border);
  margin: 0 8px;
}

.input-row {
  width: 100%;
}

.message-textarea {
  width: 100%;
}

.message-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-tools {
  display: flex;
  gap: 8px;
}

.info-panel {
  background: white;
  border-left: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.info-tabs {
  display: flex;
  border-bottom: 1px solid var(--color-border);
}

.info-tab {
  flex: 1;
  padding: 14px 8px;
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all 0.2s;
}

.info-tab:hover {
  color: var(--color-primary);
}

.info-tab.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 600;
}

.info-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.info-section {
  margin-bottom: 24px;
}

.info-section h4 {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 12px;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.info-item span {
  font-size: 13px;
  font-weight: 500;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-item {
  background: var(--color-surface);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 11px;
  color: var(--color-text-muted);
}

.quick-reply-panel-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.add-reply-btn {
  width: 100%;
}

.reply-category {
  margin-bottom: 16px;
}

.category-header {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}

.reply-item {
  background: var(--color-surface);
  border-radius: 10px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 8px;
}

.reply-item:hover {
  background: rgba(79, 70, 229, 0.08);
}

.reply-tag {
  font-size: 10px;
  font-weight: 600;
  color: var(--color-primary);
  background: rgba(79, 70, 229, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 6px;
}

.reply-content {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.4;
}

.history-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.history-date {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-bottom: 4px;
}

.history-preview {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.quick-reply-panel {
  display: flex;
  flex-direction: column;
}

.quick-reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
}

.quick-reply-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 300px;
  overflow-y: auto;
}
</style>