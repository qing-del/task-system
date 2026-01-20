<template>
  <div class="game-container" :class="{ 'fade-out': isLoggingOut }">
    <div class="header">
      <div class="brand">
        <span class="logo">🧬</span> LifeGame <span class="version">v0.3</span>
      </div>
      <div class="user-info">
        <div class="user-profile-trigger" @click="openProfile">
          <el-avatar :size="40" :src="player.headPhoto" class="user-avatar">
            {{ player.username ? player.username.substring(0, 1).toUpperCase() : 'U' }}
          </el-avatar>
          <div class="user-text">
            <span class="commander-name">{{ player.username || 'Unknown' }}</span>
            <el-tag size="small" effect="dark" class="level-tag">Lv.{{ player.level }}</el-tag>
          </div>
        </div>

        <el-button type="primary" size="small" @click="goToShop" style="margin-left: 20px">🛒 商店</el-button>
        <el-button type="danger" size="small" plain @click="handleLogout" style="margin-left: 10px">断开连接</el-button>
      </div>

      <el-dialog v-model="profileVisible" title="👨‍🚀 指挥官档案 (User Profile)" width="480px" destroy-on-close>
        <el-tabs v-model="activeTab" class="profile-tabs">

          <el-tab-pane label="基本资料" name="info">
            <div class="avatar-upload-container">
              <el-upload class="avatar-uploader" action="#" :show-file-list="false" :http-request="handleUploadAvatar"
                :before-upload="beforeAvatarUpload">
                <img v-if="player.headPhoto" :src="player.headPhoto" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon">
                  <Plus />
                </el-icon>
                <div class="upload-tip">点击更换头像 (Max 2MB)</div>
              </el-upload>
            </div>

            <el-form :model="profileForm" label-position="top" class="profile-form">
              <el-form-item label="指挥官代号 (Username)">
                <el-input v-model="profileForm.newUsername" :placeholder="player.username" />
              </el-form-item>
              <el-button type="primary" @click="updateUsername"
                style="width: 100%; margin-top: 10px;">保存档案修改</el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="安全设置" name="security">
            <el-form :model="passwordForm" label-position="top" class="profile-form">
              <el-form-item label="原验证密钥 (Old Password)">
                <el-input v-model="passwordForm.password" type="password" show-password />
              </el-form-item>
              <el-form-item label="新验证密钥 (New Password)">
                <el-input v-model="passwordForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-button type="danger" @click="updatePassword" style="width: 100%; margin-top: 10px;">更新安全密钥</el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-dialog>
    </div>

    <div class="main-dashboard">
      <el-card class="status-card glow-effect">
        <template #header>
          <div class="card-header">
            <span>🧬 身体机能</span>
          </div>
        </template>

        <div class="attributes-box">
          <div class="attr-row">
            <span class="attr-label">🧠 精神 (Spirit)</span>
            <el-progress :percentage="calculateAttrPercent(player.spirit)" :format="() => player.spirit"
              color="#b39ddb" />
          </div>
          <div class="attr-row">
            <span class="attr-label">💪 体魄 (Body)</span>
            <el-progress :percentage="calculateAttrPercent(player.body)" :format="() => player.body" color="#ef9a9a" />
          </div>
          <div class="attr-row">
            <span class="attr-label">💴 硬币 (Coin)</span>
            <el-progress :percentage="calculateAttrPercent(player.coin)" :format="() => player.coin" color="#ef9a9a" />
          </div>
        </div>

        <div class="exp-section">
          <div class="exp-info">
            <span>EXP 进度</span>
            <span class="exp-num">{{ player.currentExp }} / {{ getNextLevelExp() }}</span>
          </div>
          <el-progress :text-inside="true" :stroke-width="18" :percentage="calculateExpPercent()" status="exception"
            color="linear-gradient(90deg, #409eff, #36d1dc)" />
        </div>
      </el-card>

      <el-card class="task-list-card glow-effect">
        <template #header>
          <div class="card-header">
            <span>📂 任务档案 (Mission Archives)</span>
            <el-button type="primary" size="small" @click="handleGenerateTask" :loading="generateLoading">
              ⚡ 呼叫 AI 生成新任务
            </el-button>
            <el-button type="success" size="small" @click="openCreateTaskDialog" style="margin-left: 10px">📝
              创建任务</el-button>
          </div>
        </template>

        <el-form :model="searchCondition" inline style="margin-bottom: 15px;">
          <el-form-item label="任务标题">
            <el-input v-model="searchCondition.title" placeholder="请输入标题" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchCondition.status" clearable placeholder="请选择状态">
              <el-option label="进行中" :value="0" />
              <el-option label="已完成" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="taskList" style="width: 100%" height="400" @row-dblclick="openTaskDetail"
          v-loading="listLoading" :row-class-name="tableRowClassName">
          <el-table-column label="ID" width="60" align="center">
            <template #default="scope">
              {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="title" label="任务目标" show-overflow-tooltip />
          <el-table-column prop="expReward" label="奖励EXP" width="100" align="center">
            <template #default="scope">
              <el-tag type="warning" size="small">+{{ scope.row.expReward }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 1" type="success" effect="dark">已完成</el-tag>
              <el-tag v-else type="warning" effect="plain">进行中</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="210" align="center">
            <template #default="scope">
              <el-button type="success" size="small" @click="handleCompleteTask(scope.row)">完成</el-button>
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-area">
          <el-pagination background layout="prev, pager, next" :total="totalTasks" :page-size="pageSize"
            v-model:current-page="currentPage" @current-change="fetchTaskList" />
        </div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" title="任务详情" width="30%" class="task-dialog">
      <div v-if="selectedTask">
        <h3>{{ selectedTask.title }}</h3>
        <p class="desc-text">{{ selectedTask.description }}</p>

        <div class="reward-section">
          <div class="reward-item">
            <span class="reward-label">🏆 经验奖励:</span>
            <span class="reward-value">+{{ selectedTask.expReward }} EXP</span>
          </div>
          <div class="reward-item">
            <span class="reward-label">🎁 属性奖励:</span>
            <span class="reward-value">
              {{ selectedTask.rewardType === 0 ? '精神' : '体魄' }} +{{ selectedTask.reward }}
            </span>
          </div>
          <div class="reward-item">
            <span class="reward-label">💴 硬币奖励:</span>
            <span class="reward-value">+{{ selectedTask.coinReward }}</span>
          </div>
          <div class="reward-item">
            <span class="reward-label">📌 任务状态:</span>
            <span class="reward-value">
              <el-tag :type="selectedTask.status === 1 ? 'success' : 'warning'">
                {{ selectedTask.status === 1 ? '已完成' : '进行中' }}
              </el-tag>
            </span>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关 闭</el-button>
          <el-button v-if="selectedTask && selectedTask.status === 0" type="primary" @click="handleCompleteTask"
            :loading="completeLoading">
            ✅ 立即完成
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="createDialogVisible" :title="isEditing ? '编辑' : '创建新任务'" width="30%">
      <el-form :model="newTask">
        <el-form-item label="任务标题">
          <el-input v-model="newTask.title" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="newTask.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="奖励EXP">
          <el-input-number v-model="newTask.expReward" :min="0" />
        </el-form-item>
        <el-form-item label="奖励类型">
          <el-select v-model="newTask.rewardType" clearable placeholder="请选择状态">
            <el-option label="精神属性" :value="0" />
            <el-option label="肉体属性" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖励属性">
          <el-input-number v-model="newTask.reward" :min="0" />
        </el-form-item>
        <el-form-item label="硬币奖励">
          <el-input-number v-model="newTask.coinReward" :min="0" />
        </el-form-item>
        <el-form-item v-if="isEditing === true" label="完成状态">
          <el-select v-model="newTask.rewardType" clearable placeholder="请选择状态">
            <el-option label="未完成" :value="0" />
            <el-option label="已完成" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTask">{{ isEditing ? '确认修改' : '创建' }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue' // 引入图标

import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { useRouter } from 'vue-router'
import { ElFormItem, ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import { jwtDecode } from 'jwt-decode'


const router = useRouter()
const token = localStorage.getItem('token')
let username = 'Guest'
try {
  if (token) username = jwtDecode(token).sub
} catch (e) { console.error(e) }

// --- 数据定义 ---
const player = reactive({
  username: username,
  level: 1,
  spirit: 0,
  body: 0,
  currentExp: 0,
  totalExp: 100,
  coin: 0,
  headPhoto: '' // <--- 新增这个字段
})

// 用户接口开发之后的新增字段
const profileVisible = ref(false)
const activeTab = ref('info')
const profileForm = reactive({ newUsername: '' })
const passwordForm = reactive({ password: '', newPassword: '' })

// 列表相关
const taskList = ref([])
const totalTasks = ref(0)
const currentPage = ref(1)
const pageSize = ref(5) // 每页显示5条
const listLoading = ref(false)
const isLoggingOut = ref(false)
const generateLoading = ref(false)

// 详情弹窗相关
const dialogVisible = ref(false)
const selectedTask = ref(null)
const completeLoading = ref(false)

// 筛选与创建任务相关
const searchCondition = ref({
  title: '',
  status: null
})

const createDialogVisible = ref(false)
const isEditing = ref(false)
const newTask = ref({
  title: '',
  description: '',
  expReward: 0,
  rewardType: 0,
  reward: 0,
  coinReward: 0
})

// --- 初始化 ---
onMounted(async () => {
  await fetchStatus()     // 获取等级、属性
  await fetchTaskList()   // 获取任务
  await fetchUserInfo()   // <--- 新增：获取头像和完整用户名
})

// --- API 方法 ---

// 1. 获取状态
const fetchStatus = async () => {
  try {
    const res = await request.get('/player/status')
    if (res) Object.assign(player, res)
  } catch (e) { console.error(e) }
}

// 2. 获取任务列表 (对接条件查询接口)
const fetchTaskList = async (condition = {}) => {
  listLoading.value = true
  try {
    const res = await request.post(`/task/taskListByCondition`, condition, {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    if (res) {
      taskList.value = res.rows || []
      totalTasks.value = res.total || 0
    }
  } catch (e) {
    ElMessage.error('获取任务列表失败')
  } finally {
    listLoading.value = false
  }
}

// 3. 生成新任务
const handleGenerateTask = async () => {
  try {
    await ElMessageBox.confirm('确定要生成新任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    generateLoading.value = true
    const res = await request.post('/task/generate')
    if (res) {
      ElNotification.success({ title: '新任务', message: `任务 [${res.title}] 已送达档案库` })
      // 生成完后，刷新列表到第一页，看最新任务
      currentPage.value = 1
      await fetchTaskList()
    }
  } catch (e) {
    ElMessage.error('AI 连接中断')
  } finally {
    generateLoading.value = false
  }
}

// 4. 完成任务
const handleCompleteTask = async () => {
  if (!selectedTask.value) return
  completeLoading.value = true
  try {
    const res = await request.post(`/task/complete?taskId=${selectedTask.value.id}`)
    if (res) {
      Object.assign(player, res) // 更新属性
      ElMessage.success(`结算完成！经验 +${selectedTask.value.expReward}`)
      dialogVisible.value = false
      await fetchTaskList() // 刷新列表状态
    }
  } catch (e) {
    ElMessage.error('结算失败')
  } finally {
    completeLoading.value = false
  }
}

// 5. 获取用户详情 (包含头像)
const fetchUserInfo = async () => {
  try {
    // 假设你的 Token 里解析出来的 username 是准的，或者这里直接调 /user/getInfo 不传参（如果后端支持从 Token 取）
    // 根据你的 UserController，它需要 username 参数
    const res = await request.get(`/user/getInfo?username=${player.username}`)
    if (res) {
      player.headPhoto = res.headPhoto
      // 更新一下本地显示的用户名，以防有变化
      player.username = res.username
    }
  } catch (e) {
    console.error("获取用户信息失败", e)
  }
}

// 打开弹窗
const openProfile = () => {
  profileForm.newUsername = player.username
  profileVisible.value = true
}


// 6. 上传头像
const handleUploadAvatar = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    // 调用后端接口
    const res = await request.post('/user/uploadAvatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    // 你的后端直接返回的是 String URL，但在 request.js 拦截器里可能被处理过
    // 如果拦截器直接返回 response.data，那么 res 就是 url 字符串
    const url = res 
    
    if (url && typeof url === 'string' && url.startsWith('http')) {
      player.headPhoto = url
      ElMessage.success('头像上传成功！')
    } else {
      ElMessage.warning('上传返回异常，请刷新查看')
    }
    // 刷新一下信息
    await fetchUserInfo()
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

// 上传前校验
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 7. 修改用户名
const updateUsername = async () => {
  if (!profileForm.newUsername) return ElMessage.warning('用户名不能为空')
  try {
    const res = await request.put('/user/updateName', {
      username: player.username, 
      newUsername: profileForm.newUsername
    })
    if (res.result) {
      ElMessage.success('更名成功，系统将重新连接...')
      profileVisible.value = false
      handleLogout() // 强制登出让用户重新登录
    } else {
      ElMessage.error(res.info)
    }
  } catch (e) { ElMessage.error('更名失败') }
}

// 8. 修改密码
const updatePassword = async () => {
  if (!passwordForm.password || !passwordForm.newPassword) return ElMessage.warning('请填写完整')
  try {
    const res = await request.put('/user/updatePassword', {
      username: player.username,
      password: passwordForm.password,
      newPassword: passwordForm.newPassword
    })
    if (res.result) {
      ElMessage.success('密钥更新成功，请重新验证')
      profileVisible.value = false
      handleLogout()
    } else {
      ElMessage.error(res.info)
    }
  } catch (e) { ElMessage.error('修改失败') }
}
// --- 交互辅助 ---
const openTaskDetail = (row) => {
  selectedTask.value = row
  dialogVisible.value = true
}

const handleSearch = () => {
  currentPage.value = 1
  fetchTaskList(searchCondition.value)
}

const resetSearch = () => {
  searchCondition.value = { title: '', status: null }
  currentPage.value = 1
  fetchTaskList({})
}

const openCreateTaskDialog = () => {
  newTask.value = { title: '', description: '', expReward: 0 }
  isEditing.value = false
  newTask.status = 0
  createDialogVisible.value = true
}

const submitTask = async () => {
  try {
    const action = isEditing.value ? '修改' : '创建'
    await ElMessageBox.confirm(`确定要${action}该任务吗？`, '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (isEditing.value) {
      await request.put('/task/update', newTask.value)
      ElMessage.success('任务更新成功！')
    } else {
      await request.post('/task/create', newTask.value)
      ElMessage.success('任务创建成功！')
    }

    createDialogVisible.value = false
    isEditing.value = false
    currentPage.value = 1
    fetchTaskList(searchCondition.value)
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(isEditing.value ? '更新任务失败' : '创建任务失败')
    }
  }
}

const handleEdit = (row) => {
  newTask.value = { ...row }
  isEditing.value = true
  createDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/task/delete?id=${row.id}`)
    ElMessage.success('删除成功')
    fetchTaskList()
  } catch (e) {
    // 用户取消
  }
}

const tableRowClassName = ({ row }) => {
  if (row.status === 1) return 'success-row'
  return ''
}

// 计算属性
const getNextLevelExp = () => player.totalExp || 100 // 防止为0
const calculateExpPercent = () => {
  const max = getNextLevelExp()
  if (max === 0) return 0
  return Math.min((player.currentExp / max) * 100, 100)
}
const calculateAttrPercent = (val) => Math.min(val, 100)

const handleLogout = () => {
  isLoggingOut.value = true
  ElMessage.success('正在登出...')
  setTimeout(() => {
    localStorage.removeItem('token')
    router.push('/')
  }, 500)
}

const goToShop = () => {
  router.push('/shop')
}

</script>

<style scoped>
.game-container {
  min-height: 100vh;
  background-color: #0f172a;
  color: #e2e8f0;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, sans-serif;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background: rgba(30, 41, 59, 0.9);
  border-bottom: 1px solid #334155;
  border-radius: 12px;
  margin-bottom: 20px;
}

.brand {
  font-size: 20px;
  font-weight: bold;
  color: #38bdf8;
}

.main-dashboard {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.status-card {
  width: 350px;
  background: #1e293b;
  border: 1px solid #334155;
  color: white;
}

.task-list-card {
  width: 600px;
  background: #1e293b;
  border: 1px solid #334155;
  color: white;
}

/* 覆盖 Element 表格样式以适应深色主题 */
:deep(.el-table) {
  background-color: #1e293b;
  color: #e2e8f0;
  --el-table-tr-bg-color: #1e293b;
  --el-table-header-bg-color: #0f172a;
  --el-table-border-color: #334155;
}

:deep(.el-table th.el-table__cell) {
  background-color: #0f172a;
  color: #94a3b8;
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell) {
  background-color: #334155;
  cursor: pointer;
}

:deep(.el-card) {
  background: #1e293b;
  border: none;
  color: white;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #334155;
  padding: 15px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-area {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.desc-text {
  background: #f0f2f5;
  padding: 10px;
  border-radius: 4px;
  color: #333;
  line-height: 1.6;
}

.reward-section {
  margin-top: 15px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.reward-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed #dee2e6;
  font-size: 14px;
}

.reward-item:last-child {
  border-bottom: none;
}

.reward-label {
  font-weight: 600;
  color: #495057;
}

.reward-value {
  color: #007bff;
}

/* 经验条样式 */
.attr-row {
  margin-bottom: 15px;
}

.attr-label {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  color: #cbd5e1;
}

.fade-out {
  transition: opacity 0.5s ease;
  opacity: 0;
}

/* 用户信息区交互样式 */
.user-profile-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 8px;
  transition: background 0.3s;
}
.user-profile-trigger:hover {
  background: rgba(255, 255, 255, 0.1);
}
.user-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.commander-name {
  font-weight: bold;
  font-size: 14px;
  color: #e2e8f0;
}

/* 头像上传组件样式 */
.avatar-upload-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.avatar-uploader .el-upload {
  border: 1px dashed #4b5563;
  border-radius: 50%; /* 圆形头像 */
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  background: #1f2937;
}
.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover; /* 防止图片变形 */
}
.upload-tip {
  text-align: center;
  font-size: 12px;
  color: #6b7280;
  margin-top: 8px;
}
.profile-form {
  padding: 0 10px;
}
</style>