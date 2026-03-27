<template>
  <div class="register">
    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <a href="#" @click.prevent="$router.push('/login')" class="back-link">
            <span class="back-arrow">←</span> 返回登录
          </a>
          <div class="header-main">
            <div class="logo-icon">📖</div>
            <h1>加入书香漂流</h1>
            <p>开启你的阅读分享之旅</p>
          </div>
        </div>
        
        <form @submit.prevent="register" class="register-form">
          <div class="form-row">
            <div class="form-field">
              <label>学号</label>
              <input 
                type="text" 
                v-model="form.studentId" 
                placeholder="10位学号"
                maxlength="10"
                :class="{ error: errors.studentId }"
                @input="validateStudentId"
                @blur="validateStudentId"
              >
              <span v-if="errors.studentId" class="error-hint">{{ errors.studentId }}</span>
            </div>
            
            <div class="form-field">
              <label>姓名</label>
              <input 
                type="text" 
                v-model="form.name" 
                placeholder="真实姓名"
                :class="{ error: errors.name }"
                @input="validateName"
                @blur="validateName"
              >
              <span v-if="errors.name" class="error-hint">{{ errors.name }}</span>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-field">
              <label>手机号</label>
              <input 
                type="tel" 
                v-model="form.phone" 
                placeholder="11位手机号"
                maxlength="11"
                :class="{ error: errors.phone }"
                @input="validatePhone"
                @blur="validatePhone"
              >
              <span v-if="errors.phone" class="error-hint">{{ errors.phone }}</span>
            </div>
            
            <div class="form-field">
              <label>邮箱</label>
              <input 
                type="email" 
                v-model="form.email" 
                placeholder="your@email.com"
                :class="{ error: errors.email }"
                @input="validateEmail"
                @blur="validateEmail"
              >
              <span v-if="errors.email" class="error-hint">{{ errors.email }}</span>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-field">
              <label>密码</label>
              <input 
                type="password" 
                v-model="form.password" 
                placeholder="至少6位"
                :class="{ error: errors.password }"
                @input="validatePassword"
                @blur="validatePassword"
              >
              <span v-if="errors.password" class="error-hint">{{ errors.password }}</span>
            </div>
            
            <div class="form-field">
              <label>确认密码</label>
              <input 
                type="password" 
                v-model="form.confirmPassword" 
                placeholder="再次输入密码"
                :class="{ error: errors.confirmPassword }"
                @input="validateConfirmPassword"
                @blur="validateConfirmPassword"
              >
              <span v-if="errors.confirmPassword" class="error-hint">{{ errors.confirmPassword }}</span>
            </div>
          </div>
          
          <button type="submit" class="register-btn" :disabled="loading || !isFormValid">
            {{ loading ? '注册中...' : '注册账号' }}
          </button>
        </form>
        
        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">去登录</router-link>
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
        studentId: '',
        name: '',
        phone: '',
        email: '',
        password: '',
        confirmPassword: ''
      },
      errors: {
        studentId: '',
        name: '',
        phone: '',
        email: '',
        password: '',
        confirmPassword: ''
      },
      loading: false
    }
  },
  computed: {
    isFormValid() {
      return !Object.values(this.errors).some(error => error) &&
             this.form.studentId &&
             this.form.name &&
             this.form.phone &&
             this.form.email &&
             this.form.password &&
             this.form.confirmPassword &&
             this.form.password === this.form.confirmPassword
    }
  },
  methods: {
    validateStudentId() {
      const val = this.form.studentId
      if (!val) this.errors.studentId = ''
      else if (!/^\d{10}$/.test(val)) this.errors.studentId = '学号必须为10位数字'
      else this.errors.studentId = ''
    },
    validateName() {
      const val = this.form.name
      if (!val) this.errors.name = ''
      else if (val.length < 2) this.errors.name = '姓名至少2个字'
      else if (!/^[\u4e00-\u9fa5]{2,}$/.test(val)) this.errors.name = '请输入中文姓名'
      else this.errors.name = ''
    },
    validatePhone() {
      const val = this.form.phone
      if (!val) this.errors.phone = ''
      else if (!/^1[3-9]\d{9}$/.test(val)) this.errors.phone = '请输入正确的手机号'
      else this.errors.phone = ''
    },
    validateEmail() {
      const val = this.form.email
      const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!val) this.errors.email = ''
      else if (!regex.test(val)) this.errors.email = '请输入正确的邮箱'
      else this.errors.email = ''
    },
    validatePassword() {
      const val = this.form.password
      if (!val) this.errors.password = ''
      else if (val.length < 6) this.errors.password = '密码至少6位'
      else this.errors.password = ''
      if (this.form.confirmPassword) this.validateConfirmPassword()
    },
    validateConfirmPassword() {
      const val = this.form.confirmPassword
      if (!val) this.errors.confirmPassword = ''
      else if (val !== this.form.password) this.errors.confirmPassword = '两次密码不一致'
      else this.errors.confirmPassword = ''
    },
    async register() {
      this.validateStudentId()
      this.validateName()
      this.validatePhone()
      this.validateEmail()
      this.validatePassword()
      this.validateConfirmPassword()
      
      if (!this.isFormValid) {
        alert('请正确填写所有信息')
        return
      }
      
      this.loading = true
      try {
        const response = await this.$request.post('/auth/register', {
          studentId: this.form.studentId,
          name: this.form.name,
          phone: this.form.phone,
          email: this.form.email,
          password: this.form.password
        })
        
        if (response.success) {
          alert('注册成功！请登录')
          this.$router.push('/login')
        } else {
          alert(response.message || '注册失败')
        }
      } catch (error) {
        alert('注册失败，请稍后重试')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register {
  min-height: 100vh;
  background: #f5f0e8;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.register-container {
  width: 100%;
  max-width: 680px;
}

.register-card {
  background: white;
  border-radius: 28px;
  padding: 32px 36px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
}

.register-header {
  margin-bottom: 32px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #8b9a8e;
  text-decoration: none;
  font-size: 14px;
  margin-bottom: 20px;
  transition: color 0.2s;
}

.back-link:hover {
  color: #8fc1b0;
}

.back-arrow {
  font-size: 16px;
}

.header-main {
  text-align: center;
}

.logo-icon {
  font-size: 44px;
  margin-bottom: 12px;
}

.header-main h1 {
  font-size: 26px;
  font-weight: 500;
  color: #2c5a4f;
  margin: 0 0 6px 0;
}

.header-main p {
  font-size: 14px;
  color: #8b9a8e;
  margin: 0;
}

.register-form {
  margin-bottom: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 16px;
}

.form-field {
  margin-bottom: 8px;
}

.form-field label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #5a6e5c;
  margin-bottom: 6px;
}

.form-field input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e2e6e0;
  border-radius: 12px;
  font-size: 14px;
  background: #fefcf8;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-field input:focus {
  outline: none;
  border-color: #8fc1b0;
  background: white;
}

.form-field input.error {
  border-color: #e8a4a4;
}

.error-hint {
  display: block;
  font-size: 11px;
  color: #e8a4a4;
  margin-top: 4px;
}

.register-btn {
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
  margin-top: 16px;
}

.register-btn:hover {
  background: #7aa992;
}

.register-btn:disabled {
  background: #cbd5c7;
  cursor: not-allowed;
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #8b9a8e;
}

.login-link {
  color: #8fc1b0;
  text-decoration: none;
  margin-left: 6px;
  font-weight: 500;
}

@media (max-width: 600px) {
  .register-card {
    padding: 28px 20px;
  }
  .form-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>