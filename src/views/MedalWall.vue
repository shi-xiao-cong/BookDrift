<template>
  <div class="medal-wall">
    <router-link to="/user" class="back-link">
        <span class="arrow-icon">�?/span> 返回个人中心
      </router-link>
    <div class="header-section">
      <h2>�?荣耀勋章�?�?/h2>
      
    </div>
    
    <!-- 加载状�?-->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载勋章�?..</p>
    </div>
    
    <!-- 错误状�?-->
    <div v-else-if="error" class="error-state">
      <p class="error-icon">�?/p>
      <p class="error-message">{{ error }}</p>
      <button class="retry-btn" @click="fetchMedals">重新加载</button>
    </div>
    
    <!-- 正常显示勋章内容 -->
    <div v-else>
      <!-- 捐赠模块 -->
      <div class="medal-section">
        <h3 class="section-title">📦 捐赠模块</h3>
        <div v-if="donationMedals.length === 0" class="empty-module">
          暂无捐赠勋章
        </div>
        <div v-else class="medal-grid">
          <div v-for="medal in donationMedals" :key="medal.id" class="medal-item">
            <div class="medal-icon" :class="{ 'unlocked': medal.unlocked }">
              {{ medal.icon }}
            </div>
            <div class="medal-name">{{ medal.name }}</div>
            <div class="medal-desc">{{ medal.description }}</div>
            <div class="medal-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: medal.progress + '%' }"></div>
              </div>
              <div class="progress-text">
                {{ medal.current }}/{{ medal.required }}
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 阅读模块 -->
      <div class="medal-section">
        <h3 class="section-title">📖 阅读模块</h3>
        <div v-if="readingMedals.length === 0" class="empty-module">
          暂无阅读勋章
        </div>
        <div v-else class="medal-grid">
          <div v-for="medal in readingMedals" :key="medal.id" class="medal-item">
            <div class="medal-icon" :class="{ 'unlocked': medal.unlocked }">
              {{ medal.icon }}
            </div>
            <div class="medal-name">{{ medal.name }}</div>
            <div class="medal-desc">{{ medal.description }}</div>
            <div class="medal-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: medal.progress + '%' }"></div>
              </div>
              <div class="progress-text">
                {{ medal.current }}/{{ medal.required }}
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 笔记模块 -->
      <div class="medal-section">
        <h3 class="section-title">📝 笔记模块</h3>
        <div v-if="noteMedals.length === 0" class="empty-module">
          暂无笔记勋章
        </div>
        <div v-else class="medal-grid">
          <div v-for="medal in noteMedals" :key="medal.id" class="medal-item">
            <div class="medal-icon" :class="{ 'unlocked': medal.unlocked }">
              {{ medal.icon }}
            </div>
            <div class="medal-name">{{ medal.name }}</div>
            <div class="medal-desc">{{ medal.description }}</div>
            <div class="medal-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: medal.progress + '%' }"></div>
              </div>
              <div class="progress-text">
                {{ medal.current }}/{{ medal.required }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MedalWall',
  data() {
    return {
      allMedals: [],
      loading: true,
      error: null,
      apiBaseUrl: 'import.meta.env.VITE_API_BASE_URL'  // 使用代理路径
    }
  },
  computed: {
    donationMedals() {
      return this.allMedals.filter(medal => medal.type === 'donation');
    },
    readingMedals() {
      return this.allMedals.filter(medal => medal.type === 'borrow');
    },
    noteMedals() {
      return this.allMedals.filter(medal => medal.type === 'note');
    }
  },
  async created() {
    await this.fetchMedals();
  },
  methods: {
    async fetchMedals() {
      this.loading = true;
      this.error = null;
      
      try {
        const userId = this.getCurrentUserId();
        const token = localStorage.getItem('token');
        
        console.log('正在获取勋章数据，用户ID:', userId);
        console.log('请求URL:', `${this.apiBaseUrl}/medals/${userId}`);
        
        const response = await fetch(`${this.apiBaseUrl}/medals/${userId}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
          }
        });
        
        if (!response.ok) {
          throw new Error(`HTTP错误: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('获取到勋章数�?', data);
        
        // 处理返回的数�?        if (data.success && Array.isArray(data.data)) {
          // 检查第一条数据的结构，决定如何处�?          if (data.data.length > 0) {
            const firstItem = data.data[0];
            console.log('第一条数据结�?', firstItem);
            
            // 根据实际结构处理
            if (firstItem.medal) {
              // 格式1: �?medal 嵌套
              this.allMedals = data.data.map(item => ({
                id: item.medal.id,
                name: item.medal.name,
                description: item.medal.description,
                icon: item.medal.icon,
                type: item.medal.type,
                required: item.medal.required,
                current: item.current || 0,
                unlocked: item.unlocked || false,
                progress: Math.min(100, Math.floor(((item.current || 0) / item.medal.required) * 100))
              }));
            } else {
              // 格式2: 直接是勋章对�?              this.allMedals = data.data.map(item => ({
                id: item.id,
                name: item.name,
                description: item.description,
                icon: item.icon,
                type: item.type,
                required: item.required,
                current: item.current || 0,
                unlocked: item.unlocked || false,
                progress: Math.min(100, Math.floor(((item.current || 0) / item.required) * 100))
              }));
            }
          } else {
            // 空数�?            this.allMedals = [];
          }
          
          console.log('转换后的勋章数据:', this.allMedals);
        } else {
          console.log('返回数据格式错误，使用模拟数�?);
          this.useMockMedals();
        }
      } catch (error) {
        console.error('获取勋章数据失败:', error);
        this.error = error.message || '加载失败，请稍后重试';
        // 出错时使用模拟数�?        this.useMockMedals();
      } finally {
        this.loading = false;
      }
    },
    
    // 模拟数据（备选方案）
    useMockMedals() {
      this.allMedals = [
        {
          id: 1,
          name: '初次捐赠',
          description: '捐赠第一本书',
          icon: '🏆',
          type: 'donation',
          required: 1,
          current: 1,
          progress: 100,
          unlocked: true
        },
        {
          id: 2,
          name: '捐赠达人',
          description: '捐赠5本书',
          icon: '🌟',
          type: 'donation',
          required: 5,
          current: 5,
          progress: 100,
          unlocked: true
        },
        {
          id: 3,
          name: '捐赠大师',
          description: '捐赠10本书',
          icon: '👑',
          type: 'donation',
          required: 10,
          current: 5,
          progress: 50,
          unlocked: false
        },
        {
          id: 4,
          name: '借阅新手',
          description: '首次借阅',
          icon: '📚',
          type: 'borrow',
          required: 1,
          current: 1,
          progress: 100,
          unlocked: true
        },
        {
          id: 5,
          name: '阅读爱好�?,
          description: '借阅10本书',
          icon: '📖',
          type: 'borrow',
          required: 10,
          current: 8,
          progress: 80,
          unlocked: false
        },
        {
          id: 6,
          name: '阅读达人',
          description: '借阅20本书',
          icon: '📚',
          type: 'borrow',
          required: 20,
          current: 8,
          progress: 40,
          unlocked: false
        },
        {
          id: 7,
          name: '笔记分享�?,
          description: '分享5篇笔�?,
          icon: '✍️',
          type: 'note',
          required: 5,
          current: 5,
          progress: 100,
          unlocked: true
        },
        {
          id: 8,
          name: '笔记达人',
          description: '分享10篇笔�?,
          icon: '📝',
          type: 'note',
          required: 10,
          current: 5,
          progress: 50,
          unlocked: false
        },
        {
          id: 9,
          name: '笔记大师',
          description: '分享20篇笔�?,
          icon: '📔',
          type: 'note',
          required: 20,
          current: 5,
          progress: 25,
          unlocked: false
        }
      ];
    },
    
    getCurrentUserId() {
      const userInfoStr = localStorage.getItem('userInfo');
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr);
          return userInfo.id || 1;
        } catch (e) {
          return 1;
        }
      }
      return 1;
    }
  }
}
</script>

<style scoped>
/* ==================== 清新风格样式 ==================== */
.medal-wall {
  background: #faf8f4;
  min-height: 100vh;
  padding: 32px 40px;
}

/* 返回按钮样式 */
.back-nav {
  max-width: 1300px;
  margin: 0 auto 20px;
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

.back-arrow {
  font-size: 16px;
}

.header-section {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 32px;
}

h2 {
  font-size: 32px;
  font-weight: 500;
  background: linear-gradient(135deg, #2c5a4f, #8fc1b0);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin: 0;
  letter-spacing: 2px;
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
  max-width: 500px;
  margin: 0 auto;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-message {
  margin-bottom: 20px;
  color: #8b9a8e;
}

.retry-btn {
  background: #8fc1b0;
  color: white;
  border: none;
  padding: 8px 24px;
  border-radius: 40px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.retry-btn:hover {
  background: #7aa992;
}

/* 空模块状�?*/
.empty-module {
  text-align: center;
  padding: 48px;
  background: white;
  border-radius: 24px;
  color: #b8c4b0;
  font-size: 14px;
  border: 1px solid #efebe6;
}

/* 勋章区域 */
.medal-section {
  margin-bottom: 48px;
}

.section-title {
  font-size: 22px;
  font-weight: 500;
  margin-bottom: 24px;
  padding-left: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #2c5a4f;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 28px;
  background: #8fc1b0;
  border-radius: 2px;
}

/* 勋章网格 */
.medal-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

/* 勋章卡片 */
.medal-item {
  background: white;
  border-radius: 24px;
  padding: 24px 16px 20px;
  text-align: center;
  transition: all 0.2s;
  border: 1px solid #efebe6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.medal-item:hover {
  transform: translateY(-4px);
  border-color: #e2e6e0;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.04);
}

/* 勋章图标 */
.medal-icon {
  font-size: 48px;
  line-height: 1;
  margin-bottom: 12px;
  transition: all 0.2s;
  opacity: 0.5;
  filter: grayscale(0.3);
}

.medal-icon.unlocked {
  opacity: 1;
  filter: none;
  animation: gentlePulse 2s ease-in-out infinite;
}

@keyframes gentlePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

/* 解锁标记 */
.medal-icon.unlocked::after {
  content: '�?;
  position: relative;
  display: inline-block;
  font-size: 14px;
  background: #8fc1b0;
  color: white;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  margin-left: -12px;
  margin-bottom: -8px;
  vertical-align: middle;
  font-weight: bold;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 勋章名称 */
.medal-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #2c5a4f;
}

/* 勋章描述 */
.medal-desc {
  font-size: 12px;
  color: #8b9a8e;
  padding: 4px 8px;
  margin: 6px 0 12px;
  background: #faf8f4;
  border-radius: 30px;
  display: inline-block;
}

.medal-progress {
  margin-top: 12px;
  width: 100%;
}

/* 进度�?*/
.progress-bar {
  width: 100%;
  height: 4px;
  background: #efebe6;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 6px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #8fc1b0, #b8c4b0);
  border-radius: 2px;
  transition: width 0.5s ease-out;
}

/* 进度文字 */
.progress-text {
  font-size: 11px;
  color: #b8c4b0;
}

/* 响应�?*/
@media (max-width: 768px) {
  .medal-wall {
    padding: 20px 20px;
  }
  
  h2 {
    font-size: 26px;
  }
  
  .medal-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 15px;
  }
  
  .medal-icon {
    font-size: 40px;
  }
  
  .medal-name {
    font-size: 14px;
  }
  
  .section-title {
    font-size: 18px;
    padding-left: 12px;
  }
}

@media (max-width: 480px) {
  .medal-wall {
    padding: 16px;
  }
  
  .header-section {
    margin-bottom: 24px;
  }
  
  h2 {
    font-size: 22px;
  }
  
  .medal-grid {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }
  
  .medal-item {
    padding: 16px 12px;
  }
  
  .medal-icon {
    font-size: 36px;
  }
  
  .medal-icon.unlocked::after {
    width: 18px;
    height: 18px;
    font-size: 12px;
  }
  
  .medal-name {
    font-size: 13px;
  }
  
  .medal-desc {
    font-size: 10px;
  }
}
</style>
