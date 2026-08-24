import { fetchApi, setAuthToken, setRefreshToken, getRefreshToken } from './http';

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
      body: JSON.stringify(getRefreshToken() ? { refreshToken: getRefreshToken() } : {}),
    });
  } catch (e) {
    // Ignore on logout
  } finally {
    setAuthToken('');
    setRefreshToken('');
  }
}
