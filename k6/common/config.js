/**
 * k6 공통 설정
 *
 * 환경변수로 오버라이드 가능:
 *   k6 run -e BASE_URL=http://prod-server k6/midpoint-recommendations.js
 *   k6 run -e MEETING_ID=my-meeting-001 k6/midpoint-routes.js
 */

export const BASE_URL     = __ENV.BASE_URL       || 'http://localhost:8080';
export const MEETING_ID   = __ENV.MEETING_ID     || 'test-meeting-001';
export const STATION_ID   = __ENV.STATION_ID     || '233';    // 김포공항역(5호선)
export const PARTICIPANT_ID = __ENV.PARTICIPANT_ID || '1';    // 김민준
export const DEPARTURE_TIME = __ENV.DEPARTURE_TIME || null;   // ISO-8601, null이면 현재 시각

/**
 * 시나리오 선택
 *   smoke  — 1 VU, 30s (기본 동작 확인)
 *   load   — 0→10 VU, 50s (정상 부하)
 *   cache  — 10 VU, 60s (캐시 효과 확인, 동일 meetingId)
 */
export const SCENARIO = __ENV.SCENARIO || 'load';

export const STAGES = {
    smoke: [
        { duration: '30s', target: 1 },
    ],
    load: [
        { duration: '10s', target: 5  },
        { duration: '30s', target: 10 },
        { duration: '10s', target: 0  },
    ],
    cache: [
        { duration: '10s', target: 10 },
        { duration: '40s', target: 10 },
        { duration: '10s', target: 0  },
    ],
};

/**
 * 응답시간으로 캐시 히트 여부를 추정하는 임계값 (ms)
 * Caffeine 캐시 히트는 수 ms, 외부 API 호출은 수백 ms 이상
 */
export const CACHE_HIT_THRESHOLD_MS = 300;