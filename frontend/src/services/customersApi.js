import { fetchApi } from './http';

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
