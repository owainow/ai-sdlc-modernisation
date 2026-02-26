export interface BillingCategory {
  id: string;
  name: string;
  description: string | null;
  hourlyRate: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBillingCategoryRequest {
  name: string;
  hourlyRate: number;
  description?: string;
}

export interface UpdateBillingCategoryRequest {
  name: string;
  hourlyRate: number;
  description?: string;
}

export interface BillableHour {
  id: string;
  customerId: string;
  userId: string;
  categoryId: string;
  hours: number;
  rateSnapshot: number;
  dateLogged: string;
  note: string | null;
  lineTotal: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBillableHourRequest {
  customerId: string;
  userId: string;
  categoryId: string;
  hours: number;
  dateLogged: string;
  note?: string;
}

export interface UpdateBillableHourRequest {
  customerId: string;
  userId: string;
  categoryId: string;
  hours: number;
  dateLogged: string;
  note?: string;
}
