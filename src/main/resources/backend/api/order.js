const getOrderDetailPage = (params) => {
  return $axios({
    url: '/admin/order/page',
    method: 'get',
    params
  })
}

const queryOrderDetailById = (id) => {
  return $axios({
    url: `/admin/orderDetail/${id}`,
    method: 'get'
  })
}

const editOrderDetail = (params) => {
  return $axios({
    url: '/admin/order',
    method: 'put',
    data: { ...params }
  })
}
