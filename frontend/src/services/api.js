// API Service layer with integration to Spring Boot backend and fallback mock data for offline/demo mode

const API_BASE_URL = '/api/v1';

// Initial Mock Data reflecting real POS business domain
export const INITIAL_MOCK_DATA = {
  stats: {
    todaySales: 1420500,
    salesCount: 34,
    averageTicket: 41779,
    lowStockCount: 5,
    dianPendingCount: 2,
    rentability: 32.4
  },
  products: [
    { id: 1, name: 'Café Especial Molido 500g', barcode: '7701234567890', internalCode: 'PROD-001', category: 'Bebidas', price: 24500, stock: 42, minStock: 10, unit: 'Unidad' },
    { id: 2, name: 'Queso Campesino Bloque 1kg', barcode: '7701234567891', internalCode: 'PROD-002', category: 'Lácteos', price: 18000, stock: 8, minStock: 12, unit: 'Unidad' },
    { id: 3, name: 'Pan de Bono Horneado x10', barcode: '7701234567892', internalCode: 'PROD-003', category: 'Panadería', price: 12000, stock: 15, minStock: 8, unit: 'Bolsa' },
    { id: 4, name: 'Jugo Natural Naranja 1L', barcode: '7701234567893', internalCode: 'PROD-004', category: 'Bebidas', price: 8500, stock: 4, minStock: 10, unit: 'Botella' },
    { id: 5, name: 'Chocolate Corona x500g', barcode: '7701234567894', internalCode: 'PROD-005', category: 'Abarrotes', price: 9200, stock: 30, minStock: 5, unit: 'Unidad' },
    { id: 6, name: 'Arroz Roa Flor 1kg', barcode: '7701234567895', internalCode: 'PROD-006', category: 'Abarrotes', price: 4800, stock: 120, minStock: 25, unit: 'Unidad' },
    { id: 7, name: 'Aceite Gourmet 900ml', barcode: '7701234567896', internalCode: 'PROD-007', category: 'Abarrotes', price: 16500, stock: 3, minStock: 8, unit: 'Botella' },
    { id: 8, name: 'Galletas Club Social x12', barcode: '7701234567897', internalCode: 'PROD-008', category: 'Snacks', price: 7200, stock: 45, minStock: 15, unit: 'Caja' },
  ],
  categories: ['Todos', 'Bebidas', 'Lácteos', 'Panadería', 'Abarrotes', 'Snacks'],
  customers: [
    { id: 1, name: 'Cliente General (Mostrador)', identification: '222222222222', email: 'consumidorfinal@pos.com', phone: '3000000000', type: 'Consumidor Final' },
    { id: 2, name: 'Empresa Distribuidora SAS', identification: '900123456-1', email: 'facturacion@distribuidora.co', phone: '6013456789', type: 'Factura Electrónica' },
    { id: 3, name: 'María Fernanda Gómez', identification: '1020304050', email: 'mfgomez@gmail.com', phone: '3159876543', type: 'Factura Electrónica' },
  ],
  recentSales: [
    { id: 'FAC-1089', date: '2026-08-16 11:42 AM', customer: 'María Fernanda Gómez', itemsCount: 3, total: 54500, status: 'Completada', dianStatus: 'APROBADA' },
    { id: 'FAC-1088', date: '2026-08-16 11:15 AM', customer: 'Cliente General', itemsCount: 1, total: 12000, status: 'Completada', dianStatus: 'NO_REQUERIDA' },
    { id: 'FAC-1087', date: '2026-08-16 10:50 AM', customer: 'Empresa Distribuidora SAS', itemsCount: 8, total: 340000, status: 'Completada', dianStatus: 'EN_COLA' },
    { id: 'FAC-1086', date: '2026-08-16 10:12 AM', customer: 'Cliente General', itemsCount: 2, total: 33000, status: 'Completada', dianStatus: 'NO_REQUERIDA' },
  ],
  invoicingDian: [
    { id: 'FAC-1089', uuid: 'cufe-89f41b2a3c-2026', customer: 'María Fernanda Gómez', nit: '1020304050', date: '2026-08-16 11:42 AM', total: 54500, dianStatus: 'ACEPTADA_DIAN', responseCode: '200 - OK' },
    { id: 'FAC-1087', uuid: 'cufe-12a87b99c1-2026', customer: 'Empresa Distribuidora SAS', nit: '900123456-1', date: '2026-08-16 10:50 AM', total: 340000, dianStatus: 'PENDIENTE_REINTENTO', responseCode: '503 - Timeout Factus API' },
  ]
};

// Generic API helper
export async function fetchApi(endpoint, options = {}) {
  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });
    if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);
    return await response.json();
  } catch (error) {
    console.warn(`Backend endpoint ${endpoint} fallback to mock data:`, error.message);
    return null;
  }
}
