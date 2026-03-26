<template>
  <div class="donate-success">
    <div class="success-container">
      <div class="success-icon">✅</div>
      <h2>捐赠成功！</h2>
      <div class="success-info">
        <h3>{{ book.title }}</h3>
        <p>作者: {{ book.author }}</p>
        <p>书籍ID: {{ bookId }}</p>
      </div>
      <div class="success-message">
        <p>您的书籍已成功捐赠到校园闲置书籍漂流系统</p>
        <p>请保存好书籍专属二维码，用于后续的认领和归还操作</p>
      </div>
      <div class="success-actions">
        <router-link to="/" class="btn primary-btn">返回首页</router-link>
        <router-link to="/donate" class="btn secondary-btn">继续捐赠</router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      book: {
        title: '',
        author: ''
      },
      bookId: ''
    }
  },
  mounted() {
    // 从路由参数获取书籍信息
    const routeParams = this.$route.query
    this.book.title = routeParams.title
    this.book.author = routeParams.author
    this.bookId = routeParams.bookId
  }
}
</script>

<style scoped>
.donate-success {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  padding: 20px;
}

.success-container {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  padding: 40px;
  max-width: 500px;
  width: 100%;
  text-align: center;
}

.success-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

h2 {
  color: #4CAF50;
  margin-bottom: 30px;
  font-size: 28px;
}

.success-info {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.success-info h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #333;
}

.success-info p {
  color: #666;
  margin-bottom: 5px;
}

.success-message {
  margin-bottom: 30px;
  line-height: 1.6;
  color: #555;
}

.success-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
  text-decoration: none;
  display: inline-block;
}

.primary-btn {
  background-color: #4CAF50;
  color: white;
}

.primary-btn:hover {
  background-color: #45a049;
}

.secondary-btn {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.secondary-btn:hover {
  background-color: #e0e0e0;
}

@media (max-width: 768px) {
  .success-container {
    padding: 30px 20px;
  }
  
  .success-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .btn {
    width: 200px;
  }
}
</style>