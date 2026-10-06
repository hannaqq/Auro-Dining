const getComboPage = (params) => {
  return $axios({
    url: '/admin/combo/page',
    method: 'get',
    params
  })
}

const deleteCombo = (ids) => {
  return $axios({
    url: '/admin/combo',
    method: 'delete',
    params: { ids }
  })
}

const editCombo = (params) => {
  return $axios({
    url: '/admin/combo',
    method: 'put',
    data: { ...params }
  })
}

const addCombo = (params) => {
  return $axios({
    url: '/admin/combo',
    method: 'post',
    data: { ...params }
  })
}

const queryComboById = (id) => {
  return $axios({
    url: `/admin/combo/${id}`,
    method: 'get'
  })
}

const comboStatusByStatus = (params) => {
  return $axios({
    url: `/admin/combo/status/${params.status}`,
    method: 'post',
    params: { ids: params.ids }
  })
}
