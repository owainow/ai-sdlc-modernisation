import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiGet, apiPost, apiPut, apiDelete } from '../lib/api-client';
import type { BillingCategory, CreateBillingCategoryRequest, UpdateBillingCategoryRequest, BillableHour, CreateBillableHourRequest, UpdateBillableHourRequest } from '../types/billing';

// Billing Categories
export function useCategories() {
  return useQuery({
    queryKey: ['categories'],
    queryFn: () => apiGet<BillingCategory[]>('/billing/categories', { sort: 'name', direction: 'asc' }),
  });
}

export function useCreateCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateBillingCategoryRequest) => apiPost<BillingCategory>('/billing/categories', data),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['categories'] }); },
  });
}

export function useUpdateCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateBillingCategoryRequest }) => apiPut<BillingCategory>(`/billing/categories/${id}`, data),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['categories'] }); },
  });
}

export function useDeleteCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiDelete<void>(`/billing/categories/${id}`),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['categories'] }); },
  });
}

// Billable Hours
export function useBillableHours(page = 0, size = 20, filters?: { customerId?: string; userId?: string; categoryId?: string; fromDate?: string; toDate?: string }) {
  return useQuery({
    queryKey: ['billable-hours', page, size, filters],
    queryFn: () => apiGet<BillableHour[]>('/billing/hours', { page, size, sort: 'dateLogged', direction: 'desc', ...filters }),
  });
}

export function useCreateBillableHour() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateBillableHourRequest) => apiPost<BillableHour>('/billing/hours', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['billable-hours'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useUpdateBillableHour() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateBillableHourRequest }) => apiPut<BillableHour>(`/billing/hours/${id}`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['billable-hours'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useDeleteBillableHour() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiDelete<void>(`/billing/hours/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['billable-hours'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

// Dashboard
export function useRevenue() {
  return useQuery({
    queryKey: ['dashboard', 'revenue'],
    queryFn: () => apiGet<{ totalRevenue: number }>('/billing/dashboard/revenue'),
    refetchInterval: 30000,
  });
}
