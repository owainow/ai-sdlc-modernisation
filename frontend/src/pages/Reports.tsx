import { useState } from 'react';
import { useCustomers } from '../services/customerApi';
import { useCustomerBill, useMonthlySummary, useRevenueSummary } from '../services/reportApi';
import { formatCurrency, formatDate } from '../lib/formatters';

type Tab = 'customer-bill' | 'monthly-summary' | 'revenue-summary';

export default function Reports() {
  const [activeTab, setActiveTab] = useState<Tab>('customer-bill');

  const tabs: { key: Tab; label: string }[] = [
    { key: 'customer-bill', label: 'Customer Bill' },
    { key: 'monthly-summary', label: 'Monthly Summary' },
    { key: 'revenue-summary', label: 'Revenue Summary' },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900 mb-6">Reports</h1>

      <div className="border-b border-slate-200 mb-6">
        <div className="flex gap-0">
          {tabs.map(tab => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
              className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
                activeTab === tab.key
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {activeTab === 'customer-bill' && <CustomerBillTab />}
      {activeTab === 'monthly-summary' && <MonthlySummaryTab />}
      {activeTab === 'revenue-summary' && <RevenueSummaryTab />}
    </div>
  );
}

function CustomerBillTab() {
  const [selectedCustomerId, setSelectedCustomerId] = useState<string | null>(null);
  const { data: customersData } = useCustomers(0, 100);
  const { data: billData, isLoading } = useCustomerBill(selectedCustomerId);

  const customers = customersData?.data ?? [];
  const bill = billData?.data;

  return (
    <div>
      <div className="mb-4">
        <label className="block text-sm font-medium text-slate-700 mb-1">Select Customer</label>
        <select
          value={selectedCustomerId ?? ''}
          onChange={e => setSelectedCustomerId(e.target.value || null)}
          className="w-full max-w-md px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Choose a customer...</option>
          {customers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </div>

      {isLoading && <div className="text-center py-8 text-slate-500">Loading...</div>}

      {bill && (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="px-6 py-4 border-b border-slate-200">
            <h3 className="text-lg font-semibold">{bill.customerName}</h3>
          </div>
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Date</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">User</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Category</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Hours</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Rate</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Total</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Note</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {bill.lineItems.map(item => (
                <tr key={item.id} className="hover:bg-slate-50">
                  <td className="px-6 py-3 whitespace-nowrap text-sm">{formatDate(item.dateLogged)}</td>
                  <td className="px-6 py-3 whitespace-nowrap text-sm">{item.userName}</td>
                  <td className="px-6 py-3 whitespace-nowrap text-sm">{item.categoryName}</td>
                  <td className="px-6 py-3 whitespace-nowrap text-sm text-right">{item.hours}</td>
                  <td className="px-6 py-3 whitespace-nowrap text-sm text-right font-mono">{formatCurrency(item.rate)}</td>
                  <td className="px-6 py-3 whitespace-nowrap text-sm text-right font-mono">{formatCurrency(item.lineTotal)}</td>
                  <td className="px-6 py-3 text-sm text-slate-500">{item.note ?? '-'}</td>
                </tr>
              ))}
              {bill.lineItems.length === 0 && (
                <tr><td colSpan={7} className="px-6 py-8 text-center text-slate-500">No billable hours for this customer</td></tr>
              )}
            </tbody>
            {bill.lineItems.length > 0 && (
              <tfoot className="bg-slate-50 font-semibold">
                <tr>
                  <td colSpan={3} className="px-6 py-3 text-right text-sm">Totals</td>
                  <td className="px-6 py-3 text-right text-sm">{bill.totalHours}</td>
                  <td className="px-6 py-3"></td>
                  <td className="px-6 py-3 text-right text-sm font-mono">{formatCurrency(bill.totalRevenue)}</td>
                  <td className="px-6 py-3"></td>
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      )}

      {!selectedCustomerId && !isLoading && (
        <div className="text-center py-12 text-slate-400">Select a customer to view their bill</div>
      )}
    </div>
  );
}

function MonthlySummaryTab() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const { data, isLoading } = useMonthlySummary(year, month);

  const summary = data?.data;
  const years = Array.from({ length: 5 }, (_, i) => now.getFullYear() - i);
  const months = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December',
  ];

  return (
    <div>
      <div className="flex flex-wrap gap-4 mb-4">
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Year</label>
          <select value={year} onChange={e => setYear(Number(e.target.value))} className="px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
            {years.map(y => <option key={y} value={y}>{y}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Month</label>
          <select value={month} onChange={e => setMonth(Number(e.target.value))} className="px-3 py-2 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
            {months.map((m, i) => <option key={i + 1} value={i + 1}>{m}</option>)}
          </select>
        </div>
      </div>

      {isLoading && <div className="text-center py-8 text-slate-500">Loading...</div>}

      {summary && (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Customer</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Total Hours</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Total Revenue</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {summary.customers.map(row => (
                <tr key={row.customerId} className="hover:bg-slate-50">
                  <td className="px-6 py-3 text-sm font-medium text-slate-900">{row.customerName}</td>
                  <td className="px-6 py-3 text-sm text-right">{row.totalHours}</td>
                  <td className="px-6 py-3 text-sm text-right font-mono">{formatCurrency(row.totalRevenue)}</td>
                </tr>
              ))}
              {summary.customers.length === 0 && (
                <tr><td colSpan={3} className="px-6 py-8 text-center text-slate-500">No data for this period</td></tr>
              )}
            </tbody>
            {summary.customers.length > 0 && (
              <tfoot className="bg-slate-50 font-semibold">
                <tr>
                  <td className="px-6 py-3 text-sm text-right">Grand Total</td>
                  <td className="px-6 py-3 text-sm text-right">{summary.grandTotalHours}</td>
                  <td className="px-6 py-3 text-sm text-right font-mono">{formatCurrency(summary.grandTotalRevenue)}</td>
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      )}
    </div>
  );
}

function RevenueSummaryTab() {
  const { data, isLoading } = useRevenueSummary();
  const summary = data?.data;

  if (isLoading) return <div className="text-center py-8 text-slate-500">Loading...</div>;
  if (!summary) return null;

  return (
    <div className="space-y-8">
      {/* By Customer */}
      <div>
        <h3 className="text-lg font-semibold text-slate-900 mb-3">Revenue by Customer</h3>
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Customer</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Total Hours</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Avg Rate</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Total Revenue</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {summary.byCustomer.map(row => (
                <tr key={row.customerId} className="hover:bg-slate-50">
                  <td className="px-6 py-3 text-sm font-medium text-slate-900">{row.customerName}</td>
                  <td className="px-6 py-3 text-sm text-right">{row.totalHours}</td>
                  <td className="px-6 py-3 text-sm text-right font-mono">{formatCurrency(row.averageRate)}</td>
                  <td className="px-6 py-3 text-sm text-right font-mono font-semibold">{formatCurrency(row.totalRevenue)}</td>
                </tr>
              ))}
              {summary.byCustomer.length === 0 && (
                <tr><td colSpan={4} className="px-6 py-8 text-center text-slate-500">No revenue data</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* By Category */}
      <div>
        <h3 className="text-lg font-semibold text-slate-900 mb-3">Revenue by Category</h3>
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase">Category</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Hourly Rate</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Total Hours</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-slate-500 uppercase">Total Revenue</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {summary.byCategory.map(row => (
                <tr key={row.categoryId} className="hover:bg-slate-50">
                  <td className="px-6 py-3 text-sm font-medium text-slate-900">{row.categoryName}</td>
                  <td className="px-6 py-3 text-sm text-right font-mono">{formatCurrency(row.hourlyRate)}</td>
                  <td className="px-6 py-3 text-sm text-right">{row.totalHours}</td>
                  <td className="px-6 py-3 text-sm text-right font-mono font-semibold">{formatCurrency(row.totalRevenue)}</td>
                </tr>
              ))}
              {summary.byCategory.length === 0 && (
                <tr><td colSpan={4} className="px-6 py-8 text-center text-slate-500">No revenue data</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
