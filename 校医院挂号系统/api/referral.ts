// 转诊相关API
import { buildStaticImageUrl } from './file'

// 转诊申请接口
export const submitReferralApplication = async (data: any) => {
  // 模拟API调用
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: '提交成功',
        data: {
          id: 'REF' + Date.now(),
          ...data,
          applyTime: new Date().toLocaleString('zh-CN')
        }
      })
    }, 500)
  })
}

// 获取转诊记录列表
export const getReferralRecords = async (params?: {
  status?: string
  page?: number
  pageSize?: number
}) => {
  // 模拟API调用
  return new Promise((resolve) => {
    setTimeout(() => {
      // 模拟数据
      const mockRecords = [
        {
          id: 'REF20240115001',
          patientName: '张三',
          gender: '男',
          age: '35',
          phone: '138****1234',
          targetHospital: '北京协和医院',
          targetDepartment: '心内科',
          symptoms: '胸闷气短，活动后加重，伴有咳嗽',
          medicalHistory: '高血压病史3年',
          reason: '疑似冠心病，需要进一步检查',
          applyTime: '2024-01-15 09:30:45',
          reviewTime: '2024-01-15 11:20:30',
          status: '已审核',
          reviewDoctor: '李医生',
          reviewComments: '同意转诊'
        },
        {
          id: 'REF20240114002',
          patientName: '李四',
          gender: '女',
          age: '42',
          phone: '139****5678',
          targetHospital: '北京大学第一医院',
          targetDepartment: '骨科',
          symptoms: '膝关节疼痛，行走困难',
          medicalHistory: '无',
          reason: '膝关节退行性病变',
          applyTime: '2024-01-14 14:20:18',
          status: '已拒绝',
          rejectReason: '建议先在本院进行保守治疗观察'
        },
        {
          id: 'REF20240115003',
          patientName: '王五',
          gender: '男',
          age: '28',
          phone: '137****9012',
          targetHospital: '北京301医院',
          targetDepartment: '神经内科',
          symptoms: '头痛反复发作，伴有恶心呕吐',
          medicalHistory: '偏头痛病史',
          reason: '头痛症状加重，需要详细检查',
          applyTime: '2024-01-15 16:45:30',
          status: '待审核'
        },
        {
          id: 'REF20240113004',
          patientName: '赵六',
          gender: '女',
          age: '56',
          phone: '136****3456',
          targetHospital: '北京同仁医院',
          targetDepartment: '眼科',
          symptoms: '视力下降，眼睛干涩，有异物感',
          medicalHistory: '糖尿病病史10年',
          reason: '可能存在糖尿病视网膜病变',
          applyTime: '2024-01-13 10:15:22',
          reviewTime: '2024-01-13 14:30:05',
          status: '已审核',
          reviewDoctor: '张医生',
          reviewComments: '建议尽快前往眼科专科医院就诊'
        },
        {
          id: 'REF20240112005',
          patientName: '孙七',
          gender: '男',
          age: '45',
          phone: '135****7890',
          targetHospital: '北京天坛医院',
          targetDepartment: '神经外科',
          symptoms: '右侧肢体无力，言语不清',
          medicalHistory: '高血压、高血脂',
          reason: '疑似脑血管疾病',
          applyTime: '2024-01-12 18:30:45',
          reviewTime: '2024-01-12 19:15:20',
          status: '已审核',
          reviewDoctor: '王医生',
          reviewComments: '紧急转诊，疑似脑卒中'
        }
      ]

      // 根据状态筛选
      let filteredRecords = mockRecords
      if (params?.status && params.status !== '全部') {
        filteredRecords = mockRecords.filter(record => record.status === params.status)
      }

      // 模拟分页
      const page = params?.page || 1
      const pageSize = params?.pageSize || 10
      const startIndex = (page - 1) * pageSize
      const endIndex = startIndex + pageSize
      const paginatedRecords = filteredRecords.slice(startIndex, endIndex)

      resolve({
        code: 200,
        message: '查询成功',
        data: {
          records: paginatedRecords,
          total: filteredRecords.length,
          page,
          pageSize
        }
      })
    }, 300)
  })
}

// 获取转诊详情
export const getReferralDetail = async (id: string) => {
  // 模拟API调用
  return new Promise((resolve) => {
    setTimeout(() => {
      // 模拟不同ID的详情数据
      const mockDetails: Record<string, any> = {
        'REF20240115001': {
          id: 'REF20240115001',
          patientName: '张三',
          gender: '男',
          age: '35',
          phone: '138****1234',
          symptoms: '患者最近两周出现持续性胸闷气短症状，活动后明显加重，伴有轻微咳嗽。曾在我校医院进行初步检查，心电图显示异常，建议转诊上级医院进一步检查和治疗。',
          medicalHistory: '高血压病史3年，规律服用降压药物。否认糖尿病、冠心病等慢性病史。无药物过敏史。',
          reason: '疑似冠心病，需要进行冠状动脉造影等进一步检查以明确诊断。',
          targetHospital: '北京协和医院',
          targetDepartment: '心内科',
          applyTime: '2024-01-15 09:30:45',
          reviewTime: '2024-01-15 11:20:30',
          status: '已审核',
          reviewDoctor: '李医生',
          reviewComments: '患者症状和检查结果符合转诊指征，同意转诊至北京协和医院心内科进一步诊治。建议患者尽快前往就诊，并携带相关检查资料。',
          attachments: [
            { id: '1', url: buildStaticImageUrl('bjtu.jpg'), name: '心电图检查.jpg' },
            { id: '2', url: buildStaticImageUrl('hospitalpicture.png'), name: '血液检查报告.jpg' }
          ]
        },
        'REF20240114002': {
          id: 'REF20240114002',
          patientName: '李四',
          gender: '女',
          age: '42',
          phone: '139****5678',
          symptoms: '双膝关节疼痛已有半年，近一个月症状加重，上下楼梯时疼痛明显，平地行走也受到影响。',
          medicalHistory: '无慢性病史，否认药物过敏史。',
          reason: '膝关节退行性病变，可能需要手术治疗。',
          targetHospital: '北京大学第一医院',
          targetDepartment: '骨科',
          applyTime: '2024-01-14 14:20:18',
          reviewTime: '2024-01-14 16:45:30',
          status: '已拒绝',
          reviewDoctor: '刘医生',
          rejectReason: '根据X光检查结果，患者膝关节退行性病变程度较轻，建议先在本院进行保守治疗3个月，包括药物治疗、物理治疗和康复训练。如保守治疗效果不佳，再考虑转诊。',
          attachments: [
            { id: '1', url: buildStaticImageUrl('bjtu.jpg'), name: '膝关节X光片.jpg' }
          ]
        },
        'REF20240115003': {
          id: 'REF20240115003',
          patientName: '王五',
          gender: '男',
          age: '28',
          phone: '137****9012',
          symptoms: '头痛反复发作已有3年，主要为右侧颞部搏动性疼痛，每次发作持续4-6小时。近两周发作频率增加，从每月1-2次增加到每周2-3次，且伴有恶心呕吐症状。',
          medicalHistory: '有偏头痛家族史，父亲患有偏头痛。否认其他慢性病史。',
          reason: '头痛症状加重，常规药物治疗效果不佳，需要进一步检查明确病因。',
          targetHospital: '北京301医院',
          targetDepartment: '神经内科',
          applyTime: '2024-01-15 16:45:30',
          status: '待审核',
          attachments: []
        }
      }

      // 如果找不到对应的ID，返回默认数据
      const detail = mockDetails[id] || mockDetails['REF20240115001']

      resolve({
        code: 200,
        message: '查询成功',
        data: detail
      })
    }, 300)
  })
}

// 获取可转诊医院列表
export const getReferralHospitals = async () => {
  // 模拟API调用
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: '查询成功',
        data: [
          { id: 1, name: '北京协和医院', level: '三级甲等', address: '北京市东城区帅府园1号' },
          { id: 2, name: '北京大学第一医院', level: '三级甲等', address: '北京市西城区西什库大街8号' },
          { id: 3, name: '北京301医院', level: '三级甲等', address: '北京市海淀区复兴路28号' },
          { id: 4, name: '北京同仁医院', level: '三级甲等', address: '北京市东城区东交民巷1号' },
          { id: 5, name: '北京天坛医院', level: '三级甲等', address: '北京市丰台区南四环西路119号' },
          { id: 6, name: '北京儿童医院', level: '三级甲等', address: '北京市西城区南礼士路56号' },
          { id: 7, name: '北京中医医院', level: '三级甲等', address: '北京市东城区美术馆后街23号' },
          { id: 8, name: '北京安贞医院', level: '三级甲等', address: '北京市朝阳区安贞路2号' }
        ]
      })
    }, 200)
  })
}

// 取消转诊申请
export const cancelReferralApplication = async (id: string) => {
  // 模拟API调用
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        message: '取消成功'
      })
    }, 300)
  })
}