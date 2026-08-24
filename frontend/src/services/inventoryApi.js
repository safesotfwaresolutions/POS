import { fetchApi, newIdempotencyKey } from './http';

export async function getStockReportApi(belowMinStock = false) {
  return await fetchApi(`/reports/stock?belowMinStock=${belowMinStock}`);
}

export async function createPurchaseApi(purchaseData, idempotencyKey) {
  const key = idempotencyKey || newIdempotencyKey();
  return await fetchApi('/purchases', {
    method: 'POST',
    headers: { 'Idempotency-Key': key },
    body: JSON.stringify(purchaseData),
  });
}
