在其他页面复用:
1.内联提示：
<LoginPrompt mode="inline" message="该功能需要登录" login-text="立即登录" />

2.点击/校验时弹窗：
<script setup>
import { ref } from 'vue'
import LoginPrompt from '@/components/LoginPrompt.vue'
import { useUserStore } from '@/store/user'
const loginPromptRef = ref(null)
const userStore = useUserStore()
const handleClick = () => {
  if (!userStore.isLoggedIn) {
    loginPromptRef.value.open('请先登录后使用该功能')
    return
  }
  // 已登录逻辑...
}
</script>

说明:
1.组件内部直接读取 useUserStore().isLoggedIn 判断是否登录；
2.点击“去登录”统一跳转 /subpkg/auth/login；