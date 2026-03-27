<template>
  <div class="donate">
    <div class="back-nav">
      <router-link to="/" class="back-link">
        <span class="back-arrow">←</span> 返回首页
      </router-link>
    </div>
    <h2>📚 捐赠书籍</h2>
    
    <!-- 步骤条 -->
    <el-steps :active="activeStep" finish-status="success" simple style="margin-top: 20px">
      <el-step title="填写书籍信息" />
      <el-step title="生成二维码" />
      <el-step title="捐赠完成" />
    </el-steps>

    <!-- 步骤1：填写书籍信息 -->
    <div v-if="activeStep === 0" class="step-content">
      <el-card class="form-card">
        <template #header>
          <div class="card-header">
            <h3>📖 填写书籍信息</h3>
          </div>
        </template>

        <el-form :model="bookForm" label-width="80px" :rules="rules" ref="formRef">
          <el-form-item label="书名" prop="title">
            <el-input v-model="bookForm.title" placeholder="请输入书名" />
          </el-form-item>

          <el-form-item label="作者" prop="author">
            <el-input v-model="bookForm.author" placeholder="请输入作者" />
          </el-form-item>

          <el-form-item label="捐赠人" prop="donor">
            <el-input v-model="bookForm.donor" placeholder="请输入您的姓名" />
          </el-form-item>

          <el-form-item label="成色" prop="condition">
            <el-select v-model="bookForm.condition" placeholder="请选择书籍成色" style="width: 100%">
              <el-option
                v-for="item in conditionOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="ISBN" prop="isbn">
            <el-input 
              v-model="bookForm.isbn" 
              placeholder="请输入13位ISBN号，如 9787121028953"
              maxlength="13"
              show-word-limit
            />
            <div class="input-hint" v-if="bookForm.isbn && !isValidISBN(bookForm.isbn)">
              <span class="hint-error">❌ ISBN格式不正确，应为13位数字</span>
            </div>
          </el-form-item>      

          <el-form-item label="出版社" prop="publisher">
            <el-input 
              v-model="bookForm.publisher" 
              placeholder="请输入出版社名称，如 人民邮电出版社"
            />
            <div class="input-hint" v-if="bookForm.publisher && !isValidPublisher(bookForm.publisher)">
              <span class="hint-error">❌ 出版社名称必须以'出版社'结尾</span>
            </div>
          </el-form-item>

          <el-form-item label="书籍描述">
            <el-input
              v-model="bookForm.description"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 6 }"
              placeholder="请简单描述书籍状况、内容等"
            />
          </el-form-item>

          <el-form-item label="图书封面">
            <el-upload
              class="cover-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="handleCoverUpload"
              accept="image/*"
            >
              <img v-if="coverUrl" :src="coverUrl" class="cover-preview">
              <el-icon v-else class="uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitBookInfo" :loading="submitting" class="submit-btn">
              {{ submitting ? '提交中...' : '下一步，生成二维码' }}
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 步骤2：生成二维码 -->
    <div v-if="activeStep === 1" class="step-content">
      <el-card class="qr-card">
        <template #header>
          <div class="card-header">
            <h3>📱 书籍二维码</h3>
          </div>
        </template>

        <div class="qr-info">
          <div class="info-row">
            <span class="info-label">📖 书名：</span>
            <span class="info-value">{{ bookForm.title }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">✍️ 作者：</span>
            <span class="info-value">{{ bookForm.author }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">🎁 捐赠人：</span>
            <span class="info-value">{{ bookForm.donor }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">📊 成色：</span>
            <el-tag :type="getConditionType(bookForm.condition)" size="small">
              {{ getConditionLabel(bookForm.condition) }}
            </el-tag>
          </div>
          <div class="info-row">
            <span class="info-label">🔖 书籍ID：</span>
            <span class="info-value highlight">{{ bookId }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">⏰ 捐赠时间：</span>
            <span class="info-value">{{ donateTime }}</span>
          </div>
        </div>

        <div class="qr-code">
          <canvas ref="qrCanvas" width="200" height="200"></canvas>
        </div>

        <div class="qr-tips">
          <p><el-icon><InfoFilled /></el-icon> 请将此二维码打印后贴在书籍封面内侧</p>
          <p><el-icon><InfoFilled /></el-icon> 每位同学最多可同时借阅3本书，借阅周期30天</p>
        </div>

        <div class="qr-actions">
          <el-button type="primary" @click="downloadQRCode" :icon="Download">
            保存二维码
          </el-button>
          <el-button type="success" @click="completeDonation" :icon="Check">
            完成捐赠
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 步骤3：完成捐赠 -->
    <div v-if="activeStep === 2" class="step-content">
      <el-card class="success-card">
        <div class="success-content">
          <div class="success-icon">
            <el-icon><SuccessFilled /></el-icon>
          </div>
          <h2>《{{ bookForm.title }}》捐赠成功！</h2>
          <p>感谢您的捐赠！书籍ID：<span class="book-id">{{ bookId }}</span></p>
          
          <div class="success-actions">
            <el-button type="primary" @click="donateAnother">继续捐赠</el-button>
            <el-button @click="goToHome">返回首页</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Download, Check, InfoFilled, SuccessFilled } from '@element-plus/icons-vue'
import QRCode from 'qrcode'
import axios from 'axios'

const router = useRouter()
const formRef = ref(null)

// 状态变量
const activeStep = ref(0)
const submitting = ref(false)
const bookId = ref('')
const donateTime = ref('')
const coverUrl = ref('')
const qrCanvas = ref(null)

// 表单数据
const bookForm = ref({
  title: '',
  author: '',
  donor: '',
  condition: '',
  isbn: '',
  publisher: '',
  description: ''
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入书名', trigger: 'blur' },
    { min: 2, max: 50, message: '书名长度应在2-50个字符之间', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' },
    { min: 2, max: 30, message: '作者名长度应在2-30个字符之间', trigger: 'blur' }
  ],
  donor: [
    { required: true, message: '请输入捐赠人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度应在2-20个字符之间', trigger: 'blur' }
  ],
  condition: [
    { required: true, message: '请选择书籍成色', trigger: 'change' }
  ],
  isbn: [
    { required: true, message: '请输入ISBN', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入ISBN'))
      } else if (!isValidISBN(value)) {
        callback(new Error('ISBN格式不正确，应为13位数字'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  publisher: [
    { required: true, message: '请输入出版社', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入出版社'))
      } else if (!isValidPublisher(value)) {
        callback(new Error('出版社名称必须以"出版社"结尾'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ]
}

// 验证ISBN格式（13位数字）
const isValidISBN = (isbn) => {
  // 去除可能的中划线或空格
  const cleanIsbn = isbn.replace(/[-\s]/g, '')
  // 必须是13位数字
  return /^\d{13}$/.test(cleanIsbn)
}

// 验证出版社格式（必须以"出版社"结尾）
const isValidPublisher = (publisher) => {
  return publisher.endsWith('出版社') && publisher.length >= 5
}

// 成色选项
const conditionOptions = [
  { value: 'worn', label: '破旧' },
  { value: 'normal', label: '正常' },
  { value: 'good', label: '良好' },
  { value: 'new', label: '崭新' }
]

// 获取成色标签
const getConditionLabel = (value) => {
  const option = conditionOptions.find(item => item.value === value)
  return option ? option.label : '未选择'
}

// 获取成色类型
const getConditionType = (value) => {
  const map = {
    worn: 'danger',
    normal: 'info',
    good: 'success',
    new: 'primary'
  }
  return map[value] || 'info'
}

// 重置表单
const resetForm = () => {
  bookForm.value = {
    title: '',
    author: '',
    donor: '',
    condition: '',
    isbn: '',
    publisher: '',
    description: ''
  }
  coverUrl.value = ''
}

// 处理封面上传
const handleCoverUpload = (file) => {
  // 检查文件大小（限制为500KB）
  if (file.size > 500 * 1024) {
    ElMessage.error('图片大小不能超过500KB')
    return false
  }
  
  // 检查图片格式
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  
  const reader = new FileReader()
  reader.onload = (e) => {
    // 压缩图片
    const img = new Image()
    img.src = e.target.result
    img.onload = () => {
      const canvas = document.createElement('canvas')
      let width = img.width
      let height = img.height
      
      // 限制最大尺寸
      const MAX_WIDTH = 300
      const MAX_HEIGHT = 400
      
      if (width > height) {
        if (width > MAX_WIDTH) {
          height *= MAX_WIDTH / width
          width = MAX_WIDTH
        }
      } else {
        if (height > MAX_HEIGHT) {
          width *= MAX_HEIGHT / height
          height = MAX_HEIGHT
        }
      }
      
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)
      
      // 压缩质量为0.8
      coverUrl.value = canvas.toDataURL('image/jpeg', 0.8)
    }
  }
  reader.readAsDataURL(file)
  return false
}

// 获取当前用户ID
const getCurrentUserId = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      return userInfo.id
    } catch (e) {
      console.error('解析用户信息失败', e)
      return null
    }
  }
  return null
}

// 完成捐赠（点击完成按钮）
const completeDonation = () => {
  // 延迟跳转，避免可能的渲染冲突
  setTimeout(() => {
    activeStep.value = 2
  }, 50)
}

// 生成二维码
const generateQRCode = (qrData) => {
  const canvas = qrCanvas.value
  if (!canvas) {
    console.error('二维码canvas不存在')
    ElMessage.error('二维码生成失败：canvas不存在')
    return
  }
  
  console.log('开始生成二维码，数据:', qrData)
  
  // 确保 qrData 是字符串
  let qrString = qrData
  if (typeof qrData === 'object') {
    qrString = JSON.stringify(qrData)
  } else if (qrData && typeof qrData === 'string') {
    // 如果是字符串，尝试解析看是否是JSON对象
    try {
      const parsed = JSON.parse(qrData)
      if (parsed && typeof parsed === 'object') {
        qrString = JSON.stringify(parsed)
      }
    } catch (e) {
      // 不是JSON字符串，保持原样
      console.log('qrData是普通字符串')
    }
  }
  
  QRCode.toCanvas(canvas, qrString, {
    width: 200,
    margin: 2,
    color: {
      dark: '#000000',
      light: '#ffffff'
    },
    errorCorrectionLevel: 'H'
  }).then(() => {
    console.log('二维码生成成功')
  }).catch(error => {
    console.error('二维码生成失败:', error)
    ElMessage.error('二维码生成失败，请查看控制台')
    
    // 尝试使用备用方法显示书籍ID
    try {
      const ctx = canvas.getContext('2d')
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      ctx.fillStyle = '#000000'
      ctx.font = '14px Arial'
      ctx.fillText('书籍ID:', 10, 80)
      ctx.fillText(bookId.value, 10, 110)
      ctx.fillText('请妥善保管', 10, 140)
    } catch (e) {
      console.error('备用显示也失败:', e)
    }
  })
}

// 提交书籍信息到后端
const submitBookInfo = () => {
  // 手动验证
  if (!bookForm.value.title) {
    ElMessage.error('请输入书名')
    return
  }
  if (!bookForm.value.author) {
    ElMessage.error('请输入作者')
    return
  }
  if (!bookForm.value.donor) {
    ElMessage.error('请输入捐赠人姓名')
    return
  }
  if (!bookForm.value.condition) {
    ElMessage.error('请选择书籍成色')
    return
  }
  
  // ISBN验证
  if (!bookForm.value.isbn) {
    ElMessage.error('请输入ISBN')
    return
  }
  if (!isValidISBN(bookForm.value.isbn)) {
    ElMessage.error('ISBN格式不正确，应为13位数字')
    return
  }
  
  // 出版社验证
  if (!bookForm.value.publisher) {
    ElMessage.error('请输入出版社')
    return
  }
  if (!isValidPublisher(bookForm.value.publisher)) {
    ElMessage.error('出版社名称必须以"出版社"结尾')
    return
  }
  
  submitting.value = true
  
  const userId = getCurrentUserId()
  const token = localStorage.getItem('token')
  
  console.log('当前用户ID:', userId)
  
  axios.post(`${apiBaseUrl}/books/donate`, {
    title: bookForm.value.title,
    author: bookForm.value.author,
    donorName: bookForm.value.donor,
    condition: bookForm.value.condition,
    isbn: bookForm.value.isbn.replace(/[-\s]/g, ''), // 去除可能的中划线或空格
    publisher: bookForm.value.publisher,
    description: bookForm.value.description,
    coverUrl: coverUrl.value,
    userId: userId
  }, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }).then(response => {
    console.log('捐赠响应:', response.data)
    
    if (response.data.success) {
      bookId.value = response.data.bookId
      donateTime.value = new Date().toLocaleString('zh-CN')
      
      // 先切换到步骤1
      activeStep.value = 1
      
      // 生成二维码 - 确保有足够的时间让DOM渲染
      if (response.data.qrCodeData) {
        console.log('后端返回二维码数据:', response.data.qrCodeData)
        nextTick(() => {
          setTimeout(() => {
            generateQRCode(response.data.qrCodeData)
          }, 200)
        })
      } else {
        console.warn('后端未返回二维码数据，使用本地生成')
        // 手动构建二维码数据
        const qrData = {
          bookId: bookId.value,
          title: bookForm.value.title,
          donor: bookForm.value.donor
        }
        nextTick(() => {
          setTimeout(() => {
            generateQRCode(qrData)
          }, 200)
        })
      }
      
      ElMessage.success('书籍捐赠成功')
      
      // 触发统计信息更新
      window.dispatchEvent(new CustomEvent('stats-updated', { 
        detail: { userId: userId }
      }))
    } else {
      ElMessage.error(response.data.message || '捐赠失败')
    }
  }).catch(error => {
    console.error('捐赠失败:', error)
    ElMessage.error('捐赠失败，请重试')
  }).finally(() => {
    submitting.value = false
  })
}

// 下载二维码
const downloadQRCode = () => {
  const canvas = qrCanvas.value
  if (!canvas) {
    ElMessage.error('二维码不存在')
    return
  }
  
  const link = document.createElement('a')
  link.download = `${bookForm.value.title}_二维码.png`
  link.href = canvas.toDataURL('image/png')
  link.click()
  
  ElMessage.success('二维码已保存')
}

// 继续捐赠
const donateAnother = () => {
  resetForm()
  activeStep.value = 0
}

// 返回首页
const goToHome = () => {
  router.push('/')
}

// 组件卸载前清理
onBeforeUnmount(() => {
  console.log('Donate组件卸载')
})
</script>

<style scoped>
/* ==================== 清新风格样式 ==================== */
.donate {
  background: #faf8f4;
  min-height: calc(100vh - 60px);
  padding: 32px 20px;
}

/* 返回按钮样式 */
.back-nav {
  max-width: 800px;
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
  margin-bottom: 30px;
  color: #2c5a4f;
  font-size: 28px;
  font-weight: 500;
  letter-spacing: 1px;
}

.step-content {
  max-width: 800px;
  margin: 20px auto 0;
}

/* 卡片样式 */
:deep(.el-card) {
  border-radius: 28px !important;
  border: 1px solid #efebe6 !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02) !important;
  overflow: hidden;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #efebe6;
  padding: 16px 24px;
  background: white;
}

.card-header {
  display: flex;
  justify-content: center;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #2c5a4f;
}

:deep(.el-card__body) {
  padding: 28px;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  color: #5a6e5c;
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 1px solid #e2e6e0;
  box-shadow: none;
}

:deep(.el-input__wrapper:hover) {
  border-color: #8fc1b0;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #8fc1b0;
  box-shadow: 0 0 0 2px rgba(143, 193, 176, 0.1);
}

:deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 1px solid #e2e6e0;
}

:deep(.el-textarea__inner:focus) {
  border-color: #8fc1b0;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
}

/* 封面上传区域 */
.cover-uploader {
  border: 2px dashed #e2e6e0;
  border-radius: 16px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  transition: all 0.2s;
  background: #faf8f4;
}

.cover-uploader:hover {
  border-color: #8fc1b0;
  background: #f5f0e8;
}

.cover-uploader .uploader-icon {
  font-size: 32px;
  color: #b8c4b0;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.cover-preview {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

/* 输入提示 */
.input-hint {
  margin-top: 6px;
  font-size: 12px;
}

.hint-error {
  color: #e8a4a4;
}

/* 按钮 */
:deep(.el-button--primary) {
  background: #8fc1b0;
  border-color: #8fc1b0;
  border-radius: 40px;
  padding: 10px 24px;
}

:deep(.el-button--primary:hover) {
  background: #7aa992;
  border-color: #7aa992;
}

:deep(.el-button--default) {
  border-radius: 40px;
  border-color: #e2e6e0;
  color: #8b9a8e;
}

:deep(.el-button--default:hover) {
  border-color: #8fc1b0;
  color: #8fc1b0;
}

:deep(.el-button--success) {
  background: #e8f0ec;
  border-color: #e8f0ec;
  color: #8fc1b0;
  border-radius: 40px;
}

:deep(.el-button--success:hover) {
  background: #dde9e2;
}

.submit-btn {
  background: #8fc1b0;
  border: none;
  padding: 12px 30px;
  font-size: 15px;
}

/* 二维码信息区域 */
.qr-info {
  background: #faf8f4;
  padding: 20px;
  border-radius: 20px;
  margin: 20px 0;
}

.info-row {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #efebe6;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  width: 100px;
  font-weight: 500;
  color: #8b9a8e;
}

.info-value {
  flex: 1;
  color: #5a6e5c;
}

.info-value.highlight {
  color: #8fc1b0;
  font-weight: 500;
}

/* 二维码区域 */
.qr-code {
  text-align: center;
  margin: 20px 0;
  padding: 24px;
  background: white;
  border-radius: 20px;
  border: 1px solid #efebe6;
}

.qr-code canvas {
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 提示区域 */
.qr-tips {
  background: #e8f0ec;
  padding: 16px 20px;
  border-radius: 16px;
  margin: 20px 0;
  color: #5a6e5c;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.qr-tips p {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.qr-tips .el-icon {
  font-size: 16px;
  color: #8fc1b0;
}

/* 按钮区域 */
.qr-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
}

/* 成功页面 */
.success-content {
  text-align: center;
  padding: 32px 20px;
}

.success-icon {
  margin-bottom: 20px;
}

.success-icon .el-icon {
  font-size: 72px;
  color: #8fc1b0;
  animation: scaleIn 0.5s ease;
}

@keyframes scaleIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.success-content h2 {
  color: #2c5a4f;
  margin: 20px 0 10px;
  font-size: 24px;
}

.success-content p {
  color: #6c826e;
  margin-bottom: 24px;
  font-size: 15px;
}

.book-id {
  color: #8fc1b0;
  font-weight: 500;
  font-size: 16px;
}

.success-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
  flex-wrap: wrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .donate {
    padding: 20px 16px;
  }
  
  h2 {
    font-size: 24px;
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .qr-actions {
    flex-direction: column;
  }
  
  .qr-actions .el-button {
    width: 100%;
  }
  
  .success-actions {
    flex-direction: column;
  }
  
  .success-actions .el-button {
    width: 100%;
  }
  
  .info-row {
    flex-direction: column;
    gap: 6px;
  }
  
  .info-label {
    width: 100%;
  }
}
</style>