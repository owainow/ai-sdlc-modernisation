import { useState } from 'react';
import { useCustomers } from '../services/customerApi';
import { useUsers } from '../services/userApi';
import { useCategories, useBillableHours, useCreateBillableHour, useDeleteBillableHour } from '../services/billingApi';
import { formatCurrency, formatDate } from '../lib/formatters';

function isWeekend(dateStr: string): boolean {
  const d = new Date(dateStr + 'T00:00:00');
  return d.getDay() === 0 || d.getDay() === 6;
}

export default function LogHours() {
  const [customerId, setCustomerId] = useState('');
  const [userId, setUserId] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [hours, setHours] = useState('');
  const [dateLogged, setDateLogged] = useState(new Date().toISOString().slice(0, 10));
  const [note, setNote] = useState('');
  const [formError, setFormError] = useState('');

  const [filterCustomerId, setFilterCustomerId] = useState('');
  const [filterUserId, setFilterUserId] = useState('');
  const [page, setPage] = useState(0);

  const { data: customersData } = useCustomers(0, 100);
  const { data: usersData } = useUsers(0, 100);
  const { data: categoriesData } = useCategories();
  const { data: hoursData, isLoading } = useBillableHours(page, 20, {
    customerId: filterCustomerId || undefined,
    userId: filterUserId || undefined,
  });
  const createMutation = useCreateBillableHour();
  const deleteMutation = useDeleteBillableHour();

  const customers = customersData?.data ?? [];
  const users = usersData?.data ?? [];
  const categories = categoriesData?.data ?? [];
  const billableHours = hoursData?.data ?? [];
  const meta = hoursData?.meta;

  const selectedCategory = categories.find(c => c.id === categoryId);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    if (!customerId || !userId || !categoryId) { setFormError('Customer, user, and category are required'); return; }
    const h = parseFloat(hours);
    if (isNaN(h) || h <= 0 || h > 24) { setFormError('Hours must be between 0.01 and 24'); return; }
    if (!dateLogged) { setFormError('Date is required'); return; }
    try {
      await createMutation.mutateAsync({
        customerId,
        userId,
        categoryId,
        hours: h,
        dateLogged,
        note: note.trim() || undefined,
      });
      setHours('');
      setNote('');
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { errors?: Array<{ detail?: string }> } } };
      setFormError(axiosErr.response?.data?.errors?.[0]?.detail ?? 'An error occurred');
    }
  };

  const handleDeleteHour = async (id: string) => {
    if (window.confirm('Delete this billable hour entry?')) {
      await deleteMutation.mutateAsync(id);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900 mb-6">Log Hours</h1>

      {/* Entry Form */}
      <div className="bg-white rounded-lg shadow p-6 mb-8">
        <h2 className="text-lg font-semibold mb-4">New Entry</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && <div className="text-red-600 text-sm bg-red-50 p-2 rounded">{formError}</div>}

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Customer *</label>
              <select value={customerId} onChange={e => setCustomerId(e.target.value)} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                <option value="">Select customer...</option>
                {customers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">User *</label>
              <select value={userId} onChange={e => setUserId(e.target.value)} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                <option value="">Select user...</option>
                {users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Category *</label>
              <select value={categoryId} onChange={e => setCategoryId(e.target.value)} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required>
                <option value="">Select category...</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name} ({formatCurrency(c.hourlyRate)}/hr)</option>)}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Hours *</label>
              <input type="number" step="0.25" min="0.25" max="24" value={hours} onChange={e => setHours(e.target.value)} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required />
              {selectedCategory && hours && (
                <p className="text-sm text-slate-500 mt-1">Estimated: {formatCurrency(parseFloat(hours || '0') * selectedCategory.hourlyRate)}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Date *</label>
              <input type="date" value={dateLogged} onChange={e => setDateLogged(e.target.value)} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" required />
              {dateLogged && isWeekend(dateLogged) && (
                <p className="text-sm text-amber-600 bg-amber-50 px-2 py-1 rounded mt-1">⚠ Weekend date selected</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Note</label>
              <textarea value={note} onChange={e => setNote(e.target.value)} rows={1} className="w-full px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" maxLength={1000} />
            </div>
          </div>

          <div className="flex justify-end">
            <button type="submit" disabled={createMutation.isPending} className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 disabled:opacity-50 transition-colors">
              {createMutation.isPending ? 'Saving...' : 'Log Hours'}
            </button>
          </div>
        </form>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-4 mb-4">
        <select value={filterCustomerId} onChange={e => { setFilterCustomerId(e.target.value); setPage(0); }} className="px-3 py-2 border border-slate-300 rounded-md text-sm">
          <option value="">All Customers</option>
          {customers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select value={filterUserId} onChange={e => { setFilterUserId(e.target.value); setPage(0); }} className="px-3 py-2 border border-slate-300 rounded-md text-sm">
          <option value="">All Users</option>
          {users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
        </select>
      </div>

      {/* Table */}
      {isLoading ? (
        <div className="text-center py-8 text-slate-500">Loading...</div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Date</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Customer</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">User</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Category</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Hours</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Rate</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Total</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {billableHours.map(bh => (
                <tr key={bh.id} className="hover:bg-slate-50">
                  <td className="px-6 py-4 whitespace-nowrap text-slate-900">{formatDate(bh.dateLogged)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-slate-500 font-mono text-xs">{bh.customerId.slice(0, 8)}…</td>
                  <td className="px-6 py-4 whitespace-nowrap text-slate-500 font-mono text-xs">{bh.userId.slice(0, 8)}…</td>
                  <td className="px-6 py-4 whitespace-nowrap text-slate-500 font-mono text-xs">{bh.categoryId.slice(0, 8)}…</td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-slate-900">{bh.hours}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-slate-500 font-mono">{formatCurrency(bh.rateSnapshot)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-slate-900 font-mono font-medium">{formatCurrency(bh.lineTotal)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-right">
                    <button onClick={() => handleDeleteHour(bh.id)} className="text-red-600 hover:text-red-800">Delete</button>
                  </td>
                </tr>
              ))}
              {billableHours.length === 0 && (
                <tr><td colSpan={8} className="px-6 py-8 text-center text-slate-500">No billable hours found</td></tr>
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
    </div>
  );
}
