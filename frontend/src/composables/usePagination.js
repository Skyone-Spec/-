/**
 * 分页 Hook
 * 
 * 提供通用的分页状态管理
 */

import { ref, computed } from 'vue'

export function usePagination(options = {}) {
  const {
    defaultPage = 1,
    defaultSize = 10,
    sizes = [10, 20, 50, 100]
  } = options

  // 分页状态
  const page = ref(defaultPage)
  const size = ref(defaultSize)
  const total = ref(0)

  // 计算属性
  const totalPages = computed(() => Math.ceil(total.value / size.value))
  const hasNext = computed(() => page.value < totalPages.value)
  const hasPrev = computed(() => page.value > 1)
  const startRow = computed(() => (page.value - 1) * size.value + 1)
  const endRow = computed(() => Math.min(page.value * size.value, total.value))

  // 方法
  function setPage(newPage) {
    if (newPage >= 1 && newPage <= totalPages.value) {
      page.value = newPage
    }
  }

  function setSize(newSize) {
    size.value = newSize
    page.value = 1 // 重置到第一页
  }

  function setTotal(newTotal) {
    total.value = newTotal
  }

  function nextPage() {
    if (hasNext.value) {
      page.value++
    }
  }

  function prevPage() {
    if (hasPrev.value) {
      page.value--
    }
  }

  function reset() {
    page.value = defaultPage
    total.value = 0
  }

  // 构建查询参数
  function buildParams(extraParams = {}) {
    return {
      ...extraParams,
      page: page.value - 1, // 后端通常使用 0-based
      size: size.value
    }
  }

  // 处理响应数据
  function handleResponse(response) {
    if (response.data) {
      const data = response.data
      if (data.totalElements !== undefined) {
        total.value = data.totalElements
      } else if (data.total !== undefined) {
        total.value = data.total
      }
      return data
    }
    return response
  }

  return {
    // 状态
    page,
    size,
    total,
    
    // 计算属性
    totalPages,
    hasNext,
    hasPrev,
    startRow,
    endRow,
    sizes,
    
    // 方法
    setPage,
    setSize,
    setTotal,
    nextPage,
    prevPage,
    reset,
    buildParams,
    handleResponse
  }
}

export default usePagination
