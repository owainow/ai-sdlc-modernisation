export interface Customer {
  id: string;
  name: string;
  email: string | null;
  address: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCustomerRequest {
  name: string;
  email?: string;
  address?: string;
}

export interface UpdateCustomerRequest {
  name: string;
  email?: string;
  address?: string;
}
