// Real API client — Zero mockups, strictly real data operations

const API_BASE_URL = '/api/v1';

// Genera una clave de idempotencia (UUID). Se usa en operaciones no idempotentes
// (ventas, compras) para que un reenvío no cree registros duplicados en el backend.
export function newIdempotencyKey() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return `idem-${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

let authToken = localStorage.getItem('jwt_token') || '';
let refreshToken = localStorage.getItem('refresh_token') || '';

export function getAuthToken() {
  return authToken;
}

export function setAuthToken(token) {
  authToken = token || '';
  if (token) {
    localStorage.setItem('jwt_token', token);
  } else {
    localStorage.removeItem('jwt_token');
  }
}

export function setRefreshToken(token) {
  refreshToken = token || '';
  if (token) {
    localStorage.setItem('refresh_token', token);
  } else {
    localStorage.removeItem('refresh_token');
  }
}

// Deduplica renovaciones concurrentes: varias peticiones que reciben 401 a la vez
// comparten una única promesa de refresh.
let refreshPromise = null;

async function refreshAccessToken() {
  if (refreshPromise) return refreshPromise;

  refreshPromise = (async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(refreshToken ? { refreshToken } : {}),
      });
      if (!response.ok) return false;

      const data = await response.json();
      if (data && data.token) setAuthToken(data.token);
      if (data && data.refreshToken) setRefreshToken(data.refreshToken);
      return !!(data && data.token);
    } catch {
      return false;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}

// Core HTTP Fetch Wrapper
export async function fetchApi(endpoint, options = {}, _retried = false) {
  const headers = {
    'Content-Type': 'application/json',
    ...(authToken ? { Authorization: `Bearer ${authToken}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
    credentials: 'include',
  });

  if (response.status === 401) {
    // Evita bucles: no intentar renovar el propio endpoint de refresh.
    if (!_retried && endpoint !== '/auth/refresh') {
      const refreshed = await refreshAccessToken();
      if (refreshed) {
        return fetchApi(endpoint, options, true);
      }
    }
    setAuthToken('');
    setRefreshToken('');
    throw new Error('Sesión expirada o credenciales no válidas.');
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `Error HTTP ${response.status}`);
  }

  if (response.status === 204) return true;
  return await response.json();
}

// ----------------------------------------------------
// AUTH API
// ----------------------------------------------------
export async function loginApi(username, password) {
  const data = await fetchApi('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
  if (data && data.token) {
    setAuthToken(data.token);
  }
  if (data && data.refreshToken) {
    setRefreshToken(data.refreshToken);
  }
  return data;
}

export async function logoutApi() {
  try {
    await fetchApi('/auth/logout', {
      method: 'POST',
      body: JSON.stringify(refreshToken ? { refreshToken } : {}),
    });
  } catch (e) {
    // Ignore on logout
  } finally {
    setAuthToken('');
    setRefreshToken('');
  }
}

// ----------------------------------------------------
// PRODUCTS API
// ----------------------------------------------------
export async function getProductsApi(search = '', categoryId = null, page = 0, size = 50) {
  const params = new URLSearchParams({ page, size });
  if (search) params.append('search', search);
  if (categoryId) params.append('categoryId', categoryId);

  const data = await fetchApi(`/products?${params.toString()}`);
  return data.content || data || [];
}

export async function createProductApi(productData) {
  return await fetchApi('/products', {
    method: 'POST',
    body: JSON.stringify({
      name: productData.name,
      description: productData.description || productData.name,
      internalCode: productData.internalCode,
      barcode: productData.barcode,
      categoryId: productData.categoryId || 1,
      purchasePrice: parseFloat(productData.purchasePrice || productData.price * 0.7),
      salePrice: parseFloat(productData.price || productData.salePrice),
      quantityAvailable: parseInt(productData.stock || productData.quantityAvailable || 0),
      minStock: parseInt(productData.minStock || 5),
    }),
  });
}

export async function updateProductApi(id, productData) {
  return await fetchApi(`/products/${id}`, {
    method: 'PUT',
    body: JSON.stringify(productData),
  });
}

export async function deleteProductApi(id) {
  return await fetchApi(`/products/${id}`, { method: 'DELETE' });
}

// ----------------------------------------------------
// CATEGORIES API
// ----------------------------------------------------
export async function getCategoriesApi() {
  const data = await fetchApi('/categories');
  return data.content || data || [];
}

export async function createCategoryApi(name, description = '') {
  return await fetchApi('/categories', {
    method: 'POST',
    body: JSON.stringify({ name, description }),
  });
}

// ----------------------------------------------------
// CUSTOMERS API
// ----------------------------------------------------
export async function getCustomersApi() {
  const data = await fetchApi('/customers');
  return data.content || data || [];
}

export async function createCustomerApi(customerData) {
  return await fetchApi('/customers', {
    method: 'POST',
    body: JSON.stringify({
      fullName: customerData.name || customerData.fullName,
      identification: customerData.identification,
      email: customerData.email,
      phone: customerData.phone,
      address: customerData.address || 'Ciudad'
    }),
  });
}

// ----------------------------------------------------
// SALES API
// ----------------------------------------------------
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
      cashReceived: saleCommand.cashReceived
    }),
  });
}

// ----------------------------------------------------
// INVENTORY & STOCK API
// ----------------------------------------------------
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

// ----------------------------------------------------
// SETTINGS API
// ----------------------------------------------------
export async function getSettingsApi() {
  return await fetchApi('/settings');
}

export async function updateSettingsApi(settingsData) {
  return await fetchApi('/settings', {
    method: 'PUT',
    body: JSON.stringify(settingsData),
  });
}

// ----------------------------------------------------
// BACKOFFICE API (SUPER_ADMIN — gestion de locales SaaS)
// ----------------------------------------------------
export async function getBackofficeMetricsApi() {
  return await fetchApi('/backoffice/metrics');
}

export async function getBackofficeStoresApi(status = '', q = '', page = 0, size = 20) {
  const params = new URLSearchParams({ page, size });
  if (status && status !== 'ALL') params.append('status', status);
  if (q) params.append('q', q);
  return await fetchApi(`/backoffice/stores?${params.toString()}`);
}

export async function getBackofficeStoreApi(id) {
  return await fetchApi(`/backoffice/stores/${id}`);
}

export async function createBackofficeStoreApi(storeData) {
  return await fetchApi('/backoffice/stores', {
    method: 'POST',
    body: JSON.stringify(storeData),
  });
}

export async function updateBackofficeStoreApi(id, storeData) {
  return await fetchApi(`/backoffice/stores/${id}`, {
    method: 'PUT',
    body: JSON.stringify(storeData),
  });
}

export async function changeBackofficeStoreStatusApi(id, status) {
  return await fetchApi(`/backoffice/stores/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  });
}

export async function verifyBackofficeStoreEmailApi(id) {
  return await fetchApi(`/backoffice/stores/${id}/verify-email`, {
    method: 'PATCH',
  });
}

export async function getBackofficeStoreDocumentsApi(storeId) {
  return await fetchApi(`/backoffice/stores/${storeId}/documents`);
}

export async function reviewBackofficeStoreDocumentApi(storeId, docId, status, rejectionReason = null) {
  return await fetchApi(`/backoffice/stores/${storeId}/documents/${docId}`, {
    method: 'PATCH',
    body: JSON.stringify({ status, rejectionReason }),
  });
}

export async function getBackofficeStoreCategoriesApi() {
  return await fetchApi('/backoffice/store-categories');
}

export async function createBackofficeStoreCategoryApi(name, description = '') {
  return await fetchApi('/backoffice/store-categories', {
    method: 'POST',
    body: JSON.stringify({ name, description }),
  });
}

export async function updateBackofficeStoreCategoryApi(id, name, description = '') {
  return await fetchApi(`/backoffice/store-categories/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ name, description }),
  });
}

export async function deleteBackofficeStoreCategoryApi(id) {
  return await fetchApi(`/backoffice/store-categories/${id}`, { method: 'DELETE' });
}

// ----------------------------------------------------
// BACKOFFICE API (SUPER_ADMIN — CMS de textos legales)
// ----------------------------------------------------
export async function getBackofficeLegalDocumentsApi() {
  return await fetchApi('/backoffice/legal-documents');
}

export async function getBackofficeLegalDocumentApi(id) {
  return await fetchApi(`/backoffice/legal-documents/${id}`);
}

export async function createBackofficeLegalDocumentApi(documentData) {
  return await fetchApi('/backoffice/legal-documents', {
    method: 'POST',
    body: JSON.stringify(documentData),
  });
}

export async function updateBackofficeLegalDocumentApi(id, documentData) {
  return await fetchApi(`/backoffice/legal-documents/${id}`, {
    method: 'PUT',
    body: JSON.stringify(documentData),
  });
}

export async function deleteBackofficeLegalDocumentApi(id) {
  return await fetchApi(`/backoffice/legal-documents/${id}`, { method: 'DELETE' });
}

export async function toggleBackofficeLegalDocumentPublishApi(id) {
  return await fetchApi(`/backoffice/legal-documents/${id}/publish`, { method: 'PATCH' });
}

// ----------------------------------------------------
// BACKOFFICE API (SUPER_ADMIN — Soporte y PQRs)
// ----------------------------------------------------
export async function getBackofficeSupportMetricsApi() {
  return await fetchApi('/backoffice/support/metrics');
}

export async function getBackofficeSupportTicketsApi({ type = '', status = '', priority = '', storeId = '', q = '', page = 0, size = 20 } = {}) {
  const params = new URLSearchParams({ page, size });
  if (type && type !== 'ALL') params.append('type', type);
  if (status && status !== 'ALL') params.append('status', status);
  if (priority && priority !== 'ALL') params.append('priority', priority);
  if (storeId) params.append('storeId', storeId);
  if (q) params.append('q', q);
  return await fetchApi(`/backoffice/support/tickets?${params.toString()}`);
}

export async function getBackofficeSupportTicketApi(id) {
  return await fetchApi(`/backoffice/support/tickets/${id}`);
}

export async function updateBackofficeSupportTicketApi(id, ticketData) {
  return await fetchApi(`/backoffice/support/tickets/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(ticketData),
  });
}
