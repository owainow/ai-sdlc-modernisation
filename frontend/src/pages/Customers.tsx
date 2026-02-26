import { useState } from 'react';
import { useCustomers, useCreateCustomer, useUpdateCustomer, useDeleteCustomer } from '../services/customerApi';
import { formatDateTime } from '../lib/formatters';
import type { Customer, CreateCustomerRequest } from '../types/customer';

export default function Customers() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<Customer | null>(null);
  
  const { data, isLoading } = useCustomers(page, 20, search || undefined);
  const createMutation = useCreateCustomer();
  const updateMutation = useUpdateCustomer();
  const deleteMutation = useDeleteCustomer();
  
  const customers = data?.data ?? [];
  const meta = data?.meta;

  const handleSubmit = async (formData: CreateCustomerRequest) => {
    if (editingCustomer) {
      await updateMutation.mutateAsync({ id: editingCustomer.id, data: formData });
    } else {
      await createMutation.mutateAsync(formData);
    }
    setShowForm(false);
    setEditingCustomer(null);
  };

  const handleDelete = async () => {
    if (deleteConfirm) {
      await deleteMutation.mutateAsync(deleteConfirm.id);
      setDeleteConfirm(null);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Customers</h1>
        <button
          onClick={() => { setEditingCustomer(null); setShowForm(true); }}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
        >
          + Add Customer
        </button>
      </div>

      <div className="mb-4">
        <input
          type="text"
          placeholder="Search customers..."
          value={search}
          onChange={e => { setSearch(e.target.value); setPage(0); }}
          className="w-full max-w-md px-4 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {isLoading ? (
        <div className="text-center py-8 text-slate-500">Loading...</div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Name</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Email</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Address</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Created</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {customers.map(customer => (
                <tr key={customer.id} className="hover:bg-slate-50">
                  <td className="px-6 py-4 whitespace-nowrap font-medium text-slate-900">{customer.name}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-slate-500">{customer.email ?? '-'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-slate-500">{customer.address ?? '-'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-slate-500">{formatDateTime(customer.createdAt)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-right space-x-2">
                    <button onClick={() => { setEditingCustomer(customer); setShowForm(true); }} className="text-blue-600 hover:text-blue-800">Edit</button>
                    <button onClick={() => setDeleteConfirm(customer)} className="text-red-600 hover:text-red-800">Delete</button>
                  </td>
                </tr>
              ))}
              {customers.length === 0 && (
                <tr><td colSpan={5} className="px-6 py-8 text-center text-slate-500">No customers found</td></tr>
              )}
            </tbody>
          </table>
          
          {meta && meta.totalPages > 1 && (
            <div className="px-6 py-3 bg-slate-50 flex items-center justify-between">
              <span className="text-sm text-slate-500">
                Page {meta.page + 1} of {meta.totalPages} ({meta.totalItems} total)
              </span>
              <div className="flex gap-2">
                <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} className="px-3 py-1 text-sm border rounded disabled:opacity-50">Previous</button>
                <button onClick={() => setPage(p => p + 1)} disabled={page >= meta.totalPages - 1} className="px-3 py-1 text-sm border rounded disabled:opacity-50">Next</button>
              </div>
            </div>
          )}
        </div>
      )}

      {showForm && (
        <CustomerFormModal
          customer={editingCustomer}
          onSubmit={handleSubmit}
          onClose={() => { setShowForm(false); setEditingCustomer(null); }}
          isLoading={createMutation.isPending || updateMutation.isPending}
        />
      )}

      {deleteConfirm && (
        <DeleteConfirmModal
          name={deleteConfirm.name}
          onConfirm={handleDelete}
          onCancel={() => setDeleteConfirm(null)}
          isLoading={deleteMutation.isPending}
          error={deleteMutation.error ? 'Cannot delete: customer may have associated billable hours.' : undefined}
        />
      )}
    </div>
  );
}

function CustomerFormModal({ customer, onSubmit, onClose, isLoading }: {
  customer: Customer | null;
  onSubmit: (data: CreateCustomerRequest) => Promise<void>;
  onClose: () => void;
  isLoading: boolean;
}) {
  const [name, setName] = useState(customer?.name ?? '');
  const [email, setEmail] = useState(customer?.email ?? '');
  const [address, setAddress] = useState(customer?.address ?? '');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) { setError('Name is required'); return; }
    try {
      await onSubmit({ name: name.trim(), email: email.trim() || undefined, address: address.trim() || undefined });
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { errors?: Array<{ detail?: string }> } } };
      setError(axiosErr.response?.data?.errors?.[0]?.detail ?? 'An error occurred');
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
        <h2 className="text-lg font-bold mb-4">{customer ? 'Edit Customer' : 'Add Customer'}</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && <div className="text-red-600 text-sm bg-red-50 p-2 rounded">{error}</div>}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Name *</label>
            <input type="text" value={name} onChange={e => setName(e.target.value)} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required maxLength={200} />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Address</label>
            <input type="text" value={address} onChange={e => setAddress(e.target.value)} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" maxLength={500} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose} className="px-4 py-2 text-slate-700 border border-slate-300 rounded-md hover:bg-slate-50">Cancel</button>
            <button type="submit" disabled={isLoading} className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50">{isLoading ? 'Saving...' : 'Save'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}

function DeleteConfirmModal({ name, onConfirm, onCancel, isLoading, error }: {
  name: string; onConfirm: () => void; onCancel: () => void; isLoading: boolean; error?: string;
}) {
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl p-6 w-full max-w-sm">
        <h2 className="text-lg font-bold mb-2">Delete Customer</h2>
        <p className="text-slate-600 mb-4">Are you sure you want to delete <strong>{name}</strong>? This action cannot be undone.</p>
        {error && <div className="text-red-600 text-sm bg-red-50 p-2 rounded mb-3">{error}</div>}
        <div className="flex justify-end gap-3">
          <button onClick={onCancel} className="px-4 py-2 text-slate-700 border border-slate-300 rounded-md hover:bg-slate-50">Cancel</button>
          <button onClick={onConfirm} disabled={isLoading} className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50">{isLoading ? 'Deleting...' : 'Delete'}</button>
        </div>
      </div>
    </div>
  );
}
