import { fetchApi, newIdempotencyKey } from './http';

export async function getSalesApi(page = 0, size = 20) {
  const data = await fetchApi(`/sales?page=${page}&size=${size}`);
  return data.content || data || [];
}

export async function createSaleApi(saleCommand, idempotencyKey) {
  // La Idempotency-Key evita ventas duplicadas si el request se reintenta (doble clic,
  // reintento de red o renovación 401). El mismo valor se reenvía en cada reintento.
  const key = idempotencyKey || newIdempotencyKey();
  return await fetchApi('/sales', {
    method: 'POST',
    headers: { 'Idempotency-Key': key },
    body: JSON.stringify({
      customerId: saleCommand.customerId || null,
      items: saleCommand.items.map(item => ({
        productId: item.productId,
        quantity: item.quantity,
        unitPrice: item.unitPrice
      })),
      cashReceived: saleCommand.cashReceived,
      paymentMethod: saleCommand.paymentMethod || 'CASH'
    }),
  });
}
