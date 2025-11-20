# 前端patientId与Token解析userId的关系说明

## 🔑 核心问题

**前端传递的 `patientId`** 和 **从Token解析的 `userId`** 是两个不同的ID，它们通过数据库外键关系连接起来。

---

## 📊 数据库表关系

### **表结构关系**：

```
hos_user (用户表)
  ├── user_id (主键) ← 这是从Token解析得到的
  └── user_account (登录账号)

patient (患者表)
  ├── patient_id (主键) ← 这是前端传递的 patientId
  └── user_id (外键) → hos_user.user_id ← 这是连接的关键
```

### **关系说明**：

- **`hos_user.user_id`**：用户登录账号的唯一标识（从Token解析得到）
- **`patient.patient_id`**：患者信息的唯一标识（前端传递）
- **`patient.user_id`**：外键，关联到 `hos_user.user_id`（连接桥梁）

**关系**：一个 `hos_user` 可以对应一个 `patient`（一对一关系）

---

## 🔄 数据流转过程

### **1. 登录阶段**

#### **前端登录**：
```javascript
// 文件：校医院挂号系统/subpkg/auth/login.vue
const res = await userApi.login({ userAccount, userPassword })
const token = res?.token
const userInfo = res?.user  // 包含 userId, userType 等

// 保存到状态管理
userStore.setToken(token)
userStore.setUserInfo(userInfo)
```

#### **后端登录返回**：
- 返回 `token`（JWT Token，包含用户名信息）
- 返回 `user` 对象（包含 `userId`，即 `hos_user.user_id`）

**注意**：登录时**没有返回 `patientId`**，前端也没有存储 `patientId`！

---

### **2. 挂号阶段**

#### **前端传递的数据**：
```javascript
// 文件：校医院挂号系统/subpkg/hospital/payment.vue
const patientId = uni.getStorageSync('patientId') || 1  // ⚠️ 这里可能有问题！

const record = {
  scheduleId: 123,
  patientId,  // 这是 patient.patient_id
  typeId: 1,
  // ...
}

await createRegistration(record, patientId, true)
```

**问题**：前端从本地存储获取 `patientId`，但登录时并没有存储这个值！默认取1

**⚠️ 严重问题**：如果前端默认使用 `patientId = 1`，而当前登录用户是 `userId = 262`，会导致：
- 挂号记录关联到错误的患者（`patient_id = 1` 对应 `user_id = 2`，不是262）
- 消息发给正确的用户（`user_id = 262`）
- **数据不一致**：挂号记录和消息不匹配！

**✅ 已修复**：后端现在会根据Token解析的 `userId` 自动查找对应的 `patientId`，确保数据一致性。

#### **后端接收和处理**（已修复）：
```java
// 文件：RegistrationServiceImpl.java
public Result<String> createRegistration(RegistrationRecord record, Long patientId, boolean joinWaitingQueue) {
    // ⭐ 关键修复：优先根据Token解析的userId查找patientId
    Long actualPatientId = patientId;
    String currentUserId = resolveCurrentUserId();
    if (currentUserId != null) {
        // 根据当前登录用户的userId查找对应的patientId
        Patient patientByUserId = patientMapper.selectOne(
            new LambdaQueryWrapper<Patient>().eq(Patient::getUserId, Long.valueOf(currentUserId)));
        if (patientByUserId != null && patientByUserId.getPatientId() != null) {
            actualPatientId = patientByUserId.getPatientId();  // ✅ 使用正确的patientId
            log.info("根据Token解析的userId={}找到对应的patientId={}", currentUserId, actualPatientId);
        }
    }
    
    // 1. 使用正确的 patientId（patient.patient_id）
    record.setPatientId(actualPatientId);
    
    // 2. 查询患者信息
    Patient patient = patientMapper.selectById(patientId);
    
    // 3. 计算价格（根据患者类型）
    record.setActualPrice(switch (patient.getPatientType()) {
        case 1 -> type.getStudentPrice();  // 学生
        case 2, 3 -> type.getStaffPrice(); // 教职工
        default -> type.getPriceOriginal();
    });
    
    // 4. 插入挂号记录
    registrationMapper.insertRegistration(record);
    
    // 5. 创建成功消息（使用Token解析的userId）
    createSuccessMessage(detail);
}
```

---

### **3. 创建消息阶段**

#### **获取用户ID的优先级**：
```java
// 文件：RegistrationServiceImpl.java - createSuccessMessage()
String currentUserId = resolveCurrentUserId();  // 1. 优先从Token解析

String userId = currentUserId != null ? currentUserId :
    (detail.getPatientUserId() != null ? String.valueOf(detail.getPatientUserId()) : "262");
    // 2. 如果Token解析失败，使用 patient.user_id（从挂号详情查询得到）
    // 3. 如果都失败，使用默认值 "262"

message.setUserId(userId);  // 这是 hos_user.user_id
```

#### **Token解析过程**：
```java
// 文件：RegistrationServiceImpl.java - resolveCurrentUserId()
private String resolveCurrentUserId() {
    // 1. 从HTTP请求头获取Token
    String token = TokenUtils.getTokenByRequest(request);
    
    // 2. 从Token解析用户名
    String username = JwtUtil.getUsername(token);
    
    // 3. 根据用户名查询用户表
    HosUser hosUser = hosUserMapper.selectOne(
        new LambdaQueryWrapper<HosUser>().eq(HosUser::getUserAccount, username));
    
    // 4. 返回 hos_user.user_id
    return String.valueOf(hosUser.getUserId());
}
```

#### **挂号详情查询**：
```sql
-- 文件：RegistrationMapper.java - selectRegistrationDetail()
SELECT 
    rr.record_id,
    p.user_id AS patient_user_id,  -- ⭐ 这里获取的是 patient.user_id（即 hos_user.user_id）
    -- ...
FROM registration_record rr
LEFT JOIN patient p ON rr.patient_id = p.patient_id
WHERE rr.record_id = #{recordId}
```

**关键**：`detail.getPatientUserId()` 返回的是 `patient.user_id`，也就是 `hos_user.user_id`！

---

## 🔗 连接关系总结

### **数据流向**：

```
登录阶段：
  Token → 解析用户名 → 查询 hos_user → 得到 hos_user.user_id
  ↓
  前端存储：token, userInfo (包含 userId = hos_user.user_id)
  ⚠️ 前端没有存储 patientId！

挂号阶段：
  前端传递：patientId (patient.patient_id，可能错误，如默认值1)
  ↓
  后端修复逻辑：
    1. 从Token解析 userId = 262 (hos_user.user_id)
    2. 根据 userId 查询 patient 表：SELECT * FROM patient WHERE user_id = 262
    3. 获取正确的 patientId（如 patient_id = X，对应 user_id = 262）
  ↓
  后端使用正确的 patientId：
    1. 插入 registration_record.patient_id = X（正确的患者）
    2. 查询 patient 表获取患者信息
    3. 计算价格
  ↓
  后端通过 patientId 查询挂号详情：
    SELECT p.user_id FROM patient WHERE patient_id = #{patientId}
    → 得到 patient.user_id (即 hos_user.user_id)
  ↓
  创建消息时：
    优先：从Token解析 hos_user.user_id
    备用：从挂号详情获取 patient.user_id (也是 hos_user.user_id)
  ↓
  消息表存储：message_log.user_id = hos_user.user_id
```

---

## ⚠️ 潜在问题

### **问题1：前端没有正确存储 patientId**

**当前代码**：
```javascript
// payment.vue
const patientId = uni.getStorageSync('patientId') || 1  // ⚠️ 可能不存在！
```

**解决方案**：
1. **登录时查询并存储**：
```javascript
// login.vue - 登录成功后
const res = await userApi.login({...})
const userInfo = res?.user

// 如果是患者，查询patientId
if (userInfo.userType === 1) {
  const patientRes = await userApi.getPatientInfo()  // 需要新增接口
  uni.setStorageSync('patientId', patientRes.patientId)
}
```

2. **或者通过 userId 查询**：
```javascript
// 后端新增接口：根据 userId 查询 patientId
GET /applet/patient/getByUserId?userId={userId}
```

### **问题2：消息查询时的匹配**

**当前逻辑**（已修复）：
- 通过预约成功消息中的 `user_id` 匹配当前用户
- 而不是通过 `patient.patient_id` 匹配

**原因**：
- 消息表中的 `user_id` 是 `hos_user.user_id`
- 前端传递的 `userId` 也是 `hos_user.user_id`（从Token解析或从userInfo获取）
- 所以可以直接匹配

---

## 📝 完整示例

### **场景：用户登录并挂号**

#### **1. 用户登录**：
```
用户账号：24301018
Token解析：hos_user.user_id = 262
前端存储：{ token: "...", userInfo: { userId: 262, ... } }
```

#### **2. 用户挂号**（修复后）：
```
前端传递：
   patientId = 1 (patient.patient_id，默认值，可能错误)
   scheduleId = 123
   typeId = 1

后端处理（修复后）：
   1. 从Token解析：hos_user.user_id = 262
   
   2. 根据 userId 查找 patientId：
      SELECT * FROM patient WHERE user_id = 262
      → patient_id = X (正确的患者ID，对应 user_id = 262)
   
   3. 插入 registration_record：
      - patient_id = X（正确的患者，不是默认值1）
      - record_id = 23 (自动生成)
   
   4. 查询挂号详情：
      SELECT p.user_id FROM patient WHERE patient_id = X
      → patient.user_id = 262（与Token解析一致）
   
   5. 创建消息：
      - 从Token解析：hos_user.user_id = 262
      - 消息表存储：user_id = 262
      - ✅ 数据一致：挂号记录和消息都关联到正确的用户
```

#### **3. 用户查询消息**：
```
前端传递：
  userId = 262 (从 userInfo.userId 获取，即 hos_user.user_id)

后端查询：
  SELECT * FROM message_log WHERE user_id = '262'
  → 找到消息（因为消息表中的 user_id = 262）
```

---

## ✅ 正确的数据关系

### **关键理解**：

1. **`patient.patient_id`** ≠ **`hos_user.user_id`**
   - `patient.patient_id`：患者信息ID（自增主键）
   - `hos_user.user_id`：用户账号ID（自增主键）

2. **`patient.user_id`** = **`hos_user.user_id`**
   - 这是外键关系，用于连接两个表

3. **消息表中的 `user_id`** = **`hos_user.user_id`**
   - 用于标识接收消息的用户
   - 与前端传递的 `userId`（也是 `hos_user.user_id`）匹配

4. **挂号记录中的 `patient_id`** = **`patient.patient_id`**
   - 用于关联患者信息
   - 通过 `patient` 表可以找到对应的 `user_id`

---

## 🔧 建议的改进

### **1. 登录时存储 patientId**：

```javascript
// login.vue
const onSubmit = async () => {
  const res = await userApi.login({...})
  const userInfo = res?.user
  
  if (userInfo.userType === 1) {
    // 查询患者信息
    const patientRes = await userApi.getPatientByUserId(userInfo.userId)
    if (patientRes && patientRes.patientId) {
      uni.setStorageSync('patientId', patientRes.patientId)
    }
  }
}
```

### **2. 后端新增接口**：

```java
@GetMapping("/patient/getByUserId")
public Result<Patient> getPatientByUserId(@RequestParam Long userId) {
    Patient patient = patientService.getByUserId(userId);
    return Result.OK(patient);
}
```

---

## 📊 数据关系图

```
┌─────────────────┐
│   hos_user      │
│  user_id: 262   │ ← Token解析得到
│  user_account   │
└────────┬────────┘
         │ 1:1
         │ (外键)
         ↓
┌─────────────────┐
│    patient      │
│ patient_id: 1   │ ← 前端传递
│   user_id: 262  │ ← 关联到 hos_user
└────────┬────────┘
         │ 1:N
         │ (外键)
         ↓
┌─────────────────┐
│registration_    │
│   record        │
│ patient_id: 1   │ ← 使用 patient.patient_id
│  record_id: 23  │
└────────┬────────┘
         │ 1:1
         │ (通过 record_id)
         ↓
┌─────────────────┐
│  message_log    │
│   user_id: 262  │ ← 使用 hos_user.user_id
│appointment_id:23│ ← 关联到 registration_record
└─────────────────┘
```

---

## 🎯 总结

1. **前端传递的 `patientId`** = `patient.patient_id`（用于插入挂号记录）
2. **从Token解析的 `userId`** = `hos_user.user_id`（用于创建消息）
3. **连接关系**：`patient.user_id` = `hos_user.user_id`（外键）
4. **消息匹配**：通过 `hos_user.user_id` 匹配，因为消息表和前端都使用这个ID

**关键**：虽然 `patientId` 和 `userId` 不同，但它们通过 `patient.user_id` 这个外键连接起来，最终都指向同一个用户账号。

