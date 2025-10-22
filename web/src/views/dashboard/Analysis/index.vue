<template>
  <div class="dashboard-container">
    <a-card title="欢迎使用" :bordered="false">
      <p>这是一个分析页面</p>
      <p>登录成功后，您可以访问所有功能模块。</p>
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="8">
        <a-card title="用户统计" :bordered="false">
          <a-statistic
            title="总用户数"
            :value="112893"
            :precision="0"
            suffix="人"
            :value-style="{ color: '#3f8600' }"
          />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="今日访问" :bordered="false">
          <a-statistic
            title="访问量"
            :value="9280"
            :precision="0"
            suffix="次"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="系统状态" :bordered="false">
          <a-statistic
            title="在线用户"
            :value="328"
            :precision="0"
            suffix="人"
            :value-style="{ color: '#cf1322' }"
          />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import { Card, Row, Col, Statistic } from 'ant-design-vue';

  const ACard = Card;
  const ARow = Row;
  const ACol = Col;
  const AStatistic = Statistic;
</script>

<style scoped>
  .dashboard-container {
    padding: 20px;
  }
</style>
