# Render + Neon 무료 배포

프론트는 기존 Cloudflare Pages에 두고, 백엔드는 Render Free Docker 서비스,
데이터는 Neon PostgreSQL/PostGIS에 저장한다. AWS 계정이나 ECR은 필요하지 않다.
아래 절차는 새 DB로 복구하는 방법이다. 기존 모임 데이터는 이전 DB의 백업이 있어야 복원할 수 있다.

## 1. Neon DB 만들기

1. [Neon](https://console.neon.tech/)에 가입하고 Free 프로젝트를 만든다.
2. PostgreSQL 15를 선택할 수 있으면 사용하고, Render 싱가포르와 가까운 리전을 고른다.
3. Connect에서 **Connection pooling을 끈 직접 연결**을 확인한다.
4. 호스트, DB 이름, 사용자명, 비밀번호를 보관한다. 채팅이나 git에 붙여 넣지 않는다.

Render에 입력할 JDBC URL은 다음 형식이다. Neon의 `postgresql://user:password@...` 문자열을
그대로 넣지 않고 **`jdbc:postgresql://`** 접두사를 사용한다. 사용자명과 비밀번호는 URL에서 분리한다.

```text
DB_JDBC_URL=jdbc:postgresql://<Neon 호스트>/<DB 이름>?sslmode=require&channelBinding=require&connectTimeout=10
DB_USERNAME=<Neon 사용자명>
DB_PASSWORD=<Neon 비밀번호>
```

Flyway가 첫 기동 때 PostGIS 확장, 테이블과 지하철역 데이터를 자동 생성한다.
SQL Editor에서 수동으로 테이블이나 역 데이터를 생성할 필요가 없다.
DB 사용자는 `CREATE EXTENSION postgis`를 실행할 수 있는 프로젝트 소유 역할을 사용한다.

### Neon CLI로 연결한 경우

이 저장소에는 `neon.ts`와 Neon CLI용 npm 설정을 추가했다. Java 앱과 Gradle 빌드는 그대로 사용한다.
Node 22.20 이상과 Java 25가 필요하다. 현재 프로젝트 연결은 로컬 `.neon`에 저장되며 git에서 제외된다.

```bash
npm install
neon login
neon init -y --org-id org-little-cell-42661468 --project-id damp-cell-53014586 --branch production --services postgres
npm run neon:deploy
npm run build
npm start
```

`neon deploy`는 `DATABASE_URL`, `DATABASE_URL_UNPOOLED`, `NEON_BRANCH`를 기존 `.env`에 병합한다.
`npm run neon:deploy`는 그 다음 `npm run neon:env`를 실행해 direct URL을
`DB_JDBC_URL`, `DB_USERNAME`, `DB_PASSWORD`로 변환한다. API 키 등 다른 기존 값은 보존한다.
직접 `neon deploy`를 실행했다면 `npm run neon:env`도 실행한다.

`npm start`는 `.env` 또는 `.env.local`을 읽고 direct URL에서 JDBC 설정을 다시 계산한 뒤
`prod,render` 프로필로 빌드된 JAR를 실행한다. 기본 포트는 10000이며 `PORT`로 변경할 수 있다.
이 명령은 로컬 실행이다. 공개 API 링크는 아래 Render 배포 단계까지 진행해야 한다.

Render에서는 Node를 실행하지 않는다. `.env`에 동기화한 JDBC 변수 3개와 기존 API 키 3개를
아래 표대로 넣는다. `.env`의 비밀번호나 전체 접속 문자열을 채팅·커밋·공개 로그에 올리지 않는다.

## 2. Render 백엔드 만들기

1. 변경된 백엔드 코드를 GitHub에 올린다. 기존 AWS 자동 배포는 기본 비활성화돼 있다.
2. [Render](https://dashboard.render.com/)에 가입하고 GitHub 저장소 접근을 연결한다.
3. **New → Blueprint**에서 이 저장소와 변경을 반영한 브랜치를 선택한다.
   루트의 `render.yaml`이 Docker/Free/싱가포르 설정을 불러온다.
4. 다음 6개 값을 입력한다. API 키 3개는 기존 로컬 `.env` 값을 재사용한다.

| Render 변수 | 값 |
| --- | --- |
| `DB_JDBC_URL` | 위에서 만든 Neon JDBC URL |
| `DB_USERNAME` | Neon 사용자명 |
| `DB_PASSWORD` | Neon 비밀번호 |
| `KAKAO_API_KEY` | 기존 Kakao REST API 키 |
| `GOOGLE_API_KEY` | 기존 Google Routes API 키 |
| `ODSAY_API_KEY` | 기존 ODsay 서버 키 |

`SPRING_PROFILES_ACTIVE=prod,render`와 `ADMIN_TOKEN`은 Blueprint가 설정한다.
`prod,render` 순서가 중요하다. `render` 단독으로 실행하지 않는다.
입력 형태는 `infra/deploy/.env.render.example`도 참고할 수 있다.
로컬 `.env` 전체를 업로드할 필요는 없다.

5. 배포를 시작하고 Flyway 마이그레이션 및 서버 기동 로그를 확인한다.
6. 발급된 `https://<서비스명>.onrender.com/actuator/health/readiness`가
   `{"status":"UP"}`을 반환하는지 확인한다. 서비스 이름이 이미 사용 중이면 다른 이름을 선택한다.

Blueprint는 연결한 브랜치의 CI가 통과하면 자동 배포한다. 첫 배포는 Blueprint 생성 시 실행된다.
AWS 배포가 다시 필요하면 GitHub Actions의 `Deploy to AWS`를 수동 실행하거나
저장소 변수 `ENABLE_AWS_DEPLOY=true`를 설정한다.

## 3. 기존 API 도메인 연결

1. Render 서비스의 Settings → Custom Domains에 `api.moyeorak.site`를 추가한다.
2. Cloudflare DNS에서 기존 `api` A/AAAA 레코드를 Render가 안내하는 CNAME으로 교체한다.
   연결 검증 동안에는 DNS only로 설정하고 Render의 인증서 발급 완료를 확인한다.
3. `https://api.moyeorak.site/actuator/health/readiness` 응답을 확인한다.

도메인 권한이 없으면 우선 `onrender.com` 주소를 사용할 수 있다. 그 경우 프론트 GitHub Secret의
`VITE_API_BASE_URL`을 그 주소로 바꾸고 프론트를 재배포한다.
도메인을 유지하면 기존 `VITE_API_BASE_URL=https://api.moyeorak.site`를 계속 사용한다.

## 4. 프론트의 첫 접속 대기 활성화

같은 프로젝트의 `dnd-14th-8-frontend`에도 다음 변경이 함께 필요하다.

- `backend-readiness.ts`: 동시에 발생한 요청이 준비 상태 확인 한 번을 공유한다.
- `BackendConnectionProvider`: 연결에 1초 이상 걸리면 안내 화면을 보여 주고 최대 5분 기다린다.
- 일반/관리자 Axios: 준비 확인 후 실제 API 요청을 한 번 보낸다. 생성·투표 요청은 자동 재전송하지 않는다.
- 프론트 배포 워크플로: 저장소 변수 `VITE_API_READINESS_CHECK`를 빌드에 전달한다.

프론트 GitHub의 Settings → Secrets and variables → Actions → **Variables**에서
`VITE_API_READINESS_CHECK=true`를 추가하고 변경된 프론트를 재배포한다.
로컬에서 확인하려면 프론트 `.env`에도 같은 값을 넣는다.
기존 서버를 사용하는 환경에서는 기본값 `false`로 기존 동작을 유지할 수 있다.

대기 중에는 읽기 전용 `/actuator/health/readiness`만 재확인한다.
연결 실패 시 화면의 **다시 연결하기** 버튼으로 새로 시도한다.
브라우저를 15분 이상 방치했다가 돌아와도 다음 API 요청 전에 준비 상태를 다시 확인한다.
주기적으로 서버를 깨우는 ping은 사용하지 않는다.

## 5. 배포 후 확인

1. 페이지 첫 접속에서 대기 안내 후 정상 화면으로 전환되는지 확인한다.
2. 모임 생성 → 참가 → 일정 투표 → 출발지 등록 → 중간 장소 추천을 확인한다.
3. Render를 재시작해도 생성한 모임을 조회할 수 있는지 확인한다. 데이터는 Neon에 남아야 한다.
4. 15분 이상 서비스 요청이 없는 뒤 새 창으로 들어가 자동으로 다시 연결되는지 확인한다.
5. Google/Kakao/ODsay 키에 서버 IP 제한이 있으면 Render 서비스가 표시하는 outbound IP 목록을 등록한다.
   outbound IP는 전용 고정 IP 한 개로 가정하지 않는다.
6. 프론트 도메인을 변경했다면 지도 키의 허용 도메인도 갱신한다.

## 무료 자원을 위한 설정

- Java 25 런타임, 힙 상한 50%, Serial GC와 C1 JIT로 512MB 인스턴스에서 시작한다.
  작은 CPU 할당에서 초기 컴파일 비용을 줄이기 위한 설정이다. 유료 전환 후에는
  Render의 `JAVA_TOOL_OPTIONS` 환경변수로 조정할 수 있다.
- DB 풀은 최대 4개, 최소 유휴 0개이며 30초 후 유휴 연결을 반납한다.
  keepalive를 꺼서 Neon의 자동 절전을 방해하지 않는다.
- 외부 API 지표 스냅샷 잡과 Slack 일일 요약 스케줄은 Render 프로필에서 비활성화한다.
  관리자 통계의 과거 스냅샷·재시작 간 누적 사용량과 quota Slack 알림은 이 구성에서 제공하지 않는다.
  외부 API의 실제 쿼터는 각 제공사 콘솔에서 확인한다.
- Render 상태 확인은 DB를 조회하지 않는 readiness 경로를 사용한다.
  `/actuator/health`는 수동 DB 상태 진단에만 사용한다. 주기적인 외부 모니터 대상으로 등록하지 않는다.
- 로그는 표준 출력으로 확인하며 DB나 로그를 Render 로컬 파일에 저장하지 않는다.
- 프로필은 기존 API 사용량 제한을 유지한다. 서버 무료 한도와 지도·경로 API 과금은 별개다.

정책과 할당량은 서비스 콘솔에서 확인한다. 무료 서버의 첫 기동 시간과 실제 메모리 사용량은
실제 Render 배포 후에도 확인해야 한다. 로컬 Docker 512MB 검증만으로 Render 운영을 보장하지 않는다.

## 로컬 검증 결과 (2026-10-29)

- 백엔드 239개 테스트와 커버리지 기준 통과. 실제 PostGIS를 사용하는 Render 프로필 테스트 포함.
- 프론트 164개 단위 테스트, 변경 파일 린트, TypeScript 및 PWA 배포 빌드 통과.
- Render Blueprint 공식 JSON Schema 검증 통과.
- 메모리 512MiB, CPU 0.1개, swap 없음으로 Docker 실행: 첫 기동 약 215초,
  API 사용 후 약 330MiB 사용, OOM 없음.
- 역 데이터 654개 마이그레이션, 모임 생성·조회, 앱 재시작 후 데이터 유지 확인.
- 브라우저에서 서버 준비 전 안내, 배경 입력 차단, 준비 후 자동 복구 확인.

이는 로컬에서 CPU를 엄격히 제한한 결과다. 실제 Render의 CPU 배분과 Neon 네트워크에 따라
시간은 달라질 수 있다. Neon production 연결과 실제 DB 검증은 이후 완료했으며
[Neon 연결 검증 기록](neon-setup-verification.md)에 결과를 정리했다.
Render 계정 연결과 공개 서버 배포, 외부 지도·경로 API를 포함한 전체 사용자 흐름 검증은 남아 있다.
로컬 프론트 빌드에는 지도 키가 없어 경고가 나지만,
기존 GitHub 배포에서는 프론트 Secrets의 지도 키를 주입한다.

## 나중에 유료 전환

백엔드 메모리 부족이나 첫 접속 대기가 문제가 되면 Render의 유료 compute plan을 선택하고
`render.yaml`의 `plan`도 같은 값으로 갱신한다. Blueprint에서 `free`가 다시 적용되지 않게 한다.
DB는 Neon 무료 사용량 내에 있으면 그대로 유지할 수 있다. DB 사용량이 한도에 도달했을 때만
Neon 플랜을 별도로 변경한다. 계정 플랜 업그레이드와 개별 서비스 compute 변경을 구분한다.

서버 절전이 없어져도 DB 첫 연결을 위한 준비 확인을 유지할 수 있다.
프론트의 확인 화면이 필요 없으면 `VITE_API_READINESS_CHECK=false`로 다시 빌드한다.

## 참고

- [Render Blueprint](https://render.com/docs/blueprint-spec)
- [Render Free 제한](https://render.com/docs/free)
- [Render Health Checks](https://render.com/docs/health-checks)
- [Neon Java 연결](https://neon.com/docs/guides/java)
- [Neon PostGIS](https://neon.com/docs/extensions/postgis)
- [Neon Scale to Zero](https://neon.com/docs/introduction/scale-to-zero)
