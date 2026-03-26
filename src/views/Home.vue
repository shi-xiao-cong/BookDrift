<template>
  <div class="home">
    <nav class="navbar">
      <div class="nav-container">
        <div class="logo">
          <span class="logo-icon">📖</span>
          <span class="logo-text">书香漂流</span>
        </div>
        
        <div class="nav-links">
          <router-link to="/user" class="nav-link">我的</router-link>
          <router-link v-if="isAdmin" to="/admin" class="nav-link">管理</router-link>
          <button @click="logout" class="logout-btn">退�?/button>
        </div>
        
        <button class="menu-toggle" @click="showMobileMenu = !showMobileMenu">�?/button>
      </div>
      
      <div class="mobile-menu" v-if="showMobileMenu">
        <router-link to="/user" @click="showMobileMenu = false">我的</router-link>
        <router-link v-if="isAdmin" to="/admin" @click="showMobileMenu = false">管理</router-link>
        <button @click="logout">退�?/button>
      </div>
    </nav>
    
    <main class="main">
      <section class="hero">
        <div class="hero-content">
          <h1>让闲置的书籍 遇见新的读�?/h1>
          <p>捐赠、认领、归还，让每一本书都继续它的旅�?/p>
          <div class="hero-buttons">
            <router-link to="/donate" class="btn-primary">📖 捐赠书籍</router-link>
            <router-link to="/borrow" class="btn-secondary">�?认领书籍</router-link>
            <router-link to="/scan" class="btn-secondary">🔄 归还书籍</router-link>
          </div>
        </div>
      </section>
      
      <section class="search-section">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索书名、作�?.."
            @keyup.enter="handleSearch"
          >
          <button @click="handleSearch">搜索</button>
        </div>
        <div class="search-info" v-if="!loading">
          <span>{{ searchKeyword ? `找到 ${filteredBooks.length} 本书` : `�?${books.length} 本可认领` }}</span>
          <button v-if="searchKeyword" @click="clearSearch" class="clear-search">清除</button>
        </div>
      </section>
      
      <section class="books-section">
        <div class="section-header">
          <h2>可认领书�?/h2>
          <select v-model="sortOption" class="sort-select" @change="sortBooks">
            <option value="default">默认排序</option>
            <option value="title-asc">书名 A-Z</option>
            <option value="author-asc">作�?A-Z</option>
          </select>
        </div>
        
        <div v-if="loading" class="loading-state">
          <div class="loading-dots">
            <span></span><span></span><span></span>
          </div>
          <p>加载�?..</p>
        </div>
        
        <div v-else-if="filteredBooks.length === 0" class="empty-state">
          <div class="empty-icon">📚</div>
          <h3>暂无书籍</h3>
          <p v-if="searchKeyword">没有找到"{{ searchKeyword }}"相关的书�?/p>
          <p v-else>暂时没有可认领的书籍</p>
          <router-link v-if="!searchKeyword" to="/donate" class="empty-btn">去捐�?/router-link>
        </div>
        
        <div v-else class="book-grid">
          <div 
            v-for="book in sortedBooks" 
            :key="book.id" 
            class="book-card"
            @click="goToBookDetail(book.id)"
          >
            <div class="book-cover">
              <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title">
              <div v-else class="cover-placeholder">
                <span>{{ book.title.charAt(0) }}</span>
              </div>
              <span class="book-status" :class="book.status">
                {{ book.status === 'available' ? '可认�? : '已借出' }}
              </span>
            </div>
            <div class="book-info">
              <h3 class="book-title">{{ book.title }}</h3>
              <p class="book-author">{{ book.author }}</p>
              <div class="book-meta">
                <div class="meta-top">
                <span class="book-condition" :class="book.condition">
                  {{ getConditionText(book.condition) }}
                  </span>
                <span class="donor">捐赠者：{{ book.donorName || '匿名捐赠' }}</span>
                </div>
                <div class="meta-bottom">
                <span class="detail">详情 �?/span>
              </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
    
    <button v-show="showScrollTop" @click="scrollToTop" class="scroll-top">�?/button>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Home',
  data() {
    return {
      books: [],
      filteredBooks: [],
      sortedBooks: [],
      searchKeyword: '',
      sortOption: 'default',
      loading: false,
      showScrollTop: false,
      showMobileMenu: false,
      apiBaseUrl: 'import.meta.env.VITE_API_BASE_URL'
    }
  },
  computed: {
    isAdmin() {
      const userInfo = localStorage.getItem('userInfo')
      return userInfo ? JSON.parse(userInfo).role === 'admin' : false
    }
  },
  watch: {
    searchKeyword() {
      this.filterBooks()
    },
    filteredBooks: {
      handler() { this.sortBooks() },
      immediate: true
    }
  },
  mounted() {
    const token = localStorage.getItem('token')
    if (!token) {
      this.$router.push('/login')
      return
    }
    this.fetchAvailableBooks()
    window.addEventListener('scroll', this.handleScroll)
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.handleScroll)
  },
  methods: {
    async fetchAvailableBooks() {
      this.loading = true
      try {
        const token = localStorage.getItem('token')
        const response = await axios.get(`${this.apiBaseUrl}/books/available`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        
        if (Array.isArray(response.data)) {
          this.books = response.data
        } else if (response.data.success && Array.isArray(response.data.data)) {
          this.books = response.data.data
        } else {
          this.useMockBooks()
        }
        this.filteredBooks = [...this.books]
      } catch (error) {
        this.useMockBooks()
        this.filteredBooks = [...this.books]
      } finally {
        this.loading = false
      }
    },
    getConditionText(condition) {
    const map = {
      new: '崭新',
      good: '良好',
      normal: '正常',
      worn: '破旧'
    }
    return map[condition] || condition || '未标�?
  },
    useMockBooks() {
      this.books = [
        { id: 1, title: '瓦尔登湖', author: '亨利·梭罗', publisher: '上海译文出版�?, condition: 'good', donorName: '林同�?, status: 'available', coverUrl: '' },
        { id: 2, title: '小王�?, author: '圣埃克苏佩里', publisher: '人民文学出版�?, condition: 'new', donorName: '陈同�?, status: 'available', coverUrl: '' },
        { id: 3, title: '活着', author: '余华', publisher: '作家出版�?, condition: 'normal', donorName: '王同�?, status: 'available', coverUrl: '' },
        { id: 4, title: '百年孤独', author: '加西亚·马尔克�?, publisher: '南海出版公司', condition: 'good', donorName: '张同�?, status: 'available', coverUrl: '' },
        { id: 5, title: '解忧杂货�?, author: '东野圭吾', publisher: '南海出版公司', condition: 'good', donorName: '李同�?, status: 'available', coverUrl: '' }
      ]
    },
    
    filterBooks() {
      if (!this.searchKeyword.trim()) {
        this.filteredBooks = [...this.books]
      } else {
        const kw = this.searchKeyword.toLowerCase().trim()
        this.filteredBooks = this.books.filter(book => 
          book.title.toLowerCase().includes(kw) ||
          book.author.toLowerCase().includes(kw)
        )
      }
    },
    
    sortBooks() {
      let sorted = [...this.filteredBooks]
      if (this.sortOption === 'title-asc') {
        sorted.sort((a, b) => a.title.localeCompare(b.title))
      } else if (this.sortOption === 'author-asc') {
        sorted.sort((a, b) => a.author.localeCompare(b.author))
      }
      this.sortedBooks = sorted
    },
    
    handleSearch() { this.filterBooks() },
    clearSearch() { this.searchKeyword = '' },
    goToBookDetail(id) { this.$router.push(`/book/${id}`) },
    handleScroll() { this.showScrollTop = window.scrollY > 400 },
    scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }) },
    logout() {
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.home {
  min-height: 100vh;
  background: #faf8f4;
}

/* 导航�?*/
.navbar {
  background: white;
  border-bottom: 1px solid #efebe6;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 20px;
  font-weight: 500;
  color: #2c5a4f;
  letter-spacing: 1px;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-link {
  color: #5a6e5c;
  text-decoration: none;
  font-size: 15px;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #8fc1b0;
}

.nav-link.router-link-active {
  color: #8fc1b0;
}

.logout-btn {
  background: none;
  border: none;
  color: #c9b6a0;
  font-size: 15px;
  cursor: pointer;
  padding: 0;
  transition: color 0.2s;
}

.logout-btn:hover {
  color: #e8a4a4;
}

.menu-toggle {
  display: none;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #5a6e5c;
}

.mobile-menu {
  display: none;
  padding: 16px 24px;
  background: white;
  border-top: 1px solid #efebe6;
}

.mobile-menu a, .mobile-menu button {
  display: block;
  padding: 12px 0;
  color: #5a6e5c;
  text-decoration: none;
  font-size: 16px;
  background: none;
  border: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.mobile-menu a:hover, .mobile-menu button:hover {
  color: #8fc1b0;
}

/* Hero 区域 - 三个按钮都在这里 */
.hero {
  background: linear-gradient(135deg, #e8f0ec 0%, #dde9e2 100%);
  padding: 60px 24px;
  text-align: center;
}

.hero-content h1 {
  font-size: 42px;
  font-weight: 500;
  color: #2c5a4f;
  margin-bottom: 16px;
  line-height: 1.2;
}

.hero-content p {
  font-size: 16px;
  color: #6c826e;
  margin-bottom: 32px;
}

.hero-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-primary {
  background: #8fc1b0;
  color: white;
  padding: 12px 28px;
  border-radius: 40px;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-primary:hover {
  background: #7aa992;
  transform: translateY(-2px);
}

.btn-secondary {
  background: white;
  color: #8fc1b0;
  padding: 12px 28px;
  border-radius: 40px;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  border: 1px solid #d4e0d8;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-secondary:hover {
  border-color: #8fc1b0;
  transform: translateY(-2px);
}

/* 搜索区域 */
.search-section {
  max-width: 600px;
  margin: -28px auto 32px;
  padding: 0 24px;
}

.search-box {
  display: flex;
  gap: 8px;
  background: white;
  border-radius: 48px;
  padding: 4px 4px 4px 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #efebe6;
}

.search-box input {
  flex: 1;
  border: none;
  padding: 12px 0;
  font-size: 15px;
  background: transparent;
  outline: none;
  color: #2c5a4f;
}

.search-box input::placeholder {
  color: #cbd5c7;
}

.search-box button {
  background: #8fc1b0;
  border: none;
  padding: 8px 24px;
  border-radius: 40px;
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-box button:hover {
  background: #7aa992;
}

.search-info {
  text-align: center;
  margin-top: 12px;
  font-size: 13px;
  color: #8b9a8e;
}

.clear-search {
  background: none;
  border: none;
  color: #c9b6a0;
  margin-left: 12px;
  cursor: pointer;
  font-size: 13px;
}

/* 书籍区域 */
.books-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 12px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 500;
  color: #2c5a4f;
}

.sort-select {
  padding: 8px 16px;
  border: 1px solid #e2e6e0;
  border-radius: 30px;
  background: white;
  font-size: 14px;
  color: #5a6e5c;
  cursor: pointer;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
}

.book-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.2s;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  border: 1px solid #efebe6;
}

.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
  border-color: #e2e6e0;
}

.book-cover {
  position: relative;
  padding-top: 140%;
  background: #f5f0e8;
  overflow: hidden;
}

.book-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f0ec, #dde9e2);
  font-size: 48px;
  font-weight: 500;
  color: #8fc1b0;
}

.book-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.book-status.available {
  color: #8fc1b0;
}

.book-status.borrowed {
  color: #c9b6a0;
}

.book-info {
  padding: 16px;
}

.book-title {
  font-size: 16px;
  font-weight: 500;
  color: #2c5a4f;
  margin-bottom: 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-author {
  font-size: 13px;
  color: #8b9a8e;
  margin-bottom: 12px;
}

.book-condition {
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
}

.book-condition.new {
  background: #e8f0ec;
  color: #8fc1b0;
}

.book-condition.good {
  background: #e8f0ec;
  color: #8fc1b0;
}

.book-condition.normal {
  background: #f5f0e8;
  color: #c9b6a0;
}

.book-condition.worn {
  background: #fef0ec;
  color: #e8a4a4;
}

.book-meta {
  border-top: 1px solid #efebe6;
  padding-top: 12px;
}

.meta-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.meta-bottom {
  text-align: right;
}
.donor {
  color: #b8c4b0;
}

.detail {
  color: #8fc1b0;
  transition: transform 0.2s;
}

.book-card:hover .detail {
  transform: translateX(4px);
}

/* 加载状�?*/
.loading-state {
  text-align: center;
  padding: 60px;
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

/* 空状�?*/
.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 24px;
  border: 1px solid #efebe6;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 18px;
  color: #5a6e5c;
  margin-bottom: 8px;
}

.empty-state p {
  color: #8b9a8e;
  margin-bottom: 20px;
}

.empty-btn {
  display: inline-block;
  padding: 8px 24px;
  background: #8fc1b0;
  color: white;
  text-decoration: none;
  border-radius: 40px;
  font-size: 14px;
}

/* 回到顶部 */
.scroll-top {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  border: 1px solid #efebe6;
  color: #8fc1b0;
  font-size: 20px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
}

.scroll-top:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 响应�?*/
@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  
  .menu-toggle {
    display: block;
  }
  
  .mobile-menu {
    display: block;
  }
  
  .hero-content h1 {
    font-size: 32px;
  }
  
  .hero-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .btn-primary, .btn-secondary {
    width: 180px;
    justify-content: center;
  }
  
  .book-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .book-grid {
    grid-template-columns: 1fr;
  }
}
</style>
