import type { ValidationRule } from 'ant-design-vue/lib/form/Form';
import type { RuleObject } from 'ant-design-vue/lib/form/interface';
import { ref, computed, unref, Ref } from 'vue';
import { useI18n } from '/@/hooks/web/useI18n';
import { checkOnlyUser } from '/@/api/sys/user';
import { defHttp } from '/@/utils/http/axios';
import { OAUTH2_THIRD_LOGIN_TENANT_ID } from "/@/enums/cacheEnum";
import { getAuthCache } from "/@/utils/auth";

export enum LoginStateEnum {
  LOGIN,
  REGISTER,
  RESET_PASSWORD,
  MOBILE,
  QR_CODE,
}

const currentState = ref(LoginStateEnum.LOGIN);

export function useLoginState() {
  function setLoginState(state: LoginStateEnum) {
    currentState.value = state;
  }

  const getLoginState = computed(() => currentState.value);

  function handleBackLogin() {
    setLoginState(LoginStateEnum.LOGIN);
  }

  return { setLoginState, getLoginState, handleBackLogin };
}

export function useFormValid<T extends Object = any>(formRef: Ref<any>) {
  async function validForm() {
    const form = unref(formRef);
    if (!form) return;
    const data = await form.validate();
    return data as T;
  }

  return { validForm };
}

export function useFormRules(formData?: Recordable) {
  const { t } = useI18n();

  const getAccountFormRule = computed(() => createRule(t('sys.login.accountPlaceholder')));
  const getPasswordFormRule = computed(() => createRule(t('sys.login.passwordPlaceholder')));

  const getRegisterAccountRule = computed(() => createRegisterAccountRule('account'));

  const validatePolicy = async (_: RuleObject, value: boolean) => {
    return !value ? Promise.reject(t('sys.login.policyPlaceholder')) : Promise.resolve();
  };

  const validateConfirmPassword = (password: string) => {
    return async (_: RuleObject, value: string) => {
      if (!value) {
        return Promise.reject(t('sys.login.passwordPlaceholder'));
      }
      if (value !== password) {
        return Promise.reject(t('sys.login.diffPwd'));
      }
      return Promise.resolve();
    };
  };

  const getFormRules = computed((): { [k: string]: ValidationRule | ValidationRule[] } => {
    const accountFormRule = unref(getAccountFormRule);
    const passwordFormRule = unref(getPasswordFormRule);

    const registerAccountRule = unref(getRegisterAccountRule);

    switch (unref(currentState)) {
      // register form rules
      case LoginStateEnum.REGISTER:
        return {
          account: registerAccountRule,
          password: passwordFormRule,
          confirmPassword: [{ validator: validateConfirmPassword(formData?.password), trigger: 'change' }],
          policy: [{ validator: validatePolicy, trigger: 'change' }],
        };

      // reset password form rules
      case LoginStateEnum.RESET_PASSWORD:
        return {
          username: accountFormRule,
          confirmPassword: [{ validator: validateConfirmPassword(formData?.password), trigger: 'change' }],
        };

      // mobile form rules (已移除手机号和验证码相关，这里简化处理)
      case LoginStateEnum.MOBILE:
        return {
          account: accountFormRule,
        };

      // login form rules
      default:
        return {
          account: accountFormRule,
          password: passwordFormRule,
        };
    }
  });
  return { getFormRules };
}

function createRule(message: string) {
  return [
    {
      required: true,
      message,
      trigger: 'change',
    },
  ];
}
function createRegisterAccountRule(type) {
  return [
    {
      validator: checkUsername,
      trigger: 'change',
    },
  ];
}

function checkUsername(rule, value, callback) {
  const { t } = useI18n();
  if (!value) {
    return Promise.reject(t('sys.login.accountPlaceholder'));
  } else {
    return new Promise((resolve, reject) => {
      checkOnlyUser({ username: value }).then((res) => {
        res.success ? resolve() : reject('用户名已存在!');
      });
    });
  }
}

//update-begin---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3不支持auth2登录------------
/**
 * 判断是否是OAuth2APP环境
 */
export function isOAuth2AppEnv() {
  return /wxwork|dingtalk/i.test(navigator.userAgent);
}

/**
 * 判断是否是钉钉环境
 */
export function isOAuth2DingAppEnv() {
  return /dingtalk/i.test(navigator.userAgent);
}

/**
 * 后台构造oauth2登录地址
 * @param source
 * @param tenantId
 */
export function sysOAuth2Login(source) {
  let url = `${window._CONFIG['domianURL']}/sys/thirdLogin/oauth2/${source}/login`;
  url += `?state=${encodeURIComponent(window.location.origin)}`;
  //update-begin---author:wangshuai ---date:20230224  for：[QQYUN-3440]新建企业微信和钉钉配置表，通过租户模式隔离------------
  let tenantId = getAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID);
  if(tenantId){
    url += `&tenantId=${tenantId}`;
  }
  //update-end---author:wangshuai ---date:20230224  for：[QQYUN-3440]新建企业微信和钉钉配置表，通过租户模式隔离------------
  window.location.href = url;
}
//update-end---author:wangshuai ---date:20220629  for：[issues/I5BG1I]vue3不支持auth2登录------------

//update-begin---author:wangshuai ---date:20241108  for：[QQYUN-9421]vue3新版auth登录，用户不用点击登录------------
/**
 * 后台callBack
 * @param code
 */
export function sysOAuth2Callback(code:string) {
  let url = `${window._CONFIG['domianURL']}/sys/thirdLogin/oauth2/dingding/login`;
  url += `?state=${encodeURIComponent(window.location.origin)}&authCode=${code}`;
  let tenantId = getAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID);
  if(tenantId){
    url += `&tenantId=${tenantId}`;
  }
  window.location.href = url;
}
//update-end---author:wangshuai ---date:20241108  for：[QQYUN-9421]vue3新版auth登录，用户不用点击登录------------