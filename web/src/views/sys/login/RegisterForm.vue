<template>
  <template v-if="getShow">
    <LoginFormTitle class="enter-x" />
    <Form class="p-4 enter-x" :model="formData" :rules="getFormRules" ref="formRef">
      <FormItem name="account" class="enter-x">
        <Input class="fix-auto-fill" size="large" v-model:value="formData.account" :placeholder="t('sys.login.userName')" />
      </FormItem>
      <FormItem name="password" class="enter-x">
        <StrengthMeter size="large" v-model:value="formData.password" :placeholder="t('sys.login.password')" />
      </FormItem>
      <FormItem name="confirmPassword" class="enter-x">
        <InputPassword size="large" visibilityToggle v-model:value="formData.confirmPassword" :placeholder="t('sys.login.confirmPassword')" />
      </FormItem>

      <FormItem class="enter-x" name="policy">
        <!-- No logic, you need to deal with it yourself -->
        <Checkbox v-model:checked="formData.policy" size="small">
          {{ t('sys.login.policy') }}
        </Checkbox>
      </FormItem>

      <Button type="primary" class="enter-x" size="large" block @click="handleRegister" :loading="loading">
        {{ t('sys.login.registerButton') }}
      </Button>
      <Button size="large" block class="mt-4 enter-x" @click="handleBackLogin">
        {{ t('sys.login.backSignIn') }}
      </Button>
    </Form>
  </template>
</template>
<script lang="ts" setup>
  import { reactive, ref, unref, computed, toRaw } from 'vue';
  import LoginFormTitle from './LoginFormTitle.vue';
  import { Form, Input, Button, Checkbox } from 'ant-design-vue';
  import { StrengthMeter } from '/@/components/StrengthMeter';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useLoginState, useFormRules, useFormValid, LoginStateEnum } from './useLogin';
  import { register } from '/@/api/sys/user';
  const FormItem = Form.Item;
  const InputPassword = Input.Password;
  const { t } = useI18n();
  const { handleBackLogin, getLoginState } = useLoginState();
  const { notification } = useMessage();
  const formRef = ref();
  const loading = ref(false);
  const formData = reactive({
    account: '',
    password: '',
    confirmPassword: '',
    policy: false,
  });

  const { getFormRules } = useFormRules(formData);
  const { validForm } = useFormValid(formRef);
  const getShow = computed(() => unref(getLoginState) === LoginStateEnum.REGISTER);
  /**
   * 注册
   */
  async function handleRegister() {
    const data = await validForm();
    if (!data) return;

    try {
      loading.value = true;
      const resultInfo = await register(
        toRaw({
          userAccount: data.account,
          userPassword: data.password,
          checkPassword: data.confirmPassword,
          userType: 3,
        })
      );
      console.log("lala:", resultInfo);
      if (resultInfo.data.code === 20000) {
        // 注册成功时优先取后端返回的 description，无则用默认文案
        const successDesc = resultInfo?.data?.description || resultInfo?.data?.message || t('sys.api.registerMsg');
        notification.success({
          message: t('sys.login.registerSuccessTitle'),
          description: successDesc,
          duration: 3,
        });
        handleBackLogin();
      } else {
        // 优先取 description 作为错误描述，无则取 message 或默认提示
        const errorDesc = resultInfo?.data?.description || resultInfo?.data?.message || t('sys.api.networkExceptionMsg');
        notification.warning({
          message: t('sys.api.errorTip'),
          description: errorDesc,
          duration: 3,
        });
      }
    } catch (error: any) {
      // 捕获请求异常时，优先取后端返回的 description
      const errorDesc = error?.response?.data?.description || error?.message || t('sys.api.networkExceptionMsg');
      notification.error({
        message: t('sys.api.errorTip'),
        description: errorDesc,
        duration: 3,
      });
    } finally {
      loading.value = false;
    }
  }
</script>