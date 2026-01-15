<template>
  <div class="shop-container" :class="{ 'fade-out': isLoggingOut }">
    <div class="header">
      <div class="brand">
        <span class="logo">🛒</span> 商品商店 <span class="version">v0.1</span>
      </div>
      <div class="user-info">
        <span class="commander-name">指挥官: {{ player.username || 'Guest' }}</span>
        <el-tag size="small" effect="dark" class="level-tag">Lv.{{ player.level }}</el-tag>
         <el-button type="success" size="small" plain @click="handback" style="margin-left: 15px">返回</el-button>
        <el-button type="danger" size="small" plain @click="handleLogout" style="margin-left: 15px">退出</el-button>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧玩家面板 -->
      <div class="player-panel">
        <el-card class="player-card glow-effect">
          <div class="player-header">
            <span class="logo">指挥官面板</span>
          </div>
          <div class="player-stats">
            <div class="stat-item">
              <span class="label">用户名：</span>
              <span class="value">{{ player.username || 'Guest' }}</span>
            </div>
            <div class="stat-item">
              <span class="label">等级：</span>
              <el-tag size="small" effect="dark" type="warning">Lv.{{ player.level }}</el-tag>
            </div>
            <div class="stat-item">
              <span class="label">精神：</span>
              <span class="value spirit">{{ player.spirit }}</span>
            </div>
            <div class="stat-item">
              <span class="label">体魄：</span>
              <span class="value body">{{ player.body }}</span>
            </div>
            <div class="stat-item">
              <span class="label">经验值：</span>
              <span class="value">{{ player.currentExp }}/{{player.totalExp}}</span>
            </div>
          </div>
          <div class="player-actions">
            <el-button type="success" size="small" @click="handback">返回游戏</el-button>
            <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
          </div>
        </el-card>
      </div>

      <!-- 右侧商品列表 -->
      <div class="shop-list-panel">
        <el-card class="shop-list-card glow-effect">
          <template #header>
            <div class="card-header">
              <span class="shop-title">🛒 商品商店</span>
              <el-form :model="searchCondition" inline>
                <el-form-item label="商品名称">
                  <el-input v-model="searchCondition.name" placeholder="请输入名称" clearable />
                </el-form-item>
                <el-button type="primary" size="small" @click="fetchShopList">搜索</el-button>
                <el-button size="small" @click="resetSearch">重置</el-button>
              </el-form>
            </div>
          </template>

          <el-table 
            :data="itemList" 
            style="width: 100%" 
            height="400" 
            v-loading="listLoading"
          >
            <el-table-column label="序号" width="60" align="center">
              <template #default="scope">
                {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品名称" show-overflow-tooltip />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="消耗" width="120" align="center">
              <template #default="scope">
                <el-tag 
                  :type="getCostTypeTag(scope.row.costType)" 
                  size="small"
                >
                  {{ getCostTypeName(scope.row.costType) }} {{ scope.row.costValue }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="效果" width="120" align="center">
              <template #default="scope">
                <el-tag 
                  :type="getEffectTypeTag(scope.row.effectType)" 
                  size="small"
                >
                  {{ getEffectTypeName(scope.row.effectType) }} +{{ scope.row.effectValue }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="库存" width="80" align="center">
              <template #default="scope">
                <el-tag 
                  :type="scope.row.stack ? 'success' : 'info'" 
                  size="small"
                >
                  {{scope.row.stack == -1 ? '无穷' : scope.row.stack }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="scope">
                <el-button type="success" size="small" @click="handleBuy(scope.row.id)">购买</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-area">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="totalItems"
              :page-size="pageSize"
              v-model:current-page="currentPage"
              @current-change="fetchShopList"
            />
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { jwtDecode } from 'jwt-decode'

const router = useRouter()
const token = localStorage.getItem('token')
let username = 'Guest'
try {
  if(token) username = jwtDecode(token).sub
} catch(e) { console.error(e) }

// --- 数据定义 ---
const player = reactive({
  username: username,
  level: 1,
  spirit: 0,
  body: 0
})

const itemList = ref([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(5)
const listLoading = ref(false)
const isLoggingOut = ref(false)

const searchCondition = reactive({
  name: ''
})

// --- 初始化 ---
onMounted(async () => {
  await fetchStatus()
  await fetchShopList()
})

// --- API 方法 ---
const fetchStatus = async () => {
  try {
    const res = await request.get('/player/status')
    if (res) Object.assign(player, res)
  } catch (e) { console.error(e) }
}

const fetchShopList = async () => {
  listLoading.value = true
  try {
    const res = await request.post('/shop/list', searchCondition, {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    if (res) {
      itemList.value = res.rows || [] 
      totalItems.value = res.total || 0
    }
  } catch (e) {
    ElMessage.error('获取商品列表失败')
  } finally {
    listLoading.value = false
  }
}

const handleBuy = async (itemId) => {
  if (!player.id) {
    ElMessage.error('玩家信息未加载')
    return
  }
  
  try {
    const res = await request.post('/shop/buyItem', null, {
      params: {
        itemId: itemId,
        playerId: player.id
      }
    })
    
    if (res.result) {
      ElMessage.success('购买成功！')
      await fetchStatus() // 刷新玩家状态
      await fetchShopList()
    } else {
      ElMessage.error('购买失败')
      ElMessage.error(res.info)
    }
  } catch (e) {
    ElMessage.error('购买请求失败')
  }
}

const resetSearch = () => {
  searchCondition.name = ''
  currentPage.value = 1
  fetchShopList()
}

const handback = () => {
  router.push('/game')
}

const handleLogout = () => {
  isLoggingOut.value = true
  ElMessage.success('正在登出...')
  setTimeout(() => {
    localStorage.removeItem('token')
    router.push('/')
  }, 500)
}

const getCostTypeName = (type) => {
  const types = ['经验', '精神', '体魄', '金币'];
  return types[type] || '未知';
};

const getCostTypeTag = (type) => {
  const tags = ['warning', 'primary', 'success', 'danger'];
  return tags[type] || 'info';
};

const getEffectTypeName = (type) => {
  const types = ['经验', '精神', '体魄'];
  return types[type] || '未知';
};

const getEffectTypeTag = (type) => {
  const tags = ['warning', 'primary', 'success'];
  return tags[type] || 'info';
};

</script>

<style scoped>
.shop-container {
  padding: 20px;
  min-height: 100vh;
  background: #0f172a;
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
}

.main-content {
  display: flex;
  gap: 20px;
  margin-top: 20px;
  flex: 1;
}

.player-panel {
  flex: 1;
  max-width: 300px;
}

.shop-list-panel {
  flex: 2;
  min-width: 0; /* 修复flex容器宽度异常扩展问题 */
}

.player-card {
  background: #1e293b;
  border: 1px solid #334155;
  color: white;
  border-radius: 12px;
  overflow: hidden;
}

.player-header {
  padding: 15px;
  background: #0f172a;
  font-weight: bold;
  border-bottom: 1px solid #334155;
}

.player-stats {
  padding: 20px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #334155;
}

.label {
  color: #94a3b8;
}

.value {
  font-weight: bold;
}

.spirit {
  color: #3b82f6;
}

.body {
  color: #10b981;
}

.player-actions {
  padding: 15px 20px;
  display: flex;
  gap: 10px;
  border-top: 1px solid #334155;
}

.shop-title {
  font-weight: bold;
  color: #f97316;
  font-size: 1.1em;
}

:deep(.el-table) { 
  width: 100%;
  table-layout: fixed; /* 确保表格列宽正确计算 */
  background-color: #1e293b; 
  color: #e2e8f0; 
  --el-table-tr-bg-color: #1e293b; 
}
:deep(.el-table th.el-table__cell) { 
  background-color: #0f172a; 
  color: #94a3b8; 
}

.shop-list-card { 
  width: 100%; 
  background: #1e293b; 
  border: 1px solid #334155; 
  color: white; 
  border-radius: 12px;
}

.pagination-area { 
  margin-top: 20px; 
  display: flex; 
  justify-content: center; 
}

.glow-effect {
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.2);
  transition: box-shadow 0.3s ease;
}

.glow-effect:hover {
  box-shadow: 0 0 25px rgba(59, 130, 246, 0.4);
}
</style>