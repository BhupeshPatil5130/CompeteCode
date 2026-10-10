import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || 'https://competecode.onrender.com';

let onUnauthorized = null;

export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler;
}

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry && !originalRequest.url.includes('/auth/login') && !originalRequest.url.includes('/auth/register')) {
      
      if (isRefreshing) {
        return new Promise(function(resolve, reject) {
          failedQueue.push({ resolve, reject });
        }).then(token => {
          originalRequest.headers['Authorization'] = 'Bearer ' + token;
          return api(originalRequest);
        }).catch(err => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        isRefreshing = false;
        onUnauthorized?.();
        return Promise.reject(error);
      }

      try {
        const { data } = await axios.post(`${API_BASE}/auth/refresh`, { refreshToken });
        localStorage.setItem('token', data.token);
        localStorage.setItem('refreshToken', data.refreshToken);
        
        api.defaults.headers.common['Authorization'] = 'Bearer ' + data.token;
        originalRequest.headers['Authorization'] = 'Bearer ' + data.token;
        
        processQueue(null, data.token);
        
        return api(originalRequest);
      } catch (err) {
        processQueue(err, null);
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        onUnauthorized?.();
        return Promise.reject(err);
      } finally {
        isRefreshing = false;
      }
    }
    
    return Promise.reject(error);
  }
);

export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

export const problemApi = {
  getAll: (params) => api.get('/problems', { params }),
  getById: (id) => api.get(`/problems/${id}`),
  getSamples: (id) => api.get(`/problems/${id}/samples`),
  getStarterCode: (id, language) => api.get(`/problems/${id}/starter-code`, { params: { language } }),
  getExplanation: (id) => api.get(`/problems/${id}/explain`),
  analyzeComplexity: (id, code, language) => api.post(`/problems/${id}/analyze-complexity`, { code, language }),
  getAcceptanceRate: (id) => api.get(`/problems/${id}/acceptance-rate`).catch(() => ({ data: { total: 0, accepted: 0, acceptanceRate: 0 } })),
};

export const submissionApi = {
  submit: (data) => api.post('/submissions', data),
  runCode: (data) => api.post('/submissions/run', data),
  getFeedback: (id) => api.get(`/submissions/${id}/feedback`),
  getMy: (params) => api.get('/submissions/my', { params }).catch(() => ({ data: [] })),
  getMyStats: () => api.get('/submissions/my/stats').catch(() => ({
    data: { totalSolved: 0, easySolved: 0, mediumSolved: 0, hardSolved: 0, streak: 0 }
  })),
  getHeatmap: () => api.get('/submissions/my/heatmap').catch(() => ({ data: {} })),
};

export const battleApi = {
  create: (data) => api.post('/battles', data),
  join: (roomCode) => api.post(`/battles/join/${roomCode}`),
  joinTeam: (battleId, teamId) => api.post(`/battles/${battleId}/teams/${teamId}/join`),
  start: (id) => api.post(`/battles/${id}/start`),
  complete: (id) => api.post(`/battles/${id}/complete`),
  getActive: () => api.get('/battles/active'),
  getById: (id) => api.get(`/battles/${id}`),
  getByRoom: (roomCode) => api.get(`/battles/room/${roomCode}`),
  getLeaderboard: (id) => api.get(`/battles/${id}/leaderboard`),
  getTeamLeaderboard: (id) => api.get(`/battles/${id}/teams`),
};

export const roadmapApi = {
  getAll: () => api.get('/roadmaps'),
  getById: (id) => api.get(`/roadmaps/${id}`),
  getProgress: (id) => api.get(`/roadmaps/${id}/progress`),
  markComplete: (roadmapId, problemId) =>
    api.post(`/roadmaps/${roadmapId}/problems/${problemId}/complete`),
};

export const leaderboardApi = {
  getGlobal: () => api.get('/leaderboard'),
};

export const profileApi = {
  get: () => api.get('/profile').catch(() => ({ data: null })),
  update: (data) => api.put('/profile', data),
};

export default api;
