<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './schedule.data';
  import { createSchedule, updateSchedule, getAvailableRoom } from '/@/api/hospital/schedule';
  import { useMessage } from '/@/hooks/web/useMessage';
  import dayjs from 'dayjs';

  export default defineComponent({
    name: 'ScheduleModal',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const isUpdate = ref(true);
      const rowId = ref('');

      const [registerForm, { setFieldsValue, updateSchema, resetFields, validate }] = useForm({
        labelWidth: 100,
        baseColProps: { span: 24 },
        schemas: formSchema,
        showActionButtonGroup: false,
        actionColOptions: {
          span: 23,
        },
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;

        if (unref(isUpdate)) {
          rowId.value = data.record.scheduleId;
          setFieldsValue({
            ...data.record,
          });
        }
      });

      const getTitle = computed(() => (!unref(isUpdate) ? '新增排班' : '编辑排班'));

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });
          
          // 格式化日期
          const dateStr = values.scheduleDate ? (values.scheduleDate.format ? values.scheduleDate.format('YYYY-MM-DD') : values.scheduleDate) : '';
          
          // 转换时段为shift字符串
          const shiftMap: { [key: number]: string } = { 1: '上午', 2: '下午', 3: '晚上' };
          const shiftStr = shiftMap[values.timeSlot] || '上午';
          
          if (unref(isUpdate)) {
            // 更新排班
            await updateSchedule({
              scheduleId: rowId.value,
              doctorId: values.doctorId,
              deptId: values.deptId,
              date: dateStr,
              shift: shiftStr,
              slots: values.maxQuota,
              maxQuota: values.maxQuota,
              roomNumber: values.roomNumber || undefined,
              status: values.status,
            });
          } else {
            // 创建排班 - 如果没有指定诊室，则随机分配
            let roomNumber = values.roomNumber;
            if (!roomNumber || roomNumber.trim() === '') {
              try {
                // 调用后端接口获取可用诊室
                const timeSlot = values.timeSlot || 1;
                roomNumber = await getAvailableRoom({
                  date: dateStr,
                  timeSlot: timeSlot,
                });
              } catch (error) {
                console.error('获取可用诊室失败，使用默认值:', error);
                // 如果后端接口不存在，使用简单的随机分配
                const rooms = ['A-101', 'A-102', 'A-103', 'A-104', 'A-105', 'B-201', 'B-202'];
                roomNumber = rooms[Math.floor(Math.random() * rooms.length)];
              }
            }
            
            await createSchedule({
              doctorId: values.doctorId,
              deptId: values.deptId,
              date: dateStr,
              shift: shiftStr,
              slots: values.maxQuota,
              maxQuota: values.maxQuota,
              roomNumber: roomNumber,
            });
          }
          
          closeModal();
          emit('success', { isUpdate: unref(isUpdate), values: { ...values, scheduleId: rowId.value } });
          createMessage.success('操作成功');
        } catch (error: any) {
          createMessage.error('操作失败：' + (error?.message || '未知错误'));
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return { registerModal, registerForm, getTitle, handleSubmit };
    },
  });
</script>