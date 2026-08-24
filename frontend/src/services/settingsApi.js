import { fetchApi } from './http';

export async function getSettingsApi() {
  return await fetchApi('/settings');
}

export async function updateSettingsApi(settingsData) {
  return await fetchApi('/settings', {
    method: 'PUT',
    body: JSON.stringify(settingsData),
  });
}
