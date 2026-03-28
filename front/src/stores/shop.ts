import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ShopType } from '@/types/shop'
import { getShopTypes } from '@/api/shop'

export const useShopStore = defineStore('shop', () => {
  const shopTypes = ref<ShopType[]>([])
  const currentTypeId = ref<number | null>(null)

  async function fetchShopTypes() {
    if (shopTypes.value.length > 0) return
    shopTypes.value = await getShopTypes()
  }

  function setCurrentType(typeId: number | null) {
    currentTypeId.value = typeId
  }

  return {
    shopTypes,
    currentTypeId,
    fetchShopTypes,
    setCurrentType
  }
})
