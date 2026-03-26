<template>
  <div class="book-detail">
    <div class="container">
      <!-- 返回导航 -->
      <div class="back-nav">
        <router-link to="/" class="back-link">
          <span class="back-arrow">�?/span> 返回首页
        </router-link>
      </div>

      <!-- 加载状�?-->
      <div v-if="loading" class="loading-state">
        <div class="loading-dots">
          <span></span><span></span><span></span>
        </div>
        <p>加载�?..</p>
      </div>

      <!-- 错误状�?-->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">📖</div>
        <p>{{ error }}</p>
        <button @click="loadBookData($route.params.id)" class="retry-btn">重试</button>
      </div>

      <template v-else>
        <!-- 书籍信息卡片 -->
        <div class="book-card">
          <div class="book-cover">
            <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title">
            <div v-else class="cover-placeholder">
              <span>{{ book.title?.charAt(0) || '�? }}</span>
            </div>
          </div>
          
          <div class="book-info">
            <h1 class="book-title">{{ book.title }}</h1>
            <p class="book-author">{{ book.author }}</p>
            
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">ISBN</span>
                <span class="info-value">{{ book.isbn || '暂无' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">出版�?/span>
                <span class="info-value">{{ book.publisher || '暂无' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">成色</span>
                <span class="condition-badge" :class="book.condition">
                  {{ getConditionText(book.condition) }}
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">状�?/span>
                <span class="status-badge" :class="book.status">
                  {{ book.status === 'available' ? '可认�? : '已借出' }}
                </span>
              </div>
            </div>
            
            <div class="book-description">
              <h3>内容简�?/h3>
              <p>{{ book.description || '暂无简�? }}</p>
            </div>
          </div>
          
          <!-- 二维码区�?-->
          <div class="qrcode-area" v-if="book.qrCodeData">
            <div class="qrcode-box">
              <canvas ref="bookQrCanvas" width="120" height="120"></canvas>
              <p class="qrcode-hint">扫码借阅/归还</p>
              <button @click="downloadQRCode" class="download-btn">保存二维�?/button>
            </div>
          </div>
        </div>

        <!-- 书籍漂流轨迹 - 保留完整功能 -->
        <div class="trajectory-section">
          <div class="section-header">
            <h2>📊 书籍漂流轨迹</h2>
            <span class="section-subtitle">从捐赠到现在的完整历�?/span>
          </div>
          <div id="trajectory-chart" ref="chartRef" class="chart-container"></div>
          <div class="trajectory-timeline">
            <div v-for="(record, index) in trajectoryRecords" :key="index" class="timeline-item">
              <div class="timeline-marker" :class="getTimelineClass(record.action)"></div>
              <div class="timeline-content">
                <span class="timeline-time">{{ record.time }}</span>
                <span class="timeline-action" :class="getActionClass(record.action)">{{ record.action }}</span>
                <span class="timeline-user">{{ record.user }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 读书笔记 -->
        <div class="notes-section">
          <div class="notes-header">
            <h2>📝 读书笔记</h2>
            <select v-model="sortOrder" class="sort-select">
              <option value="desc">最新优�?/option>
              <option value="asc">最早优�?/option>
            </select>
          </div>

          <div v-if="notes.length === 0" class="empty-notes">
            <p>还没有人留下笔记，写下第一条吧~</p>
          </div>

          <div v-else class="notes-list">
            <div v-for="note in sortedNotes" :key="note.id" class="note-card">
              <div class="note-header">
                <div class="note-user">
                  <div class="user-avatar" v-if="note.userAvatar">
                    <img :src="note.userAvatar" :alt="note.userName" class="avatar-img">
                  </div>
                 <div class="user-avatar" v-else>
                  {{ note.userName?.charAt(0) || 'U' }}
                 </div>
                  <span class="user-name">{{ note.userName || note.user }}</span>
                </div>
                <span class="note-time">{{ formatDate(note.createdAt || note.time) }}</span>
              </div>

              <div v-if="!note.isEditing" class="note-content">{{ note.content }}</div>
              
              <div v-else class="note-edit">
                <textarea v-model="note.content" rows="3" maxlength="500"></textarea>
                <div class="edit-actions">
                  <button @click="saveNote(note)" class="save-btn" :disabled="note.content.length > 500">保存</button>
                  <button @click="cancelEdit(note)" class="cancel-btn">取消</button>
                </div>
              </div>

              <div class="note-footer">
                <button @click="toggleLike(note)" class="like-btn" :class="{ liked: note.liked }">
                  <span>{{ note.liked ? '❤️' : '🤍' }}</span>
                  <span>{{ note.likes || 0 }}</span>
                </button>
                
                <div v-if="isCurrentUser(note)" class="note-actions">
                  <button @click="editNote(note)" class="edit-btn">编辑</button>
                  <button @click="deleteNote(note)" class="delete-btn">删除</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 添加笔记 -->
          <div class="add-note">
            <h3>写下你的想法</h3>
            <textarea 
              v-model="newNote" 
              rows="4" 
              placeholder="分享你的读书心得、感悟或摘抄..."
              maxlength="500"
            ></textarea>
            <div class="word-count">
              <span :class="{ exceed: newNote.length > 500 }">{{ newNote.length }}/500</span>
            </div>
            <button @click="submitNote" class="submit-btn" :disabled="!newNote.trim() || newNote.length > 500">
              发布笔记
            </button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'
import QRCode from 'qrcode'
import { ElMessage } from 'element-plus'
export default {
  data() {
    return {
      book: null,
      trajectoryRecords: [],
      notes: [],
      loading: true,
      error: null,
      sortOrder: 'desc',
      newNote: '',
      chart: null,
      apiBaseUrl: 'import.meta.env.VITE_API_BASE_URL',
      currentUser: null,
      handleResize: null,
      currentUserAvatar:''
    }
  },
  computed: {
    sortedNotes() {
      return [...this.notes].sort((a, b) => {
        const timeA = new Date(a.createdAt || a.time).getTime()
        const timeB = new Date(b.createdAt || b.time).getTime()
        return this.sortOrder === 'desc' ? timeB - timeA : timeA - timeB
      })
    }
  },
  created() {
    this.getCurrentUser()
    const bookId = this.$route.params.id
    if (bookId) this.loadBookData(bookId)
  },
  watch: {
    '$route.params.id': {
      handler(newId) { if (newId) this.loadBookData(newId) },
      immediate: true
    }
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
    if (this.handleResize) {
      window.removeEventListener('resize', this.handleResize)
    }
  },
  methods: {
    getConditionText(condition) {
      const map = {
        new: '崭新',
        good: '良好',
        normal: '正常',
        worn: '破旧'
      }
      return map[condition] || condition || '未标�?
    },
    getCurrentUser() {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        try {
          this.currentUser = JSON.parse(userInfoStr)
          this.currentUserAvatar = this.currentUser.avatarUrl ||''
        } catch (e) {}
      }
    },
   
    isCurrentUser(note) {
      return this.currentUser && note.userId === this.currentUser.id
    },
    
    formatDate(dateStr) {
      if (!dateStr) return ''
      try {
        const date = new Date(dateStr)
        return `${date.getMonth() + 1}�?{date.getDate()}日`
      } catch {
        return dateStr
      }
    },
    
    getTimelineClass(action) {
      switch(action) {
        case '捐赠': return 'marker-donate'
        case '认领': return 'marker-borrow'
        case '归还': return 'marker-return'
        default: return 'marker-other'
      }
    },
    
    getActionClass(action) {
      switch(action) {
        case '捐赠': return 'action-donate'
        case '认领': return 'action-borrow'
        case '归还': return 'action-return'
        default: return ''
      }
    },
    
    async loadBookData(bookId) {
      this.loading = true
      this.error = null
      
      if (this.chart) {
        this.chart.dispose()
        this.chart = null
      }
      
      try {
        const response = await axios.get(`${this.apiBaseUrl}/books/${bookId}`)
        if (response.data) {
          this.book = response.data
          await this.loadBookTrajectory(bookId)
          await this.loadBookNotes(bookId)
          
          this.$nextTick(() => {
            setTimeout(() => {
              this.initChart()
              this.generateBookQRCode()
            }, 100)
          })
        } else {
          this.error = '书籍不存�?
        }
      } catch (err) {
        this.error = '加载失败，请重试'
        this.useMockData(bookId)
      } finally {
        this.loading = false
      }
    },
    
    async loadBookTrajectory(bookId) {
      try {
        const response = await axios.get(`${this.apiBaseUrl}/books/${bookId}/trajectory`)
        this.trajectoryRecords = response.data || []
      } catch {
        this.trajectoryRecords = []
      }
    },
    
    async loadBookNotes(bookId) {
  try {
    const response = await axios.get(`${this.apiBaseUrl}/notes/book/${bookId}`)
    console.log('笔记响应:', response.data)
    
    if (response.data && response.data.success) {
      this.notes = response.data.data.map(note => ({
        ...note,
        isEditing: false,
        liked: note.liked || false,
        userAvatar: note.userAvatar || '',
        userId: note.userId
      }))
    } else {
      this.notes = []
    }
  } catch (err) {
    console.error('加载笔记失败:', err)
    this.notes = []
  }
},
    
    useMockData(bookId) {
      this.book = {
        id: bookId,
        title: '瓦尔登湖',
        author: '亨利·梭罗',
        isbn: '9787532767890',
        publisher: '上海译文出版�?,
        description: '《瓦尔登湖》是美国作家梭罗独居瓦尔登湖畔的记录，描绘了他两年多时间里的所见、所闻和所思�?,
        status: 'available',
        qrCodeData: JSON.stringify({ bookId: 'BOOK1', title: '瓦尔登湖', donor: '林同�? })
      }
      this.trajectoryRecords = [
        { time: '2024-03-01', action: '捐赠', user: '林同�? },
        { time: '2024-03-15', action: '认领', user: '陈同�? },
        { time: '2024-04-10', action: '归还', user: '陈同�? }
      ]
      this.notes = []
    },
    
    generateBookQRCode() {
      if (!this.book?.qrCodeData) return
      const canvas = this.$refs.bookQrCanvas
      if (!canvas) return
      
      let qrString = this.book.qrCodeData
      if (typeof this.book.qrCodeData === 'object') {
        qrString = JSON.stringify(this.book.qrCodeData)
      }
      
      QRCode.toCanvas(canvas, qrString, { width: 120, margin: 1 })
        .catch(err => console.error('二维码生成失�?', err))
    },
    
    downloadQRCode() {
      const canvas = this.$refs.bookQrCanvas
      if (!canvas) return
      const link = document.createElement('a')
      link.download = `${this.book.title}_二维�?png`
      link.href = canvas.toDataURL('image/png')
      link.click()
    },
    
    initChart() {
      if (!this.$refs.chartRef || !this.trajectoryRecords || this.trajectoryRecords.length === 0) {
        return
      }
      
      if (this.chart) {
        this.chart.dispose()
        this.chart = null
      }
      
      try {
        this.chart = echarts.init(this.$refs.chartRef)
        
        const dates = this.trajectoryRecords.map(record => record.time)
        const actions = this.trajectoryRecords.map(record => {
          switch(record.action) {
            case '捐赠': return 3
            case '认领': return 2
            case '归还': return 1
            default: return 2
          }
        })
        
        const option = {
          title: { 
            text: '书籍漂流时间�?,
            left: 'center',
            top: 0,
            textStyle: {
              fontSize: 14,
              fontWeight: 'normal',
              color: '#5a6e5c'
            }
          },
          tooltip: {
            trigger: 'axis',
            formatter: (params) => {
              const index = params[0].dataIndex
              const record = this.trajectoryRecords[index]
              return `${record.time}<br/>${record.action}: ${record.user}`
            }
          },
          grid: {
            left: '10%',
            right: '5%',
            bottom: '15%',
            top: '15%',
            containLabel: true
          },
          xAxis: [{
            type: 'category',
            data: dates,
            axisLine: { lineStyle: { color: '#d4e0d8', width: 1 } },
            axisTick: { show: false },
            axisLabel: {
              formatter: (value, index) => {
                const record = this.trajectoryRecords[index]
                return `${record.user}\n${value}`
              },
              interval: 0,
              rotate: 30,
              fontSize: 10,
              color: '#8b9a8e'
            }
          }],
          yAxis: [{
            type: 'value',
            min: 0,
            max: 4,
            interval: 1,
            axisLine: { show: false },
            axisTick: { show: false },
            axisLabel: {
              formatter: (value) => {
                switch(value) {
                  case 3: return '捐赠'
                  case 2: return '借出'
                  case 1: return '归还'
                  default: return ''
                }
              },
              fontSize: 11,
              color: '#b8c4b0'
            },
            splitLine: { 
              lineStyle: { type: 'dashed', color: '#efebe6' } 
            }
          }],
          series: [{
            data: actions,
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: { width: 2, color: '#8fc1b0' },
            itemStyle: { color: '#8fc1b0' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(143, 193, 176, 0.3)' },
                { offset: 1, color: 'rgba(143, 193, 176, 0.05)' }
              ])
            }
          }]
        }
        
        this.chart.setOption(option)
        
        if (this.handleResize) {
          window.removeEventListener('resize', this.handleResize)
        }
        
        this.handleResize = () => {
          if (this.chart) {
            this.chart.resize()
          }
        }
        window.addEventListener('resize', this.handleResize)
        
      } catch (error) {
        console.error('图表初始化失�?', error)
      }
    },
    
    async toggleLike(note) {
      try {
        const response = await axios.post(`${this.apiBaseUrl}/notes/like`, {
          noteId: note.id,
          userId: this.currentUser?.id || 1
        })
        if (response.data.success) {
          note.liked = response.data.liked
          note.likes += note.liked ? 1 : -1
        }
      } catch {
        note.liked = !note.liked
        note.likes += note.liked ? 1 : -1
      }
    },
    
    editNote(note) {
      note.isEditing = true
      note.backupContent = note.content
    },
    
    async saveNote(note) {
      if (!note.content.trim()) return
      try {
        const response = await axios.put(`${this.apiBaseUrl}/notes/${note.id}`, {
          content: note.content,
          userId: this.currentUser?.id || 1
        })
        if (response.data.success) {
          note.isEditing = false
          delete note.backupContent
        }
      } catch {
        note.isEditing = false
      }
    },
    
    cancelEdit(note) {
      note.content = note.backupContent || note.content
      note.isEditing = false
      delete note.backupContent
    },
    
    async deleteNote(note) {
      if (!confirm('确定删除这条笔记吗？')) return
      try {
        const response = await axios.delete(`${this.apiBaseUrl}/notes/${note.id}`, {
          params: { userId: this.currentUser?.id || 1 }
        })
        if (response.data.success) {
          this.notes = this.notes.filter(n => n.id !== note.id)
        }
      } catch {
        this.notes = this.notes.filter(n => n.id !== note.id)
      }
    },
    
    async submitNote() {
  if (!this.newNote.trim() || this.newNote.length > 500) return
  
  try {
    const response = await axios.post(`${this.apiBaseUrl}/notes`, {
      bookId: this.book.id,
      userId: this.currentUser?.id || 1,
      userName: this.currentUser?.name || '匿名',
      content: this.newNote
    })
    
    console.log('发布笔记响应:', response.data)
    
    if (response.data && response.data.success) {
      const newNote = response.data.data
      this.notes.unshift({
        id: newNote.id,
        userId: this.currentUser?.id || 1,
        userName: this.currentUser?.name || '匿名',
        userAvatar: newNote.userAvatar || this.currentUserAvatar,
        content: this.newNote,
        likes: 0,
        liked: false,
        createdAt: new Date().toISOString(),
        isEditing: false,
        backupContent: null
      })
      this.newNote = ''
      
      window.dispatchEvent(new CustomEvent('stats-updated', { 
        detail: { userId: this.currentUser?.id || 1 }
      }))
      
      ElMessage.success('笔记发布成功')
    } else {
      ElMessage.error(response.data?.message || '发布失败')
    }
  } catch (err) {
    console.error('提交失败:', err)
    ElMessage.error('提交失败，请重试')
  }
}
  }
}
</script>

<style scoped>
.book-detail {
  background: #faf8f4;
  min-height: 100vh;
  padding: 32px 0;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

.back-nav {
  margin-bottom: 24px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #8b9a8e;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.back-link:hover {
  color: #8fc1b0;
}

/* 书籍卡片 */
.book-card {
  background: white;
  border-radius: 28px;
  padding: 32px;
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02);
  border: 1px solid #efebe6;
  margin-bottom: 24px;
}

.book-cover {
  width: 200px;
  height: 260px;
  flex-shrink: 0;
  background: #f5f0e8;
  border-radius: 16px;
  overflow: hidden;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f0ec, #dde9e2);
  font-size: 64px;
  font-weight: 500;
  color: #8fc1b0;
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 28px;
  font-weight: 500;
  color: #2c5a4f;
  margin: 0 0 8px 0;
}

.book-author {
  font-size: 16px;
  color: #8b9a8e;
  margin-bottom: 24px;
}

.info-list {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #efebe6;
}

.info-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.info-label {
  font-size: 13px;
  color: #b8c4b0;
}

.info-value {
  font-size: 14px;
  color: #5a6e5c;
}

condition-badge {
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.condition-badge.new {
  background: #e8f0ec;
  color: #8fc1b0;
}

.condition-badge.good {
  background: #e8f0ec;
  color: #8fc1b0;
}

.condition-badge.normal {
  background: #f5f0e8;
  color: #c9b6a0;
}

.condition-badge.worn {
  background: #fef0ec;
  color: #e8a4a4;
}
.status-badge {
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
}

.status-badge.available {
  background: #e8f0ec;
  color: #8fc1b0;
}

.status-badge.borrowed {
  background: #f5f0e8;
  color: #c9b6a0;
}

.book-description h3 {
  font-size: 16px;
  font-weight: 500;
  color: #5a6e5c;
  margin: 0 0 12px 0;
}

.book-description p {
  font-size: 14px;
  line-height: 1.6;
  color: #6c826e;
  margin: 0;
}

/* 二维码区�?*/
.qrcode-area {
  flex-shrink: 0;
  text-align: center;
}

.qrcode-box {
  background: #faf8f4;
  padding: 16px;
  border-radius: 20px;
  text-align: center;
}

.qrcode-box canvas {
  background: white;
  padding: 8px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.qrcode-hint {
  font-size: 11px;
  color: #b8c4b0;
  margin: 8px 0;
}

.download-btn {
  background: none;
  border: 1px solid #e2e6e0;
  padding: 6px 16px;
  border-radius: 30px;
  font-size: 12px;
  color: #8fc1b0;
  cursor: pointer;
  transition: all 0.2s;
}

.download-btn:hover {
  background: #e8f0ec;
  border-color: #8fc1b0;
}

/* 漂流轨迹区域 - 完整保留 */
.trajectory-section {
  background: white;
  border-radius: 28px;
  padding: 28px 32px;
  margin-bottom: 24px;
  border: 1px solid #efebe6;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 500;
  color: #2c5a4f;
  margin: 0;
}

.section-subtitle {
  font-size: 12px;
  color: #b8c4b0;
}

.chart-container {
  width: 100%;
  height: 300px;
  margin: 20px 0;
}

.trajectory-timeline {
  margin-top: 20px;
  padding: 0 12px;
}

.timeline-item {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  position: relative;
  padding-left: 20px;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 16px;
  bottom: -16px;
  width: 2px;
  background: linear-gradient(to bottom, #e2e6e0, #f5f0e8);
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-marker {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  z-index: 1;
}

.marker-donate {
  background: #c9b6a0;
  box-shadow: 0 0 0 3px rgba(201, 182, 160, 0.15);
}

.marker-borrow {
  background: #8fc1b0;
  box-shadow: 0 0 0 3px rgba(143, 193, 176, 0.15);
}

.marker-return {
  background: #b8c4b0;
  box-shadow: 0 0 0 3px rgba(184, 196, 176, 0.15);
}

.timeline-content {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.timeline-time {
  font-size: 13px;
  color: #b8c4b0;
  min-width: 90px;
}

.timeline-action {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.action-donate {
  background: #f5f0e8;
  color: #c9b6a0;
}

.action-borrow {
  background: #e8f0ec;
  color: #8fc1b0;
}

.action-return {
  background: #efebe6;
  color: #b8c4b0;
}

.timeline-user {
  font-size: 13px;
  color: #5a6e5c;
}

/* 笔记区域 */
.notes-section {
  background: white;
  border-radius: 28px;
  padding: 28px 32px;
  border: 1px solid #efebe6;
}

.notes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.notes-header h2 {
  font-size: 20px;
  font-weight: 500;
  color: #2c5a4f;
  margin: 0;
}

.sort-select {
  padding: 6px 12px;
  border: 1px solid #e2e6e0;
  border-radius: 30px;
  background: white;
  font-size: 13px;
  color: #5a6e5c;
  cursor: pointer;
}

.empty-notes {
  text-align: center;
  padding: 48px;
  color: #b8c4b0;
  background: #faf8f4;
  border-radius: 20px;
}

.notes-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 32px;
}

.note-card {
  background: #faf8f4;
  border-radius: 20px;
  padding: 20px;
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.note-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #e8f0ec, #dde9e2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  color: #8fc1b0;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #5a6e5c;
}

.note-time {
  font-size: 12px;
  color: #b8c4b0;
}

.note-content {
  font-size: 14px;
  line-height: 1.6;
  color: #6c826e;
  margin-bottom: 16px;
}

.note-edit textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e2e6e0;
  border-radius: 16px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  margin-bottom: 12px;
  background: white;
}

.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.like-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  font-size: 14px;
  color: #b8c4b0;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 30px;
  transition: all 0.2s;
}

.like-btn.liked {
  color: #e8a4a4;
}

.note-actions {
  display: flex;
  gap: 12px;
}

.edit-btn, .delete-btn {
  background: none;
  border: none;
  font-size: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 30px;
  transition: all 0.2s;
}

.edit-btn {
  color: #8fc1b0;
}

.edit-btn:hover {
  background: #e8f0ec;
}

.delete-btn {
  color: #e8a4a4;
}

.delete-btn:hover {
  background: #fef0ec;
}

.save-btn, .cancel-btn {
  padding: 6px 16px;
  border-radius: 30px;
  font-size: 12px;
  cursor: pointer;
  border: none;
}

.save-btn {
  background: #8fc1b0;
  color: white;
}

.save-btn:disabled {
  background: #e2e6e0;
  cursor: not-allowed;
}

.cancel-btn {
  background: none;
  border: 1px solid #e2e6e0;
  color: #b8c4b0;
}

.edit-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

/* 添加笔记 */
.add-note {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #efebe6;
}

.add-note h3 {
  font-size: 16px;
  font-weight: 500;
  color: #5a6e5c;
  margin: 0 0 16px 0;
}

.add-note textarea {
  width: 100%;
  padding: 16px;
  border: 1px solid #e2e6e0;
  border-radius: 20px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  background: #faf8f4;
}

.add-note textarea:focus {
  outline: none;
  border-color: #8fc1b0;
}

.word-count {
  text-align: right;
  font-size: 11px;
  color: #b8c4b0;
  margin: 8px 0 12px;
}

.word-count .exceed {
  color: #e8a4a4;
}

.submit-btn {
  background: #8fc1b0;
  border: none;
  padding: 10px 24px;
  border-radius: 40px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: #7aa992;
}

.submit-btn:disabled {
  background: #e2e6e0;
  cursor: not-allowed;
}

/* 加载状�?*/
.loading-state {
  text-align: center;
  padding: 80px 20px;
}

.loading-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background: #8fc1b0;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 错误状�?*/
.error-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 28px;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.retry-btn {
  margin-top: 20px;
  padding: 8px 24px;
  background: #8fc1b0;
  border: none;
  border-radius: 40px;
  color: white;
  cursor: pointer;
}

/* 响应�?*/
@media (max-width: 768px) {
  .book-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .info-list {
    justify-content: center;
  }
  
  .book-cover {
    width: 160px;
    height: 210px;
  }
  
  .timeline-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .notes-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .chart-container {
    height: 250px;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 16px;
  }
  
  .book-card, .trajectory-section, .notes-section {
    padding: 20px;
  }
  
  .book-title {
    font-size: 22px;
  }
}
</style>
