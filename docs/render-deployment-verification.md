# Render 공개 서버 배포 검증

검증일: 2026-10-29 (Asia/Seoul)

## 공개 주소와 구성

- 프론트: https://moyeorak.site
- 백엔드: https://moyeorak-backend.onrender.com
- 상태 확인: https://moyeorak-backend.onrender.com/actuator/health/readiness
- Render Dashboard: https://dashboard.render.com/web/srv-dbh1p2t9fdbs73daie9g
- Render workspace: My Workspace (`tea-d29g5sqdbo4c73bg31f0`)
- 서비스: `srv-dbh1p2t9fdbs73daie9g`, Docker / Free / Singapore
- DB: 기존 Neon production / moyeorak
- 프로필: `prod,render`

Render 연동으로 웹 서비스 하나를 직접 생성했다. Blueprint 관리 서비스는 아니다.
환경변수와 서비스 설정은 Render Dashboard에서 관리한다. `render.yaml`은 같은 구성으로
새 Blueprint를 만들 때 사용할 참고 구성이다. 현재 서비스와 별도로 Blueprint를 적용하면
중복 서비스가 생길 수 있으므로 기존 서비스를 재사용한다.

배포 소스는 백엔드 `codex/render-neon-free` 브랜치의 `3227d3ad5fff4e3ff882cbf7355cbb02f328b49c`.
코드 변경 시 자동 배포한다. 백엔드 PR은 https://github.com/dnd-side-project/dnd-14th-8-backend/pull/172
이며, 추후 dev로 통합한 뒤 Render의 연결 브랜치도 dev로 바꿀 수 있다.
연결 브랜치를 삭제하면 이후 배포에 영향을 주므로 서비스의 소스 설정을 먼저 변경한다.

프론트 PR https://github.com/dnd-side-project/dnd-14th-8-frontend/pull/170 은 main으로 반영했다.
운영 커밋은 `b674badf113a60a9303ea49c26d51f3431b4b11f`.
기존 Cloudflare Pages 운영 배포를 사용하고 API 주소는 Render의 onrender.com 주소로 변경했다.
`VITE_API_READINESS_CHECK=true`를 활성화했다.
기존 `api.moyeorak.site` DNS는 변경하지 않았으며 새 프론트에서는 사용하지 않는다.

## 배포와 검증 결과

첫 배포 `dep-dbh1p3l9fdbs73daig5g`: live, 3분 35초.
readiness 경로 저장 후 자동 배포 `dep-dbh1psbvvlss73bc50s0`: live, 2분 5초.
두 번째 인스턴스 Spring Boot 기동 로그: 97.997초.
Render 메모리 관측값: 약 249MiB / 512MiB (초기 관측이며 장기 부하 검증은 아님).

공개 서버를 대상으로 다음을 검증했다.

- readiness, DB health: HTTP 200 / UP
- moyeorak.site Origin의 CORS GET 및 OPTIONS: 통과
- 모임 생성, 참가자 조회, 인원 변경, 일정 조회: 통과
- 일정 후보 등록, 2명 투표, 결과 조회, 투표 종료: 통과
- 출발지 2개 등록 및 조회: 통과
- 운영 주소 moyeorak.site에서 브라우저로 모임 생성 후 일정 화면으로 전환: 통과
- 위 검증에서 만든 모임과 관련 투표 데이터: 소유 확인 후 삭제 완료
- 중간 장소 추천: 실패. 원인은 아래 Google API 키 IP 제한

GitHub CI:

- 백엔드 테스트 및 JaCoCo: 성공
  https://github.com/dnd-side-project/dnd-14th-8-backend/actions/runs/40722622072
- 프론트 PR 빌드 / 린트 / preview 배포: 모두 성공
- 프론트 main 운영 배포: 성공
  https://github.com/dnd-side-project/dnd-14th-8-frontend/actions/runs/40723415595
- Cloudflare Pages 운영 빌드: https://91e959f9.dnd-14th-8-frontend.pages.dev

## 남은 외부 API 설정

중간 장소 추천 호출에서 Google Routes가 HTTP 403을 반환했다.
확인한 오류 원인은 `API_KEY_IP_ADDRESS_BLOCKED`이며 Render 서버에서 나가는 요청 IP가
기존 Google API 키의 허용 IP에 포함되지 않았다. 사용자는 해당 Google Cloud 프로젝트에
접근 권한이 없어 여기서 수정하지 못했다.

Render Dashboard의 Connect → Outbound IP Addresses에서 확인한 범위:

```text
74.220.52.0/24
74.220.60.0/24
```

키 관리자에게 전달할 내용:

> 모여락 백엔드를 Render Singapore 무료 서버로 이전했습니다. 기존 Google Routes API 키의
> IP 주소 제한에 `74.220.52.0/24`, `74.220.60.0/24`를 추가해 주세요.
> 현재 중간 장소 추천에서 `403 API_KEY_IP_ADDRESS_BLOCKED`가 발생합니다.
> 기존 허용 IP와 Routes API 제한은 유지하고 새 범위만 추가하면 됩니다.

IP 등록 후 중간 장소 추천과 ODsay/Kakao를 포함한 개인 경로 흐름을 다시 검증해야 한다.
무료 Render의 outbound IP는 같은 리전의 다른 서비스와 공유하며 전용 IP가 아니다.
리전이나 플랫폼 IP 정책이 변경되면 Dashboard에서 다시 확인한다.

## 운영 메모

무료 서버는 유휴 후 절전하며 첫 접속에서 기동 대기 시간이 발생한다. 프론트는 최대 5분 동안
readiness를 확인하고 연결 안내를 표시한다. 주기적인 서버 깨우기 요청은 추가하지 않았다.
데이터는 Neon에 저장되며 Render의 로컬 파일에 의존하지 않는다.
비밀번호·전체 DB URL·API 키·ADMIN_TOKEN 값은 이 문서나 Git에 포함하지 않는다.
