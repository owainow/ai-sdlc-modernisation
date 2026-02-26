import { useQuery } from '@tanstack/react-query';
import { apiGet } from '../lib/api-client';
import type { CustomerBillResponse, MonthlySummaryResponse, RevenueSummaryResponse } from '../types/reports';

export function useCustomerBill(customerId: string | null) {
  return useQuery({
    queryKey: ['reports', 'customer-bill', customerId],
    queryFn: () => apiGet<CustomerBillResponse>('/reports/customer-bill', { customerId }),
    enabled: !!customerId,
  });
}

export function useMonthlySummary(year: number, month: number) {
  return useQuery({
    queryKey: ['reports', 'monthly-summary', year, month],
    queryFn: () => apiGet<MonthlySummaryResponse>('/reports/monthly-summary', { year, month }),
  });
}

export function useRevenueSummary() {
  return useQuery({
    queryKey: ['reports', 'revenue-summary'],
    queryFn: () => apiGet<RevenueSummaryResponse>('/reports/revenue-summary'),
  });
}
