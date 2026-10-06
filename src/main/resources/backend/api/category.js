const getCategoryPage = (params) => {
  return $axios({
    url: '/admin/category/page',
    method: 'get',
    params
  })
}

const queryCategoryById = (id) => {
  return $axios({
    url: `/admin/category/${id}`,
    method: 'get'
  })
}

const deleCategory = (id) => {
  return $axios({
    url: '/admin/category',
    method: 'delete',
    params: { ids }
  })
}

const editCategory = (params) => {
  return $axios({
    url: '/admin/category',
    method: 'put',
    data: { ...params }
  })
}

const addCategory = (params) => {
  return $axios({
    url: '/admin/category',
    method: 'post',
    data: { ...params }
  })
}
