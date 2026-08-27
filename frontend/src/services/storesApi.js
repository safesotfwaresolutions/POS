// Auto-registro self-service del local del ADMINISTRATOR autenticado.
// Distinto de services/backoffice/storesApi.js, que es 100% para SUPER_ADMIN.
import { fetchApi } from './http';

export async function registerOwnStoreApi(payload) {
  return fetchApi('/stores', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function getOwnStoreApi() {
  return fetchApi('/stores/me');
}
