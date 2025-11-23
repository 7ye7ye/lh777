// noinspection JSUnusedGlobalSymbols

import { useWebSocket } from '@vueuse/core';
import { getToken } from '/@/utils/auth';
import type { Ref } from 'vue';

// 自定义 WebSocket 返回类型接口
interface MyWebSocketResult {
  data: Ref<any>;
  status: Ref<'OPEN' | 'CONNECTING' | 'CLOSED'>;
  close: () => void;
  send: (data: string | ArrayBuffer | Blob) => boolean;
  open: () => void;
  ws: Ref<WebSocket | undefined>;
}

let result: MyWebSocketResult;
const listeners = new Map<(data: any) => void, null>();

/**
 * 开启 WebSocket 链接，全局只需执行一次
 * @param url WebSocket 服务器地址
 */
export function connectWebSocket(url: string) {
  const token = (getToken() || '') as string;

  // 合并所有配置选项
  const options = {
    // 自动重连 (遇到错误最多重复连接10次)
    autoReconnect: {
      retries: 10,
      delay: 5000
    },
    // 心跳检测
    heartbeat: {
      message: "ping",
      interval: 55000
    },
    // 连接事件处理
    onConnected: () => {
      console.log('[WebSocket] 连接成功');
    },
    onDisconnected: (_ws: WebSocket, event: CloseEvent) => {
      console.log('[WebSocket] 连接断开：', event);
    },
    onError: (_ws: WebSocket, event: Event) => {
      console.log('[WebSocket] 连接发生错误: ', event);
    },
    onMessage: (_ws: WebSocket, e: MessageEvent) => {
      console.debug('[WebSocket] -----接收消息-------', e.data);
      handleWebSocketMessage(e);
    },
    // 只有当token存在时才设置protocols
    protocols: token ? [token] : undefined
  };

  result = useWebSocket(url, options);
}

/**
 * 处理 WebSocket 消息
 * @param e 消息事件
 */
function handleWebSocketMessage(e: MessageEvent) {
  try {
    // 过滤心跳消息
    if (e.data === 'ping') {
      return;
    }

    const data = JSON.parse(e.data);

    // 通知所有监听器
    for (const callback of listeners.keys()) {
      try {
        callback(data);
      } catch (err) {
        console.error('[WebSocket] 监听器执行错误：', err);
      }
    }
  } catch (err) {
    console.error('[WebSocket] data解析失败：', err, '原始数据：', e.data);
  }
}

/**
 * 添加 WebSocket 消息监听
 * @param callback 消息回调函数
 */
export function onWebSocket(callback: (data: any) => void) {
  if (typeof callback !== 'function') {
    console.warn('[WebSocket] 添加 WebSocket 消息监听失败：传入的参数不是一个方法');
    return;
  }

  if (!listeners.has(callback)) {
    listeners.set(callback, null);
  }
}

/**
 * 解除 WebSocket 消息监听
 * @param callback 要移除的回调函数
 */
export function offWebSocket(callback: (data: any) => void) {
  listeners.delete(callback);
}

/**
 * 获取 WebSocket 实例
 */
export function useMyWebSocket(): MyWebSocketResult {
  return result;
}

/**
 * 获取当前监听器数量（用于调试）
 */
export function getListenerCount(): number {
  return listeners.size;
}

/**
 * 清空所有监听器
 */
export function clearAllListeners(): void {
  listeners.clear();
}
