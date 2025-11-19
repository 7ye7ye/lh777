<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>排班调整管理系统</title>
  <link rel="stylesheet" href="https://unpkg.com/ant-design-vue@3.2.15/dist/antd.min.css">
  <script src="https://unpkg.com/vue@3.2.36/dist/vue.global.prod.js"></script>
  <script src="https://unpkg.com/ant-design-vue@3.2.15/dist/antd.min.js"></script>
  <style>
    .page-wrapper {
      padding: 20px;
      background-color: #f5f5f5;
      min-height: 100vh;
    }
    .page-content {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      padding: 24px;
    }
    .page-title {
      font-size: 20px;
      font-weight: 600;
      margin-bottom: 20px;
      color: #262626;
      border-bottom: 1px solid #f0f0f0;
      padding-bottom: 12px;
    }
    .toolbar {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 20px;
      flex-wrap: wrap;
    }
    .field {
      display: flex;
      align-items: center;
    }
    .field-label {
      margin-right: 8px;
      color: rgba(0,0,0,0.88);
      white-space: nowrap;
    }
    .section {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
    .ant-table-wrapper {
      margin-top: 16px;
    }
    .ant-card {
      border-radius: 8px;
    }
    .ant-input, .ant-picker {
      border-radius: 6px;
    }
    .ant-btn-primary {
      border-radius: 6px;
    }
    @media (max-width: 768px) {
      .toolbar {
        flex-direction: column;
        align-items: flex-start;
      }
      .field {
        width: 100%;
        margin-bottom: 12px;
      }
      .ant-input, .ant-picker {
        width: 100% !important;
      }
    }
  </style>
</head>
<body>
<div id="app">
  <div class="page-wrapper">
    <div class="page-content">
      <div class="page-title">排班调整</div>

      <!-- 查询条件区域 -->
      <div class="toolbar">
        <div class="field">
          <span class="field-label">医生：</span>
          <a-input
            v-model:value="query.doctor"
            placeholder="输入医生姓名"
            style="width: 200px"
            allow-clear
          />
        </div>
        <div class="field">
          <span class="field-label">日期：</span>
          <a-date-picker
            v-model:value="query.date"
            placeholder="请选择日期"
            style="width: 200px"
          />
        </div>
        <a-button type="primary" @click="handleSearch">查询</a-button>
      </div>

      <!-- 排班数据表格 -->
      <a-table
        :data-source="rows"
        :columns="columns"
        row-key="id"
        bordered
        :pagination="false"
        :scroll="{ x: 800 }"
      />

      <!-- 操作按钮区域 -->
      <div class="section">
        <a-space>
          <a-button type="primary" @click="notify">启动通知</a-button>
        </a-space>
      </div>
    </div>
  </div>
</div>

<script>
  const { createApp } = Vue;
  const {
    Card,
    Input,
    DatePicker,
    Button,
    Table,
    Space,
    message
  } = antd;

  const App = {
    components: {
      'a-card': Card,
      'a-input': Input,
      'a-date-picker': DatePicker,
      'a-button': Button,
      'a-table': Table,
      'a-space': Space
    },
    data() {
      return {
        query: {
          doctor: '',
          date: null
        },
        rows: [
          { id: 1, doctor: '张三', date: '2025-11-05', clinic: '内科', old: '上午', new: '下午' },
          { id: 2, doctor: '李四', date: '2025-11-06', clinic: '外科', old: '下午', new: '上午' },
          { id: 3, doctor: '王五', date: '2025-11-07', clinic: '儿科', old: '全天', new: '休息' },
          { id: 4, doctor: '赵六', date: '2025-11-08', clinic: '妇产科', old: '休息', new: '上午' }
        ],
        columns: [
          {
            title: '医生',
            dataIndex: 'doctor',
            key: 'doctor',
            width: 120
          },
          {
            title: '日期',
            dataIndex: 'date',
            key: 'date',
            width: 120
          },
          {
            title: '科室',
            dataIndex: 'clinic',
            key: 'clinic',
            width: 120
          },
          {
            title: '原排班',
            dataIndex: 'old',
            key: 'old',
            width: 120
          },
          {
            title: '新排班',
            dataIndex: 'new',
            key: 'new',
            width: 120
          }
        ]
      }
    },
    methods: {
      handleSearch() {
        message.info(`查询条件: 医生=${this.query.doctor || '空'}, 日期=${this.query.date ? this.queryDate : '空'}`);
        // 实际应用中这里会调用API获取数据
      },
      notify() {
        message.success('已发送排班调整通知');
      }
    },
    computed: {
      queryDate() {
        return this.query.date ? this.query.date.format('YYYY-MM-DD') : '';
      }
    }
  };

  createApp(App).use(antd).mount('#app');
</script>
</body>
</html>
