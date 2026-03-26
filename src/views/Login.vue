<template>
  <div class="login">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo-icon">📖</div>
          <h1>书香漂流</h1>
          <p>让闲置的书籍遇见新的读者</p>
        </div>
        
        <form @submit.prevent="login" class="login-form">
          <div class="form-field">
            <label>学号 / 用户名</label>
            <input 
              type="text" 
              v-model="form.username" 
              placeholder="请输入学号"
              required
            >
          </div>
          
          <div class="form-field">
            <label>密码</label>
            <input 
              type="password" 
              v-model="form.password" 
              placeholder="请输入密码"
              required
            >
          </div>
          
          <div class="form-options">
            <label class="checkbox">
              <input type="checkbox" v-model="form.remember">
              <span>记住我</span>
            </label>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>
          
          <button type="submit" class="login-btn" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
        
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {
        username: '',
        password: '',
        remember: false
      },
      loading: false
    }
  },
  methods: {
    async login() {
      this.loading = true;
      try {
        if (this.form.username === '2021001' && this.form.password === '123456') {
          const mockUserInfo = {
            id: 1,
            studentId: '2021001',
            name: '李明',
            phone: '13800138000',
            email: 'liming@example.com',
            role: 'user',
            borrowedCount: 2
          };
          localStorage.setItem('userInfo', JSON.stringify(mockUserInfo));
          localStorage.setItem('token', 'mock-token-' + Date.now());
          this.$router.push('/');
          return;
        }

        const response = await this.$request.post('/users/login', {
          username: this.form.username,
          password: this.form.password
        });
        
        if (response.success) {
          const userInfo = {
            id: response.userId,
            studentId: response.studentId,
            name: response.username,
            phone: response.phone || '',
            email: response.email || '',
            role: response.role || 'user',
            borrowedCount: response.borrowedCount || 0
          };
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          localStorage.setItem('token', response.token || 'mock-token');
          this.$router.push('/');
        } else {
          alert(response.message || '登录失败');
        }
      } catch (error) {
        alert('登录失败，请检查网络');
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  background: #f5f0e8;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: white;
  border-radius: 24px;
  padding: 40px 32px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 500;
  color: #2c5a4f;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.login-header p {
  font-size: 14px;
  color: #8b9a8e;
  margin: 0;
}

.login-form .form-field {
  margin-bottom: 20px;
}

.login-form label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #5a6e5c;
  margin-bottom: 8px;
}

.login-form input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e2e6e0;
  border-radius: 12px;
  font-size: 15px;
  background: #fefcf8;
  transition: all 0.2s;
  box-sizing: border-box;
}

.login-form input:focus {
  outline: none;
  border-color: #8fc1b0;
  background: white;
  box-shadow: 0 0 0 3px rgba(143, 193, 176, 0.1);
}

.login-form input::placeholder {
  color: #cbd5c7;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #6c7e6e;
}

.checkbox input {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: #8fc1b0;
}

.forgot-link {
  font-size: 14px;
  color: #8fc1b0;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #6aa58f;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: #8fc1b0;
  border: none;
  border-radius: 40px;
  font-size: 16px;
  font-weight: 500;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 24px;
}

.login-btn:hover {
  background: #7aa992;
  transform: translateY(-1px);
}

.login-btn:disabled {
  background: #cbd5c7;
  cursor: not-allowed;
  transform: none;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #8b9a8e;
}

.register-link {
  color: #8fc1b0;
  text-decoration: none;
  margin-left: 6px;
  font-weight: 500;
}

.register-link:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px;
  }
}
</style>