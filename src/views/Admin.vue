<template>
  <div class="admin">
    <!-- 添加返回按钮 -->
    <div class="back-nav">
      <router-link to="/" class="back-link">
        <span class="back-arrow">�?/span> 返回首页
      </router-link>
    </div>

    <h2>管理员中�?/h2>
    <div class="admin-nav">
      <button @click="activeTab = 'users'" :class="{ active: activeTab === 'users' }">用户管理</button>
      <button @click="activeTab = 'books'" :class="{ active: activeTab === 'books' }">书籍管理</button>
      <button @click="activeTab = 'statistics'" :class="{ active: activeTab === 'statistics' }">系统统计</button>
    </div>
    
    <div class="admin-content">
      <!-- 加载状�?-->
      <div v-if="loading" class="loading-container">
        <div class="loading-dots">
          <span></span><span></span><span></span>
        </div>
        <p>加载�?..</p>
      </div>
      
      <!-- 用户管理 -->
      <div v-else-if="activeTab === 'users'" class="users-tab">
        <h3>用户列表</h3>
        <div class="search-bar">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索用户�?学号"
            @keyup.enter="searchUsers"
          >
          <button class="btn btn-primary" @click="searchUsers">搜索</button>
          <button class="btn btn-outline" @click="resetSearch">重置</button>
        </div>
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>学号</th>
                <th>姓名</th>
                <th>手机�?/th>
                <th>邮箱</th>
                <th>角色</th>
                <th>借阅�?/th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.studentId }}</td>
                <td>{{ user.name }}</td>
                <td>{{ user.phone || '-' }}</td>
                <td>{{ user.email || '-' }}</td>
                <td>
                  <select v-model="user.role" @change="updateUserRole(user)" :disabled="user.id === currentUserId" class="role-select">
                    <option value="user">普通用�?/option>
                    <option value="admin">管理�?/option>
                  </select>
                </td>
                <td>{{ user.borrowedCount || 0 }}�?/td>
                <td>
                  <button class="btn btn-danger" @click="deleteUser(user)" :disabled="user.id === currentUserId">
                    删除
                  </button>
                </td>
              </tr>
              <tr v-if="users.length === 0">
                <td colspan="8" class="empty-data">暂无用户数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <!-- 书籍管理 -->
      <div v-else-if="activeTab === 'books'" class="books-tab">
        <h3>书籍列表</h3>
        <div class="search-bar">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索书名/作�?ISBN"
            @keyup.enter="searchBooks"
          >
          <button class="btn btn-primary" @click="searchBooks">搜索</button>
          <button class="btn btn-outline" @click="resetSearch">重置</button>
        </div>
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>书名</th>
                <th>作�?/th>
                <th>ISBN</th>
                <th>出版�?/th>
                <th>状�?/th>
                <th>捐赠�?/th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="book in books" :key="book.id">
                <td>{{ book.id }}</td>
                <td>
                  <router-link :to="`/book/${book.id}`" class="book-link">
                    {{ book.title }}
                  </router-link>
                </td>
                <td>{{ book.author }}</td>
                <td>{{ book.isbn || '-' }}</td>
                <td>{{ book.publisher || '-' }}</td>
                <td>
                  <span :class="['status-badge', book.status]">
                    {{ book.status === 'available' ? '可认�? : '已借出' }}
                  </span>
                </td>
                <td>{{ book.donorName || '匿名' }}</td>
                <td>
                  <button class="btn btn-edit" @click="editBook(book)">编辑</button>
                  <button class="btn btn-danger" @click="deleteBook(book)">删除</button>
                </td>
              </tr>
              <tr v-if="books.length === 0">
                <td colspan="8" class="empty-data">暂无书籍数据</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 编辑书籍弹窗 -->
        <div v-if="editDialogVisible" class="modal-overlay" @click="closeEditDialog">
          <div class="modal-container" @click.stop>
            <div class="modal-header">
              <h3>编辑书籍</h3>
              <button class="close-btn" @click="closeEditDialog">&times;</button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label>书名</label>
                <input type="text" v-model="editForm.title">
              </div>
              <div class="form-group">
                <label>作�?/label>
                <input type="text" v-model="editForm.author">
              </div>
              <div class="form-group">
                <label>ISBN</label>
                <input type="text" v-model="editForm.isbn">
              </div>
              <div class="form-group">
                <label>出版�?/label>
                <input type="text" v-model="editForm.publisher">
              </div>
              <div class="form-group">
                <label>描述</label>
                <textarea v-model="editForm.description" rows="3"></textarea>
              </div>
              <div class="form-group">
                <label>状�?/label>
                <select v-model="editForm.status">
                  <option value="available">可认�?/option>
                  <option value="borrowed">已借出</option>
                </select>
              </div>
              <div class="form-group">
                <label>成色</label>
                <select v-model="editForm.condition">
                  <option value="new">崭新</option>
                  <option value="good">良好</option>
                  <option value="normal">正常</option>
                  <option value="worn">破旧</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-outline" @click="closeEditDialog">取消</button>
              <button class="btn btn-primary" @click="saveBookEdit">保存</button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 系统统计 -->
      <div v-else-if="activeTab === 'statistics'" class="statistics-tab">
        <h3>系统统计</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ statistics.totalBooks || 0 }}</div>
            <div class="stat-label">总书籍数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ statistics.availableBooks || 0 }}</div>
            <div class="stat-label">可认领书�?/div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ statistics.borrowedBooks || 0 }}</div>
            <div class="stat-label">已借出书籍</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ statistics.totalDonations || 0 }}</div>
            <div class="stat-label">总捐赠数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ statistics.totalBorrows || 0 }}</div>
            <div class="stat-label">总借阅�?/div>
          </div>
        </div>
        <button class="refresh-btn" @click="loadStatistics">刷新统计</button>
      </div>
    </div>
  </div>
</template>
<script>
import axios from 'axios'

export default {
  data() {
    return {
      activeTab: 'users',
      searchKeyword: '',
      loading: false,
      users: [],
      books: [],
      statistics: {},
      editDialogVisible: false,
      editForm: {
        id: null,
        title: '',
        author: '',
        isbn: '',
        publisher: '',
        description: '',
        status: '',
        condition: ''
      },
      currentUserId: null,
      apiBaseUrl: 'import.meta.env.VITE_API_BASE_URL'
    }
  },
  mounted() {
    // 获取当前登录用户ID
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      try {
        this.currentUserId = JSON.parse(userInfo).id
      } catch (e) {
        console.error('解析用户信息失败', e)
      }
    }
    this.loadUsers()
    this.loadStatistics()
  },
  methods: {
    // 加载用户列表
    async loadUsers() {
      this.loading = true
      try {
        const token = localStorage.getItem('token')
        const response = await axios.get(`${this.apiBaseUrl}/users`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (Array.isArray(response.data)) {
          this.users = response.data
        } else if (response.data.success && Array.isArray(response.data.data)) {
          this.users = response.data.data
        } else {
          console.error('用户数据格式错误', response.data)
          this.users = []
        }
      } catch (error) {
        console.error('加载用户失败:', error)
        this.$message?.error('加载用户失败')
      } finally {
        this.loading = false
      }
    },
    
    // 搜索用户
    async searchUsers() {
      if (!this.searchKeyword.trim()) {
        this.loadUsers()
        return
      }
      this.loading = true
      try {
        const token = localStorage.getItem('token')
        const response = await axios.get(`${this.apiBaseUrl}/users/search`, {
          params: { keyword: this.searchKeyword },
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (response.data.success && Array.isArray(response.data.data)) {
          this.users = response.data.data
        } else if (Array.isArray(response.data)) {
          this.users = response.data
        } else {
          this.users = []
        }
      } catch (error) {
        console.error('搜索失败:', error)
        this.$message?.error('搜索失败')
      } finally {
        this.loading = false
      }
    },
    
    // 重置搜索
    resetSearch() {
      this.searchKeyword = ''
      if (this.activeTab === 'users') {
        this.loadUsers()
      } else {
        this.loadBooks()
      }
    },
    
    // 更新用户角色
    async updateUserRole(user) {
      if (user.id === this.currentUserId) {
        this.$message?.warning('不能修改自己的角�?)
        // 恢复原角�?        await this.loadUsers()
        return
      }
      try {
        const token = localStorage.getItem('token')
        const response = await axios.put(`${this.apiBaseUrl}/users/${user.id}/role`, 
          { role: user.role },
          { headers: { 'Authorization': `Bearer ${token}` } }
        )
        if (response.data.success) {
          this.$message?.success('角色更新成功')
        } else {
          this.$message?.error(response.data.message || '更新失败')
          await this.loadUsers()
        }
      } catch (error) {
        console.error('更新角色失败:', error)
        this.$message?.error('更新角色失败')
        await this.loadUsers()
      }
    },
    
    // 删除用户
    async deleteUser(user) {
      if (user.id === this.currentUserId) {
        this.$message?.warning('不能删除自己的账�?)
        return
      }
      if (!confirm(`确定要删除用�?${user.name}"吗？`)) return
      
      try {
        const token = localStorage.getItem('token')
        const response = await axios.delete(`${this.apiBaseUrl}/users/${user.id}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (response.data.success) {
          this.$message?.success('删除成功')
          this.loadUsers()
          this.loadStatistics()
        } else {
          this.$message?.error(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error)
        this.$message?.error('删除失败')
      }
    },
    
    // 加载书籍列表
    async loadBooks() {
      this.loading = true
      try {
        const token = localStorage.getItem('token')
        const response = await axios.get(`${this.apiBaseUrl}/books/all`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (response.data.success && Array.isArray(response.data.data)) {
          this.books = response.data.data
        } else if (Array.isArray(response.data)) {
          this.books = response.data
        } else {
          console.error('书籍数据格式错误', response.data)
          this.books = []
        }
      } catch (error) {
        console.error('加载书籍失败:', error)
        this.$message?.error('加载书籍失败')
        // 使用备用数据
        this.books = []
      } finally {
        this.loading = false
      }
    },
    
    // 搜索书籍
    async searchBooks() {
      if (!this.searchKeyword.trim()) {
        this.loadBooks()
        return
      }
      this.loading = true
      try {
        const token = localStorage.getItem('token')
        const response = await axios.get(`${this.apiBaseUrl}/books/search`, {
          params: { keyword: this.searchKeyword },
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (response.data.success && Array.isArray(response.data.data)) {
          this.books = response.data.data
        } else if (Array.isArray(response.data)) {
          this.books = response.data
        } else {
          this.books = []
        }
      } catch (error) {
        console.error('搜索失败:', error)
        this.$message?.error('搜索失败')
      } finally {
        this.loading = false
      }
    },
    
    // 编辑书籍
    editBook(book) {
      this.editForm = {
        id: book.id,
        title: book.title,
        author: book.author,
        isbn: book.isbn || '',
        publisher: book.publisher || '',
        description: book.description || '',
        status: book.status,
        condition: book.condition || 'normal',
        coverUrl: book.coverUrl || ''
      }
      this.editDialogVisible = true
    },
    
    // 关闭编辑弹窗
    closeEditDialog() {
      this.editDialogVisible = false
      this.editForm = {
        id: null,
        title: '',
        author: '',
        isbn: '',
        publisher: '',
        description: '',
        status: '',
        condition: ''
      }
    },
    
    // 保存书籍编辑
    async saveBookEdit() {
      if (!this.editForm.title || !this.editForm.author) {
        this.$message?.warning('书名和作者不能为�?)
        return
      }
      try {
        const token = localStorage.getItem('token')
        const response = await axios.put(`${this.apiBaseUrl}/books/${this.editForm.id}`, this.editForm, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (response.data.success) {
          this.$message?.success('更新成功')
          this.closeEditDialog()
          this.loadBooks()

          // 触发全局事件，通知其他组件书籍状态已变化
      window.dispatchEvent(new CustomEvent('book-status-changed', {
        detail: { bookId: this.editForm.id, status: this.editForm.status }
      }))
        } else {
          this.$message?.error(response.data.message || '更新失败')
        }
      } catch (error) {
        console.error('更新失败:', error)
        this.$message?.error('更新失败')
      }
    },
    
    // 删除书籍
    async deleteBook(book) {
      if (!confirm(`确定要删除书�?${book.title}"吗？此操作不可恢复！`)) return
      
      try {
        const token = localStorage.getItem('token')
        const response = await axios.delete(`${this.apiBaseUrl}/books/${book.id}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (response.data.success) {
          this.$message?.success('删除成功')
          this.loadBooks()
          this.loadStatistics()
        } else {
          this.$message?.error(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error)
        this.$message?.error('删除失败')
      }
    },
    
    // 加载统计信息
    async loadStatistics() {
      try {
        const token = localStorage.getItem('token')
        const response = await axios.get(`${this.apiBaseUrl}/statistics/system`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (response.data.success && response.data.data) {
          this.statistics = response.data.data
        } else if (response.data) {
          this.statistics = response.data
        } else {
          this.statistics = {}
        }
      } catch (error) {
        console.error('加载统计失败:', error)
        this.statistics = {}
      }
    }
  },
  watch: {
    activeTab(newTab) {
      this.searchKeyword = ''
      if (newTab === 'users') {
        this.loadUsers()
      } else if (newTab === 'books') {
        this.loadBooks()
      } else if (newTab === 'statistics') {
        this.loadStatistics()
      }
    }
  }
}
</script>

<style scoped>
/* ==================== 清新风格样式 ==================== */
.admin {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  background: #faf8f4;
  min-height: calc(100vh - 60px);
  border-radius: 0;
  box-shadow: none;
}

/* 返回按钮样式 */
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

.back-arrow {
  font-size: 16px;
}

h2 {
  text-align: center;
  font-size: 28px;
  font-weight: 500;
  color: #2c5a4f;
  margin-bottom: 28px;
}

/* 导航标签 */
.admin-nav {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
  gap: 8px;
  border-bottom: none;
}

.admin-nav button {
  padding: 10px 28px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 15px;
  color: #8b9a8e;
  border-radius: 40px;
  transition: all 0.2s;
  font-weight: 500;
}

.admin-nav button:hover {
  color: #8fc1b0;
  background: #f5f0e8;
}

.admin-nav button.active {
  color: white;
  background: #8fc1b0;
  border-bottom: none;
}

.admin-content {
  min-height: 400px;
}

/* 标题 */
h3 {
  font-size: 20px;
  font-weight: 500;
  color: #2c5a4f;
  margin-bottom: 20px;
}

/* 搜索�?*/
.search-bar {
  margin-bottom: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.search-bar input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e2e6e0;
  border-radius: 40px;
  font-size: 14px;
  background: white;
  transition: all 0.2s;
}

.search-bar input:focus {
  outline: none;
  border-color: #8fc1b0;
  box-shadow: 0 0 0 3px rgba(143, 193, 176, 0.1);
}

/* 按钮样式 */
.btn {
  padding: 8px 20px;
  border: none;
  border-radius: 40px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background: #8fc1b0;
  color: white;
}

.btn-primary:hover {
  background: #7aa992;
  transform: translateY(-1px);
}

.btn-outline {
  background: white;
  border: 1px solid #e2e6e0;
  color: #8b9a8e;
}

.btn-outline:hover {
  border-color: #8fc1b0;
  color: #8fc1b0;
}

.btn-edit {
  background: #e8f0ec;
  color: #8fc1b0;
  margin-right: 8px;
}

.btn-edit:hover {
  background: #dde9e2;
}

.btn-danger {
  background: #fef0ec;
  color: #e8a4a4;
}

.btn-danger:hover {
  background: #fce8e2;
  color: #e09494;
}

.btn-danger:disabled {
  background: #efebe6;
  color: #cbd5c7;
  cursor: not-allowed;
}

/* 表格样式 */
.table-wrapper {
  overflow-x: auto;
  border-radius: 20px;
  border: 1px solid #efebe6;
  background: white;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.data-table th {
  background: #faf8f4;
  padding: 14px 16px;
  text-align: left;
  font-weight: 500;
  color: #5a6e5c;
  border-bottom: 1px solid #efebe6;
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #efebe6;
  color: #6c826e;
}

.data-table tr:hover {
  background: #faf8f4;
}

.data-table tr:last-child td {
  border-bottom: none;
}

/* 角色选择�?*/
.role-select {
  padding: 6px 10px;
  border: 1px solid #e2e6e0;
  border-radius: 20px;
  background: white;
  font-size: 13px;
  color: #5a6e5c;
  cursor: pointer;
}

.role-select:focus {
  outline: none;
  border-color: #8fc1b0;
}

.role-select:disabled {
  background: #efebe6;
  cursor: not-allowed;
}

/* 书籍链接 */
.book-link {
  color: #8fc1b0;
  text-decoration: none;
}

.book-link:hover {
  text-decoration: underline;
}

/* 状态标�?*/
.status-badge {
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.available {
  background: #e8f0ec;
  color: #8fc1b0;
}

.status-badge.borrowed {
  background: #fef0ec;
  color: #e8a4a4;
}

.empty-data {
  text-align: center;
  color: #b8c4b0;
  padding: 48px !important;
}

/* 统计卡片 */
.statistics-tab {
  background: white;
  border-radius: 28px;
  padding: 24px;
  border: 1px solid #efebe6;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: #faf8f4;
  padding: 24px 20px;
  border-radius: 20px;
  text-align: center;
  transition: all 0.2s;
  border: 1px solid #efebe6;
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: #e2e6e0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.stat-value {
  font-size: 32px;
  font-weight: 500;
  color: #8fc1b0;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: #8b9a8e;
}

.refresh-btn {
  display: block;
  margin: 0 auto;
  padding: 10px 32px;
  background: #8fc1b0;
  color: white;
  border: none;
  border-radius: 40px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.refresh-btn:hover {
  background: #7aa992;
  transform: translateY(-1px);
}

/* 加载状�?*/
.loading-container {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 28px;
  border: 1px solid #efebe6;
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

.loading-container p {
  color: #b8c4b0;
  font-size: 14px;
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
  background-color: white;
  border-radius: 28px;
  width: 90%;
  max-width: 500px;
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
  padding: 20px 24px;
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
  transition: all 0.2s;
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

.modal-body {
  padding: 24px;
}

.modal-body .form-group {
  margin-bottom: 20px;
}

.modal-body label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #5a6e5c;
  font-size: 13px;
}

.modal-body input,
.modal-body select,
.modal-body textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e2e6e0;
  border-radius: 12px;
  font-size: 14px;
  box-sizing: border-box;
  background: white;
  transition: all 0.2s;
}

.modal-body input:focus,
.modal-body select:focus,
.modal-body textarea:focus {
  outline: none;
  border-color: #8fc1b0;
  box-shadow: 0 0 0 3px rgba(143, 193, 176, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #efebe6;
}

/* 响应�?*/
@media (max-width: 768px) {
  .admin {
    padding: 20px 16px;
  }
  
  h2 {
    font-size: 24px;
  }
  
  .admin-nav {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .admin-nav button {
    padding: 8px 20px;
    font-size: 14px;
  }
  
  .search-bar {
    flex-direction: column;
  }
  
  .search-bar input,
  .search-bar .btn {
    width: 100%;
  }
  
  .data-table {
    font-size: 12px;
  }
  
  .data-table th,
  .data-table td {
    padding: 10px 12px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .modal-container {
    width: 95%;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .admin-nav button {
    flex: 1;
    text-align: center;
  }
  
  .btn-edit, .btn-danger {
    margin-bottom: 4px;
    display: inline-block;
  }
}
</style>
