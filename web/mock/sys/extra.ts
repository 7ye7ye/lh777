import { resultSuccess, baseUrl } from '../_util';
import type { MockMethod } from 'vite-plugin-mock';

// 补充一些在未启动后端时仍会请求的接口，避免代理到 127.0.0.1:8095 报错

const tinyPng = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=';

const mocks: MockMethod[] = [
  // 验证码图片（兼容两种路径）
  {
    url: `${baseUrl}/sys/randomImage/`,
    method: 'get',
    // vite-plugin-mock 不支持前缀匹配，这里仅用于占位
    response: () => resultSuccess(tinyPng),
  },
  {
    url: /\/sys\/randomImage\/.*/, // 正则匹配 /sys/randomImage/*
    method: 'get',
    response: () => resultSuccess(tinyPng),
  },

  // 公告列表（登录页右上角等处会拉取）
  {
    url: `${baseUrl}/sys/annountCement/listByUser`,
    method: 'get',
    response: () =>
      resultSuccess([
        { id: 1, title: '系统维护通知', content: '本周日凌晨进行维护。', createTime: '2025-11-05' },
      ]),
  },
  {
    url: '/sys/annountCement/listByUser',
    method: 'get',
    response: () =>
      resultSuccess([
        { id: 1, title: '系统维护通知', content: '本周日凌晨进行维护。', createTime: '2025-11-05' },
      ]),
  },
];

export default mocks;


