import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiGet, apiPost, apiPut, apiDelete } from '../lib/api-client';
import type { User, CreateUserRequest, UpdateUserRequest } from '../types/user';

export function useUsers(page = 0, size = 20, search?: string) {
  return useQuery({
    queryKey: ['users', page, size, search],
    queryFn: () => apiGet<User[]>('/users', { page, size, search, sort: 'createdAt', direction: 'desc' }),
  });
}

export function useUser(id: string) {
  return useQuery({
    queryKey: ['users', id],
    queryFn: () => apiGet<User>(`/users/${id}`),
    enabled: !!id,
  });
}

export function useUserCount() {
  return useQuery({
    queryKey: ['users', 'count'],
    queryFn: () => apiGet<number>('/users/count'),
  });
}

export function useCreateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateUserRequest) => apiPost<User>('/users', data),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['users'] }); },
  });
}

export function useUpdateUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateUserRequest }) => apiPut<User>(`/users/${id}`, data),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['users'] }); },
  });
}

export function useDeleteUser() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiDelete<void>(`/users/${id}`),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['users'] }); },
  });
}
