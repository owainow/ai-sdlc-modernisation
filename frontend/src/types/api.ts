export interface ApiResponse<T> {
  status: 'success' | 'error';
  data: T | null;
  errors: ProblemDetail[] | null;
  meta: PageMeta | null;
}

export interface PageMeta {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string | null;
}
