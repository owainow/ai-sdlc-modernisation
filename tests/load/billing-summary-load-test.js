import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 30 },
    { duration: '1m', target: 30 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 2s p95 for billing summary
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8083';
const CUSTOMER_ID = __ENV.CUSTOMER_ID || '00000000-0000-0000-0000-000000000001';

export default function () {
  const summaryRes = http.get(
    `${BASE_URL}/api/v1/billing/summary?customerId=${CUSTOMER_ID}&fromDate=2026-01-01&toDate=2026-02-23`
  );
  check(summaryRes, {
    'billing summary status 200': (r) => r.status === 200,
  });

  sleep(1);
}
