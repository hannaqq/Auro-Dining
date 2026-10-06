function getMemberList (params) {
  return $axios({
    url: '/admin/employee/page',
    method: 'get',
    params
  })
}

function enableOrDisableEmployee (params) {
  return $axios({
    url: '/admin/employee',
    method: 'put',
    data: { ...params }
  })
}

function addEmployee (params) {
  return $axios({
    url: '/admin/employee',
    method: 'post',
    data: { ...params }
  })
}

function editEmployee (params) {
  return $axios({
    url: '/admin/employee',
    method: 'put',
    data: { ...params }
  })
}

function queryEmployeeById (id) {
  return $axios({
    url: `/admin/employee/${id}`,
    method: 'get'
  })
}
