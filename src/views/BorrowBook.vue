<template>
  <div class="borrow-book">
    <div class="back-nav">
      <router-link to="/" class="back-link">
        <span class="back-arrow">←</span> 返回首页
      </router-link>
    </div>
    <h2>认领书籍</h2>
    
    <!-- 步骤条 -->
    <el-steps :active="activeStep" finish-status="success" simple style="margin-top: 20px; margin-bottom: 30px">
      <el-step title="扫码验证身份" />
      <el-step title="扫描书籍二维码" />
      <el-step title="确认借阅信息" />
    </el-steps>

    <!-- 步骤1：扫码验证身份 -->
    <div v-if="activeStep === 0" class="step-content">
      <el-card class="scan-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="header-title">扫码验证身份</span>
            <el-tag type="warning" effect="dark" size="small">请扫描个人中心二维码</el-tag>
          </div>
        </template>

        <!-- 模式切换 -->
        <div class="mode-switch">
          <el-radio-group v-model="scanMode" size="large">
            <el-radio-button value="camera">
              <el-icon><Camera /></el-icon>
              <span>摄像头扫码</span>
            </el-radio-button>
            <el-radio-button value="upload">
              <el-icon><Picture /></el-icon>
              <span>上传图片</span>
            </el-radio-button>
            <el-radio-button value="manual">
              <el-icon><Edit /></el-icon>
              <span>手动输入</span>
            </el-radio-button>
          </el-radio-group>
        </div>

        <!-- 摄像头扫码模式 -->
        <div v-if="scanMode === 'camera'" class="camera-mode">
          <div class="camera-wrapper">
            <div class="camera-container">
              <video 
                ref="videoRef" 
                class="scanner-video"
                :class="{ 'hidden': !isCameraActive }"
                autoplay
                playsinline
              ></video>
              
              <div v-if="isCameraActive" class="scan-frame">
                <div class="scan-line"></div>
                <div class="scan-corner top-left"></div>
                <div class="scan-corner top-right"></div>
                <div class="scan-corner bottom-left"></div>
                <div class="scan-corner bottom-right"></div>
              </div>

              <div v-if="!isCameraActive" class="camera-placeholder">
                <el-icon :size="50"><Camera /></el-icon>
                <p class="placeholder-text">摄像头未开启</p>
              </div>
            </div>

            <div class="camera-select" v-if="cameras.length > 0 && isCameraActive">
              <span class="select-label">切换摄像头：</span>
              <el-select v-model="selectedCamera" size="small" style="width: 200px" @change="switchCamera">
                <el-option
                  v-for="camera in cameras"
                  :key="camera.deviceId"
                  :label="camera.label || `摄像头 ${cameras.indexOf(camera) + 1}`"
                  :value="camera.deviceId"
                />
              </el-select>
            </div>

            <div class="camera-controls">
              <el-button 
                v-if="!isCameraActive" 
                type="primary" 
                @click="startCamera"
                :icon="VideoCamera"
                :loading="cameraLoading"
                size="large"
              >
                开启摄像头
              </el-button>
              <el-button 
                v-else 
                type="danger" 
                @click="stopCamera"
                :icon="VideoPause"
                size="large"
              >
                关闭摄像头
              </el-button>
            </div>
          </div>
        </div>

        <!-- 上传图片模式 -->
        <div v-if="scanMode === 'upload'" class="upload-mode">
          <el-upload
            class="upload-area"
            drag
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept="image/*"
          >
            <el-icon class="upload-icon"><Picture /></el-icon>
            <div class="upload-text">
              点击或拖拽二维码图片到此区域
            </div>
            <div class="upload-hint">
              支持 jpg、png 格式，请确保图片清晰
            </div>
          </el-upload>

          <div v-if="previewUrl" class="preview-area">
            <img :src="previewUrl" class="preview-image" alt="预览图" />
            <div class="preview-actions">
              <el-button type="success" @click="scanQRFromImage" :loading="imageScanning">
                {{ imageScanning ? '识别中...' : '识别二维码' }}
              </el-button>
              <el-button type="danger" @click="clearPreview" plain>
                重新上传
              </el-button>
            </div>
          </div>
        </div>

        <!-- 手动输入模式 -->
        <div v-if="scanMode === 'manual'" class="manual-mode">
          <el-form :model="manualForm" label-width="80px">
            <el-form-item label="学号" required>
              <el-input 
                v-model="manualForm.studentId" 
                placeholder="请输入学号"
                clearable
                size="large"
              />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input 
                v-model="manualForm.name" 
                placeholder="请输入姓名（可选）"
                clearable
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitManual" :loading="manualLoading" size="large" style="width: 100%">
                {{ manualLoading ? '验证中...' : '验证身份' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="scanStatus" class="scan-status" :class="statusClass">
          <el-icon v-if="scanStatus.includes('成功')"><SuccessFilled /></el-icon>
          <el-icon v-else-if="scanStatus.includes('失败') || scanStatus.includes('错误')"><WarningFilled /></el-icon>
          <el-icon v-else><Loading /></el-icon>
          <span>{{ scanStatus }}</span>
        </div>
      </el-card>
    </div>

    <!-- 步骤2：扫描书籍二维码 -->
    <div v-if="activeStep === 1" class="step-content">
      <el-card class="scan-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="header-title">扫描书籍二维码</span>
            <el-tag type="warning" effect="dark" size="small">请扫描书籍上的二维码</el-tag>
          </div>
        </template>

        <!-- 用户信息确认 -->
        <div v-if="studentInfo" class="user-confirm-card">
          <el-alert
            title="用户身份已确认"
            :description="`当前借阅人：${studentInfo.name} (${studentInfo.studentId})，可借数量：${maxBorrowCount}本`"
            type="success"
            show-icon
            :closable="false"
          />
        </div>

        <!-- 模式切换 -->
        <div class="mode-switch">
          <el-radio-group v-model="bookScanMode" size="large">
            <el-radio-button value="camera">
              <el-icon><Camera /></el-icon>
              <span>摄像头扫码</span>
            </el-radio-button>
            <el-radio-button value="upload">
              <el-icon><Picture /></el-icon>
              <span>上传图片</span>
            </el-radio-button>
          </el-radio-group>
        </div>

        <!-- 摄像头扫码模式 -->
        <div v-if="bookScanMode === 'camera'" class="camera-mode">
          <div class="camera-wrapper">
            <div class="camera-container">
              <video 
                ref="bookVideoRef" 
                class="scanner-video"
                :class="{ 'hidden': !isBookCameraActive }"
                autoplay
                playsinline
              ></video>
              
              <div v-if="isBookCameraActive" class="scan-frame">
                <div class="scan-line"></div>
                <div class="scan-corner top-left"></div>
                <div class="scan-corner top-right"></div>
                <div class="scan-corner bottom-left"></div>
                <div class="scan-corner bottom-right"></div>
              </div>

              <div v-if="!isBookCameraActive" class="camera-placeholder">
                <el-icon :size="50"><Camera /></el-icon>
                <p class="placeholder-text">摄像头未开启</p>
              </div>
            </div>

            <div class="camera-controls">
              <el-button 
                v-if="!isBookCameraActive" 
                type="primary" 
                @click="startBookCamera"
                :icon="VideoCamera"
                :loading="bookCameraLoading"
                size="large"
              >
                开启摄像头
              </el-button>
              <el-button 
                v-else 
                type="danger" 
                @click="stopBookCamera"
                :icon="VideoPause"
                size="large"
              >
                关闭摄像头
              </el-button>
            </div>
          </div>
        </div>

        <!-- 上传图片模式 -->
        <div v-if="bookScanMode === 'upload'" class="upload-mode">
          <el-upload
            class="upload-area"
            drag
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleBookFileChange"
            accept="image/*"
          >
            <el-icon class="upload-icon"><Picture /></el-icon>
            <div class="upload-text">
              点击或拖拽书籍二维码图片到此区域
            </div>
            <div class="upload-hint">
              支持 jpg、png 格式，请确保图片清晰
            </div>
          </el-upload>

          <div v-if="bookPreviewUrl" class="preview-area">
            <img :src="bookPreviewUrl" class="preview-image" alt="预览图" />
            <div class="preview-actions">
              <el-button type="success" @click="scanBookQRFromImage" :loading="bookImageScanning">
                {{ bookImageScanning ? '识别中...' : '识别书籍二维码' }}
              </el-button>
              <el-button type="danger" @click="clearBookPreview" plain>
                重新上传
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="bookScanStatus" class="scan-status" :class="getBookStatusClass()">
          <el-icon v-if="bookScanStatus.includes('成功')"><SuccessFilled /></el-icon>
          <el-icon v-else-if="bookScanStatus.includes('失败') || bookScanStatus.includes('错误') || bookScanStatus.includes('已借出') || bookScanStatus.includes('已达')"><WarningFilled /></el-icon>
          <el-icon v-else><Loading /></el-icon>
          <span>{{ bookScanStatus }}</span>
        </div>

        <div class="step-actions">
          <el-button @click="goBackToUserScan">上一步</el-button>
        </div>
      </el-card>
    </div>

    <!-- 步骤3：确认借阅信息 -->
    <div v-if="activeStep === 2" class="step-content">
      <el-card class="borrow-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="header-title">确认借阅信息</span>
            <el-tag type="success" effect="dark" size="small">
              即将借阅：{{ selectedBook?.title }}
            </el-tag>
          </div>
        </template>

        <!-- 书籍信息卡片 -->
        <div v-if="selectedBook" class="book-info-card">
          <div class="book-cover">
            <img v-if="selectedBook.coverUrl" :src="selectedBook.coverUrl" :alt="selectedBook.title">
            <div v-else class="cover-placeholder">📚</div>
          </div>
          <div class="book-details">
            <h3 class="book-title">{{ selectedBook.title }}</h3>
            <p class="book-author">{{ selectedBook.author }}</p>
            <p class="book-isbn">ISBN: {{ selectedBook.isbn || '暂无' }}</p>
            <div class="book-tags">
              <el-tag :type="getConditionType(selectedBook.condition)" size="small" effect="light">
                {{ getConditionLabel(selectedBook.condition) }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 用户信息 -->
        <div class="user-info-card">
          <h4>借阅人信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="学号">{{ studentInfo?.studentId }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ studentInfo?.name }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ studentInfo?.phone || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="已借阅数量">
              <el-tag :type="studentInfo?.borrowedCount >= 3 ? 'danger' : 'success'" size="small">
                {{ studentInfo?.borrowedCount }} 本
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-form :model="borrowForm" label-width="100px" class="borrow-form">
          <el-form-item label="借阅周期">
            <div class="period-selector">
              <el-slider 
                v-model="borrowForm.period" 
                :min="1" 
                :max="30" 
                :step="1"
                show-input
                input-size="small"
                :show-input-controls="true"
              />
              <div class="form-tip">借阅天数：{{ borrowForm.period }} 天</div>
              <div class="form-tip">应还日期：{{ dueDate }}</div>
            </div>
          </el-form-item>

          <el-form-item label="取书方式">
            <el-radio-group v-model="borrowForm.pickupMethod">
              <el-radio-button value="self">自行取书</el-radio-button>
              <el-radio-button value="delivery">送书上门</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="联系电话">
            <el-input 
              v-model="borrowForm.contact" 
              placeholder="请输入手机号"
              maxlength="11"
              show-word-limit
              clearable
            />
          </el-form-item>

          <el-form-item label="备注">
            <el-input
              v-model="borrowForm.remarks"
              type="textarea"
              :rows="3"
              placeholder="可选，填写特殊要求"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>

        <div class="form-actions">
          <el-button @click="goBackToBookScan">重新扫码</el-button>
          <el-button 
            type="primary" 
            @click="confirmBorrow"
            :disabled="submitting"
            :loading="submitting"
          >
            {{ submitting ? '借阅中...' : '确认借阅' }}
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 成功弹窗 -->
    <el-dialog v-model="successDialogVisible" title="借阅成功" width="90%" max-width="400px" center>
      <div class="success-content">
        <div class="success-icon">✓</div>
        <h3>借阅成功！</h3>
        <p>您已成功借阅《{{ selectedBook?.title }}》</p>
        <p>借阅周期：{{ borrowForm.period }}天</p>
        <p>应还日期：{{ dueDate }}</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="goToUserCenter">查看我的借阅</el-button>
        <el-button @click="continueBorrow">继续借阅</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Camera, 
  Picture, 
  VideoCamera, 
  VideoPause, 
  SuccessFilled,
  WarningFilled,
  Loading,
  Edit
} from '@element-plus/icons-vue'
import axios from 'axios'
import jsQR from 'jsqr'

const router = useRouter()

// ==================== 状态变量 ====================
const activeStep = ref(0)

// 用户扫码相关
const scanMode = ref('camera')
const videoRef = ref(null)
const isCameraActive = ref(false)
const cameraLoading = ref(false)
const cameras = ref([])
const selectedCamera = ref('')
const scanInterval = ref(null)

// 图片上传相关
const previewUrl = ref('')
const imageScanning = ref(false)

// 手动输入表单
const manualForm = reactive({
  studentId: '',
  name: ''
})
const manualLoading = ref(false)

// 状态信息
const scanStatus = ref('')
const studentInfo = ref(null)

// 书籍扫码相关
const bookScanMode = ref('camera')
const bookVideoRef = ref(null)
const isBookCameraActive = ref(false)
const bookCameraLoading = ref(false)
const bookScanInterval = ref(null)
const bookPreviewUrl = ref('')
const bookImageScanning = ref(false)
const bookScanStatus = ref('')
const selectedBook = ref(null)

// 表单数据
const borrowForm = reactive({
  period: 14,
  pickupMethod: 'self',
  contact: '',
  remarks: ''
})

// 借阅相关
const successDialogVisible = ref(false)
const submitting = ref(false)

// ==================== 计算属性 ====================
const maxBorrowCount = computed(() => {
  return Math.max(0, 3 - (studentInfo.value?.borrowedCount || 0))
})

const dueDate = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() + borrowForm.period)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

const statusClass = computed(() => {
  if (scanStatus.value.includes('成功')) return 'status-success'
  if (scanStatus.value.includes('失败') || scanStatus.value.includes('错误')) return 'status-error'
  return 'status-scanning'
})

// 获取书籍状态样式类
const getBookStatusClass = () => {
  if (bookScanStatus.value.includes('成功')) return 'status-success'
  if (bookScanStatus.value.includes('失败') || bookScanStatus.value.includes('错误') || 
      bookScanStatus.value.includes('已借出') || bookScanStatus.value.includes('已达')) {
    return 'status-error'
  }
  return 'status-scanning'
}

// 获取成色标签
const getConditionLabel = (condition) => {
  const map = {
    worn: '破旧',
    normal: '正常',
    good: '良好',
    new: '崭新'
  }
  return map[condition] || '未知'
}

const getConditionType = (condition) => {
  const map = {
    worn: 'danger',
    normal: 'info',
    good: 'success',
    new: 'primary'
  }
  return map[condition] || 'info'
}

// ==================== 辅助函数 ====================
// 设置 Canvas 的 willReadFrequently 属性以提高性能
const setupCanvasForFrequentRead = (canvas) => {
  if (canvas && canvas.getContext) {
    const ctx = canvas.getContext('2d', { willReadFrequently: true })
    return ctx
  }
  return null
}

// ==================== 用户扫码相关函数 ====================
const getCameras = async () => {
  try {
    const devices = await navigator.mediaDevices.enumerateDevices()
    cameras.value = devices.filter(device => device.kind === 'videoinput')
    
    const backCamera = cameras.value.find(camera => 
      camera.label.toLowerCase().includes('back') || 
      camera.label.toLowerCase().includes('rear') ||
      camera.label.toLowerCase().includes('后置')
    )
    
    if (backCamera) {
      selectedCamera.value = backCamera.deviceId
    } else if (cameras.value.length > 0) {
      selectedCamera.value = cameras.value[0].deviceId
    }
  } catch (error) {
    console.error('获取摄像头列表失败:', error)
  }
}

const startCamera = async () => {
  cameraLoading.value = true
  scanStatus.value = '正在启动摄像头...'
  
  try {
    await getCameras()
    
    const constraints = {
      video: selectedCamera.value ? {
        deviceId: { exact: selectedCamera.value }
      } : {
        facingMode: 'environment'
      }
    }
    
    const stream = await navigator.mediaDevices.getUserMedia({ video: constraints.video })
    
    if (videoRef.value) {
      videoRef.value.srcObject = stream
      isCameraActive.value = true
      
      videoRef.value.onloadedmetadata = () => {
        videoRef.value.play()
        scanStatus.value = '正在扫描二维码...'
        startScanning()
      }
    }
  } catch (error) {
    console.error('摄像头启动失败:', error)
    if (error.name === 'NotAllowedError') {
      scanStatus.value = '请允许访问摄像头权限'
      ElMessage.error('请允许访问摄像头权限')
    } else if (error.name === 'NotFoundError') {
      scanStatus.value = '未找到摄像头设备'
      ElMessage.error('未找到摄像头设备')
    } else {
      scanStatus.value = '摄像头启动失败'
      ElMessage.error('摄像头启动失败：' + error.message)
    }
  } finally {
    cameraLoading.value = false
  }
}

const startScanning = () => {
  if (!videoRef.value || !isCameraActive.value) return
  
  const canvas = document.createElement('canvas')
  const context = setupCanvasForFrequentRead(canvas)
  
  scanInterval.value = setInterval(() => {
    if (!videoRef.value || !isCameraActive.value) return
    
    const video = videoRef.value
    if (video.readyState === video.HAVE_ENOUGH_DATA && video.videoWidth > 0 && video.videoHeight > 0) {
      canvas.width = video.videoWidth
      canvas.height = video.videoHeight
      context.drawImage(video, 0, 0, canvas.width, canvas.height)
      
      const imageData = context.getImageData(0, 0, canvas.width, canvas.height)
      const code = jsQR(imageData.data, imageData.width, imageData.height, {
        inversionAttempts: 'dontInvert'
      })
      
      if (code) {
        handleUserQRResult(code.data)
      }
    }
  }, 500)
}

const switchCamera = () => {
  if (isCameraActive.value) {
    stopCamera()
    setTimeout(() => {
      startCamera()
    }, 300)
  }
}

const stopCamera = () => {
  if (scanInterval.value) {
    clearInterval(scanInterval.value)
    scanInterval.value = null
  }
  
  if (videoRef.value && videoRef.value.srcObject) {
    const tracks = videoRef.value.srcObject.getTracks()
    tracks.forEach(track => track.stop())
    videoRef.value.srcObject = null
  }
  
  isCameraActive.value = false
  scanStatus.value = ''
}

// 处理用户二维码结果
const handleUserQRResult = (text) => {
  console.log('识别到用户二维码:', text)
  
  if (navigator.vibrate) {
    navigator.vibrate(200)
  }
  
  const { studentId, name } = parseQRData(text)
  
  if (!studentId) {
    scanStatus.value = '二维码数据无效'
    ElMessage.error('二维码数据无效，请扫描个人中心二维码')
    return
  }
  
  scanStatus.value = '识别成功！正在验证...'
  verifyStudent(studentId, name)
  
  stopCamera()
}

// 解析二维码数据
const parseQRData = (text) => {
  let studentId = ''
  let name = ''
  
  try {
    if (text.startsWith('{') && text.endsWith('}')) {
      const qrData = JSON.parse(text)
      studentId = qrData.studentId || qrData.userId || qrData.id || ''
      name = qrData.name || qrData.userName || ''
    } 
    else if (text.includes('studentId=') || text.includes('userId=')) {
      const params = new URLSearchParams(text)
      studentId = params.get('studentId') || params.get('userId') || ''
      name = params.get('name') || params.get('userName') || ''
      
      if (name && name.includes('%')) {
        try {
          name = decodeURIComponent(name)
        } catch (e) {
          console.error('解码失败', e)
        }
      }
    } 
    else if (/^\d+$/.test(text)) {
      studentId = text
      name = '用户'
    } else {
      studentId = text
      name = text
    }
  } catch (e) {
    console.error('解析二维码数据失败:', e)
    studentId = text
    name = text
  }
  
  return { studentId, name }
}

// 上传图片相关函数
const handleFileChange = (file) => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = URL.createObjectURL(file.raw)
  scanStatus.value = ''
}

const clearPreview = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
}

const scanQRFromImage = async () => {
  if (!previewUrl.value) {
    ElMessage.warning('请先上传二维码图片')
    return
  }

  imageScanning.value = true
  scanStatus.value = '正在识别二维码...'

  try {
    const img = new Image()
    img.src = previewUrl.value
    
    await new Promise((resolve, reject) => {
      img.onload = resolve
      img.onerror = reject
    })
    
    const canvas = document.createElement('canvas')
    const ctx = setupCanvasForFrequentRead(canvas)
    
    canvas.width = img.width
    canvas.height = img.height
    ctx.drawImage(img, 0, 0, img.width, img.height)
    
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    const code = jsQR(imageData.data, imageData.width, imageData.height, {
      inversionAttempts: 'dontInvert'
    })
    
    if (code) {
      handleUserQRResult(code.data)
    } else {
      // 反转颜色再试一次
      const invertedImageData = invertImageData(imageData)
      const invertedCode = jsQR(invertedImageData.data, invertedImageData.width, invertedImageData.height)
      
      if (invertedCode) {
        handleUserQRResult(invertedCode.data)
      } else {
        scanStatus.value = '未识别到二维码'
        ElMessage.warning('未识别到二维码，请确保图片清晰且包含有效的二维码')
      }
    }
  } catch (error) {
    console.error('识别失败:', error)
    scanStatus.value = '识别失败，请重试'
    ElMessage.error('二维码识别失败，请确保图片清晰')
  } finally {
    imageScanning.value = false
  }
}

const invertImageData = (imageData) => {
  const data = new Uint8ClampedArray(imageData.data.length)
  for (let i = 0; i < imageData.data.length; i += 4) {
    data[i] = 255 - imageData.data[i]
    data[i + 1] = 255 - imageData.data[i + 1]
    data[i + 2] = 255 - imageData.data[i + 2]
    data[i + 3] = imageData.data[i + 3]
  }
  return new ImageData(data, imageData.width, imageData.height)
}

// 手动输入
const submitManual = async () => {
  if (!manualForm.studentId) {
    ElMessage.warning('请输入学号')
    return
  }
  
  manualLoading.value = true
  scanStatus.value = '正在验证用户信息...'
  
  verifyStudent(manualForm.studentId, manualForm.name || '用户')
  
  setTimeout(() => {
    manualLoading.value = false
  }, 1000)
}

// 用户验证
const verifyStudent = (studentId, defaultName = '') => {
  axios.get(`${this.apiBaseUrl}/users`)
    .then(response => {
      const users = response.data
      const user = users.find(u => u.studentId === studentId)
      
      if (user) {
        studentInfo.value = {
          id: user.id,
          studentId: user.studentId,
          name: user.name || defaultName || '用户',
          phone: user.phone || '',
          borrowedCount: user.borrowedCount || 0,
          email: user.email || ''
        }
        
        if (user.borrowedCount >= 3) {
          scanStatus.value = '已达到最大借阅数量限制'
          ElMessage.warning('您已达到最大借阅数量（3本），请先归还书籍')
          return
        }
        
        scanStatus.value = '验证成功！'
        ElMessage.success(`欢迎 ${studentInfo.value.name}`)
        
        if (user.phone) {
          borrowForm.contact = user.phone
        }
        
        if (previewUrl.value) {
          URL.revokeObjectURL(previewUrl.value)
          previewUrl.value = ''
        }
        
        setTimeout(() => {
          activeStep.value = 1
        }, 1000)
        
      } else {
        scanStatus.value = '验证失败，用户不存在'
        ElMessage.error('验证失败，该用户未注册')
      }
    })
    .catch(error => {
      console.error('验证失败:', error)
      scanStatus.value = '验证失败，请重试'
      ElMessage.error('验证失败，请重试')
    })
}

// ==================== 书籍扫码相关函数 ====================
const startBookCamera = async () => {
  bookCameraLoading.value = true
  bookScanStatus.value = '正在启动摄像头...'
  
  try {
    const constraints = {
      video: { facingMode: 'environment' }
    }
    
    const stream = await navigator.mediaDevices.getUserMedia({ video: constraints.video })
    
    if (bookVideoRef.value) {
      bookVideoRef.value.srcObject = stream
      isBookCameraActive.value = true
      
      bookVideoRef.value.onloadedmetadata = () => {
        bookVideoRef.value.play()
        bookScanStatus.value = '正在扫描书籍二维码...'
        startBookScanning()
      }
    }
  } catch (error) {
    console.error('摄像头启动失败:', error)
    if (error.name === 'NotAllowedError') {
      bookScanStatus.value = '请允许访问摄像头权限'
      ElMessage.error('请允许访问摄像头权限')
    } else {
      bookScanStatus.value = '摄像头启动失败'
      ElMessage.error('摄像头启动失败：' + error.message)
    }
  } finally {
    bookCameraLoading.value = false
  }
}

const startBookScanning = () => {
  if (!bookVideoRef.value || !isBookCameraActive.value) return
  
  const canvas = document.createElement('canvas')
  const context = setupCanvasForFrequentRead(canvas)
  
  bookScanInterval.value = setInterval(() => {
    if (!bookVideoRef.value || !isBookCameraActive.value) return
    
    const video = bookVideoRef.value
    if (video.readyState === video.HAVE_ENOUGH_DATA && video.videoWidth > 0 && video.videoHeight > 0) {
      canvas.width = video.videoWidth
      canvas.height = video.videoHeight
      context.drawImage(video, 0, 0, canvas.width, canvas.height)
      
      const imageData = context.getImageData(0, 0, canvas.width, canvas.height)
      const code = jsQR(imageData.data, imageData.width, imageData.height, {
        inversionAttempts: 'dontInvert'
      })
      
      if (code) {
        handleBookQRResult(code.data)
      }
    }
  }, 500)
}

const stopBookCamera = () => {
  if (bookScanInterval.value) {
    clearInterval(bookScanInterval.value)
    bookScanInterval.value = null
  }
  
  if (bookVideoRef.value && bookVideoRef.value.srcObject) {
    const tracks = bookVideoRef.value.srcObject.getTracks()
    tracks.forEach(track => track.stop())
    bookVideoRef.value.srcObject = null
  }
  
  isBookCameraActive.value = false
  bookScanStatus.value = ''
}

// 处理书籍二维码结果 - 修复版：直接使用bookId查询
const handleBookQRResult = (text) => {
  console.log('识别到书籍二维码:', text)
  
  if (navigator.vibrate) {
    navigator.vibrate(200)
  }
  
  let bookId = null
  let title = null
  
  try {
    // 尝试解析JSON格式
    if (text.startsWith('{') && text.endsWith('}')) {
      const bookData = JSON.parse(text)
      bookId = bookData.bookId || bookData.id
      title = bookData.title
    } 
    // 尝试解析URL参数格式
    else if (text.includes('bookId=')) {
      const params = new URLSearchParams(text)
      bookId = params.get('bookId')
      title = params.get('title')
    } 
    // 直接作为bookId
    else {
      bookId = text
    }
  } catch (e) {
    console.error('解析书籍二维码失败:', e)
    bookId = text
  }
  
  if (!bookId) {
    bookScanStatus.value = '二维码数据无效'
    ElMessage.error('二维码数据无效，请扫描正确的书籍二维码')
    return
  }
  
  bookScanStatus.value = '识别成功！正在验证书籍...'
  
  // 停止摄像头扫描
  stopBookCamera()
  
  // 直接使用完整的bookId字符串查询
  verifyBook(bookId)
}

// 书籍图片上传相关
const handleBookFileChange = (file) => {
  if (bookPreviewUrl.value) {
    URL.revokeObjectURL(bookPreviewUrl.value)
  }
  bookPreviewUrl.value = URL.createObjectURL(file.raw)
  bookScanStatus.value = ''
}

const clearBookPreview = () => {
  if (bookPreviewUrl.value) {
    URL.revokeObjectURL(bookPreviewUrl.value)
    bookPreviewUrl.value = ''
  }
}

const scanBookQRFromImage = async () => {
  if (!bookPreviewUrl.value) {
    ElMessage.warning('请先上传二维码图片')
    return
  }

  bookImageScanning.value = true
  bookScanStatus.value = '正在识别二维码...'

  try {
    const img = new Image()
    img.src = bookPreviewUrl.value
    
    await new Promise((resolve, reject) => {
      img.onload = resolve
      img.onerror = reject
    })
    
    const canvas = document.createElement('canvas')
    const ctx = setupCanvasForFrequentRead(canvas)
    
    canvas.width = img.width
    canvas.height = img.height
    ctx.drawImage(img, 0, 0, img.width, img.height)
    
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    const code = jsQR(imageData.data, imageData.width, imageData.height, {
      inversionAttempts: 'dontInvert'
    })
    
    if (code) {
      handleBookQRResult(code.data)
    } else {
      bookScanStatus.value = '未识别到二维码'
      ElMessage.warning('未识别到二维码，请确保图片清晰')
    }
  } catch (error) {
    console.error('识别失败:', error)
    bookScanStatus.value = '识别失败，请重试'
    ElMessage.error('二维码识别失败')
  } finally {
    bookImageScanning.value = false
  }
}

// 验证书籍 - 通过bookId直接查询（使用字符串格式）
const verifyBook = async (bookId) => {
  try {
    console.log('验证书籍，bookId:', bookId)
    
    // 尝试通过查询参数搜索书籍
    const response = await axios.get(`${apiBaseUrl}/books/available`)
    const books = response.data
    
    console.log('所有可借阅书籍:', books)
    
    // 在书籍列表中查找匹配的书籍
    // 支持多种匹配方式：
    // 1. 直接匹配 id
    // 2. 匹配 bookId 字段
    // 3. 匹配 title + author 组合
    // 4. 模糊匹配 title
    let foundBook = null
    
    // 方法1：直接匹配 id（转换为字符串比较）
    foundBook = books.find(book => String(book.id) === String(bookId))
    
    // 方法2：匹配 bookId 字段
    if (!foundBook) {
      foundBook = books.find(book => book.bookId === bookId)
    }
    
    // 方法3：尝试解析bookId中的数字部分（如果bookId是BOOKxxx格式）
    if (!foundBook && typeof bookId === 'string' && bookId.startsWith('BOOK')) {
      const numericPart = bookId.replace('BOOK', '')
      foundBook = books.find(book => String(book.id) === numericPart)
    }
    
    // 方法4：如果还是找不到，尝试通过书名搜索
    if (!foundBook && bookId.length > 0) {
      foundBook = books.find(book => 
        book.title === bookId || 
        book.title.includes(bookId) ||
        book.author === bookId
      )
    }
    
    if (!foundBook) {
      bookScanStatus.value = '未找到该书籍，请确认二维码是否正确'
      ElMessage.error('未找到该书籍，请确认二维码是否正确')
      return
    }
    
    console.log('找到书籍:', foundBook)
    
    // 检查书籍状态
    if (foundBook.status === 'borrowed') {
      bookScanStatus.value = '该书已被借出，无法重复借阅'
      ElMessage.warning('该书已被借出，无法重复借阅')
      return
    }
    
    // 检查用户借阅上限
    if (maxBorrowCount.value <= 0) {
      bookScanStatus.value = '您已达到最大借阅数量（3本）'
      ElMessage.warning('您已达到最大借阅数量（3本），请先归还书籍')
      return
    }
    
    selectedBook.value = foundBook
    bookScanStatus.value = '验证成功！'
    ElMessage.success(`成功识别书籍：${foundBook.title}`)
    
    setTimeout(() => {
      activeStep.value = 2
    }, 1000)
    
  } catch (error) {
    console.error('验证书籍失败:', error)
    console.error('错误详情:', error.response?.data)
    
    // 显示更详细的错误信息
    const errorMsg = error.response?.data?.message || error.message || '验证失败，请重试'
    bookScanStatus.value = `验证失败：${errorMsg}`
    ElMessage.error(`验证书籍失败：${errorMsg}`)
  }
}

// ==================== 步骤导航 ====================
const goBackToUserScan = () => {
  activeStep.value = 0
  bookScanStatus.value = ''
  selectedBook.value = null
  if (bookPreviewUrl.value) {
    URL.revokeObjectURL(bookPreviewUrl.value)
    bookPreviewUrl.value = ''
  }
}

const goBackToBookScan = () => {
  activeStep.value = 1
}

// 确认借阅
const confirmBorrow = async () => {
  if (!borrowForm.contact) {
    ElMessage.warning('请输入联系电话')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(borrowForm.contact)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确认借阅《${selectedBook.value.title}》？`, '确认借阅', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    submitting.value = true
    
    const response = await axios.post(`${apiBaseUrl}/borrow/borrow`, {
      userId: studentInfo.value.id,
      bookId: selectedBook.value.id,
      period: borrowForm.period,
      pickupMethod: borrowForm.pickupMethod,
      contact: borrowForm.contact,
      remarks: borrowForm.remarks
    })
    
    if (response.data.success) {
      const borrowedBook = {
        id: selectedBook.value.id,
        title: selectedBook.value.title,
        author: selectedBook.value.author,
        borrowDate: new Date().toISOString().split('T')[0],
        dueDate: response.data.dueDate || dueDate.value
      }
      
      window.dispatchEvent(new CustomEvent('borrowed-books-updated', { 
        detail: { 
          userId: studentInfo.value.id,
          books: [borrowedBook]
        }
      }))
      
      window.dispatchEvent(new CustomEvent('stats-updated', { 
        detail: { userId: studentInfo.value.id }
      }))
      
      successDialogVisible.value = true
      ElMessage.success('借阅成功')
    } else {
      ElMessage.error(response.data.message || '借阅失败')
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('借阅失败:', error)
      if (error.response) {
        ElMessage.error('借阅失败：' + (error.response.data?.message || '未知错误'))
      } else {
        ElMessage.error('借阅失败，请重试')
      }
    }
  } finally {
    submitting.value = false
  }
}

// 路由跳转
const goToUserCenter = () => {
  successDialogVisible.value = false
  router.push('/user')
}

const continueBorrow = () => {
  successDialogVisible.value = false
  activeStep.value = 1
  selectedBook.value = null
  borrowForm.period = 14
  borrowForm.pickupMethod = 'self'
  borrowForm.contact = studentInfo.value?.phone || ''
  borrowForm.remarks = ''
  bookScanStatus.value = ''
  
  if (bookPreviewUrl.value) {
    URL.revokeObjectURL(bookPreviewUrl.value)
    bookPreviewUrl.value = ''
  }
}

// 监听模式切换
watch(scanMode, (newMode) => {
  if (newMode !== 'camera' && isCameraActive.value) {
    stopCamera()
  }
})

// 生命周期
onMounted(() => {
  getCameras()
})

onUnmounted(() => {
  stopCamera()
  stopBookCamera()
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  if (bookPreviewUrl.value) {
    URL.revokeObjectURL(bookPreviewUrl.value)
  }
})
</script>

<style scoped>
/* ==================== 清新风格样式 ==================== */
.borrow-book {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  background: #faf8f4;
  min-height: calc(100vh - 40px);
}

/* 返回按钮样式 */
.back-nav {
  margin-bottom: 20px;
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
  margin-bottom: 20px;
  color: #2c5a4f;
  font-weight: 500;
  font-size: 28px;
}

.step-content {
  margin-top: 20px;
}

/* 卡片样式 - 清新圆角 */
:deep(.el-card) {
  border-radius: 24px !important;
  border: 1px solid #efebe6 !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02) !important;
  background: white;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #efebe6;
  padding: 16px 20px;
  background: white;
  border-radius: 24px 24px 0 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: #2c5a4f;
}

/* 模式切换按钮 - 清新风格 */
.mode-switch {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

:deep(.el-radio-group) {
  gap: 12px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}

:deep(.el-radio-button__inner) {
  border-radius: 40px !important;
  padding: 8px 20px;
  border: 1px solid #e2e6e0;
  background: white;
  color: #8b9a8e;
  transition: all 0.2s;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #8fc1b0;
  border-color: #8fc1b0;
  color: white;
  box-shadow: none;
}

:deep(.el-radio-button__inner:hover) {
  border-color: #8fc1b0;
  color: #8fc1b0;
}

/* 摄像头区域 */
.camera-mode {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.camera-wrapper {
  width: 100%;
  max-width: 500px;
}

.camera-container {
  width: 100%;
  height: 350px;
  background: #2c5a4f;
  border-radius: 20px;
  overflow: hidden;
  position: relative;
  margin-bottom: 15px;
  border: 1px solid #e2e6e0;
}

.scanner-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scanner-video.hidden {
  display: none;
}

.camera-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #2c5a4f;
  color: white;
}

.placeholder-text {
  font-size: 16px;
  opacity: 0.9;
  margin-top: 12px;
}

/* 扫描框 */
.scan-frame {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 250px;
  height: 250px;
  pointer-events: none;
}

.scan-corner {
  position: absolute;
  width: 30px;
  height: 30px;
  border-color: #8fc1b0;
  border-style: solid;
}

.scan-corner.top-left {
  top: 0;
  left: 0;
  border-width: 3px 0 0 3px;
}

.scan-corner.top-right {
  top: 0;
  right: 0;
  border-width: 3px 3px 0 0;
}

.scan-corner.bottom-left {
  bottom: 0;
  left: 0;
  border-width: 0 0 3px 3px;
}

.scan-corner.bottom-right {
  bottom: 0;
  right: 0;
  border-width: 0 3px 3px 0;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #8fc1b0, transparent);
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% { top: 0; }
  50% { top: 100%; }
  100% { top: 0; }
}

.camera-select {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.select-label {
  font-size: 14px;
  color: #5a6e5c;
}

.camera-controls {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

/* 上传区域 */
.upload-mode {
  padding: 20px 0;
}

.upload-area {
  width: 100%;
  margin-bottom: 20px;
}

:deep(.el-upload-dragger) {
  border: 2px dashed #e2e6e0 !important;
  border-radius: 20px !important;
  background: #faf8f4 !important;
  transition: all 0.2s;
}

:deep(.el-upload-dragger:hover) {
  border-color: #8fc1b0 !important;
}

.upload-icon {
  font-size: 48px;
  color: #b8c4b0;
  margin-bottom: 10px;
}

.upload-text {
  font-size: 16px;
  color: #5a6e5c;
  margin-bottom: 5px;
}

.upload-hint {
  font-size: 12px;
  color: #b8c4b0;
}

.preview-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.preview-image {
  max-width: 300px;
  max-height: 300px;
  border-radius: 12px;
  border: 1px solid #efebe6;
  object-fit: contain;
}

.preview-actions {
  display: flex;
  gap: 10px;
}

/* 手动输入 */
.manual-mode {
  padding: 30px;
  max-width: 400px;
  margin: 0 auto;
}

/* 扫描状态 */
.scan-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border-radius: 40px;
  margin-top: 20px;
  font-size: 14px;
}

.status-success {
  background-color: #e8f0ec;
  color: #8fc1b0;
}

.status-error {
  background-color: #fef0ec;
  color: #e8a4a4;
}

.status-scanning {
  background-color: #f5f0e8;
  color: #c9b6a0;
}

/* 用户确认卡片 */
.user-confirm-card {
  margin-bottom: 20px;
}

:deep(.el-alert) {
  border-radius: 16px;
  background: #e8f0ec;
  border: none;
}

:deep(.el-alert__title) {
  color: #2c5a4f;
}

/* 书籍信息卡片 */
.book-info-card {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 16px;
  background: #faf8f4;
  border-radius: 20px;
}

.book-cover {
  width: 100px;
  height: 140px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #efebe6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  font-size: 36px;
}

.book-details {
  flex: 1;
}

.book-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 500;
  color: #2c5a4f;
}

.book-author {
  margin: 5px 0;
  color: #8b9a8e;
  font-size: 14px;
}

.book-isbn {
  margin: 5px 0;
  color: #b8c4b0;
  font-size: 13px;
}

.book-tags {
  margin-top: 10px;
}

/* 用户信息卡片 */
.user-info-card {
  margin-bottom: 20px;
  padding: 16px;
  background: #faf8f4;
  border-radius: 20px;
}

.user-info-card h4 {
  margin: 0 0 12px;
  color: #5a6e5c;
  font-size: 15px;
  font-weight: 500;
}

:deep(.el-descriptions) {
  background: transparent;
}

:deep(.el-descriptions__label) {
  color: #b8c4b0;
  font-weight: normal;
}

:deep(.el-descriptions__content) {
  color: #5a6e5c;
}

/* 表单 */
.borrow-form {
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #b8c4b0;
  margin-top: 5px;
}

.period-selector {
  width: 100%;
}

/* 按钮样式 - 清新风格 */
.step-actions {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
}

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

:deep(.el-button--danger) {
  border-radius: 40px;
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

/* 成功弹窗 */
.success-content {
  text-align: center;
  padding: 20px;
}

.success-icon {
  width: 60px;
  height: 60px;
  background: #8fc1b0;
  color: white;
  font-size: 32px;
  line-height: 60px;
  border-radius: 50%;
  margin: 0 auto 20px;
}

.success-content h3 {
  margin: 10px 0;
  color: #2c5a4f;
  font-weight: 500;
}

.success-content p {
  margin: 5px 0;
  color: #6c826e;
}

/* 响应式 */
@media (max-width: 768px) {
  .borrow-book {
    padding: 10px;
  }
  
  .camera-container {
    height: 300px;
  }
  
  .scan-frame {
    width: 200px;
    height: 200px;
  }
  
  :deep(.el-radio-button__inner) {
    padding: 6px 14px;
    font-size: 13px;
  }
  
  .book-info-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .book-cover {
    width: 120px;
    height: 160px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
  
  .preview-image {
    max-width: 250px;
  }
  
  .manual-mode {
    padding: 20px;
  }
  
  .preview-actions {
    flex-direction: column;
  }
}
</style>