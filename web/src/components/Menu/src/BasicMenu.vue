<template>
  <Menu
    :selectedKeys="selectedKeys"
    :defaultSelectedKeys="defaultSelectedKeys"
    :mode="mode"
    :openKeys="getOpenKeys"
    :inlineIndent="inlineIndent"
    :theme="theme"
    @openChange="handleOpenChange"
    :class="getMenuClass"
    @click="handleMenuClick"
    :subMenuOpenDelay="0.2"
    v-bind="getInlineCollapseOptions"
  >
    <template v-for="item in items" :key="item.path">
      <BasicSubMenuItem :item="item" :theme="theme" :isHorizontal="isHorizontal" />
    </template>
  </Menu>
</template>

<script lang="ts">
import type { MenuState } from './types';
import { computed, defineComponent, unref, reactive, watch, toRefs, ref } from 'vue';
import { Menu } from 'ant-design-vue';
import BasicSubMenuItem from './components/BasicSubMenuItem.vue';
import { MenuModeEnum, MenuTypeEnum } from '/@/enums/menuEnum';
import { useOpenKeys } from './useOpenKeys';
import { RouteLocationNormalizedLoaded, useRouter } from 'vue-router';
import { isFunction, isUrl } from '/@/utils/is';
import { basicProps } from './props';
import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
import { REDIRECT_NAME } from '/@/router/constant';
import { useDesign } from '/@/hooks/web/useDesign';
import { getCurrentParentPath } from '/@/router/menus';
import { listenerRouteChange } from '/@/logics/mitt/routeChange';
import { getAllParentPath } from '/@/router/helper/menuHelper';
import { createBasicRootMenuContext } from './useBasicMenuContext';
import { URL_HASH_TAB } from '/@/utils';

export default defineComponent({
  name: 'BasicMenu',
  components: {
    Menu,
    BasicSubMenuItem,
  },
  props: basicProps,
  emits: ['menuClick'],
  setup(props, { emit }) {
    const isClickGo = ref(false);
    const currentActiveMenu = ref('');
    const router = useRouter();

    const menuState = reactive<MenuState>({
      defaultSelectedKeys: [],
      openKeys: [],
      selectedKeys: [],
      collapsedOpenKeys: [],
    });

    createBasicRootMenuContext({ menuState: menuState });

    const { prefixCls } = useDesign('basic-menu');
    const { items, mode, accordion } = toRefs(props);
    const { getCollapsed, getTopMenuAlign, getSplit } = useMenuSetting();
    const { currentRoute } = useRouter();

    const { handleOpenChange, setOpenKeys, getOpenKeys } = useOpenKeys(menuState, items, mode as any, accordion);

    // ========== 修复：定义 getIsTopMenu 计算属性 ==========
    const getIsTopMenu = computed(() => {
      const { type, mode } = props;
      return (type === MenuTypeEnum.TOP_MENU && mode === MenuModeEnum.HORIZONTAL) || (props.isHorizontal && unref(getSplit));
    });

    const getMenuClass = computed(() => {
      const align = props.isHorizontal && unref(getSplit) ? 'start' : unref(getTopMenuAlign);
      return [
        prefixCls,
        `justify-${align}`,
        {
          [`${prefixCls}__second`]: !props.isHorizontal && unref(getSplit),
          [`${prefixCls}__sidebar-hor`]: unref(getIsTopMenu), // 现在这里可以正常使用了
        },
      ];
    });

    const getInlineCollapseOptions = computed(() => {
      const isInline = props.mode === MenuModeEnum.INLINE;
      const inlineCollapseOptions: { inlineCollapsed?: boolean } = {};
      if (isInline) {
        inlineCollapseOptions.inlineCollapsed = props.mixSider ? false : unref(getCollapsed);
      }
      return inlineCollapseOptions;
    });

    // ========== 修复菜单点击路由跳转问题 ==========
    async function handleMenuClick({ key }: { item: any; key: string; keyPath: string[] }) {
      console.log('菜单点击:', key);

      const { beforeClickFn } = props;

      // 处理外部链接
      if (isUrl(key)) {
        const url = key.replace(URL_HASH_TAB, '#');
        window.open(url);
        return;
      }

      // 处理新标签页打开
      const findItem = getMatchingMenu(props.items, key);
      if (findItem?.internalOrExternal) {
        window.open(location.origin + key);
        return;
      }

      // 前置处理函数
      if (beforeClickFn && isFunction(beforeClickFn)) {
        const flag = await beforeClickFn(key);
        if (!flag) return;
      }

      // 标记为点击跳转
      isClickGo.value = true;

      try {
        // 关键：使用 router.push 进行跳转
        if (unref(currentRoute).path !== key) {
          await router.push(key);
          console.log('路由跳转成功:', key);
        } else {
          console.log('已经是当前路由:', key);
          // 如果是当前路由，强制重新加载
          await router.replace(key);
          console.log('强制重新加载路由');
        }
      } catch (error) {
        console.error('路由跳转失败:', error);
        // 降级方案
        if (!key.startsWith('http')) {
          window.location.hash = key;
        }
      }

      // 更新菜单状态
      menuState.selectedKeys = [key];
      setOpenKeys(key);

      // 触发菜单点击事件
      emit('menuClick', key, { key });
    }

    async function handleMenuChange(route?: RouteLocationNormalizedLoaded) {
      const targetRoute = route || unref(currentRoute);
      console.log('路由变化:', targetRoute.path);

      if (unref(isClickGo)) {
        isClickGo.value = false;
        return;
      }

      const path = targetRoute.meta?.currentActiveMenu || targetRoute.path;
      setOpenKeys(path);

      if (unref(currentActiveMenu)) {
        menuState.selectedKeys = [unref(currentActiveMenu)];
        return;
      }

      if (props.isHorizontal && unref(getSplit)) {
        const parentPath = await getCurrentParentPath(path);
        menuState.selectedKeys = [parentPath];
      } else {
        const parentPaths = await getAllParentPath(props.items, path);
        menuState.selectedKeys = parentPaths;
      }
    }

    // 监听路由变化
    listenerRouteChange((route) => {
      if (route.name === REDIRECT_NAME) return;
      console.log('路由监听器触发:', route.path);
      handleMenuChange(route);
      currentActiveMenu.value = route.meta?.currentActiveMenu as string;

      if (unref(currentActiveMenu)) {
        menuState.selectedKeys = [unref(currentActiveMenu)];
        setOpenKeys(unref(currentActiveMenu));
      }
    });

    // 监听菜单项变化
    !props.mixSider &&
    watch(
      () => props.items,
      () => {
        handleMenuChange();
      }
    );

    // ========== 辅助函数 ==========
    const getMatchingMenu = (menus: any[], path: string): any => {
      for (let i = 0; i < menus.length; i++) {
        const item = menus[i];
        if (item.path === path && !item.redirect && !item.paramPath) {
          return item;
        } else if (item.children?.length) {
          const result = getMatchingMenu(item.children, path);
          if (result) {
            return result;
          }
        }
      }
      return null;
    };

    // 混合菜单相关的辅助函数（如果原代码中有需要保留）
    return {
      handleMenuClick,
      getInlineCollapseOptions,
      getMenuClass,
      handleOpenChange,
      getOpenKeys,
      getIsTopMenu, // 现在正确导出了
      ...toRefs(menuState),
    };
  },
});
</script>

<style lang="less">
@import './index.less';
</style>
