function loginApi(data) {
  return $axios({
    'url': '/admin/employee/login',
    'method': 'post',
    data
  })
}

function logoutApi(){
  return $axios({
    'url': '/admin/employee/logout',
    'method': 'post',
  })
}
