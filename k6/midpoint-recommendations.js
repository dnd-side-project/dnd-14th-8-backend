/**
 * GET /api/locations/midpoint-recommendations 성능 테스트
 *
 * 실행 예시:
 *   k6 run k6/midpoint-recommendations.js                          # load (기본)
 *   k6 run -e SCENARIO=smoke k6/midpoint-recommendations.js        # smoke
 *   k6 run -e SCENARIO=cache k6/midpoint-recommendations.js        # 캐시 효과 확인
 *   k6 run -e MEETING_ID=my-meeting k6/midpoint-recommendations.js # 다른 모임
 *
 * Grafana 연동:
 *   k6 run --out influxdb=http://localhost:8086/k6 k6/midpoint-recommendations.js
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import {
    BASE_URL,
    MEETING_ID,
    DEPARTURE_TIME,
    SCENARIO,
    STAGES,
    CACHE_HIT_THRESHOLD_MS,
} from './common/config.js';

// ── 커스텀 메트릭 ─────────────────────────────────────────────────────────────
const cacheHitDuration  = new Trend('cache_hit_duration_ms',  true);
const cacheMissDuration = new Trend('cache_miss_duration_ms', true);
const cacheHitRate      = new Rate('cache_hit_rate');

// ── 옵션 ─────────────────────────────────────────────────────────────────────
export const options = {
    stages: STAGES[SCENARIO],
    thresholds: {
        // 전체 요청: p95 5초 이내 (외부 API 호출 포함)
        http_req_duration:       ['p(95)<5000'],
        // 실패율: 1% 미만
        http_req_failed:         ['rate<0.01'],
        // 캐시 히트 응답: p95 300ms 이내 (cache 시나리오 시 의미 있음)
        cache_hit_duration_ms:   ['p(95)<300'],
    },
};

// ── URL 빌드 ─────────────────────────────────────────────────────────────────
function buildUrl() {
    let url = `${BASE_URL}/api/locations/midpoint-recommendations?meetingId=${MEETING_ID}`;
    if (DEPARTURE_TIME) {
        url += `&departureTime=${encodeURIComponent(DEPARTURE_TIME)}`;
    }
    return url;
}

// ── 메인 테스트 함수 ──────────────────────────────────────────────────────────
export default function () {
    const url = buildUrl();

    group('GET /midpoint-recommendations', () => {
        const res = http.get(url, {
            tags: { scenario: SCENARIO, endpoint: 'midpoint-recommendations' },
        });

        // ── 기본 검증 ────────────────────────────────────────────────────────
        const passed = check(res, {
            'status is 200': (r) => r.status === 200,
            'body is JSON':  (r) => r.headers['Content-Type'].includes('application/json'),
            'has data field': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return body.data !== undefined && body.data !== null;
                } catch {
                    return false;
                }
            },
            'recommendations exist': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return Array.isArray(body.data.recommendations)
                        && body.data.recommendations.length > 0;
                } catch {
                    return false;
                }
            },
        });

        // ── 캐시 히트 여부 메트릭 기록 ────────────────────────────────────────
        // Caffeine 캐시 히트: 수 ms / 외부 API 호출: 수백~수천 ms
        if (res.timings.duration < CACHE_HIT_THRESHOLD_MS) {
            cacheHitDuration.add(res.timings.duration);
            cacheHitRate.add(1);
        } else {
            cacheMissDuration.add(res.timings.duration);
            cacheHitRate.add(0);
        }

        // ── 실패 시 로그 ─────────────────────────────────────────────────────
        if (!passed || res.status !== 200) {
            console.error(
                `[FAIL] status=${res.status} duration=${res.timings.duration}ms body=${res.body.substring(0, 200)}`
            );
        }
    });

    sleep(1);
}

// ── 테스트 종료 후 요약 출력 ───────────────────────────────────────────────────
export function handleSummary(data) {
    const metrics = data.metrics;
    const totalReqs    = metrics.http_reqs?.values?.count ?? 0;
    const failedReqs   = metrics.http_req_failed?.values?.passes ?? 0;
    const p95Duration  = metrics.http_req_duration?.values?.['p(95)'] ?? 0;
    const hitRate      = metrics.cache_hit_rate?.values?.rate ?? 0;
    const hitP95       = metrics.cache_hit_duration_ms?.values?.['p(95)'] ?? 0;
    const missP95      = metrics.cache_miss_duration_ms?.values?.['p(95)'] ?? 0;

    const summary = `
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  midpoint-recommendations 성능 테스트 결과
  시나리오: ${SCENARIO} | meetingId: ${MEETING_ID}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  전체 요청 수       : ${totalReqs}
  실패 요청 수       : ${failedReqs}
  전체 p95 응답시간  : ${p95Duration.toFixed(0)}ms
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  캐시 히트율        : ${(hitRate * 100).toFixed(1)}%
  캐시 히트 p95      : ${hitP95.toFixed(0)}ms
  캐시 미스 p95      : ${missP95.toFixed(0)}ms
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
`;

    return {
        stdout: summary,
    };
}