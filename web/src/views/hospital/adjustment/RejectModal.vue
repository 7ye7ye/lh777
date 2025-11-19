<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="驳回申请" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { approveAdjustment } from '/@/api/hospital/adjustment';
  import { useMessage } from '/@/hooks/web/useMessage';

  const formSchema = [
    {
      field: 'rejectReason',
      label: '驳回原因',
      component: 'InputTextArea',
      required: true,
      componentProps: {
        placeholder: '请输入驳回原因',
        rows: 4,
      },
    },
  ];

  export default defineComponent({
    name: 'RejectModal',
    components: { BasicModal, BasicForm },
    emits: ['success', 'register'],
    setup(_, { emit }) {
      const { createMessage } = useMessage();
      const adjustmentId = ref('');

      const [registerForm, { resetFields, validate }] = useForm({
        labelWidth: 100,
        baseColProps: { span: 24 },
        schemas: formSchema,
        showActionButtonGroup: false,
      });

      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        resetFields();
        setModalProps({ confirmLoading: false });
        adjustmentId.value = data.record.adjustmentId;
      });

      async function handleSubmit() {
        try {
          const values = await validate();
          setModalProps({ confirmLoading: true });
          
          await approveAdjustment({
            adjustmentId: adjustmentId.value,
            status: 3, // 驳回
            rejectReason: values.rejectReason,
          });
          
          closeModal();
          emit('success');
          createMessage.success('驳回成功');
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      return { registerModal, registerForm, handleSubmit };
    },
  });
</script>