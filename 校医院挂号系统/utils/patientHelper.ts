import { patientApi } from '../api/patient'
import { useUserStore } from '../store/user'

declare const uni: any

const STORAGE_KEY = 'currentPatientCard'

const readCachedPatient = () => {
  try {
    const stored = uni.getStorageSync(STORAGE_KEY)
    if (stored && typeof stored === 'object' && stored.patientId) {
      return stored
    }
  } catch (error) {
    console.warn('[patientHelper] read cache failed', error)
  }
  return null
}

type PatientInfo = {
  patientId?: number
  [key: string]: any
} | null

const writeCachedPatient = (info: PatientInfo) => {
  try {
    if (info && info.patientId) {
      uni.setStorageSync(STORAGE_KEY, info)
    } else {
      uni.removeStorageSync(STORAGE_KEY)
    }
  } catch (error) {
    console.warn('[patientHelper] write cache failed', error)
  }
}

export const clearPatientCache = () => {
  try {
    uni.removeStorageSync(STORAGE_KEY)
  } catch (error) {
    console.warn('[patientHelper] clear cache failed', error)
  }
}

export const getCachedPatient = () => {
  return readCachedPatient()
}

export const fetchPatientCard = async () => {
  const userStore = useUserStore() as any
  const userId = userStore?.userInfo?.userId
  if (!userId) {
    return null
  }
  try {
    const cardInfo = await patientApi.getCard({ userId }) as PatientInfo
    if (cardInfo && cardInfo.patientId) {
      writeCachedPatient(cardInfo)
      return cardInfo
    }
    writeCachedPatient(null)
    return null
  } catch (error) {
    console.error('[patientHelper] fetch patient card failed', error)
    return null
  }
}

export const ensurePatientCard = async () => {
  const userStore = useUserStore() as any
  const currentUserId = userStore?.userInfo?.userId

  const cached = getCachedPatient()
  // 如果缓存里的就诊卡属于当前登录用户，则直接复用
  if (cached && cached.patientId && (!currentUserId || cached.userId === currentUserId)) {
    return cached
  }

  // 否则清掉旧缓存，按当前登录用户重新拉取
  clearPatientCache()
  return await fetchPatientCard()
}

export const ensurePatientId = async () => {
  const card = await ensurePatientCard()
  return card?.patientId ?? null
}

