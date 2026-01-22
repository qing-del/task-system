import { createRouter, createWebHistory } from "vue-router";

const routes = [
    {
        path: '/',
        name: "Login",
        component: () => import('../views/Login.vue')   // 懒加载
    },
    {
        path: '/game',
        name: 'Game',
        component: () => import('../views/Game.vue')    // 游戏主界面
    },
    {
        path: '/shop',
        name: 'Shop',
        component: () => import('../views/Shop.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router