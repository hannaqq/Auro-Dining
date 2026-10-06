const getDishPage = (params) => {
  return $axios({
    url: '/admin/dish/page',
    method: 'get',
    params
  })
}

const deleteDish = (ids) => {
  return $axios({
    url: '/admin/dish',
    method: 'delete',
    params: { ids }
  })
}

const editDish = (params) => {
  return $axios({
    url: '/admin/dish',
    method: 'put',
    data: { ...params }
  })
}

const addDish = (params) => {
  return $axios({
    url: '/admin/dish',
    method: 'post',
    data: { ...params }
  })
}

const queryDishById = (id) => {
  return $axios({
    url: `/admin/dish/${id}`,
    method: 'get'
  })
}

const getCategoryList = (params) => {
  return $axios({
    url: '/category/list',
    method: 'get',
    params
  })
}

const queryDishList = (params) => {
  return $axios({
    url: '/dish/list',
    method: 'get',
    params
  })
}

const commonDownload = (params) => {
  return $axios({
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
    },
    url: '/common/download',
    method: 'get',
    params
  })
}

const dishStatusByStatus = (params) => {
  return $axios({
    url: `/admin/dish/status/${params.status}`,
    method: 'post',
    params: { ids: params.id }
  })
}
