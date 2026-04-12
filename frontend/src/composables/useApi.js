/**
 * API 调用 Hook
 * 
 * 提供统一的 API 调用封装，支持：
 * - 加载状态
 * - 错误处理
 * - 分页
 */

import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export function useApi() {
  const loading = ref(false)
  const error = ref(null)

  // 通用请求封装
  async function request(apiFunc, options = {}) {
    const {
      loading: showLoading = true,
      message: showMessage = true,
      successMessage,
      errorMessage,
      onSuccess,
      onError
    } = options

    loading.value = true
    error.value = null

    try {
      const response = await apiFunc()
      
      if (response.success !== false) {
        if (showMessage && successMessage) {
          ElMessage.success(successMessage)
        }
        onSuccess?.(response)
        return response
      } else {
        throw new Error(response.message || '请求失败')
      }
    } catch (err) {
      error.value = err
      const msg = err.response?.data?.message || err.message
      
      if (showMessage) {
        ElMessage.error(errorMessage || msg)
      }
      
      onError?.(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  // GET 请求
  function get(apiFunc, options = {}) {
    return request(apiFunc, { ...options })
  }

  // POST 请求
  function post(apiFunc, options = {}) {
    return request(apiFunc, { ...options })
  }

  // PUT 请求
  function put(apiFunc, options = {}) {
    return request(apiFunc, { ...options })
  }

  // DELETE 请求
  async function remove(apiFunc, options = {}) {
    const { confirm: needConfirm, confirmTitle = '确认操作', confirmMessage = '确定要删除吗？' } = options

    if (needConfirm) {
      try {
        await ElMessageBox.confirm(confirmMessage, confirmTitle, {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch {
        return // 用户取消
      }
    }

    return request(apiFunc, { ...options })
  }

  // 带分页的列表请求
  async function fetchList(apiFunc, params, options = {}) {
    const { onSuccess } = options

    return request(
      () => apiFunc(params),
      {
        ...options,
        onSuccess: (response) => {
          if (onSuccess && response.data) {
            onSuccess(response.data)
          }
        }
      }
    )
  }

  // 带分页的列表请求（简化版）
  function useList(apiFunc) {
    const list = ref([])
    const total = ref(0)
    const { page, size, buildParams, handleResponse, setTotal } = usePagination()

    async function fetchList(params = {}) {
      loading.value = true
      try {
        const response = await apiFunc(buildParams(params))
        const data = handleResponse(response)
        list.value = data.content || data.list || []
        setTotal(data.totalElements || data.total || 0)
        return list.value
      } catch (err) {
        error.value = err
        throw err
      } finally {
        loading.value = false
      }
    }

    function reset() {
      list.value = []
      total.value = 0
      page.value = 1
    }

    return {
      list,
      total,
      page,
      size,
      loading,
      error,
      fetchList,
      reset
    }
  }

  return {
    loading,
    error,
    request,
    get,
    post,
    put,
    remove,
    fetchList,
    useList
  }
}

// 重新导出 usePagination
export { usePagination } from './usePagination'

export default useApi
