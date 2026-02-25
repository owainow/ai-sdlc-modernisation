import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '2m', target: 20 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'], // 3s p95 for reports
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8084';

export default function () {
  // Monthly report
  const monthlyRes = http.get(`${BASE_URL}/api/v1/reports/monthly?year=2026&month=2`);
  check(monthlyRes, {
    'monthly report status 200': (r) => r.status === 200,
  });

  sleep(2);

  // Range report
  const rangeRes = http.get(`${BASE_URL}/api/v1/reports/range?fromDate=2026-01-01&toDate=2026-02-23`);
  check(rangeRes, {
    'range report status 200': (r) => r.status === 200,
  });

  sleep(2);
}
