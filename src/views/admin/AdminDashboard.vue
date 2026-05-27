<template>
  <div class="admin-layout">
    <header class="admin-header">
      <div class="header-left">
        <h1 class="admin-brand">智能客服系统</h1>
        <span class="admin-subtitle">管理后台</span>
      </div>
      <div class="header-right">
        <el-button text>
          <el-icon><Bell /></el-icon>
        </el-button>
        <div class="admin-avatar">管理员</div>
      </div>
    </header>

    <div class="admin-body">
      <nav class="admin-nav">
        <div class="nav-section">
          <div class="nav-section-title">客户服务</div>
          <a
            v-for="item in navItems.customer"
            :key="item.key"
            :class="['nav-item', { active: activeNav === item.key }]"
            @click="activeNav = item.key"
          >
            <component :is="item.icon" />
            <span>{{ item.label }}</span>
          </a>
        </div>
        <div class="nav-section">
          <div class="nav-section-title">系统管理</div>
          <a
            v-for="item in navItems.system"
            :key="item.key"
            :class="['nav-item', { active: activeNav === item.key }]"
            @click="activeNav = item.key"
          >
            <component :is="item.icon" />
            <span>{{ item.label }}</span>
          </a>
        </div>
      </nav>

      <main class="admin-content">
        <div v-if="activeNav === 'accounts'" class="content-panel">
          <div class="panel-header">
            <div>
              <h2 class="panel-title">客服账号管理</h2>
              <p class="panel-desc">管理客服人员的账号、权限和状态</p>
            </div>
            <el-button type="primary">
              <el-icon><Plus /></el-icon>
              新建账号
            </el-button>
          </div>
          <div class="toolbar">
            <el-input
              v-model="accountSearch"
              placeholder="搜索账号..."
              prefix-icon="Search"
              clearable
              style="width: 240px"
            />
            <div class="toolbar-gap"></div>
            <el-select v-model="statusFilter" placeholder="状态" style="width: 120px">
              <el-option label="全部" value="" />
              <el-option label="在线" value="online" />
              <el-option label="离线" value="offline" />
            </el-select>
          </div>
          <el-table :data="filteredAccounts" stripe class="data-table">
            <el-table-column prop="name" label="姓名" min-width="120">
              <template #default="{ row }">
                <div class="user-cell">
                  <div class="user-avatar" :style="{ background: row.avatar }">{{ row.name.slice(0, 1) }}</div>
                  <div class="user-info">
                    <span class="user-name">{{ row.name }}</span>
                    <span class="user-id">ID: {{ row.id }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="role" label="角色" width="120">
              <template #default="{ row }">
                <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'" size="small">
                  {{ row.role === 'admin' ? '管理员' : '客服' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <span class="status-indicator" :class="row.status">
                  {{ row.status === 'online' ? '在线' : '离线' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="todaySessions" label="今日接待" width="100" align="center" />
            <el-table-column prop="totalSessions" label="累计接待" width="100" align="center" />
            <el-table-column prop="rating" label="满意度" width="120">
              <template #default="{ row }">
                <div class="rating-cell">
                  <el-rate v-model="row.rating" disabled text-color="#f59e0b" />
                  <span class="rating-value">{{ row.rating.toFixed(1) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="joinedAt" label="入职时间" width="120" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button text size="small" @click="editAccount(row)">编辑</el-button>
                <el-button text size="small" type="danger" @click="deleteAccount(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-else-if="activeNav === 'faq'" class="content-panel">
          <div class="panel-header">
            <div>
              <h2 class="panel-title">FAQ 知识库</h2>
              <p class="panel-desc">管理常见问题和标准回复</p>
            </div>
            <el-button type="primary">
              <el-icon><Plus /></el-icon>
              新建 FAQ
            </el-button>
          </div>
          <div class="toolbar">
            <el-input
              v-model="faqSearch"
              placeholder="搜索问题..."
              prefix-icon="Search"
              clearable
              style="width: 300px"
            />
            <div class="toolbar-gap"></div>
            <el-select v-model="faqCategory" placeholder="分类" style="width: 140px">
              <el-option label="全部分类" value="" />
              <el-option label="产品咨询" value="product" />
              <el-option label="技术支持" value="tech" />
              <el-option label="账单相关" value="billing" />
            </el-select>
          </div>
          <div class="faq-list">
            <div
              v-for="faq in filteredFaqs"
              :key="faq.id"
              class="faq-item"
            >
              <div class="faq-category-tag" :style="{ background: faq.categoryColor }">
                {{ faq.category }}
              </div>
              <div class="faq-content">
                <div class="faq-question">{{ faq.question }}</div>
                <div class="faq-answer">{{ faq.answer }}</div>
              </div>
              <div class="faq-meta">
                <span class="faq-views">{{ faq.views }}次浏览</span>
                <span class="faq-used">{{ faq.used }}次使用</span>
              </div>
              <div class="faq-actions">
                <el-button text size="small" @click="editFaq(faq)">编辑</el-button>
                <el-button text size="small" type="primary" @click="useFaq(faq)">使用</el-button>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="activeNav === 'statistics'" class="content-panel">
          <div class="panel-header">
            <div>
              <h2 class="panel-title">对话统计报表</h2>
              <p class="panel-desc">查看客服工作数据和会话分析</p>
            </div>
            <div class="date-range-picker">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              />
            </div>
          </div>

          <div class="stats-overview">
            <div class="stat-card">
              <div class="stat-icon" style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)">
                <el-icon><ChatDotSquare /></el-icon>
              </div>
              <div class="stat-data">
                <span class="stat-value">1,284</span>
                <span class="stat-label">总会话数</span>
              </div>
              <div class="stat-trend up">
                <el-icon><Top /></el-icon>
                +12.5%
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: linear-gradient(135deg, #22c55e 0%, #10b981 100%)">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-data">
                <span class="stat-value">856</span>
                <span class="stat-label">独立访客</span>
              </div>
              <div class="stat-trend up">
                <el-icon><Top /></el-icon>
                +8.2%
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%)">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-data">
                <span class="stat-value">4.2分钟</span>
                <span class="stat-label">平均响应时长</span>
              </div>
              <div class="stat-trend down">
                <el-icon><Bottom /></el-icon>
                -15.3%
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon" style="background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%)">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-data">
                <span class="stat-value">96.8%</span>
                <span class="stat-label">访客满意度</span>
              </div>
              <div class="stat-trend up">
                <el-icon><Top /></el-icon>
                +2.1%
              </div>
            </div>
          </div>

          <div class="chart-grid">
            <div class="chart-card">
              <h3>日会话量趋势</h3>
              <div class="chart-placeholder">
                <div class="chart-bars">
                  <div class="bar" v-for="n in 14" :key="n" :style="{ height: Math.random() * 60 + 20 + '%' }"></div>
                </div>
              </div>
            </div>
            <div class="chart-card">
              <h3>会话来源分布</h3>
              <div class="chart-placeholder donut">
                <div class="donut-chart">
                  <div class="donut-segment" style="--p: 45; --c: #4f46e5"></div>
                  <div class="donut-segment" style="--p: 30; --c: #22c55e"></div>
                  <div class="donut-segment" style="--p: 15; --c: #f59e0b"></div>
                  <div class="donut-segment" style="--p: 10; --c: #a8a29e"></div>
                </div>
                <div class="donut-legend">
                  <div class="legend-item"><span style="background: #4f46e5"></span>官网 45%</div>
                  <div class="legend-item"><span style="background: #22c55e"></span>App 30%</div>
                  <div class="legend-item"><span style="background: #f59e0b"></span>推广 15%</div>
                  <div class="legend-item"><span style="background: #a8a29e"></span>其他 10%</div>
                </div>
              </div>
            </div>
          </div>

          <div class="rankings">
            <div class="ranking-card">
              <h3>客服工作量排行</h3>
              <div class="ranking-list">
                <div v-for="(agent, index) in agentRankings" :key="agent.name" class="ranking-item">
                  <span class="rank-num" :class="{ top3: index < 3 }">{{ index + 1 }}</span>
                  <div class="rank-avatar" :style="{ background: agent.avatar }">{{ agent.name.slice(0, 1) }}</div>
                  <div class="rank-info">
                    <span class="rank-name">{{ agent.name }}</span>
                    <span class="rank-sessions">{{ agent.sessions }} 次接待</span>
                  </div>
                  <div class="rank-score">
                    <el-rate v-model="agent.rating" disabled size="small" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="content-placeholder">
          <div class="placeholder-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 16v-4M12 8h.01"/>
            </svg>
          </div>
          <h3>选择一个菜单</h3>
          <p>从左侧导航选择一个管理功能</p>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  Bell, Plus, Search, ChatDotSquare, User, Clock, Star, Top, Bottom,
  Message, Setting, DataLine, Folder, Document, Tools
} from '@element-plus/icons-vue'

const activeNav = ref('statistics')
const accountSearch = ref('')
const statusFilter = ref('')
const faqSearch = ref('')
const faqCategory = ref('')
const dateRange = ref('')

const navItems = {
  customer: [
    { key: 'accounts', label: '客服账号', icon: User },
    { key: 'faq', label: 'FAQ 知识库', icon: Document },
    { key: 'statistics', label: '对话统计', icon: DataLine }
  ],
  system: [
    { key: 'skills', label: '技能组管理', icon: Tools },
    { key: 'schedule', label: '接待时段', icon: Clock },
    { key: 'transfer', label: '转接规则', icon: Message }
  ]
}

const accounts = ref([
  { id: 'A001', name: '张晓华', role: 'admin', status: 'online', todaySessions: 23, totalSessions: 1842, rating: 4.9, joinedAt: '2024-01-15', avatar: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)' },
  { id: 'A002', name: '李婷婷', role: 'agent', status: 'online', todaySessions: 18, totalSessions: 956, rating: 4.7, joinedAt: '2024-03-20', avatar: 'linear-gradient(135deg, #22c55e 0%, #10b981 100%)' },
  { id: 'A003', name: '王建国', role: 'agent', status: 'offline', todaySessions: 0, totalSessions: 2341, rating: 4.8, joinedAt: '2023-11-08', avatar: 'linear-gradient(135deg, #f59e0b 0%, #f97316 100%)' },
  { id: 'A004', name: '陈美玲', role: 'agent', status: 'online', todaySessions: 12, totalSessions: 567, rating: 4.6, joinedAt: '2024-06-01', avatar: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)' },
  { id: 'A005', name: '刘伟', role: 'agent', status: 'offline', todaySessions: 0, totalSessions: 789, rating: 4.5, joinedAt: '2024-04-12', avatar: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)' }
])

const filteredAccounts = computed(() => {
  return accounts.value.filter(a => {
    const matchSearch = !accountSearch.value || a.name.includes(accountSearch.value)
    const matchStatus = !statusFilter.value || a.status === statusFilter.value
    return matchSearch && matchStatus
  })
})

const faqs = ref([
  { id: 'F001', category: '产品咨询', categoryColor: '#4f46e5', question: '如何购买企业版套餐？', answer: '企业版需要联系我们的销售团队，请留下您的联系方式和需求描述，我们会尽快与您联系。', views: 1234, used: 567 },
  { id: 'F002', category: '技术支持', categoryColor: '#22c55e', question: '账号无法登录怎么办？', answer: '请尝试以下步骤：1. 清除浏览器缓存 2. 使用密码找回功能 3. 联系技术支持。', views: 2341, used: 892 },
  { id: 'F003', category: '账单相关', categoryColor: '#f59e0b', question: '如何查看账单明细？', answer: '登录后进入"账户设置" -> "账单管理"，可以查看所有历史账单和发票下载。', views: 876, used: 234 },
  { id: 'F004', category: '产品咨询', categoryColor: '#4f46e5', question: '免费版有哪些限制？', answer: '免费版支持最多5个客服账号，每日100次会话，提供基础统计报表。', views: 1567, used: 423 }
])

const filteredFaqs = computed(() => {
  return faqs.value.filter(f => {
    const matchSearch = !faqSearch.value || f.question.includes(faqSearch.value)
    const matchCat = !faqCategory.value || f.category.toLowerCase().includes(faqCategory.value)
    return matchSearch && matchCat
  })
})

const agentRankings = ref([
  { name: '张晓华', sessions: 156, rating: 4.9, avatar: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)' },
  { name: '王建国', sessions: 143, rating: 4.8, avatar: 'linear-gradient(135deg, #f59e0b 0%, #f97316 100%)' },
  { name: '李婷婷', sessions: 138, rating: 4.7, avatar: 'linear-gradient(135deg, #22c55e 0%, #10b981 100%)' },
  { name: '陈美玲', sessions: 121, rating: 4.6, avatar: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)' },
  { name: '刘伟', sessions: 98, rating: 4.5, avatar: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)' }
])

function editAccount(row: any) { console.log('edit', row) }
function deleteAccount(row: any) { console.log('delete', row) }
function editFaq(faq: any) { console.log('edit faq', faq) }
function useFaq(faq: any) { console.log('use faq', faq) }
</script>

<style scoped>
.admin-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--color-surface);
}

.admin-header {
  height: 64px;
  background: white;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.admin-brand {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 400;
  color: var(--color-text-primary);
}

.admin-subtitle {
  font-size: 13px;
  color: var(--color-text-muted);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-avatar {
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

.admin-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.admin-nav {
  width: 220px;
  background: white;
  border-right: 1px solid var(--color-border);
  padding: 20px 12px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  overflow-y: auto;
}

.nav-section-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 0 12px;
  margin-bottom: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
}

.nav-item:hover {
  background: var(--color-surface);
  color: var(--color-primary);
}

.nav-item.active {
  background: rgba(79, 70, 229, 0.1);
  color: var(--color-primary);
  font-weight: 600;
}

.admin-content {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
}

.content-panel {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: var(--shadow-sm);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.panel-title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 400;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.panel-desc {
  font-size: 13px;
  color: var(--color-text-muted);
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.toolbar-gap {
  flex: 1;
}

.data-table {
  border-radius: 12px;
  overflow: hidden;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 13px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 600;
  font-size: 14px;
}

.user-id {
  font-size: 11px;
  color: var(--color-text-muted);
}

.status-indicator {
  font-size: 12px;
  font-weight: 500;
}

.status-indicator.online { color: #22c55e; }
.status-indicator.offline { color: #a8a29e; }

.rating-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-value {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.faq-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: var(--color-surface);
  border-radius: 12px;
  transition: all 0.2s;
}

.faq-item:hover {
  background: rgba(79, 70, 229, 0.04);
}

.faq-category-tag {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.faq-content {
  flex: 1;
  min-width: 0;
}

.faq-question {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}

.faq-answer {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.faq-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: right;
  flex-shrink: 0;
}

.faq-views, .faq-used {
  font-size: 11px;
  color: var(--color-text-muted);
}

.faq-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-data {
  flex: 1;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-muted);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 6px;
}

.stat-trend.up {
  color: #22c55e;
  background: rgba(34, 197, 94, 0.1);
}

.stat-trend.down {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.chart-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 20px;
  margin-bottom: 28px;
}

.chart-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  box-shadow: var(--shadow-sm);
}

.chart-card h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 16px;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 8px;
  padding: 0 8px;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  width: 100%;
  height: 100%;
}

.bar {
  flex: 1;
  background: linear-gradient(to top, #4f46e5, #818cf8);
  border-radius: 6px 6px 0 0;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.bar:hover { opacity: 1; }

.chart-placeholder.donut {
  align-items: center;
  justify-content: center;
  gap: 32px;
}

.donut-chart {
  width: 140px;
  height: 140px;
  position: relative;
}

.donut-segment {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: conic-gradient(
    var(--c) calc(var(--p) * 1%),
    transparent calc(var(--p) * 1%)
  );
  mask: radial-gradient(transparent 55%, black 56%);
  -webkit-mask: radial-gradient(transparent 55%, black 56%);
}

.donut-legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.legend-item span {
  width: 12px;
  height: 12px;
  border-radius: 4px;
}

.rankings {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.ranking-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  box-shadow: var(--shadow-sm);
}

.ranking-card h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 16px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  transition: background 0.2s;
}

.ranking-item:hover {
  background: var(--color-surface);
}

.rank-num {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-muted);
  background: var(--color-surface);
}

.rank-num.top3 {
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  color: white;
}

.rank-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 13px;
}

.rank-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.rank-name {
  font-weight: 600;
  font-size: 14px;
}

.rank-sessions {
  font-size: 12px;
  color: var(--color-text-muted);
}

.rank-score :deep(.el-rate) {
  gap: 2px;
}

.content-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--color-text-muted);
  text-align: center;
}

.placeholder-icon {
  margin-bottom: 16px;
  opacity: 0.3;
}

.content-placeholder h3 {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 400;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.date-range-picker {
  display: flex;
  gap: 12px;
}
</style>