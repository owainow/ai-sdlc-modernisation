import { useState } from 'react';
import { useCategories, useCreateCategory, useUpdateCategory, useDeleteCategory } from '../services/billingApi';
import { formatCurrency, formatDateTime } from '../lib/formatters';
import type { BillingCategory, CreateBillingCategoryRequest } from '../types/billing';

export default function BillingCategories() {
  const [showForm, setShowForm] = useState(false);
  const [editingCategory, setEditingCategory] = useState<BillingCategory | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<BillingCategory | null>(null);

  const { data, isLoading } = useCategories();
  const createMutation = useCreateCategory();
  const updateMutation = useUpdateCategory();
  const deleteMutation = useDeleteCategory();

  const categories = data?.data ?? [];

  const handleSubmit = async (formData: CreateBillingCategoryRequest) => {
    if (editingCategory) {
      await updateMutation.mutateAsync({ id: editingCategory.id, data: formData });
    } else {
      await createMutation.mutateAsync(formData);
    }
    setShowForm(false);
    setEditingCategory(null);
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
        <h1 className="text-2xl font-bold text-slate-900">Billing Categories</h1>
        <button
          onClick={() => { setEditingCategory(null); setShowForm(true); }}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
        >
          + Add Category
        </button>
      </div>

      {isLoading ? (
        <div className="text-center py-8 text-slate-500">Loading...</div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Name</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Description</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Hourly Rate</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Created</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {categories.map(category => (
                <tr key={category.id} className="hover:bg-slate-50">
                  <td className="px-6 py-4 whitespace-nowrap font-medium text-slate-900">{category.name}</td>
                  <td className="px-6 py-4 text-slate-500">{category.description ?? '-'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-slate-900 font-mono">{formatCurrency(category.hourlyRate)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-slate-500">{formatDateTime(category.createdAt)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-right space-x-2">
                    <button onClick={() => { setEditingCategory(category); setShowForm(true); }} className="text-blue-600 hover:text-blue-800">Edit</button>
                    <button onClick={() => setDeleteConfirm(category)} className="text-red-600 hover:text-red-800">Delete</button>
                  </td>
                </tr>
              ))}
              {categories.length === 0 && (
                <tr><td colSpan={5} className="px-6 py-8 text-center text-slate-500">No categories found</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {showForm && (
        <CategoryFormModal
          category={editingCategory}
          onSubmit={handleSubmit}
          onClose={() => { setShowForm(false); setEditingCategory(null); }}
          isLoading={createMutation.isPending || updateMutation.isPending}
        />
      )}

      {deleteConfirm && (
        <DeleteConfirmModal
          name={deleteConfirm.name}
          onConfirm={handleDelete}
          onCancel={() => setDeleteConfirm(null)}
          isLoading={deleteMutation.isPending}
          error={deleteMutation.error ? 'Cannot delete: category may have associated billable hours.' : undefined}
        />
      )}
    </div>
  );
}

function CategoryFormModal({ category, onSubmit, onClose, isLoading }: {
  category: BillingCategory | null;
  onSubmit: (data: CreateBillingCategoryRequest) => Promise<void>;
  onClose: () => void;
  isLoading: boolean;
}) {
  const [name, setName] = useState(category?.name ?? '');
  const [hourlyRate, setHourlyRate] = useState(category?.hourlyRate?.toString() ?? '');
  const [description, setDescription] = useState(category?.description ?? '');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) { setError('Name is required'); return; }
    const rate = parseFloat(hourlyRate);
    if (isNaN(rate) || rate < 0.01) { setError('Hourly rate must be at least $0.01'); return; }
    try {
      await onSubmit({ name: name.trim(), hourlyRate: rate, description: description.trim() || undefined });
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { errors?: Array<{ detail?: string }> } } };
      setError(axiosErr.response?.data?.errors?.[0]?.detail ?? 'An error occurred');
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
        <h2 className="text-lg font-bold mb-4">{category ? 'Edit Category' : 'Add Category'}</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && <div className="text-red-600 text-sm bg-red-50 p-2 rounded">{error}</div>}
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Name *</label>
            <input type="text" value={name} onChange={e => setName(e.target.value)} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required maxLength={200} />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Hourly Rate *</label>
            <input type="number" step="0.01" min="0.01" value={hourlyRate} onChange={e => setHourlyRate(e.target.value)} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
            <input type="text" value={description} onChange={e => setDescription(e.target.value)} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" maxLength={500} />
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
        <h2 className="text-lg font-bold mb-2">Delete Category</h2>
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
