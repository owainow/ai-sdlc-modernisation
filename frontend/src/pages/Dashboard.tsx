import { useCustomerCount } from '../services/customerApi';
import { useUserCount } from '../services/userApi';
import { useRevenue } from '../services/billingApi';
import { formatCurrency } from '../lib/formatters';

function MetricCard({ title, value, loading }: { title: string; value: string; loading: boolean }) {
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h3 className="text-sm font-medium text-slate-500 uppercase tracking-wide">{title}</h3>
      <p className="mt-2 text-3xl font-bold text-slate-900">
        {loading ? <span className="animate-pulse text-slate-300">Loading...</span> : value}
      </p>
    </div>
  );
}

export default function Dashboard() {
  const { data: customerCount, isLoading: loadingCustomers } = useCustomerCount();
  const { data: userCount, isLoading: loadingUsers } = useUserCount();
  const { data: revenue, isLoading: loadingRevenue } = useRevenue();

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900 mb-6">Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <MetricCard
          title="Total Customers"
          value={String(customerCount?.data ?? 0)}
          loading={loadingCustomers}
        />
        <MetricCard
          title="Total Users"
          value={String(userCount?.data ?? 0)}
          loading={loadingUsers}
        />
        <MetricCard
          title="Total Revenue"
          value={formatCurrency(revenue?.data?.totalRevenue ?? 0)}
          loading={loadingRevenue}
        />
      </div>
    </div>
  );
}
