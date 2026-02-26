export interface CustomerBillLineItem {
  id: string;
  userName: string;
  categoryName: string;
  hours: number;
  rate: number;
  lineTotal: number;
  dateLogged: string;
  note: string | null;
}

export interface CustomerBillResponse {
  customerId: string;
  customerName: string;
  lineItems: CustomerBillLineItem[];
  totalHours: number;
  totalRevenue: number;
}

export interface MonthlySummaryRow {
  customerId: string;
  customerName: string;
  totalHours: number;
  totalRevenue: number;
}

export interface MonthlySummaryResponse {
  year: number;
  month: number;
  customers: MonthlySummaryRow[];
  grandTotalHours: number;
  grandTotalRevenue: number;
}

export interface RevenueSummaryByCustomer {
  customerId: string;
  customerName: string;
  totalHours: number;
  totalRevenue: number;
  averageRate: number;
}

export interface RevenueSummaryByCategory {
  categoryId: string;
  categoryName: string;
  hourlyRate: number;
  totalHours: number;
  totalRevenue: number;
}

export interface RevenueSummaryResponse {
  byCustomer: RevenueSummaryByCustomer[];
  byCategory: RevenueSummaryByCategory[];
}
