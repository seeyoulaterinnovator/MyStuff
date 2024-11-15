import axios from 'axios';

export const baseApiConfig = {
  baseURL: '/auth/realms/user/',
};

export const api = axios.create(baseApiConfig);
