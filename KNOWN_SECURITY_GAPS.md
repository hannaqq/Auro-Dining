# Known security test gaps

These cases are intentionally not part of the green CI suite yet because the
current implementation does not enforce the expected ownership checks.

## Address ownership

- A signed-in user should not be able to read, update, delete, or select another
  user's address by supplying its ID.
- Current affected endpoints include `GET /addressBook/{id}`,
  `PUT /addressBook`, `DELETE /addressBook`, and `PUT /addressBook/default`.
- Order submission should verify that `addressBookId` belongs to the current
  user before copying the address into an order.

## Order ownership

- `POST /order/again` should verify that the requested order belongs to the
  current user before copying its details into the shopping cart.

Once the ownership checks are implemented, add these cases as blocking
integration tests and remove them from this document.
