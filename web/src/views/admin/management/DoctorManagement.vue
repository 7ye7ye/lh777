<template>
  <div class="doctor-management">
    <div class="page-header">
      <h2>医生管理</h2>
    </div>

    <a-card class="content-card">
      <!-- 搜索和操作区域 -->
      <div class="search-operate-area">
        <div class="search-form">
          <a-row :gutter="[16, 16]">
            <a-col :span="6">
              <a-input
                v-model:value="searchForm.doctorName"
                placeholder="医生姓名"
                prefix-icon="user"
              />
            </a-col>
            <a-col :span="6">
              <a-select
                v-model:value="searchForm.deptId"
                placeholder="选择科室"
                allow-clear
              >
                <a-select-option
                  v-for="dept in departmentOptions"
                  :key="dept.deptId"
                  :value="dept.deptId"
                >
                  {{ dept.deptName }}
                </a-select-option>
              </a-select>
            </a-col>
            <a-col :span="6">
              <a-select
                v-model:value="searchForm.title"
                placeholder="医生职称"
                allow-clear
              >
                <a-select-option value="主任医师">主任医师</a-select-option>
                <a-select-option value="副主任医师">副主任医师</a-select-option>
                <a-select-option value="主治医师">主治医师</a-select-option>
                <a-select-option value="住院医师">住院医师</a-select-option>
                <a-select-option value="主任护师">主任护师</a-select-option>
                <a-select-option value="副主任护师">副主任护师</a-select-option>
                <a-select-option value="主管护师">主管护师</a-select-option>
                <a-select-option value="报销专员">报销专员</a-select-option>
              </a-select>
            </a-col>
            <a-col :span="6">
              <a-select
                v-model:value="searchForm.isActive"
                placeholder="出诊状态"
                allow-clear
              >
                <a-select-option value="1">正常出诊</a-select-option>
                <a-select-option value="0">暂停出诊</a-select-option>
              </a-select>
            </a-col>
          </a-row>
          <a-row :gutter="[16, 16]" style="margin-top: 16px;">
            <a-col :span="6">
              <a-button type="primary" @click="handleSearch">
                <template #icon>
                  <SearchOutlined />
                </template>
                搜索
              </a-button>
              <a-button style="margin-left: 8px" @click="resetSearch">
                <template #icon>
                  <ReloadOutlined />
                </template>
                重置
              </a-button>
            </a-col>
          </a-row>
        </div>
        
        <div class="operate-buttons">
          <a-button type="primary" @click="showAddModal" disabled>
              <template #icon>
                <PlusOutlined />
              </template>
              添加医生
            </a-button>
        </div>
      </div>

      <!-- 医生列表表格 -->
      <div class="table-container">
        <a-table
          :columns="columns"
          :data-source="doctorList"
          :loading="loading"
          :pagination="pagination"
          row-key="doctorId"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'avatar'">
              <a-avatar :src="record.avatar" :alt="record.doctorName" size="small" />
            </template>
            <template v-else-if="column.key === 'action'">
              <div class="action-buttons">
                <a-button type="link" size="small" @click="handleEdit(record)">
                  编辑
                </a-button>
                <a-button type="link" size="small" danger @click="handleDelete(record)">
                  删除
                </a-button>
              </div>
            </template>
          </template>
        </a-table>
      </div>
    </a-card>

    <!-- 添加/编辑医生模态框 -->
    <a-modal
      v-model:visible="doctorModalVisible"
      :title="modalTitle"
      @ok="handleModalSubmit"
      @cancel="handleModalCancel"
      cancelText="取消"
      okText="确定"
    >
      <a-form
        :model="doctorForm"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
        ref="formRef"
      >
        <a-form-item label="医生姓名" name="doctorName">
          <a-input v-model:value="doctorForm.doctorName" placeholder="请输入医生姓名" />
        </a-form-item>
        <a-form-item label="所属科室" name="deptId">
          <a-select v-model:value="doctorForm.deptId" placeholder="请选择科室">
            <a-select-option
              v-for="dept in departmentOptions"
              :key="dept.deptId"
              :value="dept.deptId"
            >
              {{ dept.deptName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="医生职称" name="title">
          <a-select v-model:value="doctorForm.title" placeholder="请选择职称">
            <a-select-option value="主任医师">主任医师</a-select-option>
            <a-select-option value="副主任医师">副主任医师</a-select-option>
            <a-select-option value="主治医师">主治医师</a-select-option>
            <a-select-option value="住院医师">住院医师</a-select-option>
            <a-select-option value="主任护师">主任护师</a-select-option>
            <a-select-option value="副主任护师">副主任护师</a-select-option>
            <a-select-option value="主管护师">主管护师</a-select-option>
            <a-select-option value="报销专员">报销专员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="擅长领域" name="specialty">
          <a-input v-model:value="doctorForm.specialty" placeholder="请输入擅长领域" />
        </a-form-item>
        <a-form-item label="医生简介" name="doctorDesc">
            <a-textarea
              v-model:value="doctorForm.doctorDesc"
              placeholder="请输入医生简介"
              :rows="4"
            />
          </a-form-item>
        <a-form-item label="医生头像">
          <a-upload
            list-type="picture-card"
            :file-list="fileList"
            :before-upload="beforeUpload"
            :custom-request="customRequest"
            @change="handleUploadChange"
          >
            <div v-if="fileList.length < 1">
              <PlusOutlined />
              <div style="margin-top: 8px">上传头像</div>
            </div>
          </a-upload>
        </a-form-item>
        <a-form-item label="出诊状态" name="isActive">
          <a-select v-model:value="doctorForm.isActive" placeholder="请选择出诊状态">
            <a-select-option value="1">正常出诊</a-select-option>
            <a-select-option value="0">暂停出诊</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { message, Upload, Modal } from 'ant-design-vue';
import type { PaginationProps } from 'ant-design-vue';
import type { ColumnsType } from 'ant-design-vue/es/table';
import { PlusOutlined, SearchOutlined, ReloadOutlined, DownOutlined, DeleteOutlined, UserOutlined } from '@ant-design/icons-vue';
import { getDepartmentList } from '/@/api/hospital/department';
import type { Department } from '/@/api/hospital/department';
import { getDoctorList, createDoctor, updateDoctor, deleteDoctor, batchDeleteDoctors, getDoctorDetail, registerDoctorAccount } from '/@/api/hospital/doctor';
import type { Doctor, RegisterDoctorParams } from '/@/api/hospital/doctor';

// 表格数据
const doctorList = ref<Doctor[]>([]);
const loading = ref(false);

// 分页配置
const pagination = reactive<PaginationProps>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条数据`,
});

// 搜索表单
const searchForm = reactive({
  doctorName: '',
  deptId: undefined,
  title: undefined,
  isActive: undefined,
});

// 科室选项
const departmentOptions = ref<Department[]>([]);

// 模态框相关
const doctorModalVisible = ref(false);
const isEditMode = ref(false);
const currentDoctorId = ref<number | null>(null);
const formRef = ref<any>();

// 医生表单
const doctorForm = reactive({
  doctorName: '',
  deptId: undefined,
  title: '',
  specialty: '',
  doctorDesc: '',
  avatar: '',
  isActive: 1, // 默认正常出诊
});

// 文件上传相关
const fileList = ref<any[]>([]);

// 表单验证规则
const formRules = {
  doctorName: [
    { required: true, message: '请输入医生姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度在 2-10 个字符', trigger: 'blur' },
  ],
  deptId: [{ required: true, message: '请选择所属科室', trigger: 'change' }],
  title: [{ required: true, message: '请选择医生职称', trigger: 'change' }],
  specialty: [{ required: true, message: '请输入擅长领域', trigger: 'blur' }],
  doctorDesc: [{ required: false, message: '请输入医生简介', trigger: 'blur' }],
  isActive: [{ required: true, message: '请选择出诊状态', trigger: 'change' }],
};

// 模态框标题
const modalTitle = computed(() => (isEditMode.value ? '编辑医生' : '添加医生'));

// 获取出诊状态文本
function getActiveStatusText(status: number): string {
  return status === 1 ? '正常出诊' : '暂停出诊';
}

// 表格列配置
const columns: ColumnsType<Doctor> = [
  {
    title: '医生ID',
    key: 'doctorId',
    dataIndex: 'doctorId',
    width: 80,
    sorter: (a, b) => a.doctorId - b.doctorId,
  },
  {
    title: '头像',
    key: 'avatar',
    dataIndex: 'avatar',
    width: 60,
  },
  {
    title: '医生姓名',
    key: 'doctorName',
    dataIndex: 'doctorName',
    sorter: (a, b) => a.doctorName.localeCompare(b.doctorName),
  },
  {
    title: '所属科室',
    key: 'deptId',
    dataIndex: 'deptId',
    customRender: ({ text }) => {
      // 确保text是数字类型进行比较
      const dept = departmentOptions.value.find(d => d.deptId === Number(text));
      return dept?.deptName || '未分配科室';
    },
  },
  {
    title: '职称',
    key: 'title',
    dataIndex: 'title',
  },
  {
    title: '擅长领域',
    key: 'specialty',
    dataIndex: 'specialty',
    ellipsis: true,
  },
  {
    title: '出诊状态',
    key: 'isActive',
    dataIndex: 'isActive',
    customRender: ({ text }) => getActiveStatusText(text),
    filters: [
      { text: '正常出诊', value: 1 },
      { text: '暂停出诊', value: 0 },
    ],
    onFilter: (value, record) => record.isActive === value,
  },
  {
    title: '医生简介',
    key: 'doctorDesc',
    dataIndex: 'doctorDesc',
    ellipsis: true,
    width: 200,
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
  },
];

// 获取科室列表
async function fetchDepartments() {
  try {
    const response = await getDepartmentList();
    let departments: Department[] = [];
    
    // 处理不同格式的响应
    if (Array.isArray(response)) {
      departments = response;
    } else if (response?.records && Array.isArray(response.records)) {
      departments = response.records;
    }
    
    // 对科室数据进行类型转换和验证
    departmentOptions.value = departments.map(dept => ({
      ...dept,
      deptId: Number(dept.deptId),
      deptName: dept.deptName || '未知科室'
    }));
  } catch (error) {
    console.error('获取科室列表失败:', error);
    message.error('获取科室列表失败');
    departmentOptions.value = [];
  }
}

// 重置搜索表单
function resetSearch() {
  Object.assign(searchForm, {
    doctorName: '',
    deptId: undefined,
    title: undefined,
    isActive: undefined,
  });
}

// 获取医生列表
async function fetchDoctorList() {
  loading.value = true;
  try {
    // 构建查询参数，确保使用正确的字段名
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      // 支持所有四个搜索条件
      doctorName: searchForm.doctorName || undefined,
      deptId: searchForm.deptId || undefined,
      title: searchForm.title || undefined,
      isActive: searchForm.isActive !== undefined ? Number(searchForm.isActive) : undefined,
    };
    const response = await getDoctorList(params);
    
    // 安全地处理API响应，确保数据结构正确
    if (response?.records && Array.isArray(response.records)) {
      // 对每个医生数据进行类型转换，确保字段类型正确
      let records = response.records.map(doctor => ({
        ...doctor,
        doctorId: Number(doctor.doctorId),
        deptId: Number(doctor.deptId),
        isActive: Number(doctor.isActive),
        // 确保其他字段有合理的默认值
        doctorName: doctor.doctorName || '',
        title: doctor.title || '',
        specialty: doctor.specialty || '',
        doctorDesc: doctor.doctorDesc || '',
        avatar: doctor.avatar || ''
      }));
      
      // 在客户端进行多条件筛选，确保符合所有条件
      if (searchForm.doctorName || searchForm.deptId || searchForm.title || searchForm.isActive !== undefined) {
        records = records.filter(doctor => {
          // 医生姓名筛选
          if (searchForm.doctorName && !doctor.doctorName.toLowerCase().includes(searchForm.doctorName.toLowerCase())) {
            return false;
          }
          // 科室级联筛选：选择一级科室时显示其下所有二级科室的医生
          if (searchForm.deptId) {
            const selectedDept = departmentOptions.value.find(dept => dept.deptId === Number(searchForm.deptId));
            if (selectedDept) {
              // 如果选择的是一级科室(deptLevel=1)，则检查医生所在科室是否为该一级科室或其下的二级科室
              if (selectedDept.deptLevel === 1) {
                const doctorDept = departmentOptions.value.find(dept => dept.deptId === doctor.deptId);
                // 医生所在科室是一级科室本身或其下二级科室
                if (!doctorDept || 
                    (doctorDept.deptLevel === 1 && doctorDept.deptId !== selectedDept.deptId) || 
                    (doctorDept.deptLevel === 2 && doctorDept.parentDeptId !== selectedDept.deptId)) {
                  return false;
                }
              } else {
                // 如果选择的是二级科室，只检查精确匹配
                if (String(doctor.deptId) !== String(searchForm.deptId)) {
                  return false;
                }
              }
            } else {
              // 未找到对应的科室信息，进行精确匹配
              if (String(doctor.deptId) !== String(searchForm.deptId)) {
                return false;
              }
            }
          }
          // 职称筛选
          if (searchForm.title && doctor.title !== searchForm.title) {
            return false;
          }
          // 出诊状态筛选
          if (searchForm.isActive !== undefined && String(doctor.isActive) !== String(searchForm.isActive)) {
            return false;
          }
          // 所有条件都匹配
          return true;
        });
      }
      
      doctorList.value = records;
      pagination.total = records.length;
    } else {
      console.warn('API响应格式不符合预期:', response);
      doctorList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('获取医生列表失败:', error);
    message.error('获取医生列表失败');
    doctorList.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
}

// 搜索医生
function handleSearch() {
  pagination.current = 1;
  fetchDoctorList();
}

// 表格变化处理
function handleTableChange(newPagination: any) {
  // 正确更新分页信息
  Object.assign(pagination, newPagination);
  // 直接调用fetchDoctorList获取最新数据
  fetchDoctorList();
}

// 显示添加医生模态框
function showAddModal() {
  isEditMode.value = false;
  currentDoctorId.value = null;
  resetDoctorForm();
  doctorModalVisible.value = true;
}

// 编辑医生
async function handleEdit(record: Doctor) {
  isEditMode.value = true;
  currentDoctorId.value = record.doctorId;
  
  try {
    // 获取医生详情
    const doctorDetail = await getDoctorDetail(record.doctorId);
    
    // 检查返回数据是否存在且格式正确
    if (!doctorDetail || typeof doctorDetail !== 'object') {
      console.error('获取的医生详情数据格式不正确:', doctorDetail);
      message.error('获取医生详情失败：数据格式错误');
      return;
    }
    
    // 填充表单数据
    Object.assign(doctorForm, {
      doctorName: doctorDetail.doctorName || '',
      deptId: doctorDetail.deptId || null,
      title: doctorDetail.title || '',
      specialty: doctorDetail.specialty || '',
      doctorDesc: doctorDetail.doctorDesc || '',
      avatar: doctorDetail.avatar || '',
      isActive: doctorDetail.isActive !== undefined ? doctorDetail.isActive : 1,
    });
    
    // 设置文件列表
    if (doctorDetail.avatar) {
      fileList.value = [{
        uid: '-1',
        name: 'avatar.jpg',
        status: 'done',
        url: doctorDetail.avatar,
      }];
    } else {
      fileList.value = [];
    }
    
    doctorModalVisible.value = true;
  } catch (error) {
    console.error('获取医生详情失败:', error);
    message.error('获取医生详情失败');
  }
}

// 删除医生
function handleDelete(record: Doctor) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除医生「${record.doctorName}」吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteDoctor(record.doctorId);
        message.success('删除成功');
        fetchDoctorList();
      } catch (error) {
        console.error('删除医生失败:', error);
        message.error('删除失败');
      }
    },
  });
}

// 批量删除
function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的医生');
    return;
  }
  
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 位医生吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        // 将字符串ID转换为数字
        const doctorIds = selectedRowKeys.value.map(id => parseInt(id));
        await batchDeleteDoctors(doctorIds);
        message.success('批量删除成功');
        fetchDoctorList();
        selectedRowKeys.value = [];
      } catch (error) {
        console.error('批量删除医生失败:', error);
        message.error('批量删除失败');
      }
    },
  });
}

// 重置医生表单
function resetDoctorForm() {
  Object.assign(doctorForm, {
    doctorName: '',
    deptId: undefined,
    title: '',
    specialty: '',
    doctorDesc: '',
    avatar: '',
    isActive: 1,
  });
  fileList.value = [];
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
    
    if (isEditMode.value && currentDoctorId.value) {
      // 编辑操作
      await updateDoctor(doctorData);
      message.success('编辑成功');
    } else {
      // 添加操作 - 使用registerDoctorAccount来创建医生和用户账号
      // 生成临时账号和密码
      const userAccount = `doctor_${Date.now()}_${Math.floor(Math.random() * 1000)}`;
      const password = Math.random().toString(36).substring(2, 10);
      
      // 简化参数传递，只传递必要字段
      const registerParams: RegisterDoctorParams = {
        doctorName: doctorForm.doctorName,
        userAccount: userAccount,
        userPassword: password,
        deptId: Number(doctorForm.deptId),
        title: doctorForm.title,
        specialty: doctorForm.specialty,
        isActive: true // 设置默认值
      };
      
      await registerDoctorAccount(registerParams);
      message.success('添加成功');
      message.info(`医生账号已创建，账号: ${userAccount}, 初始密码: ${password}`);
    }
    
    doctorModalVisible.value = false;
    fetchDoctorList();
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

// 文件上传相关处理
function beforeUpload(file: File) {
  // 支持SVG格式图像
  const isSvg = file.type === 'image/svg+xml' || file.name.toLowerCase().endsWith('.svg');
  if (!isSvg) {
    message.error('只能上传 SVG 格式的图像!');
    return Upload.LIST_IGNORE;
  }
  // SVG文件通常很小，可以适当放宽大小限制
  const isLt500K = file.size / 1024 < 500; // 500KB限制
  if (!isLt500K) {
    message.error('SVG图像大小不能超过 500KB!');
    return Upload.LIST_IGNORE;
  }
  return false; // 阻止默认上传，使用自定义上传
}

function customRequest({ onSuccess, file }: any) {
  // 模拟上传成功
  setTimeout(() => {
    // 使用文件名生成模拟的图片URL
    const fileName = doctorForm.doctorName || '医生';
    const mockUrl = `https://via.placeholder.com/150?text=${fileName}`;
    onSuccess({ url: mockUrl });
  }, 500);
}

function handleUploadChange({ file }: any) {
  if (file.status === 'done') {
    // 使用模拟的上传URL
    doctorForm.avatar = file.response?.url || `https://via.placeholder.com/150?text=${doctorForm.doctorName || '医生'}`;
    message.success('上传成功');
  } else if (file.status === 'error') {
    message.error('上传失败');
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchDepartments();
  fetchDoctorList();
});
</script>

<style scoped>
.doctor-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
}

.content-card {
  margin-bottom: 24px;
}

.search-operate-area {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 16px;
}

.operate-buttons {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

/* 为了确保删除按钮显示为红色 */
:deep(.ant-btn-danger) {
  color: #ff4d4f;
}

:deep(.ant-btn-danger:hover) {
  color: #ff7875;
}
</style>