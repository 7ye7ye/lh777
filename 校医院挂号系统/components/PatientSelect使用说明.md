# PatientSelect 组件使用说明

> 组件路径：`@/components/PatientSelect.vue`

## 一、组件功能说明

`PatientSelect` 用来在挂号等需要“绑定就诊人/就诊卡”的场景下，统一处理就诊人选择逻辑：

- **未登录**：提醒用户先登录，再进行挂号。
- **没有任何就诊卡**：提示“当前账号还没有就诊卡，无法挂号”，并引导去创建就诊卡页面。
- **只有一张就诊卡**：自动使用这一张卡对应的 `patientId`。
- **有多张就诊卡**：通过 `uni.showActionSheet` 弹出列表，让用户选择本次就诊的就诊人。

组件本身只显示一个简洁提示卡片，真正的“选择逻辑”通过对外暴露的方法 `ensurePatientSelected` 来完成。

---

## 二、对外暴露的方法

### 1. `ensurePatientSelected()`

- **调用方式**：`await patientSelectRef.value.ensurePatientSelected()`
- **返回值**：
  - 成功选择时：`{ patientId: number }`
  - 用户取消 / 未登录且未去登录 / 没有就诊卡且未去创建等情况：`null`

> 建议写法：
>
> ```ts
> const selected = await patientSelectRef.value.ensurePatientSelected()
> if (!selected || !selected.patientId) {
>   // 用户没有完成选择，直接 return，后续挂号逻辑不要继续
>   return
> }
> const patientId = selected.patientId
> ```

---

## 三、在页面中如何使用

下面以 `subpkg/hospital/appointment.vue`（预约挂号页面）为例说明。

### 1. 引入组件

```vue
<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PatientSelect from '@/components/PatientSelect.vue'
// ... 其他 import
</script>
```

### 2. 在模板中挂载组件，并建立 ref

```vue
<template>
  <view class="appointment-page-wrapper">
    <view class="detail-bg">
      <!-- 就诊卡选择提示卡片 -->
      <PatientSelect ref="patientSelectRef" />

      <!-- 下面是原来的医生信息、科室、挂号类型等内容 -->
      <!-- ... -->
    </view>
  </view>
</template>
```

在 `<script setup>` 中增加对应的 `ref`：

```ts
const patientSelectRef = ref(null)
```

> 注意：`PatientSelect` 本身不负责发起挂号，只提供“选择就诊卡”的能力，真正调用要在业务按钮（比如“确认预约”）里完成。

### 3. 在“确认预约”前先选择就诊卡

假设页面中有一个 `confirmAppointment` 函数，在发起挂号之前，先调用 `ensurePatientSelected`：

```ts
const confirmAppointment = async () => {
  // 1. 先校验是否已选择预约信息（你原有的逻辑）
  if (!canSubmit.value) {
    uni.showToast({
      title: '请完整选择预约信息',
      icon: 'none'
    })
    return
  }

  // 2. 通过 PatientSelect 组件选择就诊人/就诊卡
  const selected = await (patientSelectRef.value?.ensurePatientSelected?.())
  if (!selected || !selected.patientId) {
    // 用户未完成选择，直接终止后续流程
    return
  }
  const patientId = selected.patientId

  // 3. 使用 patientId 进行后续业务逻辑，例如：
  //    - 检查是否重复挂号：checkDuplicateBySchedule(patientId, scheduleId)
  //    - 创建挂号记录 / 加入候补队列
  //    - 跳转到支付页面时携带 patientId

  const scheduleId = selectedSchedule.value.schedule_id ?? selectedSchedule.value.scheduleId

  // 示例：检查是否重复挂号
  const isDuplicate = await checkDuplicateBySchedule(patientId, scheduleId)
  if (isDuplicate) {
    uni.showToast({
      title: '您已预约过该时段，请勿重复挂号',
      icon: 'none'
    })
    return
  }

  // 后续是原来的成功提示和跳转逻辑
  // ...
}
```

### 4. 在候补队列逻辑中使用 `patientId`

如果页面中有“号源已满，加入候补队列”的逻辑（例如当前使用的是写死 `patientId = 1`），建议也通过 `PatientSelect` 选择：

```ts
const selectedPatient = await (patientSelectRef.value?.ensurePatientSelected?.())
if (!selectedPatient || !selectedPatient.patientId) {
  return
}
const patientId = selectedPatient.patientId

const resData = await addWaitingQueue({
  scheduleId,
  patientId,
  recordId
})
```

---

## 四、使用注意事项

- **必须确保用户已登录**：
  - 组件内部已经做了“未登录提示 + 去登录”的处理。
  - 页面本身也可以继续使用现有的 `checkAuth` / `createAuthHandler` 做一层登录校验，两者不冲突。

- **不要在多个地方同时缓存 `patientId`**：
  - 建议每次点击“确认预约”或“加入候补”时，重新调用一次 `ensurePatientSelected`，保证使用的是用户当前想用的就诊人。

- **组件本身无 props**：
  - 当前版本不需要向 `PatientSelect` 传入任何属性，只需引用并通过 ref 调用方法即可。

- **视觉层面的说明**：
  - 组件会在页面中显示一张白色提示卡片，包含标题、说明文字，以及加载时的“正在获取就诊人信息，请稍候...”提示。
  - 如果你不想在某个页面展示这张卡片，但还想复用选择逻辑，可以考虑在模板中不渲染这个组件，而是将逻辑抽为独立工具函数（目前项目中是以组件形式提供）。

---

## 五、适用场景建议

- 预约挂号页面（`appointment.vue`）：**强烈建议接入**。
- 检查预约、住院预约等其他需要绑定“就诊人”的功能：可以复用同一套逻辑。
- 任何需要“选择就诊卡 / 就诊人”的业务入口。
