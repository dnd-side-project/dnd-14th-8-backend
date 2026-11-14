# Render 공개 서버 배포 검증

검증일: 2026-10-29 (Asia/Seoul)

## 공개 주소와 구성

- 프론트: https://moyeorak.site
- 백엔드: https://moyeorak-backend.onrender.com
- 상태 확인: https://moyeorak-backend.onrender.com/actuator/health/readiness
- Render Dashboard: https://dashboard.render.com/web/<render-service-id>
- Render workspace: My Workspace (`tea-d29g5sqdbo4c73bg31f0`)
- 서비스: `<render-service-id>`, Docker / Free / Singapore
- DB: 기존 Neon production / moyeorak
- 프로필: `prod,render`

Render 연동으로 웹 서비스 하나를 직접 생성했다. Blueprint 관리 서비스는 아니다.
환경변수와 서비스 설정은 Render Dashboard에서 관리한다. `render.yaml`은 같은 구성으로
새 Blueprint를 만들 때 사용할 참고 구성이다. 현재 서비스와 별도로 Blueprint를 적용하면
중복 서비스가 생길 수 있으므로 기존 서비스를 재사용한다.

첫 배포 소스는 백엔드 `codex/render-neon-free` 브랜치의 `3227d3ad5fff4e3ff882cbf7355cbb02f328b49c`였다.
이 브랜치를 PR #172로 dev에 통합한 뒤 Render의 연결 브랜치를 `dev`로 바꿨다(2026-10-29).
연결 브랜치를 바꾸거나 삭제할 때는 서비스의 소스 설정을 먼저 변경한다.

### 배포는 수동이다 (#182)

이 서비스는 GitHub 연동이 아니라 공개 저장소 주소(Public Git Repository)로 연결돼 있다(2026-11-14 확인).

- Account Settings → Git Deployment Credentials의 GitHub 자격 증명(`meteorqz6`)을 펼치면 "No repositories found."다.
  Render GitHub 앱이 dnd-side-project 조직 저장소에 접근하지 못한다.
- 그래서 서비스 Settings → Source → Edit의 Git Provider 탭에도 저장소 목록 없이 연결 버튼만 보인다.
- 이 워크스페이스의 자격 증명으로는 이 저장소를 GitHub 연동할 수 없으므로, 서비스는 공개 저장소 주소로 만들어졌다.

Render는 공개 저장소 주소로 연결한 서비스를 자동 배포하지 않는다("Services that use a prebuilt
Docker image or a public Git repository URL must be deployed manually.", Render Deploys 문서).
그래서 Auto-Deploy 설정이 "On Commit"이어도 dev에 커밋이 들어와 배포되지 않는다.

이전에는 "dev에 코드가 반영되면 CI 통과 후 자동 배포한다"고 적었지만 실제와 달랐다.
10-29의 `f124bdd` 배포는 연결 브랜치를 dev로 바꾼 설정 변경("Branch updated")으로 일어났고,
그 뒤 dev 커밋(#176, #178, #181)은 자동으로 배포되지 않았다.

dev에 머지한 뒤에는 다음을 한다.

1. Render Dashboard → moyeorak-backend → **Manual Deploy → Deploy latest commit**
2. Events에서 "Deploy live for <커밋>"을 확인한다(기록상 2~4분).
3. `https://moyeorak-backend.onrender.com/actuator/health/readiness`가 UP인지 확인한다.
   API가 바뀐 경우 `/v3/api-docs`에 바뀐 필드가 보이는지 확인한다.

자동 배포로 바꾸려면 Render GitHub 앱을 dnd-side-project 조직에 설치하고 `dnd-14th-8-backend` 접근을 허용해야 한다.
조직 관리자 승인이 필요하다. 승인되면 Git Deployment Credentials의 `meteorqz6`에 이 저장소가 보이고,
Settings → Source → Edit → Git Provider에서 `dnd-14th-8-backend`를 고른 뒤 Auto-Deploy를 "After CI Checks Pass"로 둔다.
서비스 주소와 환경변수는 그대로 유지된다.

프론트 PR https://github.com/dnd-side-project/dnd-14th-8-frontend/pull/170 은 main으로 반영했다.
운영 커밋은 `b674badf113a60a9303ea49c26d51f3431b4b11f`.
기존 Cloudflare Pages 운영 배포를 사용하고 API 주소는 Render의 onrender.com 주소로 변경했다.
`VITE_API_READINESS_CHECK=true`를 활성화했다.
기존 `api.moyeorak.site` DNS는 변경하지 않았으며 새 프론트에서는 사용하지 않는다.

## 배포와 검증 결과

첫 배포: live, 3분 35초.
readiness 경로 저장 후 자동 배포: live, 2분 5초.
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

Render Dashboard의 Connect → Outbound IP Addresses에서 범위를 확인한다.
공개 저장소라 실제 값은 기록하지 않는다. Render 무료 서버의 outbound IP는 같은 리전의 다른 서비스와 공유한다.

키 관리자에게 전달할 내용:

> 모여락 백엔드를 Render Singapore 무료 서버로 이전했습니다. 기존 Google Routes API 키의
> IP 주소 제한에 Render outbound IP 범위(`<render-outbound-ip-ranges>`)를 추가해 주세요.
> 현재 중간 장소 추천에서 `403 API_KEY_IP_ADDRESS_BLOCKED`가 발생합니다.
> 기존 허용 IP와 Routes API 제한은 유지하고 새 범위만 추가하면 됩니다.

IP 등록 후 중간 장소 추천과 ODsay/Kakao를 포함한 개인 경로 흐름을 다시 검증해야 한다.
무료 Render의 outbound IP는 같은 리전의 다른 서비스와 공유하며 전용 IP가 아니다.
리전이나 플랫폼 IP 정책이 변경되면 Dashboard에서 다시 확인한다.

## 운영 메모

무료 서버는 15분 동안 요청이 없으면 절전하고, 다음 요청에서 다시 기동한다.
2026-10-29 실측으로 절전 후 첫 `UP` 응답까지 129.5초, 배포 시 Spring Boot 기동 로그는 98.5초였다.
Better Stack Uptime이 3분마다 readiness를 확인해 절전을 막는다.
설정과 무료 한도 검토는 [외부 모니터로 절전 방지](render-neon-deployment.md#외부-모니터로-절전-방지)에 정리했다.
재시작이나 배포 직후의 기동 대기는 프론트가 최대 5분 동안 readiness를 확인하고 연결 안내를 표시해 대비한다.
데이터는 Neon에 저장되며 Render의 로컬 파일에 의존하지 않는다.
비밀번호·전체 DB URL·API 키·ADMIN_TOKEN 값은 이 문서나 Git에 포함하지 않는다.
