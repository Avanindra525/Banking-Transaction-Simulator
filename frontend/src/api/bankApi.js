import apiClient from "./client";

export const authApi = {
  signup: (payload) => apiClient.post("/auth/signup", payload),
  login: (payload) => apiClient.post("/auth/login", payload)
};

export const accountApi = {
  createAccount: (payload) => apiClient.post("/accounts", payload),
  getAccountsByUser: (userId) => apiClient.get(`/accounts?userId=${userId}`),
  getAccountById: (accountId, userId) => apiClient.get(`/accounts/${accountId}?userId=${userId}`)
};

export const transactionApi = {
  process: (payload, userId) => apiClient.post(`/api/transactions/process?userId=${userId}`, payload),
  historyByUser: (userId) => apiClient.get(`/api/transactions/user/${userId}`),
  historyByAccount: (accountId, userId) => apiClient.get(`/api/transactions/account/${accountId}?userId=${userId}`)
};

export const alertApi = {
  getAlertsByAccountId: (accountId, userId) => apiClient.get(`/alerts/${accountId}?userId=${userId}`),
  getAlertsByUser: (userId) => apiClient.get(`/alerts/user/${userId}`)
};

export const reportApi = {
  getReport: ({ userId, type = "ALL", fromDate = "", toDate = "" }) => {
    const params = new URLSearchParams({ userId: String(userId), type });
    if (fromDate) params.set("fromDate", fromDate);
    if (toDate) params.set("toDate", toDate);
    return apiClient.get(`/api/reports/transactions?${params.toString()}`);
  },
  downloadCsv: ({ userId, type = "ALL", fromDate = "", toDate = "" }) => {
    const params = new URLSearchParams({ userId: String(userId), type });
    if (fromDate) params.set("fromDate", fromDate);
    if (toDate) params.set("toDate", toDate);
    return apiClient.get(`/api/reports/transactions/csv?${params.toString()}`, { responseType: "blob" });
  }
};

export const emailApi = {
  send: (payload) => apiClient.post("/email/send", payload)
};
