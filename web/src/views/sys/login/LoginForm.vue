<template>
  <LoginFormTitle v-show="getShow" class="enter-x" />
  <Form class="p-4 enter-x" :model="formData" :rules="getFormRules" ref="formRef" v-show="getShow" @keypress.enter="handleLogin">
    <FormItem name="account" class="enter-x">
      <Input size="large" v-model:value="formData.account" :placeholder="t('sys.login.userName')" class="fix-auto-fill" />
    </FormItem>
    <FormItem name="password" class="enter-x">
      <InputPassword size="large" visibilityToggle v-model:value="formData.password" :placeholder="t('sys.login.password')" />
    </FormItem>

    <ARow class="enter-x">
      <ACol :span="12">
        <FormItem>
          <!-- No logic, you need to deal with it yourself -->
          <Checkbox v-model:checked="rememberMe" size="small">
            {{ t('sys.login.rememberMe') }}
          </Checkbox>
        </FormItem>
      </ACol>
      <ACol :span="12">
        <FormItem :style="{ 'text-align': 'right' }">
          <!-- No logic, you need to deal with it yourself -->
          <Button type="link" size="small" @click="setLoginState(LoginStateEnum.RESET_PASSWORD)">
            {{ t('sys.login.forgetPassword') }}
          </Button>
        </FormItem>
      </ACol>
    </ARow>

    <FormItem class="enter-x">
      <Button type="primary" size="large" block @click="handleLogin" :loading="loading">
        {{ t('sys.login.loginButton') }}
      </Button>
      <!-- <Button size="large" class="mt-4 enter-x" block @click="handleRegister">
              {{ t('sys.login.registerButton') }}
            </Button> -->
    </FormItem>
    <ARow class="enter-x" :gutter="[16, 16]">
      <ACol :md="12" :xs="24">
        <Button block @click="setLoginState(LoginStateEnum.QR_CODE)">
          {{ t('sys.login.qrSignInFormTitle') }}
        </Button>
      </ACol>
      <ACol :md="12" :xs="24">
        <Button block @click="setLoginState(LoginStateEnum.REGISTER)">
          {{ t('sys.login.registerButton') }}
        </Button>
      </ACol>
    </ARow>

    <Divider class="enter-x">{{ t('sys.login.otherSignIn') }}</Divider>

    <div class="flex justify-evenly enter-x" :class="`${prefixCls}-sign-in-way`">
      <a @click="onThirdLogin('github')" title="github"><GithubFilled /></a>
      <a @click="onThirdLogin('wechat_enterprise')" title="企业微信"> <icon-font class="item-icon" type="icon-qiyeweixin3" /></a>
      <a @click="onThirdLogin('dingtalk')" title="钉钉"><DingtalkCircleFilled /></a>
      <a @click="onThirdLogin('wechat_open')" title="微信"><WechatFilled /></a>
    </div>
  </Form>
  <!-- 第三方登录相关弹框 -->
  <ThirdModal ref="thirdModalRef"></ThirdModal>
</template>
<script setup lang="ts">
  import { reactive, ref, toRaw, unref, computed } from 'vue';

  import { Checkbox, Form, Input, Row, Col, Button, Divider } from 'ant-design-vue';
  import { GithubFilled, WechatFilled, DingtalkCircleFilled, createFromIconfontCN } from '@ant-design/icons-vue';
  import LoginFormTitle from './LoginFormTitle.vue';
  import ThirdModal from './ThirdModal.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';

  import { useUserStore } from '/@/store/modules/user';
  import { LoginStateEnum, useLoginState, useFormRules, useFormValid } from './useLogin';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useRouter } from 'vue-router';

  const ACol = Col;
  const ARow = Row;
  const FormItem = Form.Item;
  const InputPassword = Input.Password;
  const IconFont = createFromIconfontCN({
    scriptUrl: '//at.alicdn.com/t/font_2316098_umqusozousr.js',
  });
  const { t } = useI18n();
  const { notification } = useMessage();
  const { prefixCls } = useDesign('login');
  const userStore = useUserStore();
  const router = useRouter();

  const { setLoginState, getLoginState } = useLoginState();
  const { getFormRules } = useFormRules();

  const formRef = ref();
  const thirdModalRef = ref();
  const loading = ref(false);
  const rememberMe = ref(false);

  const formData = reactive({
    account: 'admin',
    password: '123456',
  });

  const { validForm } = useFormValid(formRef);

  const getShow = computed(() => unref(getLoginState) === LoginStateEnum.LOGIN);

  // 登录提交逻辑
  async function handleLogin() {
    const data = await validForm();
    if (!data) return;

    try {
      loading.value = true;
      const resultInfo: any = await userStore.login(
        toRaw({
          userPassword: data.password,
          userAccount: data.account,
          mode: 'none',
        })
      );
      if (resultInfo?.data?.code === 20000) {
        notification.success({
          message: t('sys.login.loginSuccessTitle'),
          description: resultInfo?.data?.description || resultInfo?.data?.message || t('sys.api.loginMsg'),
          duration: 3,
        });

        // 登录后默认跳转首页或指定页面
        await router.push({ path: '/' });
      } else {
        const errorDesc = resultInfo?.data?.description || resultInfo?.data?.message || t('sys.api.networkExceptionMsg');
        notification.warning({
          message: t('sys.api.errorTip'),
          description: errorDesc,
          duration: 3,
        });
        loading.value = false;
      }
    } catch (error: any) {
      console.error('登录错误详情:', error);
      
      // 尝试从不同位置获取后端返回的错误信息
      let errorMessage = t('sys.api.networkExceptionMsg');
      let errorDescription = '';
      
      // 1. 从axios响应中获取错误信息
      if (error?.response?.data) {
        const responseData = error.response.data;
        errorMessage = responseData.message || responseData.error || errorMessage;
        errorDescription = responseData.description || '';
      }
      
      // 2. 从error对象中获取信息
      if (error?.message) {
        errorMessage = error.message;
      }
      
      // 3. 如果有description字段，优先显示description
      const finalMessage = errorDescription || errorMessage;
      
      // 在控制台打印完整错误信息，便于调试
      console.error('错误消息:', errorMessage);
      if (errorDescription) {
        console.error('详细错误信息:', errorDescription);
      }
      
      notification.error({
        message: t('sys.api.errorTip'),
        description: finalMessage,
        duration: 4,
      });
      loading.value = false;
    }
  }

  /**
   * 第三方登录
   * @param type
   */
  function onThirdLogin(type) {
    thirdModalRef.value.onThirdLogin(type);
  }
</script>