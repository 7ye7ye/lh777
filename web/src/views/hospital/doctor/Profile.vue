<template>
  <PageWrapper title="医生个人信息">
    <a-form :model="profile" label-col="{ span: 6 }" wrapper-col="{ span: 14 }">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="姓名">
            <a-input v-model:value="profile.doctorName" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="账号">
            <a-input v-model:value="profile.userAccount" disabled />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="邮箱">
            <a-input v-model:value="profile.email" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="科室">
            <a-input v-model:value="profile.deptName" disabled />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="职称">
            <a-input v-model:value="profile.title" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="擅长">
            <a-input v-model:value="profile.specialty" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="头像URL">
        <a-input v-model:value="profile.avatar" />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="是否出诊">
            <a-switch v-model:checked="activeChecked" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="医生简介">
        <a-textarea v-model:value="profile.doctorDesc" rows="4" />
      </a-form-item>

      <a-form-item label="信息修改审核状态">
        <a-tag :color="verifyColor(profile.updateVerify)">
          {{ verifyText(profile.updateVerify) }}
        </a-tag>
      </a-form-item>

      <div style="margin-top: 12px;">
        <a-button type="primary" @click="handleSubmit">保存</a-button>
      </div>
    </a-form>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, computed } from 'vue';
import { PageWrapper } from '/@/components/Page';
import { message, Tag } from 'ant-design-vue';
import { getDoctorProfile, updateDoctorProfile, type Doctor } from '/@/api/hospital/doctor';
import { useRoute } from 'vue-router';

export default defineComponent({
  name: 'DoctorProfile',
  components: { PageWrapper, Tag },
  setup() {
    const route = useRoute();
    const profile = ref<Doctor>({
      doctorId: 0,
      doctorName: '',
      userId: 0,
      deptId: 0,
      title: '',
      specialty: '',
      doctorDesc: '',
      avatar: '',
      isActive: 1,
      updateVerify: 0,
      userAccount: '',
      email: '',
    } as any);

    const activeChecked = computed({
      get() {
        return profile.value.isActive === 1;
      },
      set(val: boolean) {
        profile.value.isActive = val ? 1 : 0;
      },
    });

    function verifyColor(v?: number) {
      switch (v) {
        case 1: return 'gold';
        case 2: return 'green';
        case 3: return 'red';
        default: return 'default';
      }
    }
    function verifyText(v?: number) {
      switch (v) {
        case 1: return '待审核';
        case 2: return '已通过';
        case 3: return '已驳回';
        default: return '未提交';
      }
    }

    async function fetchProfile() {
      // 优先：当前登录医生（仅用真实接口）
      try {
        const { getMyDoctorProfile } = await import('/@/api/hospital/doctor');
        const me: any = await getMyDoctorProfile();

        // 兼容后端异常返回：数组或空对象
        if (me && !Array.isArray(me) && me.doctorId) {
          profile.value = me as Doctor;
          return;
        }
      } catch (e) {
        console.warn('getMyDoctorProfile失败，将尝试路由doctorId兜底', e);
      }

      // 兜底：路由上携带的doctorId（仍为真实数据源）
      const doctorId = Number(route.query.doctorId || 0);
      if (doctorId) {
        const data = await getDoctorProfile({ doctorId });
        profile.value = data;
        return;
      }
      message.error('无法获取医生信息，请重新登录或检查账号绑定');
    }

    // base64url 解析 JWT 的 payload，取 username
    function parseUsernameFromToken(token?: string): string {
      if (!token) return '';
      const parts = token.split('.');
      if (parts.length < 2) return '';
      try {
        const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
        const json = JSON.parse(decodeURIComponent(escape(window.atob(base64))));
        return json?.username || json?.userAccount || '';
      } catch {
        return '';
      }
    }

    async function handleSubmit() {
      const payload = {
        doctorId: profile.value.doctorId,
        doctorName: profile.value.doctorName,
        deptId: profile.value.deptId,
        title: profile.value.title,
        specialty: profile.value.specialty,
        doctorDesc: profile.value.doctorDesc,
        avatar: profile.value.avatar,
        isActive: profile.value.isActive,
        userAccount: profile.value.userAccount,
        email: profile.value.email,
      };
      const ok = await updateDoctorProfile(payload);
      if (ok) {
        message.success('保存成功');
        fetchProfile();
      } else {
        message.error('保存失败');
      }
    }

    onMounted(fetchProfile);

    return {
      profile,
      activeChecked,
      verifyColor,
      verifyText,
      handleSubmit,
    };
  },
});
</script>