<template>
  <LoginFormTitle v-show="getShow" class="enter-x" />
  <Form class="p-4 enter-x" :model="formData" :rules="getFormRules" ref="formRef" v-show="getShow" @keypress.enter="handleLogin">
    <FormItem name="account" class="enter-x">
      <Input size="large" v-model:value="formData.account" :placeholder="t('sys.login.userName')" class="fix-auto-fill" />
    </FormItem>
    <FormItem name="password" class="enter-x">
      <InputPassword size="large" visibilityToggle v-model:value="formData.password" :placeholder="t('sys.login.password')" />
    </FormItem>

    <!--验证码-->
    <ARow class="enter-x">
      <ACol :span="12">
        <FormItem name="inputCode" class="enter-x">
          <Input size="large" v-model:value="formData.inputCode" :placeholder="t('sys.login.inputCode')" style="min-width: 100px" />
        </FormItem>
      </ACol>
      <ACol :span="8">
        <FormItem :style="{ 'text-align': 'right', 'margin-left': '20px' }" class="enter-x">
          <img
            v-if="randCodeData.requestCodeSuccess"
            style="margin-top: 2px; max-width: initial"
            :src="randCodeData.randCodeImage"
            @click="handleChangeCheckCode"
          />
          <img v-else style="margin-top: 2px; max-width: initial" src="../../../assets/images/checkcode.png" @click="handleChangeCheckCode" />
        </FormItem>
      </ACol>
    </ARow>

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
    <ARow class="enter-x">
      <ACol :md="8" :xs="24">
        <Button block @click="setLoginState(LoginStateEnum.MOBILE)">
          {{ t('sys.login.mobileSignInFormTitle') }}
        </Button>
      </ACol>
      <ACol :md="8" :xs="24" class="!my-2 !md:my-0 xs:mx-0 md:mx-2">
        <Button block @click="setLoginState(LoginStateEnum.QR_CODE)">
          {{ t('sys.login.qrSignInFormTitle') }}
        </Button>
      </ACol>
      <ACol :md="7" :xs="24">
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
  import { reactive, ref, toRaw, unref, computed, onMounted } from 'vue';

  import { Checkbox, Form, Input, Row, Col, Button, Divider } from 'ant-design-vue';
  import { GithubFilled, WechatFilled, DingtalkCircleFilled, createFromIconfontCN } from '@ant-design/icons-vue';
  import LoginFormTitle from './LoginFormTitle.vue';
  import ThirdModal from './ThirdModal.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';

  import { useUserStore } from '/@/store/modules/user';
  import { LoginStateEnum, useLoginState, useFormRules, useFormValid } from './useLogin';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { getCodeInfo } from '/@/api/sys/user';
  import { useRouter } from 'vue-router';
  //import { onKeyStroke } from '@vueuse/core';

  const ACol = Col;
  const ARow = Row;
  const FormItem = Form.Item;
  const InputPassword = Input.Password;
  const IconFont = createFromIconfontCN({
    scriptUrl: '//at.alicdn.com/t/font_2316098_umqusozousr.js',
  });
  const { t } = useI18n();
  const { notification, createErrorModal } = useMessage();
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
    inputCode: '',
  });
  const randCodeData = reactive({
    randCodeImage: '',
    requestCodeSuccess: false,
    checkKey: null,
  });

  const { validForm } = useFormValid(formRef);

  //onKeyStroke('Enter', handleLogin);

  const getShow = computed(() => unref(getLoginState) === LoginStateEnum.LOGIN);

  // 登录提交逻辑（方法名以你现有为准，这里示例 handleLogin）
  // 假设这是原有的提交函数，名称以你当前文件为准
  async function handleLogin(values: any) {
    const data = await validForm();
    if (!data) return;
  
    // 医生工号规则：10位，前6位数字 + 后4位 bjtu（不区分大小写）
    const isDoctorId = /^\d{6}bjtu$/i.test(data.account);
    if (!isDoctorId) {
      notification.error({
        message: '工号不符合医生端规则',
        description: '仅允许医生工号登录：前6位数字 + 后4位 bjtu',
        duration: 3,
      });
      return;
    }
  
    try {
      loading.value = true;
      const result = await userStore.login(
        toRaw({
          userPassword: data.password,
          userAccount: data.account,
          captcha: data.inputCode,
          checkKey: randCodeData.checkKey,
          mode: 'none',
        })
      );
      if (result) {
        const userType = result?.userInfo?.userType ?? result?.userInfo?.user?.userType;
        if (userType !== 2) {
          notification.error({
            message: '仅医生账户可登录医生端',
            description: '当前账号非医生类型（type_id ≠ 2），请使用医生账号登录',
            duration: 3,
          });
          loading.value = false;
          return;
        }
  
        notification.success({
          message: t('sys.login.loginSuccessTitle'),
          description: `${t('sys.login.loginSuccessDesc')}: ${result.userInfo?.userAccount || result.userInfo?.username || '用户'}`,
          duration: 3,
        });
  
        // 登录后跳转：优先获取个人档案，失败则回退到医生档案页
        try {
          const { getMyDoctorProfile } = await import('/@/api/hospital/doctor');
          const doctorProfile = await getMyDoctorProfile();
          const doctorId = doctorProfile?.doctorId;
          await router.push({ path: '/hospital/doctor/profile', query: doctorId ? { doctorId } : {} });
        } catch (e) {
          await router.push({ path: '/hospital/doctor/profile' });
        } finally {
          loading.value = false;
        }
      } else {
        loading.value = false;
      }
    } catch (error) {
      notification.error({
        message: t('sys.api.errorTip'),
        description: error?.message || t('sys.api.networkExceptionMsg'),
        duration: 3,
      });
      loading.value = false;
      handleChangeCheckCode();
    }
  }
  function handleChangeCheckCode() {
    formData.inputCode = '';
    //TODO 兼容mock和接口，暂时这样处理
    //update-begin---author:chenrui ---date:2025/1/7  for：[QQYUN-10775]验证码可以复用 #7674------------
    randCodeData.checkKey = new Date().getTime() + Math.random().toString(36).slice(-4); // 1629428467008;
    //update-end---author:chenrui ---date:2025/1/7  for：[QQYUN-10775]验证码可以复用 #7674------------
    getCodeInfo(randCodeData.checkKey).then((res) => {
      randCodeData.randCodeImage = res;
      randCodeData.requestCodeSuccess = true;
    });
  }

  /**
   * 第三方登录
   * @param type
   */
  function onThirdLogin(type) {
    thirdModalRef.value.onThirdLogin(type);
  }
  //初始化验证码
  onMounted(() => {
    handleChangeCheckCode();
  });
</script>
