import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  withCredentials: true,
});

// Use token-based routes by default; switch to /oauth/ for OAuth2 sessions
const PREFIX = import.meta.env.VITE_API_AUTH_PREFIX || '/token';

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('api_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      // Could redirect to login
      console.warn('Unauthorized request');
    }
    return Promise.reject(err);
  },
);

export { PREFIX };
export default api;
