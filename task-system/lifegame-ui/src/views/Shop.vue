<template>
  <div class="shop-container" :class="{ 'fade-out': isLoggingOut }">
    <div class="header">
      <div class="brand">
        <span class="logo">🛒</span> 黑市 <span class="version">v1.0</span>
      </div>
      <div class="user-info">
        <span class="commander-name">指挥官: {{ player.username || 'Guest' }}</span>
        <el-tag size="small" effect="dark" class="level-tag">Lv.{{ player.level }}</el-tag>
        <div class="currency-display">
          <el-tag type="warning" effect="plain" round>💰 {{ player.coin || 0 }}</el-tag>
        </div>
        <el-button type="success" size="small" plain @click="handback" style="margin-left: 15px">返回基地</el-button>
        <el-button type="danger" size="small" plain @click="handleLogout" style="margin-left: 10px">断开连接</el-button>
      </div>
    </div>

    <div class="main-content">
      <div class="player-panel">
        <el-card class="player-card glow-effect">
          <div class="player-header">
            <span class="logo">🧬 生体机能</span>
          </div>
          <div class="player-stats">
            <div class="stat-row">
              <span class="label">精神 (Spirit)</span>
              <span class="value spirit">{{ player.spirit }}</span>
            </div>
            <el-progress :percentage="calculateAttrPercent(player.spirit)" :show-text="false" color="#3b82f6" :stroke-width="6"/>
            
            <div class="stat-row" style="margin-top: 15px;">
              <span class="label">体魄 (Body)</span>
              <span class="value body">{{ player.body }}</span>
            </div>
            <el-progress :percentage="calculateAttrPercent(player.body)" :show-text="false" color="#10b981" :stroke-width="6"/>

            <div class="stat-separator"></div>

            <div class="stat-row">
              <span class="label">当前经验</span>
              <span class="value exp">{{ player.currentExp }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="tips-card" style="margin-top: 20px; background: #1e293b; border: 1px dashed #334155; color: #64748b;">
          <div style="text-align: center; font-size: 13px;">
            <p>💡 提示：购买后立即生效</p>
            <p>某些物品需要等级解锁</p>
          </div>
        </el-card>
      </div>

      <div class="shop-list-panel">
        <div class="filter-bar">
          <el-input 
            v-model="searchCondition.name" 
            placeholder="搜索违禁品..." 
            prefix-icon="Search"
            style="width: 200px;" 
            clearable
            @clear="fetchShopList"
            @keyup.enter="fetchShopList"
          />
          <el-button type="primary" @click="fetchShopList">扫描</el-button>
          <el-button type="info" plain @click="resetSearch">重置</el-button>
        </div>

        <div class="goods-grid" v-loading="listLoading">
          <el-row :gutter="20">
            <el-col 
              v-for="item in itemList" 
              :key="item.id" 
              :xs="24" :sm="12" :md="8" :lg="6" :xl="6"
            >
              <div class="item-card-wrapper">
                <el-card class="item-card" :body-style="{ padding: '0px' }">
                  <div class="item-image" :class="getItemBgClass(item.effectType)">
                    <span class="item-emoji">{{ getItemEmoji(item.name) }}</span>
                    <div class="stock-badge" :class="{ 'out-of-stock': item.stack === 0 }">
                      {{ item.stack === -1 ? '∞' : `剩 ${item.stack}` }}
                    </div>
                  </div>
                  
                  <div class="item-content">
                    <h3 class="item-name" :title="item.name">{{ item.name }}</h3>
                    <p class="item-desc" :title="item.description">{{ item.description }}</p>
                    
                    <div class="effect-tag">
                      <el-tag size="small" :type="getEffectTypeTag(item.effectType)" effect="light">
                        {{ getEffectTypeName(item.effectType) }} +{{ item.effectValue }}
                      </el-tag>
                    </div>

                    <div class="item-footer">
                      <div class="price-box">
                        <span class="cost-label">售价:</span>
                        <span class="cost-value" :class="getCostColorClass(item.costType)">
                          {{ item.costValue }} {{ getCostTypeName(item.costType) }}
                        </span>
                      </div>
                      <el-button 
                        type="primary" 
                        size="small" 
                        class="buy-btn"
                        :disabled="item.stack === 0"
                        @click="handleBuy(item.id)"
                      >
                        购买
                      </el-button>
                    </div>
                  </div>
                </el-card>
              </div>
            </el-col>
          </el-row>

          <el-empty v-if="itemList.length === 0 && !listLoading" description="货架空空如也..." />
        </div>

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
import { Search } from '@element-plus/icons-vue' // 需要确保安装了 icons

const router = useRouter()
const token = localStorage.getItem('token')
let username = 'Guest'
try {
  if(token) username = jwtDecode(token).sub
} catch(e) { console.error(e) }

// --- 数据定义 ---
const player = reactive({
  id: null,
  username: username,
  level: 1,
  spirit: 0,
  body: 0,
  currentExp: 0,
  coin: 0 // 假设你有金币字段
})

const itemList = ref([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(8) // 网格布局每页可以多显示一点，比如 8 或 12
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
    // 注意：这里 condition 是通过 Body 传的，分页通过 Params 传
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
  if (!player.id) return ElMessage.error('玩家信息未加载')
  
  try {
    const res = await request.post('/shop/buyItem', {
      itemId: itemId,
      playerId: player.id
    })
    
    if (res.result) {
      ElMessage.success({ message: '交易达成！属性已更新', type: 'success' })
      await fetchStatus() 
      await fetchShopList()
    } else {
      ElMessage.error(res.info || '购买失败')
    }
  } catch (e) {
    console.error(e) // 打印错误方便调试
    ElMessage.error('交易中断')
  }
}

const resetSearch = () => {
  searchCondition.name = ''
  currentPage.value = 1
  fetchShopList()
}

const handback = () => router.push('/game')

const handleLogout = () => {
  isLoggingOut.value = true
  setTimeout(() => {
    localStorage.removeItem('token')
    router.push('/')
  }, 500)
}

// --- 辅助显示函数 ---

// 根据商品名给个 Emoji，增加趣味性
const getItemEmoji = (name) => {
  if (name.includes('药') || name.includes('水')) return '🧪';
  if (name.includes('书') || name.includes('南')) return '📘';
  if (name.includes('剑') || name.includes('刀')) return '⚔️';
  if (name.includes('哑铃') || name.includes('铁')) return '🏋️';
  if (name.includes('卡')) return '💳';
  return '📦';
}

const getCostTypeName = (type) => ['EXP', '精神', '体魄', '金币'][type] || '??'
const getEffectTypeName = (type) => ['经验', '精神', '体魄'][type] || '未知'
const getEffectTypeTag = (type) => ['warning', 'primary', 'success'][type] || 'info'

// 动态样式类
const getCostColorClass = (type) => {
  return ['cost-exp', 'cost-spirit', 'cost-body', 'cost-coin'][type]
}

const getItemBgClass = (effectType) => {
  // 根据效果类型改变卡片头部的背景色
  return ['bg-exp', 'bg-spirit', 'bg-body'][effectType] || 'bg-default'
}

const calculateAttrPercent = (val) => Math.min(val, 100)
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

/* 顶部与通用布局 */
.header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 20px; height: 60px; background: rgba(30, 41, 59, 0.9);
  border-bottom: 1px solid #334155; border-radius: 12px; margin-bottom: 20px;
}
.brand { font-size: 20px; font-weight: bold; color: #f97316; }

.main-content { display: flex; gap: 20px; flex: 1; }
.player-panel { flex: 0 0 280px; } /* 固定宽度 */
.shop-list-panel { flex: 1; min-width: 0; display: flex; flex-direction: column; }

/* 玩家面板样式 */
.player-card { background: #1e293b; border: 1px solid #334155; color: white; border-radius: 12px; }
.player-header { padding: 15px; background: #0f172a; border-bottom: 1px solid #334155; font-weight: bold; }
.player-stats { padding: 15px; }
.stat-row { display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 14px; color: #94a3b8; }
.stat-row .value { color: white; font-weight: bold; }
.stat-separator { height: 1px; background: #334155; margin: 15px 0; }

/* 搜索栏 */
.filter-bar { margin-bottom: 20px; display: flex; gap: 10px; }

/* 商品卡片样式 (核心) */
.goods-grid { flex: 1; }
.item-card-wrapper { margin-bottom: 20px; }
.item-card { 
  background: #1e293b; border: 1px solid #334155; color: white; 
  transition: transform 0.2s, box-shadow 0.2s; 
}
.item-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.3); border-color: #475569; }

/* 卡片顶部图片区 */
.item-image { 
  height: 100px; display: flex; justify-content: center; align-items: center; 
  position: relative; font-size: 40px; 
}
.bg-exp { background: linear-gradient(135deg, #4b3d20, #1e293b); }
.bg-spirit { background: linear-gradient(135deg, #1e3a8a, #1e293b); }
.bg-body { background: linear-gradient(135deg, #064e3b, #1e293b); }
.bg-default { background: #334155; }

.stock-badge { 
  position: absolute; top: 5px; right: 5px; 
  background: rgba(0,0,0,0.6); padding: 2px 6px; border-radius: 4px; font-size: 10px; 
}
.stock-badge.out-of-stock { background: #ef4444; }

/* 卡片内容区 */
.item-content { padding: 15px; }
.item-name { margin: 0 0 5px 0; font-size: 16px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-desc { font-size: 12px; color: #94a3b8; height: 32px; overflow: hidden; margin-bottom: 10px; line-height: 1.4; }
.effect-tag { margin-bottom: 15px; text-align: center; }

/* 底部购买区 */
.item-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #334155; padding-top: 10px; }
.price-box { display: flex; flex-direction: column; }
.cost-label { font-size: 10px; color: #64748b; }
.cost-value { font-weight: bold; font-size: 14px; }

/* 价格颜色 */
.cost-exp { color: #facc15; }
.cost-spirit { color: #60a5fa; }
.cost-body { color: #34d399; }
.cost-coin { color: #fbbf24; }

.pagination-area { margin-top: 20px; display: flex; justify-content: center; }

/* 覆盖 Element Plus 样式 */
:deep(.el-card) { border: none; }
:deep(.el-input__wrapper) { background-color: #1e293b; box-shadow: 0 0 0 1px #334155 inset; }
</style>