import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiGet, apiPost, apiPut, apiDelete } from '../lib/api-client';
import type { Customer, CreateCustomerRequest, UpdateCustomerRequest } from '../types/customer';

export function useCustomers(page = 0, size = 20, search?: string) {
  return useQuery({
    queryKey: ['customers', page, size, search],
    queryFn: () => apiGet<Customer[]>('/customers', { page, size, search, sort: 'createdAt', direction: 'desc' }),
  });
}

export function useCustomer(id: string) {
  return useQuery({
    queryKey: ['customers', id],
    queryFn: () => apiGet<Customer>(`/customers/${id}`),
    enabled: !!id,
  });
}

export function useCustomerCount() {
  return useQuery({
    queryKey: ['customers', 'count'],
    queryFn: () => apiGet<number>('/customers/count'),
  });
}

export function useCreateCustomer() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateCustomerRequest) => apiPost<Customer>('/customers', data),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['customers'] }); },
  });
}

export function useUpdateCustomer() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateCustomerRequest }) => apiPut<Customer>(`/customers/${id}`, data),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['customers'] }); },
  });
}

export function useDeleteCustomer() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiDelete<void>(`/customers/${id}`),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['customers'] }); },
  });
}
