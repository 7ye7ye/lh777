<template>
  <div class="department-detail">
    <div class="page-header">
      <h2>科室详情</h2>
      <a-button type="link" @click="goBack">返回科室列表</a-button>
    </div>

    <a-card class="department-info-card" bordered v-if="departmentDetail">
      <template #title>
        <div class="department-title">
          <h3>{{ departmentDetail.deptName }}</h3>
          <span class="dept-level">第{{ departmentDetail.deptLevel }}级科室</span>
        </div>
      </template>
      
      <div class="info-content">
        <div class="info-section">
          <div class="info-item">
            <span class="info-label">科室ID：</span>
            <span class="info-value">{{ departmentDetail.deptId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">上级科室：</span>
            <span class="info-value">{{ parentDepartmentName || '无' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">科室位置：</span>
            <span class="info-value">{{ departmentDetail.location || '暂无信息' }}</span>
          </div>
        </div>
        
        <div class="description-section">
          <h4>科室简介</h4>
          <p>{{ departmentDetail.deptDesc || '暂无简介信息' }}</p>
        </div>
      </div>
    </a-card>

    <a-card class="doctor-list-card" bordered>
      <template #title>
        <div class="doctor-list-title">
          <h3>医生列表</h3>
          <span class="doctor-count">共 {{ doctorList.length }} 位医生</span>
        </div>
      </template>
      
      <div class="doctor-list" v-if="doctorList.length > 0">
        <div class="doctor-card" v-for="doctor in doctorList" :key="doctor.doctorId">
          <div class="doctor-header">
            <div class="doctor-avatar" v-if="doctor.avatar">
              <img :src="doctor.avatar" :alt="doctor.doctorName" />
            </div>
            <div class="doctor-avatar avatar-placeholder" v-else>
              {{ doctor.doctorName.charAt(0) }}
            </div>
            <div class="doctor-info">
              <h4>{{ doctor.doctorName }}</h4>
              <p class="doctor-title">{{ doctor.title }}</p>
            </div>
          </div>
          <div class="doctor-details">
            <p class="doctor-specialty"><span class="label">擅长：</span>{{ doctor.specialty || '暂无信息' }}</p>
            <p class="doctor-desc"><span class="label">简介：</span>{{ doctor.doctorDesc || '暂无简介' }}</p>
          </div>
          <div class="doctor-actions">
            <a-button type="primary" @click="handleEditDoctor(doctor)">编辑</a-button>
          </div>
        </div>
      </div>
      
      <div class="empty-state" v-else>
        <a-empty description="该科室暂无医生信息" />
      </div>
    </a-card>

    <!-- 编辑医生模态框 -->
    <a-modal
      v-model:visible="doctorModalVisible"
      :title="'编辑医生 - ' + currentDoctor?.doctorName"
      ok-text="保存"
      cancel-text="取消"
      @ok="handleModalSubmit"
      @cancel="handleModalCancel"
    >
      <a-form :model="doctorForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" ref="formRef">
        <a-form-item label="医生姓名" name="doctorName" :rules="[{ required: true, message: '请输入医生姓名' }]">
          <a-input v-model:value="doctorForm.doctorName" placeholder="请输入医生姓名" />
        </a-form-item>
        
        <a-form-item label="职称" name="title" :rules="[{ required: true, message: '请输入职称' }]">
          <a-input v-model:value="doctorForm.title" placeholder="请输入职称" />
        </a-form-item>
        
        <a-form-item label="擅长领域" name="specialty">
          <a-input v-model:value="doctorForm.specialty" placeholder="请输入擅长领域" />
        </a-form-item>
        
        <a-form-item label="医生简介" name="doctorDesc">
          <a-textarea v-model:value="doctorForm.doctorDesc" placeholder="请输入医生简介" rows="4" />
        </a-form-item>
        
        <a-form-item label="出诊状态" name="isActive">
          <a-select v-model:value="doctorForm.isActive">
            <a-select-option value="1">正常出诊</a-select-option>
            <a-select-option value="0">暂停出诊</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { getDepartmentDetail } from '/@/api/hospital/department';
import { getDoctorList, getDoctorDetail, updateDoctor } from '/@/api/hospital/doctor';
import type { Department } from '/@/api/hospital/department';
import type { Doctor } from '/@/api/hospital/doctor';

const router = useRouter();
const route = useRoute();

const departmentDetail = ref<Department | null>(null);
const doctorList = ref<Doctor[]>([]);
const parentDepartment = ref<Department | null>(null);
const loading = ref(false);

// 编辑医生相关状态
const doctorModalVisible = ref(false);
const currentDoctorId = ref<number | null>(null);
const currentDoctor = ref<Doctor | null>(null);
const formRef = ref();
const doctorForm = reactive({
  doctorName: '',
  title: '',
  specialty: '',
  doctorDesc: '',
  isActive: '1',
  avatar: '',
  deptId: null
});

// 获取URL参数中的deptId
const deptId = computed(() => {
  const id = route.params.deptId;
  return typeof id === 'string' ? Number(id) : 0;
});

// 获取上级科室名称
const parentDepartmentName = computed(() => {
  return parentDepartment.value?.deptName || '';
});

// 获取科室详情
async function fetchDepartmentDetail() {
  if (!deptId.value) {
    message.error('科室ID无效');
    return;
  }
  
  loading.value = true;
  try {
    const detail = await getDepartmentDetail(deptId.value);
    if (detail && typeof detail === 'object') {
      departmentDetail.value = detail as Department;
      
      // 如果有上级科室，获取上级科室信息
      if (detail.parentDeptId) {
        const parentDetail = await getDepartmentDetail(detail.parentDeptId);
        if (parentDetail && typeof parentDetail === 'object') {
          parentDepartment.value = parentDetail as Department;
        }
      }
    }
  } catch (error) {
    console.error('获取科室详情失败:', error);
    message.error('获取科室详情失败');
  } finally {
    loading.value = false;
  }
}

// 获取科室医生列表（包含子科室医生）
async function fetchDoctorList() {
  if (!deptId.value) return;
  
  try {
    // 方法一：如果后端支持获取包含子科室的医生列表（假设支持allDept参数）
    // 先尝试使用更高级的查询方式
    let response;
    try {
      // 尝试获取当前科室及所有子科室的医生
      response = await getDoctorList({ deptId: deptId.value, includeSubDepartments: true });
    } catch (apiError) {
      console.log('高级查询方式不支持，使用兼容方式获取医生列表');
      // 方法二：如果后端不支持高级查询，则需要获取所有科室下的医生
      response = await getDoctorList({});
      
      // 获取当前科室及其子科室的ID列表
      const deptIds = await fetchDepartmentTree(deptId.value);
      
      // 过滤出指定科室ID列表中的医生
      if (response && response.records && Array.isArray(response.records)) {
        response.records = response.records.filter((doctor: Doctor) => 
          deptIds.includes(Number(doctor.deptId))
        );
      }
    }
    
    if (response && response.records && Array.isArray(response.records)) {
      doctorList.value = response.records;
    }
  } catch (error) {
    console.error('获取医生列表失败:', error);
    message.error('获取医生列表失败');
  }
}

// 获取科室及其所有子科室的ID列表
async function fetchDepartmentTree(parentId: number): Promise<number[]> {
  const deptIds: number[] = [parentId];
  
  try {
    // 获取所有科室列表
    const response = await getDepartmentList();
    let allDepartments: Department[] = [];
    
    // 处理不同格式的响应
    if (Array.isArray(response)) {
      allDepartments = response;
    } else if (response?.records && Array.isArray(response.records)) {
      allDepartments = response.records;
    }
    
    // 递归收集子科室ID
    const findSubDepartments = (id: number) => {
      const subDepartments = allDepartments.filter(dept => 
        dept.parentDeptId === id
      );
      
      subDepartments.forEach(dept => {
        const deptId = Number(dept.deptId);
        deptIds.push(deptId);
        findSubDepartments(deptId);
      });
    };
    
    findSubDepartments(parentId);
  } catch (error) {
    console.error('获取科室树失败:', error);
  }
  
  return deptIds;
}

// 返回科室列表
function goBack() {
  router.push('/admin/management/department');
}

// 处理编辑医生
async function handleEditDoctor(doctor: Doctor) {
  currentDoctorId.value = doctor.doctorId;
  currentDoctor.value = doctor;
  
  try {
    // 获取医生详情
    const doctorDetail = await getDoctorDetail(doctor.doctorId);
    
    if (!doctorDetail || typeof doctorDetail !== 'object') {
      console.error('获取的医生详情数据格式不正确:', doctorDetail);
      message.error('获取医生详情失败：数据格式错误');
      return;
    }
    
    // 填充表单数据
    Object.assign(doctorForm, {
      doctorName: doctorDetail.doctorName || '',
      title: doctorDetail.title || '',
      specialty: doctorDetail.specialty || '',
      doctorDesc: doctorDetail.doctorDesc || '',
      isActive: doctorDetail.isActive !== undefined ? String(doctorDetail.isActive) : '1',
      avatar: doctorDetail.avatar || '',
      deptId: doctorDetail.deptId || null
    });
    
    doctorModalVisible.value = true;
  } catch (error) {
    console.error('获取医生详情失败:', error);
    message.error('获取医生详情失败');
  }
}

// 重置医生表单
function resetDoctorForm() {
  Object.assign(doctorForm, {
    doctorName: '',
    title: '',
    specialty: '',
    doctorDesc: '',
    isActive: '1',
    avatar: '',
    deptId: null
  });
  formRef.value?.resetFields();
}

// 模态框提交
async function handleModalSubmit() {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    const doctorData = {
      doctorId: currentDoctorId.value,
      doctorName: doctorForm.doctorName,
      deptId: Number(doctorForm.deptId),
      title: doctorForm.title,
      specialty: doctorForm.specialty,
      doctorDesc: doctorForm.doctorDesc,
      avatar: doctorForm.avatar,
      isActive: Number(doctorForm.isActive),
    };
    
    // 编辑操作
    await updateDoctor(doctorData);
    message.success('编辑成功');
    
    doctorModalVisible.value = false;
    await fetchDoctorList(); // 重新获取医生列表
    resetDoctorForm();
  } catch (error) {
    console.error('保存医生数据失败:', error);
    message.error('保存失败');
  }
}

// 关闭模态框
function handleModalCancel() {
  doctorModalVisible.value = false;
  resetDoctorForm();
}

// 页面加载时获取数据
onMounted(async () => {
  await fetchDepartmentDetail();
  await fetchDoctorList();
});
</script>

<style scoped>
.department-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
}

.department-info-card,
.doctor-list-card {
  margin-bottom: 24px;
}

.department-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.department-title h3 {
  margin: 0;
  font-size: 20px;
}

.dept-level {
  padding: 4px 12px;
  background-color: #f0f9eb;
  color: #67c23a;
  border-radius: 4px;
  font-size: 14px;
}

.info-content {
  padding-top: 16px;
}

.info-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-weight: 500;
  color: #606266;
}

.info-value {
  color: #303133;
}

.description-section {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.description-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #303133;
}

.description-section p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
}

.doctor-list-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.doctor-list-title h3 {
  margin: 0;
  font-size: 20px;
}

.doctor-count {
  color: #606266;
  font-size: 14px;
}

.doctor-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  padding-top: 16px;
}

.doctor-card {
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.doctor-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-color: #fff;
}

.doctor-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.doctor-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
}

.doctor-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #1890ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 500;
}

.doctor-info h4 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #303133;
}

.doctor-title {
  margin: 0;
  color: #67c23a;
  font-size: 14px;
}

.doctor-details p {
  margin: 8px 0;
  line-height: 1.5;
  color: #606266;
}

.doctor-details .label {
  font-weight: 500;
  color: #303133;
}

.doctor-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  padding: 40px 0;
}
</style>