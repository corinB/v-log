import axios, { AxiosInstance, AxiosError } from 'axios';
import { ApiResponse } from '../types';

const API_BASE_URL = '/api/v1';

export const apiClient: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 에러 타입 정의
export interface ApiError {
  message: string;
  status?: number;
  data?: any;
}

// API 에러 처리 헬퍼
export const handleApiError = (error: unknown): ApiError => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiResponse<any>>;
    return {
      message: axiosError.response?.data?.message || axiosError.message || '알 수 없는 오류가 발생했습니다.',
      status: axiosError.response?.status,
      data: axiosError.response?.data,
    };
  }
  return {
    message: error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.',
  };
};

// API 응답 래퍼
export const unwrapApiResponse = <T>(response: { data: ApiResponse<T> }): T => {
  return response.data.data;
};

