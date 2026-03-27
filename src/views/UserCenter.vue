<template>
  <div class="user-center" :class="themeClass">
    <div class="back-nav">
      <router-link to="/" class="back-link">
        <span class="back-arrow">�?/span> 返回首页
      </router-link>
    </div>
    
    <h2>个人中心</h2>
    
    <!-- 用户信息卡片 -->
    <div class="user-info glass-card">
      <div class="avatar-section">
        <div class="avatar-container">
          <img :src="avatarUrl" alt="用户头像" class="avatar">
          <input type="file" id="avatar-upload" class="avatar-upload" @change="handleAvatarUpload" accept="image/*">
          <label for="avatar-upload" class="avatar-edit">
            <span class="edit-icon">📷</span>
          </label>
        </div>
      </div>
      <div class="info-content">
        <div class="info-header">
        <h3>用户信息</h3>
        <button class="edit-info-btn" @click="openEditInfoDialog">
          ✏️ 编辑
        </button>
        </div>
        <div class="info-item">
          <span class="label">学号:</span>
          <span class="value">{{ userInfo.studentId || '2021001' }}</span>
        </div>
        <div class="info-item">
          <span class="label">姓名:</span>
          <span class="value">{{ userInfo.name || '张三' }}</span>
        </div>
        <div class="info-item">
          <span class="label">手机�?</span>
          <span class="value">{{ userInfo.phone || '13800138000' }}</span>
        </div>
        <div class="info-item">
          <span class="label">邮箱:</span>
          <span class="value">{{ userInfo.email || 'zhangsan@example.com' }}</span>
        </div>
        <div class="info-item">
          <button class="change-pwd-btn" @click="openChangePwdDialog">
            🔒 修改密码
          </button>
        </div>
      </div>
      <div class="qrcode-section">
        <h3>用户专属二维�?/h3>
        <div class="qrcode-container">
          <img :src="qrcodeUrl" alt="用户专属二维�? class="user-qrcode">
          <p class="qrcode-hint">用于认领和还书操�?/p>
        </div>
      </div>
    </div>
    <!-- 修改信息弹窗 -->
    <div v-if="editInfoDialogVisible" class="modal-overlay" @click="closeEditInfoDialog">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3>编辑个人信息</h3>
          <button class="close-btn" @click="closeEditInfoDialog">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>姓名 <span class="required">*</span></label>
            <input type="text" v-model="editForm.name" placeholder="请输入姓�?>
          </div>
          <div class="form-group">
            <label>手机�?/label>
            <input type="tel" v-model="editForm.phone" placeholder="请输�?1位手机号" 
            maxlength="11" 
            @input="validatePhone(editForm.phone)"
            @blur="validatePhone(editForm.phone)">
            <span v-if="phoneError" class="error-hint">{{ phoneError }}</span>
            <span v-else-if="editForm.phone && editForm.phone.length > 0 
            && editForm.phone.length === 11 && !phoneError" class="success-hint">�?手机号格式正�?/span>
            <span v-else-if="editForm.phone && editForm.phone.length < 11 
            && editForm.phone.length > 0" class="warning-hint">还需输入
             {{ 11 - editForm.phone.length }} �?/span>
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input type="email" v-model="editForm.email" placeholder="请输入邮�?
            @input="validateEmail(editForm.email)"
            @blur="validateEmail(editForm.email)">
            <span v-if="emailError" class="error-hint">{{ emailError }}</span>
            <span v-else-if="editForm.email && !emailError" class="success-hint">�?邮箱格式正确</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeEditInfoDialog">取消</button>
          <button class="btn-save" @click="saveUserInfo" :disabled="saving">
            {{ saving ? '保存�?..' : '保存' }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 修改密码弹窗 -->
    <div v-if="changePwdDialogVisible" class="modal-overlay" @click="closeChangePwdDialog">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3>修改密码</h3>
          <button class="close-btn" @click="closeChangePwdDialog">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>原密�?<span class="required">*</span></label>
            <input type="password" v-model="pwdForm.oldPassword" placeholder="请输入原密码">
          </div>
          <div class="form-group">
            <label>新密�?<span class="required">*</span></label>
            <input type="password" v-model="pwdForm.newPassword" placeholder="请输入新密码（至�?位）"
            @input="validateNewPassword"
            @blur="validateNewPassword">
            <span v-if="pwdError" class="error-hint">{{ pwdError }}</span>
            <span v-else-if="pwdForm.newPassword && pwdForm.newPassword.length < 6 && pwdForm.newPassword.length > 0" class="warning-hint">还需输入 {{ 6 - pwdForm.newPassword.length }} �?/span>
        <span v-else-if="pwdForm.newPassword && pwdForm.newPassword.length >= 6" class="success-hint">�?密码强度足够</span>
          </div>
          <div class="form-group">
            <label>确认新密�?<span class="required">*</span></label>
            <input type="password" v-model="pwdForm.confirmPassword" placeholder="请再次输入新密码"
            @input="validateConfirmPassword"
            @blur="validateConfirmPassword">
            <span v-if="confirmPwdError" class="error-hint">{{ confirmPwdError }}</span>
        <span v-else-if="pwdForm.confirmPassword && pwdForm.newPassword === pwdForm.confirmPassword" class="success-hint">�?密码一�?/span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeChangePwdDialog">取消</button>
          <button class="btn-save" @click="saveNewPassword" :disabled="changingPwd">
            {{ changingPwd ? '修改�?..' : '确认修改' }}
          </button>
        </div>
      </div>
    </div>
    <!-- 统计信息 -->
    <div class="user-stats">
      <h3>统计信息</h3>
      <div class="stats-grid">
        <div class="stat-item glass-card">
          <div class="stat-value">{{ userStats.donation_count || 0 }}</div>
          <div class="stat-label">捐赠书籍</div>
        </div>
        <div class="stat-item glass-card">
          <div class="stat-value">{{ userStats.borrow_count || 0 }}</div>
          <div class="stat-label">当前借阅</div>
        </div>
        <div class="stat-item glass-card">
          <div class="stat-value">{{ userStats.total_borrow || 0 }}</div>
          <div class="stat-label">历史借阅</div>
        </div>
        <div class="stat-item glass-card">
          <div class="stat-value">{{ userStats.note_count || 0 }}</div>
          <div class="stat-label">分享笔记</div>
        </div>
      </div>
    </div>
    
    <!-- 勋章区域 -->
    <div class="medals">
      <div class="medals-header">
        <h3>我的勋章 ({{ unlockedMedals.length }})</h3>
        <router-link to="/medal-wall" class="view-all-link">
          查看全部勋章
          <span class="arrow-icon">�?/span>
        </router-link>
      </div>
      
      <!-- 加载状�?-->
      <div v-if="medalsLoading" class="loading-state glass-card">
        <div class="loading-spinner"></div>
        <p>加载勋章�?..</p>
      </div>
      
      <!-- 勋章列表 -->
      <div v-else class="medal-grid">
        <div v-if="unlockedMedals.length === 0" class="empty-medals glass-card">
          <p>暂无已解锁勋�?/p>
          <p class="empty-hint">完成相应任务即可获得勋章</p>
        </div>
        <div v-else v-for="medal in unlockedMedals" :key="medal.id" class="medal-item glass-card" @click="showMedalDetail(medal)">
          <div class="medal-icon unlocked">
            {{ medal.icon }}
          </div>
          <div class="medal-name">{{ medal.name }}</div>
          <div class="medal-desc">{{ medal.description }}</div>
          <!-- 显示进度（如果是未完成的勋章�?-->
          <div v-if="medal.current < medal.required" class="medal-progress">
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
    
    <!-- 当前借阅 -->
    <div class="borrowed-books">
      <h3>当前借阅</h3>
      <div class="book-list">
        <div v-for="book in borrowedBooks" :key="book.id" class="book-item glass-card">
          <!-- 书籍封面 -->
          <div class="book-cover" v-if="book.bookCover">
            <img :src="book.bookCover" :alt="book.bookTitle">
          </div>
          <div class="book-cover-placeholder" v-else>
            📚
          </div>
          
          <!-- 书籍信息 -->
          <div class="book-info">
            <h4>{{ book.bookTitle || '未知书名' }}</h4>
            <p class="book-author">{{ book.bookAuthor || '未知作�? }}</p>
            <p class="borrow-date">借阅日期：{{ formatDate(book.borrowDate) }}</p>
            <p class="due-date">应还日期：{{ formatDate(book.dueDate) }}</p>
          </div>
        </div>
        
        <div v-if="borrowedBooks.length === 0" class="empty-books glass-card">
          <p>暂无借阅书籍</p>
        </div>
      </div>
    </div>

    <!-- 勋章详情弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3>勋章详情</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-content">
          <div class="modal-medal-icon" :class="{ 'unlocked': selectedMedal.unlocked }">
            {{ selectedMedal.icon }}
          </div>
          <div class="modal-medal-name">{{ selectedMedal.name }}</div>
          <div class="modal-medal-desc">{{ selectedMedal.description }}</div>
          
          <!-- 解锁要求 -->
          <div class="requirement-section">
            <h4>解锁要求</h4>
            <div class="requirement-item">
              <span class="requirement-label">{{ getRequirementText(selectedMedal) }}</span>
            </div>
          </div>

          <!-- 进度�?-->
          <div class="progress-section">
            <div class="progress-info">
              <span>当前进度</span>
              <span>{{ selectedMedal.current }}/{{ selectedMedal.required }}</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: selectedMedal.progress + '%' }"></div>
            </div>
          </div>

          <!-- 解锁状�?-->
          <div class="status-section">
            <div class="status-badge" :class="{ 'unlocked': selectedMedal.unlocked, 'locked': !selectedMedal.unlocked }">
              {{ selectedMedal.unlocked ? '已解�? : '未解�? }}
            </div>
            <div v-if="!selectedMedal.unlocked" class="hint-text">
              还需 {{ selectedMedal.required - selectedMedal.current }} {{ getUnit(selectedMedal.type) }} 即可解锁
            </div>
            <div v-else class="unlock-time">
              解锁�? {{ formatDate(selectedMedal.unlockedAt) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import QRCode from 'qrcode'

export default {
  name: 'UserCenter',
  data() {
    return {
      avatarUrl: 'https://via.placeholder.com/120',
      qrcodeUrl: '',
      showModal: false,
      selectedMedal: null,
      medals: [],
      medalsLoading: true,
      showThemePanel: false,
      currentTheme: 'default',
      userStats: {
        donation_count: 0,
        borrow_count: 0,
        total_borrow: 0,
        note_count: 0
      },
      userInfo: {
        studentId: '',
        name: '',
        phone: '',
        email: ''
      },
      borrowedBooks: [],
      apiBaseUrl: 'import.meta.env.VITE_API_BASE_URL',
      themes: [
        {
          value: 'default',
          name: '经典蓝紫',
          gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 50%, #4facfe 100%)'
        },
        {
          value: 'green',
          name: '青灰�?,
          gradient: 'linear-gradient(135deg, #dbdbdb 0%, #2f6e62 100%)'
        },
        {
          value: 'blue',
          name: '海洋�?,
          gradient: 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)'
        },
        {
          value: 'orange',
          name: '温暖�?,
          gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
        },
        {
          value: 'pink',
          name: '浪漫�?,
          gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
        },
        {
          value: 'purple',
          name: '梦幻�?,
          gradient: 'linear-gradient(135deg, #5f2c82 0%, #49a09d 100%)'
        }
      ],
      // 编辑信息弹窗
    editInfoDialogVisible: false,
    editForm: {
      name: '',
      phone: '',
      email: ''
    },
    saving: false,
    phoneError: '',
    emailError: '',
    
    // 修改密码弹窗
    changePwdDialogVisible: false,
    pwdForm: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    },
    changingPwd: false,
    pwdError: '',
    confirmPwdError: ''
    }
  
  },
  computed: {
    unlockedMedals() {
      console.log('medals数组:', this.medals);
      const unlocked = this.medals.filter(medal => medal.unlocked === true);
      console.log('过滤后的已解锁勋�?', unlocked);
      return unlocked;
    },
    themeClass() {
      return `theme-${this.currentTheme}`
    }
  },
  async created() {
    await this.fetchUserData();
    await this.fetchBorrowedBooks();
    
    // 获取头像
    await this.fetchUserAvatar();
    
    // 从本地存储加载主�?    const savedTheme = localStorage.getItem('userCenterTheme')
    if (savedTheme) {
      this.currentTheme = savedTheme
    }
    
    // 监听统计信息更新事件
    window.addEventListener('stats-updated', this.handleStatsUpdated);

    // 添加对借阅更新事件的监�?    window.addEventListener('borrowed-books-updated', this.handleBorrowedBooksUpdated);
    
    // 点击其他地方关闭主题面板
    document.addEventListener('click', this.handleClickOutside);
    window.addEventListener('book-status-changed', this.handleBookStatusChanged)
  },
  beforeDestroy() {
    // 移除事件监听
    window.removeEventListener('stats-updated', this.handleStatsUpdated);
    window.removeEventListener('borrowed-books-updated', this.handleBorrowedBooksUpdated);
    document.removeEventListener('click', this.handleClickOutside);
    window.removeEventListener('book-status-changed', this.handleBookStatusChanged)
  },
  mounted() {
    this.generateUserQRCode();
  },
  methods: {
    // 处理点击外部关闭主题面板
    handleClickOutside(event) {
      const themeSwitcher = this.$el.querySelector('.theme-switcher')
      if (themeSwitcher && !themeSwitcher.contains(event.target)) {
        this.showThemePanel = false
      }},
    handleBookStatusChanged(event) {
    const { bookId, status } = event.detail
    // 如果书籍状态变为可借阅，并且当前用户借阅列表中包含这本书，则刷新借阅列表
    if (status === 'available') {
      this.fetchBorrowedBooks()  // 重新获取当前用户的借阅列表
    }
    },
    
    // 切换主题
    changeTheme(themeValue) {
      this.currentTheme = themeValue
      localStorage.setItem('userCenterTheme', themeValue)
      this.showThemePanel = false
    },
    
    // 处理借阅更新事件
    async handleBorrowedBooksUpdated(event) {
      console.log('收到借阅更新事件:', event.detail);
      const userId = event.detail.userId || this.getCurrentUserId();
      
      // 重新获取借阅列表
      await this.fetchBorrowedBooks();
      
      // 重新获取统计信息
      const token = localStorage.getItem('token');
      await this.fetchUserStats(userId, token);
    },
    
    // 获取头像
    async fetchUserAvatar() {
      try {
        const userId = this.getCurrentUserId();
        const token = localStorage.getItem('token');
        
        const response = await fetch(`${this.apiBaseUrl}/users/avatar/${userId}`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          }
        });
        
        if (response.ok) {
          const data = await response.json();
          if (data.success && data.avatarUrl) {
            this.avatarUrl = data.avatarUrl;
            
            // 更新 localStorage
            const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
            userInfo.avatarUrl = data.avatarUrl;
            localStorage.setItem('userInfo', JSON.stringify(userInfo));
          }
        }
      } catch (error) {
        console.error('获取头像失败:', error);
      }
    },
    
    // 上传头像
    async handleAvatarUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      
      // 检查文件大小（限制�?MB�?      if (file.size > 2 * 1024 * 1024) {
        alert('图片大小不能超过2MB');
        return;
      }
      
      // 检查图片格�?      if (!file.type.startsWith('image/')) {
        alert('请选择图片文件');
        return;
      }
      
      const formData = new FormData();
      formData.append('file', file);
      
      try {
        const userId = this.getCurrentUserId();
        const token = localStorage.getItem('token');
        
        console.log('上传头像，用户ID:', userId);
        console.log('请求URL:', `${this.apiBaseUrl}/users/avatar/${userId}`);
        
        const response = await fetch(`${this.apiBaseUrl}/users/avatar/${userId}`, {
          method: 'POST',
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          },
          body: formData
        });
        
        console.log('上传响应状�?', response.status);
        
        if (!response.ok) {
          throw new Error(`HTTP错误: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('上传响应数据:', data);
        
        if (data.success) {
          this.avatarUrl = data.avatarUrl;
          
          // 更新 localStorage
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
          userInfo.avatarUrl = data.avatarUrl;
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          
          alert('头像上传成功');
        } else {
          alert(data.message || '上传失败');
        }
      } catch (error) {
        console.error('上传头像失败:', error);
        alert('上传失败，请重试: ' + error.message);
      }
    },
    
    // 处理统计信息更新
    async handleStatsUpdated(event) {
      console.log('收到统计更新事件:', event.detail);
      const userId = this.getCurrentUserId();
      const token = localStorage.getItem('token');
      
      // 只重新获取统计信息，不重新获取勋章数�?      await this.fetchUserStats(userId, token);
      console.log('统计信息已刷�?);
    },
    
    async fetchUserData() {
      this.medalsLoading = true;
      try {
        const userId = this.getCurrentUserId();
        const token = localStorage.getItem('token');
        
        console.log('===== 开始获取用户数�?=====');
        console.log('用户ID:', userId);
        
        // 1. 获取勋章数据
        await this.fetchMedals(userId, token);
        
        // 2. 获取统计信息
        await this.fetchUserStats(userId, token);
        
        // 3. 获取用户基本信息
        this.getUserInfoFromStorage();
        
      } catch (error) {
        console.error('获取用户数据失败:', error);
        this.useMockMedals();
        this.useMockStats();
      } finally {
        this.medalsLoading = false;
      }
    },
    
    // 专门获取勋章数据的方�?    async fetchMedals(userId, token) {
      try {
        const medalsResponse = await fetch(`${this.apiBaseUrl}/medals/${userId}`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          }
        });
        
        const medalsData = await medalsResponse.json();
        console.log('勋章API返回数据:', medalsData);
        
        if (medalsData.success && Array.isArray(medalsData.data)) {
          // 处理勋章数据
          if (medalsData.data.length > 0) {
            const firstItem = medalsData.data[0];
            
            if (firstItem.medal) {
              // �?medal 嵌套
              this.medals = medalsData.data.map(item => ({
                id: item.medal.id,
                name: item.medal.name,
                description: item.medal.description,
                icon: item.medal.icon,
                type: item.medal.type,
                required: item.medal.required,
                current: item.current || 0,
                unlocked: item.unlocked || false,
                progress: Math.min(100, Math.floor(((item.current || 0) / item.medal.required) * 100)),
                unlockedAt: item.unlockedAt
              }));
            } else {
              // 直接是勋章对�?              this.medals = medalsData.data.map(item => ({
                id: item.id,
                name: item.name,
                description: item.description,
                icon: item.icon,
                type: item.type,
                required: item.required,
                current: item.current || 0,
                unlocked: item.unlocked || false,
                progress: Math.min(100, Math.floor(((item.current || 0) / item.required) * 100)),
                unlockedAt: item.unlockedAt
              }));
            }
          } else {
            this.medals = [];
          }
        } else {
          console.error('获取勋章失败，使用模拟数�?);
          this.useMockMedals();
        }
      } catch (error) {
        console.error('获取勋章数据出错:', error);
        this.useMockMedals();
      }
    },
    
    // 专门获取统计信息的方�?    async fetchUserStats(userId, token) {
      try {
        console.log('开始获取统计信�? userId:', userId);
        console.log('请求URL:', `${this.apiBaseUrl}/statistics/${userId}`);
        
        const statsResponse = await fetch(`${this.apiBaseUrl}/statistics/${userId}`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          }
        });
        
        console.log('统计信息响应状�?', statsResponse.status);
        
        const statsData = await statsResponse.json();
        console.log('统计信息原始数据:', statsData);
        
        if (statsData.success) {
          this.userStats = statsData.data;
          console.log('统计信息更新成功:', this.userStats);
        } else {
          console.log('统计信息获取失败，使用模拟数�?);
          this.useMockStats();
        }
      } catch (error) {
        console.error('获取统计信息失败:', error);
        this.useMockStats();
      }
    },
    
    // 获取借阅列表
    async fetchBorrowedBooks() {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo'));
        const userId = userInfo.id;
        const token = localStorage.getItem('token');
        
        console.log('正在获取借阅书籍，用户ID:', userId);
        
        const response = await fetch(`${this.apiBaseUrl}/borrow/current/user/${userId}`, {
          headers: {
            'Authorization': token ? `Bearer ${token}` : ''
          }
        });
        
        const data = await response.json();
        console.log('原始返回数据:', data);
        
        if (data.success && Array.isArray(data.data)) {
          // 正确映射所有字�?          this.borrowedBooks = data.data.map(record => ({
            id: record.id,
            bookId: record.bookId,
            bookTitle: record.bookTitle || '未知书名',
            bookAuthor: record.bookAuthor || '未知作�?,
            bookCover: record.bookCover || '',
            borrowDate: record.borrowDate,
            dueDate: record.dueDate
          }));
          
          console.log('处理后的借阅列表:', this.borrowedBooks);
        } else {
          this.borrowedBooks = [];
        }
      } catch (error) {
        console.error('获取借阅书籍失败:', error);
        this.borrowedBooks = [];
      }
    },
    
    // 格式化日�?    formatDate(dateStr) {
      if (!dateStr) return '未知日期';
      try {
        const date = new Date(dateStr);
        // 转换为本地日期格�?YYYY/MM/DD
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}/${month}/${day}`;
      } catch (e) {
        return dateStr;
      }
    },
    
    useMockMedals() {
      this.medals = [
        {
          id: 1,
          name: '初次捐赠',
          description: '捐赠第一本书',
          icon: '🏆',
          type: 'donation',
          required: 1,
          current: 1,
          progress: 100,
          unlocked: true,
          unlockedAt: '2024-03-01T10:00:00Z'
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
          unlocked: true,
          unlockedAt: '2024-03-15T14:30:00Z'
        },
        {
          id: 3,
          name: '借阅新手',
          description: '首次借阅',
          icon: '📚',
          type: 'borrow',
          required: 1,
          current: 1,
          progress: 100,
          unlocked: true,
          unlockedAt: '2024-03-05T09:20:00Z'
        },
        {
          id: 4,
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
          id: 5,
          name: '笔记分享�?,
          description: '分享5篇笔�?,
          icon: '✍️',
          type: 'note',
          required: 5,
          current: 5,
          progress: 100,
          unlocked: true,
          unlockedAt: '2024-03-10T16:45:00Z'
        }
      ];
    },
    
    useMockStats() {
      this.userStats = {
        donation_count: 5,
        borrow_count: 3,
        total_borrow: 12,
        note_count: 8
      };
    },
    
    getUserInfoFromStorage() {
      const userInfoStr = localStorage.getItem('userInfo');
      if (userInfoStr) {
        try {
          this.userInfo = JSON.parse(userInfoStr);
          
          // 如果有保存的头像URL，也加载进来
          if (this.userInfo.avatarUrl) {
            this.avatarUrl = this.userInfo.avatarUrl;
          }
        } catch (e) {
          console.error('解析用户信息失败:', e);
          this.setDefaultUserInfo();
        }
      } else {
        this.setDefaultUserInfo();
      }
    },
    
    setDefaultUserInfo() {
      this.userInfo = {
        studentId: '2021001',
        name: '张三',
        phone: '13800138000',
        email: 'zhangsan@example.com'
      };
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
    },
    
    async generateUserQRCode() {
      try {
        const userInfo = this.userInfo;
        const userData = `studentId=${userInfo.studentId}&name=${encodeURIComponent(userInfo.name)}&phone=${userInfo.phone}&action=user`;
        this.qrcodeUrl = await QRCode.toDataURL(userData);
      } catch (error) {
        console.error('生成二维码失�?', error);
      }
    },
    
    showMedalDetail(medal) {
      this.selectedMedal = medal;
      this.showModal = true;
      document.body.style.overflow = 'hidden';
    },
    
    closeModal() {
      this.showModal = false;
      this.selectedMedal = null;
      document.body.style.overflow = '';
    },
    
    // 归还书籍
    async returnBook(bookId) {
      try {
        const userId = this.getCurrentUserId();
        const token = localStorage.getItem('token');
        
        console.log('归还书籍，bookId:', bookId);
        
        const response = await fetch(`${this.apiBaseUrl}/borrow/return/${bookId}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
          }
        });
        
        console.log('归还响应状�?', response.status);
        const data = await response.json();
        console.log('归还响应数据:', data);
        
        if (data.success) {
          alert('归还成功');
          
          // 重新获取借阅列表
          await this.fetchBorrowedBooks();
          
          // 重新获取统计信息
          await this.fetchUserStats(userId, token);
          
          // 触发统计更新事件
          window.dispatchEvent(new CustomEvent('stats-updated', { 
            detail: { userId: userId }
          }));
        } else {
          alert(data.message || '归还失败');
        }
      } catch (error) {
        console.error('归还失败:', error);
        alert('归还失败，请稍后重试');
      }
    },
    
    getRequirementText(medal) {
      const typeMap = {
        donation: '捐赠书籍',
        borrow: '阅读书籍',
        note: '发布笔记'
      };
      return `${typeMap[medal.type] || '完成'}数量达到 ${medal.required} �?条`;
    },
    
    getUnit(type) {
      const unitMap = {
        donation: '�?,
        borrow: '�?,
        note: '�?
      };
      return unitMap[type] || '�?;
    },
    // 打开编辑信息弹窗
  openEditInfoDialog() {
    this.editForm = {
      name: this.userInfo.name || '',
      phone: this.userInfo.phone || '',
      email: this.userInfo.email || ''
    }
    this.phoneError = ''
    this.emailError = ''
    this.editInfoDialogVisible = true
  },
  
  // 关闭编辑信息弹窗
  closeEditInfoDialog() {
    this.editInfoDialogVisible = false
    this.editForm = { name: '', phone: '', email: '' }
  },
  
  // 验证手机�?  validatePhone(phone) {
    if (!phone) return true
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(phone)) {
      this.phoneError = '手机号格式不正确'
      return false
    }
    this.phoneError = ''
    return true
  },
  
  // 验证邮箱
  validateEmail(email) {
    if (!email) return true
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(email)) {
      this.emailError = '邮箱格式不正�?
      return false
    }
    this.emailError = ''
    return true
  },
  
  // 保存用户信息
  async saveUserInfo() {
    // 验证姓名
    if (!this.editForm.name || this.editForm.name.trim().length < 2) {
      alert('姓名至少2个字�?)
      return
    }
    
    // 验证手机�?    if (this.editForm.phone && !this.validatePhone(this.editForm.phone)) {
      return
    }
    
    // 验证邮箱
    if (this.editForm.email && !this.validateEmail(this.editForm.email)) {
      return
    }
    
    this.saving = true
    try {
      const userId = this.getCurrentUserId()
      const token = localStorage.getItem('token')
      
      const response = await fetch(`${this.apiBaseUrl}/users/info/${userId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : ''
        },
        body: JSON.stringify({
          name: this.editForm.name.trim(),
          phone: this.editForm.phone || '',
          email: this.editForm.email || ''
        })
      })
      
      const data = await response.json()
      
      if (data.success) {
        // 更新本地用户信息
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        userInfo.name = this.editForm.name.trim()
        userInfo.phone = this.editForm.phone || ''
        userInfo.email = this.editForm.email || ''
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        
        // 更新页面显示
        this.userInfo = userInfo
        
        alert('修改成功')
        this.closeEditInfoDialog()
      } else {
        alert(data.message || '修改失败')
      }
    } catch (err) {
      console.error('修改失败:', err)
      alert('修改失败，请重试')
    } finally {
      this.saving = false
    }
  },
  
  // 打开修改密码弹窗
  openChangePwdDialog() {
    this.pwdForm = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    this.pwdError = ''
    this.changePwdDialogVisible = true
  },
  
  // 关闭修改密码弹窗
  closeChangePwdDialog() {
    this.changePwdDialogVisible = false
  },
  
  // 保存新密�?  async saveNewPassword() {
    // 验证原密�?    if (!this.pwdForm.oldPassword) {
      this.pwdError = '请输入原密码'
      return
    }
    
    // 验证新密�?    if (!this.pwdForm.newPassword) {
      this.pwdError = '请输入新密码'
      return
    }
    if (this.pwdForm.newPassword.length < 6) {
      this.pwdError = '新密码长度至�?�?
      return
    }
    
    // 验证确认密码
    if (this.pwdForm.newPassword !== this.pwdForm.confirmPassword) {
      this.pwdError = '两次输入的密码不一�?
      return
    }
    
    this.pwdError = ''
    this.changingPwd = true
    
    try {
      const userId = this.getCurrentUserId()
      const token = localStorage.getItem('token')
      
      const response = await fetch(`${this.apiBaseUrl}/users/password/${userId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : ''
        },
        body: JSON.stringify({
          oldPassword: this.pwdForm.oldPassword,
          newPassword: this.pwdForm.newPassword
        })
      })
      
      const data = await response.json()
      
      if (data.success) {
        alert('密码修改成功，请重新登录')
        this.closeChangePwdDialog()
        
        // 退出登�?        localStorage.removeItem('userInfo')
        localStorage.removeItem('token')
        this.$router.push('/login')
      } else {
        alert(data.message || '修改失败')
      }
    } catch (err) {
      console.error('修改密码失败:', err)
      alert('修改失败，请重试')
    } finally {
      this.changingPwd = false
    }
    
  },
   // 验证新密�?  validateNewPassword() {
    if (!this.pwdForm.newPassword) {
      this.pwdError = ''
      return
    }
    if (this.pwdForm.newPassword.length < 6) {
      this.pwdError = '新密码长度至�?�?
    } else {
      this.pwdError = ''
    }
    // 如果确认密码已填，重新验证确认密�?    if (this.pwdForm.confirmPassword) {
      this.validateConfirmPassword()
    }
  },
  
  // 验证确认密码
  validateConfirmPassword() {
    if (!this.pwdForm.confirmPassword) {
      this.confirmPwdError = ''
      return
    }
    if (this.pwdForm.newPassword !== this.pwdForm.confirmPassword) {
      this.confirmPwdError = '两次输入的密码不一�?
    } else {
      this.confirmPwdError = ''
    }
  }
  
  },
  watch: {
    userInfo: {
      handler() {
        this.generateUserQRCode();
      },
      deep: true
    }
  }
}
</script>

<style scoped>
/* ==================== 清新风格样式 ==================== */
.user-center {
  background: #faf8f4;
  min-height: 100vh;
  padding: 32px 20px;
}

/* 返回按钮样式 */
.back-nav {
  max-width: 1000px;
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

h2 {
  text-align: center;
  font-size: 28px;
  font-weight: 500;
  color: #2c5a4f;
  margin-bottom: 32px;
}

/* 毛玻璃卡片样�?*/
.glass-card {
  background: white;
  backdrop-filter: none;
  border: 1px solid #efebe6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  transition: all 0.2s;
}

.glass-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
  border-color: #e2e6e0;
}

/* 用户信息卡片 */
.user-info {
  max-width: 1000px;
  margin: 0 auto 32px;
  padding: 28px;
  display: flex;
  align-items: flex-start;
  gap: 32px;
  flex-wrap: wrap;
  border-radius: 28px;
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-container {
  position: relative;
  width: 100px;
  height: 100px;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #efebe6;
  transition: transform 0.2s;
}

.avatar:hover {
  transform: scale(1.02);
}

.avatar-upload {
  display: none;
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  background: #8fc1b0;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.avatar-edit:hover {
  background: #7aa992;
  transform: scale(1.05);
}

.edit-icon {
  font-size: 14px;
  color: white;
}

.info-content {
  flex: 1;
}

.info-content h3 {
  font-size: 18px;
  font-weight: 500;
  color: #2c5a4f;
  margin: 0 0 16px 0;
}

.info-item {
  display: flex;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #faf8f4;
  border-radius: 12px;
}

.label {
  width: 70px;
  font-weight: 500;
  color: #8b9a8e;
}

.value {
  color: #5a6e5c;
}

/* 二维码区�?*/
.qrcode-section {
  flex-shrink: 0;
  text-align: center;
}

.qrcode-section h3 {
  font-size: 14px;
  font-weight: 500;
  color: #5a6e5c;
  margin: 0 0 12px 0;
}

.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-qrcode {
  width: 100px;
  height: 100px;
  border: 1px solid #efebe6;
  border-radius: 16px;
  padding: 8px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.qrcode-hint {
  margin-top: 8px;
  font-size: 11px;
  color: #b8c4b0;
}

/* 统计信息 */
.user-stats {
  max-width: 1000px;
  margin: 0 auto 32px;
}

.user-stats h3 {
  font-size: 18px;
  font-weight: 500;
  color: #2c5a4f;
  margin-bottom: 16px;
  padding-left: 8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-item {
  padding: 20px;
  border-radius: 20px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 500;
  color: #8fc1b0;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: #8b9a8e;
}

/* 勋章区域 */
.medals {
  max-width: 1000px;
  margin: 0 auto 32px;
  padding: 28px;
  background: white;
  border-radius: 28px;
  border: 1px solid #efebe6;
}

.medals-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.medals-header h3 {
  font-size: 18px;
  font-weight: 500;
  color: #2c5a4f;
  margin: 0;
}

.view-all-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8fc1b0;
  text-decoration: none;
  font-size: 13px;
  transition: all 0.2s;
}

.view-all-link:hover {
  gap: 10px;
}

.arrow-icon {
  font-size: 14px;
  transition: transform 0.2s;
}

.view-all-link:hover .arrow-icon {
  transform: translateX(4px);
}

/* 加载状�?*/
.loading-state {
  text-align: center;
  padding: 40px;
  border-radius: 20px;
}

.loading-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
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

/* 勋章列表 */
.medal-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.medal-item {
  text-align: center;
  padding: 20px 12px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  background: #faf8f4;
  border: 1px solid #efebe6;
}

.medal-item:hover {
  transform: translateY(-2px);
  border-color: #e2e6e0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.medal-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.medal-name {
  font-weight: 500;
  font-size: 14px;
  color: #5a6e5c;
  margin-bottom: 6px;
}

.medal-desc {
  font-size: 11px;
  color: #b8c4b0;
  margin-bottom: 12px;
}

.medal-progress {
  margin-top: 8px;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: #efebe6;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #8fc1b0;
  border-radius: 2px;
  transition: width 0.3s;
}

.progress-text {
  font-size: 10px;
  color: #b8c4b0;
  margin-top: 4px;
}

.empty-medals {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
  border-radius: 20px;
  color: #b8c4b0;
}

.empty-hint {
  font-size: 12px;
  margin-top: 8px;
  color: #cbd5c7;
}

/* 当前借阅 */
.borrowed-books {
  max-width: 1000px;
  margin: 0 auto;
  padding: 28px;
  background: white;
  border-radius: 28px;
  border: 1px solid #efebe6;
}

.borrowed-books h3 {
  font-size: 18px;
  font-weight: 500;
  color: #2c5a4f;
  margin-bottom: 20px;
}

.book-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.book-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #faf8f4;
  border-radius: 20px;
  transition: all 0.2s;
}

.book-item:hover {
  transform: translateX(4px);
}

.book-cover, .book-cover-placeholder {
  width: 60px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #efebe6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-info {
  flex: 1;
}

.book-info h4 {
  font-size: 16px;
  font-weight: 500;
  color: #2c5a4f;
  margin: 0 0 4px 0;
}

.book-author {
  font-size: 13px;
  color: #8b9a8e;
  margin: 0 0 8px 0;
}

.borrow-date, .due-date {
  font-size: 12px;
  color: #b8c4b0;
  margin: 2px 0;
}

.due-date {
  color: #e8a4a4;
}

.empty-books {
  text-align: center;
  padding: 40px;
  border-radius: 20px;
  color: #b8c4b0;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-container {
  background: white;
  border-radius: 28px;
  width: 90%;
  max-width: 420px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  animation: modalSlideUp 0.3s ease;
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  border-bottom: 1px solid #efebe6;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #2c5a4f;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #b8c4b0;
  transition: color 0.2s;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.close-btn:hover {
  color: #e8a4a4;
  background: #fef0ec;
}

.modal-content {
  padding: 24px;
  text-align: center;
}

.modal-medal-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.modal-medal-name {
  font-size: 22px;
  font-weight: 500;
  color: #2c5a4f;
  margin-bottom: 8px;
}

.modal-medal-desc {
  font-size: 14px;
  color: #8b9a8e;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #efebe6;
}

.requirement-section {
  text-align: left;
  margin-bottom: 20px;
}

.requirement-section h4 {
  font-size: 13px;
  font-weight: 500;
  color: #5a6e5c;
  margin-bottom: 8px;
}

.requirement-item {
  background: #faf8f4;
  padding: 10px 14px;
  border-radius: 12px;
}

.requirement-label {
  font-size: 13px;
  color: #8fc1b0;
}

.progress-section {
  margin-bottom: 20px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #b8c4b0;
  margin-bottom: 8px;
}

.status-section {
  text-align: center;
  margin-top: 16px;
}

.status-badge {
  display: inline-block;
  padding: 6px 20px;
  border-radius: 40px;
  font-size: 13px;
  font-weight: 500;
}

.status-badge.unlocked {
  background: #e8f0ec;
  color: #8fc1b0;
}

.status-badge.locked {
  background: #efebe6;
  color: #b8c4b0;
}

.hint-text {
  font-size: 12px;
  color: #b8c4b0;
  margin-top: 12px;
}

.unlock-time {
  font-size: 12px;
  color: #8fc1b0;
  margin-top: 12px;
}

/* 响应�?*/
@media (max-width: 768px) {
  .user-center {
    padding: 20px 16px;
  }
  
  h2 {
    font-size: 24px;
    margin-bottom: 24px;
  }
  
  .user-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 24px;
  }
  
  .info-item {
    justify-content: center;
  }
  
  .label {
    width: auto;
    margin-right: 8px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .medal-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .book-list {
    gap: 12px;
  }
  
  .book-item {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .medal-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .medals, .borrowed-books, .user-info {
    padding: 20px;
  }
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.info-header h3 {
  margin: 0;
}

.edit-info-btn {
  background: rgba(47, 199, 133, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: rgb(16, 12, 12);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-info-btn:hover {
  background: rgba(36, 35, 35, 0.3);
  transform: translateY(-1px);
}

.change-pwd-btn {
  background: none;
  border: 1px solid rgba(81, 184, 124, 0.3);
  color: rgba(202, 17, 17, 0.8);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 8px;
}

.change-pwd-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: #ff9800;
  color: #ff9800;
}
.modal-body {
  padding: 32px 32px 24px 32px;  /* �?�?�?左，全部增加�?2px */
}
/* 弹窗内表�?*/
.modal-body .form-group {
  margin-bottom: 20px;
}

.modal-body label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #5a6e5c;
  font-size: 14px;
}

.modal-body .required {
  color: #e8a4a4;
}

.modal-body input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e2e6e0;
  border-radius: 12px;
  font-size: 14px;
  box-sizing: border-box;
  background: white;
}

.modal-body input:focus {
  outline: none;
  border-color: #8fc1b0;
}

.error-hint {
  display: block;
  font-size: 12px;
  color: #e8a4a4;
  margin-top: 4px;
}

.btn-cancel {
  background: none;
  border: 1px solid #e2e6e0;
  padding: 8px 20px;
  border-radius: 30px;
  color: #8b9a8e;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  border-color: #8fc1b0;
  color: #8fc1b0;
}

.btn-save {
  background: #8fc1b0;
  border: none;
  padding: 8px 20px;
  border-radius: 30px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-save:hover:not(:disabled) {
  background: #7aa992;
}

.btn-save:disabled {
  background: #e2e6e0;
  cursor: not-allowed;
}
</style>
