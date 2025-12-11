<template>
  <PageWrapper :title="'月度排班日历'">
    <div class="p-4">
      <a-card :bordered="false">
        <!-- 筛选栏 -->
        <a-card class="filter-bar" size="small" bordered>
          <a-row :gutter="16" wrap>
            <a-col :xs="24" :sm="24" :md="24" :lg="24">
              <a-radio-group v-model:value="viewMode">
                <a-radio-button value="doctor">按医生查看</a-radio-button>
                <a-radio-button value="dept">按科室查看</a-radio-button>
              </a-radio-group>
            </a-col>

            <a-col v-if="viewMode === 'doctor'" :xs="24" :sm="12" :md="8" :lg="6">
              <a-select
                v-model:value="doctorId"
                :options="doctorOptions"
                allowClear
                placeholder="选择医生"
              />
            </a-col>
            <a-col v-else :xs="24" :sm="12" :md="8" :lg="6">
              <a-select
                v-model:value="deptId"
                :options="deptOptions"
                allowClear
                placeholder="选择科室"
              />
            </a-col>

            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-date-picker
                v-model:value="calendarValue"
                picker="month"
                style="width: 100%"
                placeholder="选择月份"
              />
            </a-col>

            <a-col :xs="24" :sm="24" :md="24" :lg="24">
              <div class="filter-actions">
                <a-space>
                  <a-button type="primary" @click="reload">查询</a-button>
                  <a-button @click="resetFilters">重置</a-button>
                </a-space>
              </div>
            </a-col>
          </a-row>
        </a-card>

        <!-- 日历内容 -->
        <a-alert type="info" show-icon class="mb-2" :message="legendText" />
        <a-calendar v-model:value="calendarValue">
          <template #dateCellRender="{ current }">
            <div class="cell">
              <div class="cell-content">
                <template v-for="slot in getSlots(current)" :key="slot">
                  <a-tag :color="slotColor(slot)">{{ slotLabel(slot) }}</a-tag>
                </template>
              </div>
            </div>
          </template>
        </a-calendar>
      </a-card>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, watch } from 'vue';
import { PageWrapper } from '/@/components/Page';
import dayjs, { Dayjs } from 'dayjs';
import { useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import { getDepartmentList } from '/@/api/hospital/department';
import { getDoctorList } from '/@/api/hospital/doctor';
import { listMonthlyScheduleByDoctor, listMonthlyScheduleByDept, type MonthScheduleMap } from '/@/api/hospital/scheduleView';

export default defineComponent({
  name: 'AdminScheduleCalendar',
  components: { PageWrapper },
  setup() {
    const route = useRoute();
    const viewMode = ref<'doctor' | 'dept'>((route.query.type as any) || 'doctor');
    const doctorId = ref<number | undefined>(route.query.doctorId ? Number(route.query.doctorId) : undefined);
    const deptId = ref<number | undefined>(route.query.deptId ? Number(route.query.deptId) : undefined);
    const doctorOptions = ref<{ label: string; value: number }[]>([]);
    const deptOptions = ref<{ label: string; value: number }[]>([]);
    const calendarValue = ref<Dayjs>(dayjs());
    const monthMap = ref<MonthScheduleMap>({});

    function slotLabel(v: number) {
      const map: any = { 1: '上午', 2: '下午', 3: '晚上' };
      return map[v] || v;
    }

    function slotColor(v: number) {
      const map: any = { 1: 'green', 2: 'blue', 3: 'purple' };
      return map[v] || 'default';
    }

    function keyOfDate(d: Dayjs) {
      return d.format('YYYY-MM-DD');
    }

    function getSlots(current: Dayjs) {
      const key = keyOfDate(current);
      const items = monthMap.value[key] || [];
      return items.map((it) => it.timeSlot);
    }

    const legendText = ref('标签含义：绿色=上午，蓝色=下午，紫色=晚上');

    async function loadDeptOptions() {
      try {
        const list = await getDepartmentList();
        deptOptions.value = (list || []).map((d: any) => ({ label: d.deptName, value: d.deptId }));
      } catch (error) {
        console.error('加载科室列表失败:', error);
        deptOptions.value = [];
      }
    }

    async function loadDoctorOptions() {
      try {
        const response = await getDoctorList();
        // 处理API返回的数据结构：可能是 { records: [...], total: ... } 或直接是数组
        let list: any[] = [];
        if (response) {
          if (Array.isArray(response)) {
            list = response;
          } else if (response.records && Array.isArray(response.records)) {
            list = response.records;
          } else if (response.list && Array.isArray(response.list)) {
            list = response.list;
          } else if (response.data && Array.isArray(response.data)) {
            list = response.data;
          }
        }
        doctorOptions.value = list.map((d: any) => ({ 
          label: d.doctorName || d.name || '', 
          value: d.doctorId || d.id || 0 
        }));
      } catch (error: any) {
        console.error('加载医生列表失败:', error);
        message.error('加载医生列表失败：' + (error?.message || '未知错误'));
        doctorOptions.value = [];
      }
    }

    // 已移除fillMock函数，不再使用模拟数据，只显示真实的数据库数据

    async function reload() {
      const year = calendarValue.value.year();
      const month = calendarValue.value.month() + 1;
      try {
        if (viewMode.value === 'doctor') {
          if (!doctorId.value) {
            message.warning('请选择医生');
            return;
          }
          const data = await listMonthlyScheduleByDoctor({ doctorId: doctorId.value, year, month });
          monthMap.value = data || {};
          console.log('按医生查询排班数据:', { doctorId: doctorId.value, year, month, data, monthMap: monthMap.value });
        } else {
          if (!deptId.value) {
            message.warning('请选择科室');
            return;
          }
          const data = await listMonthlyScheduleByDept({ deptId: deptId.value, year, month });
          monthMap.value = data || {};
          console.log('按科室查询排班数据:', { deptId: deptId.value, year, month, data, monthMap: monthMap.value });
        }
        // 移除fillMock()调用，显示真实的数据库数据（即使为空）
        if (!monthMap.value || Object.keys(monthMap.value).length === 0) {
          message.info('该时间段暂无排班数据');
        }
      } catch (e) {
        console.error('加载排班数据失败:', e);
        message.error('加载排班数据失败: ' + (e as any)?.message || '未知错误');
        monthMap.value = {}; // 出错时清空数据，不显示模拟数据
      }
    }

    function resetFilters() {
      doctorId.value = undefined;
      deptId.value = undefined;
      calendarValue.value = dayjs();
      monthMap.value = {};
    }

    onMounted(async () => {
      await loadDeptOptions();
      await loadDoctorOptions();
      await reload();
    });

    watch(viewMode, () => {
      // 切换查看模式时重置对应的筛选条件
      if (viewMode.value === 'doctor') {
        deptId.value = undefined;
      } else {
        doctorId.value = undefined;
      }
    });

    return {
      viewMode,
      doctorId,
      deptId,
      doctorOptions,
      deptOptions,
      calendarValue,
      legendText,
      getSlots,
      slotLabel,
      slotColor,
      reload,
      resetFilters
    };
  },
});
</script>

<style scoped>
.p-4 {
  padding: 16px;
}
.mb-2 {
  margin-bottom: 8px;
}
.cell {
  min-height: 66px;
}
.cell-content {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.filter-bar {
  margin-bottom: 16px;
}
.filter-actions {
  display: flex;
  justify-content: flex-start;
  margin-top: 8px;
}
</style>

<style>
.filter-bar .ant-select,
.filter-bar .ant-input,
.filter-bar .ant-picker {
  width: 100%;
}
</style>
