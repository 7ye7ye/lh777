<template>
  <PageWrapper :title="'欢迎来到校医院管理系统'">
    <div class="department-manage">
      <!-- 操作栏 -->
      <el-row :gutter="20" class="operate-bar">
        <el-col :span="8">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索科室名称或描述" 
            clearable
            @clear="handleSearch"
            @input="handleSearch"
          ></el-input>
        </el-col>
        <el-col :span="2" :offset="14">
          <el-button type="primary" @click="showAddDialog">新增科室</el-button>
        </el-col>
      </el-row>
  
      <!-- 科室表格 -->
      <el-table 
        :data="departmentList" 
        border 
        style="width: 100%; margin-top: 20px;"
      >
        <el-table-column prop="deptId" label="ID" width="80"></el-table-column>
        <el-table-column prop="deptName" label="科室名称" width="180"></el-table-column>
        <el-table-column 
          prop="deptLevel" 
          label="科室级别" 
          width="120"
          :formatter="formatDeptLevel"
        ></el-table-column>
        <el-table-column 
          prop="parentDeptId" 
          label="上级科室" 
          width="180"
          :formatter="formatParentDept"
        ></el-table-column>
        <el-table-column prop="location" label="位置" width="200"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="200"></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button 
              type="text" 
              @click="showEditDialog(scope.row)"
            >编辑</el-button>
            <el-button 
              type="text" 
              danger 
              @click="handleDelete(scope.row.deptId)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
  
      <!-- 分页 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; text-align: right;"
      ></el-pagination>
  
      <!-- 新增/编辑弹窗 -->
      <el-dialog 
        :title="dialogTitle" 
        :visible.sync="dialogVisible" 
        width="500px"
      >
        <el-form 
          :model="form" 
          ref="form" 
          :rules="rules" 
          label-width="120px"
        >
          <el-form-item label="科室名称" prop="deptName">
            <el-input v-model="form.deptName"></el-input>
          </el-form-item>
          <el-form-item label="科室级别" prop="deptLevel">
            <el-radio-group v-model="form.deptLevel">
              <el-radio label="1">一级科室</el-radio>
              <el-radio label="2">二级科室</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item 
            label="上级科室" 
            prop="parentDeptId"
            v-if="form.deptLevel === 2"
          >
            <el-select v-model="form.parentDeptId" placeholder="请选择上级科室">
              <el-option 
                v-for="dept in firstLevelDepartments" 
                :key="dept.deptId"
                :label="dept.deptName"
                :value="dept.deptId"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="科室简介" prop="deptDesc">
            <el-input 
              v-model="form.deptDesc" 
              type="textarea" 
              rows="3"
            ></el-input>
          </el-form-item>
          <el-form-item label="科室位置" prop="location">
            <el-input v-model="form.location"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </PageWrapper>
</template>

<script>
import { 
  getAllDepartments, 
  createDepartment, 
  updateDepartment, 
  deleteDepartment,
  getFirstLevelDepartments
} from '/@/api/hospital/department'

import { PageWrapper } from '/@/components/Page';
export default {
  data() {
    return {
      // 搜索与分页
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,

      // 科室数据
      departmentList: [],
      firstLevelDepartments: [],

      // 弹窗相关
      dialogVisible: false,
      dialogTitle: '新增科室',
      form: {
        deptId: null,
        deptName: '',
        deptLevel: 1,
        parentDeptId: null,
        deptDesc: '',
        location: ''
      },
      rules: {
        deptName: [
          { required: true, message: '请输入科室名称', trigger: 'blur' }
        ],
        deptLevel: [
          { required: true, message: '请选择科室级别', trigger: 'change' }
        ],
        parentDeptId: [
          { required: true, message: '请选择上级科室', trigger: 'change' }
        ]
      }
    }
  },
  mounted() {
    this.loadDepartments()
    this.loadFirstLevelDepartments()
  },
  methods: {
    // 加载科室列表
    async loadDepartments() {
      try {
        const res = await getAllDepartments()
        if (Array.isArray(res)) {
          this.departmentList = res
          this.total = res.length
        } else if (res?.success) {
          this.departmentList = res.result || []
          this.total = (res.result || []).length
        } else {
          this.departmentList = []
          this.total = 0
        }
      } catch (error) {
        this.$message.error('加载科室列表失败')
        console.error('加载科室列表失败:', error)
      }
    },
    async loadFirstLevelDepartments() {
      try {
        const res = await getFirstLevelDepartments()
        if (Array.isArray(res)) {
          this.firstLevelDepartments = res
        } else if (res?.success) {
          this.firstLevelDepartments = res.result || []
        } else {
          this.firstLevelDepartments = []
        }
      } catch (error) {
        console.error('加载一级科室失败:', error)
      }
    },

    // 搜索科室
    handleSearch() {
      // 实际项目中应调用后端接口，这里简化处理
      if (!this.searchKeyword) {
        this.loadDepartments()
        return
      }
      this.departmentList = this.departmentList.filter(dept => 
        dept.deptName.includes(this.searchKeyword) || 
        (dept.deptDesc && dept.deptDesc.includes(this.searchKeyword))
      )
      this.total = this.departmentList.length
    },

    // 分页处理
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadDepartments()
    },
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadDepartments()
    },

    // 格式化科室级别
    formatDeptLevel(row) {
      return row.deptLevel === 1 ? '一级科室' : '二级科室'
    },

    // 格式化上级科室
    formatParentDept(row) {
      if (row.deptLevel === 1) return '无'
      const parent = this.firstLevelDepartments.find(d => d.deptId === row.parentDeptId)
      return parent ? parent.deptName : '未知'
    },

    // 显示新增弹窗
    showAddDialog() {
      this.dialogTitle = '新增科室'
      this.form = {
        deptId: null,
        deptName: '',
        deptLevel: 1,
        parentDeptId: null,
        deptDesc: '',
        location: ''
      }
      this.dialogVisible = true
    },

    // 显示编辑弹窗
    showEditDialog(row) {
      this.dialogTitle = '编辑科室'
      this.form = { ...row }
      this.dialogVisible = true
    },

    // 提交表单
    async submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            let res
            if (this.form.deptId) {
              res = await updateDepartment(this.form)
            } else {
              res = await createDepartment(this.form)
            }
            if (res === true || res?.success) {
              this.$message.success(this.form.deptId ? '更新成功' : '创建成功')
              this.dialogVisible = false
              this.loadDepartments()
            } else {
              this.$message.error(res?.message || '操作失败')
            }
          } catch (error) {
            this.$message.error('操作失败')
            console.error('提交表单失败:', error)
          }
        }
      })
    },

    // 删除科室
    async handleDelete(deptId) {
      this.$confirm('确定要删除该科室吗？删除后不可恢复', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteDepartment(deptId)
          if (res === true || res?.success) {
            this.$message.success('删除成功')
            this.loadDepartments()
          } else {
            this.$message.error(res?.message || '删除失败')
          }
        } catch (error) {
          this.$message.error('删除失败')
          console.error('删除科室失败:', error)
        }
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    }
  }
}
</script>

<style scoped>
.operate-bar {
  margin-top: 20px;
}
</style>

