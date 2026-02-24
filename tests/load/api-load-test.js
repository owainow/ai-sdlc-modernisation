import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'], // 200ms p95 for API endpoints
    http_req_failed: ['rate<0.01'],   // <1% error rate
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

export default function () {
  // List users
  const listRes = http.get(`${BASE_URL}/api/v1/users?page=0&size=20`);
  check(listRes, {
    'list users status 200': (r) => r.status === 200,
    'list users has content': (r) => JSON.parse(r.body).status === 'success',
  });

  sleep(1);
}
