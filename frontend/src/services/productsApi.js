import { fetchApi } from './http';

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
