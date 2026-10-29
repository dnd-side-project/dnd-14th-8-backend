# Neon 연결 및 검증 결과

검증일: 2026-10-29 (Asia/Seoul)

## 연결 대상과 적용 결과

- 조직: `<neon-org-id>`
- 프로젝트: `<neon-project-id>`
- 운영 브랜치: `production`
- DB: `moyeorak`, PostgreSQL 15
- 활성 서비스: Postgres

`neon deploy`는 기존 프로젝트가 선언과 일치함을 확인하고 `.env`에
`DATABASE_URL`, `DATABASE_URL_UNPOOLED`, `NEON_BRANCH`를 병합했다.
앱을 시작하면서 Flyway 마이그레이션 5개와 PostGIS 및 역 데이터 654개가 실제 Neon DB에 적용됐다.

Java PostgreSQL JDBC, Hibernate Spatial, Flyway, Hikari를 유지했다.
세션 시간대 설정과 Flyway를 위해 direct URL을 사용하고 Hikari 최대 연결 수는 4개로 제한한다.
Neon Auth, Functions, Storage 등의 추가 서비스는 선언하지 않았다.

## 코드 변경

- `neon.ts`: Neon CLI가 생성한 Postgres용 정책. 기존 브랜치 설정은 유지한다.
- `package.json`, `package-lock.json`: Neon 설정 의존성과 npm 명령 추가.
  `npm run build`는 이 Java 저장소의 `./gradlew build`를 실행한다.
- `scripts/neon-env.mjs`: Neon direct URL을 JDBC URL·사용자·비밀번호로 분리해 동기화한다.
  비밀번호는 JDBC URL에 포함하지 않으며 로그에 출력하지 않는다.
- `scripts/start-neon.mjs`: dotenv를 읽고 `prod,render` 프로필로 빌드한 JAR를 실행한다.
  시작할 때 native Neon 변수에서 JDBC 설정을 다시 계산한다.
- `scripts/neon-env.test.mjs`: URL 변환·환경변수 보존·잘못된 입력 거부를 검증한다.
- `.gitignore`, `.dockerignore`: `.neon`, `node_modules`, 비밀 환경 파일을 제외한다.

기존 `.env`는 초기화 전에 비공개 임시 백업했고, DB 외 변수의 값이 보존됐음을 확인했다.
현재 `.env` 권한은 `0600`이며 git에서 제외된다.

## 명령 결과

```text
$ npm i -g neon@latest
added 161 packages

$ neon --version
8.13.1

$ neon init -y --org-id <neon-org-id> --project-id <neon-project-id> --branch production --services postgres
Linked .neon
neon.ts created

$ neon login
Auth complete (profile DEFAULT)

$ neon deploy
No changes — branch production already matches the policy.
Utilized services: Postgres
Pulled 3 Neon variables into .env

$ npm run neon:env
Neon JDBC settings synchronized into .env: DB_JDBC_URL, DB_USERNAME, DB_PASSWORD (values hidden)

$ npm run test:neon
tests 3
pass 3
fail 0

$ npm run build
> ./gradlew build
> Task :test
> Task :check
> Task :build
BUILD SUCCESSFUL in 30s
```

Java 25를 `JAVA_HOME`으로 지정해 빌드했다. Gradle 테스트 결과 XML 기준으로
239개 실행, 실패 0개, 오류 0개, 건너뜀 0개였다.

## 실제 Neon DB를 사용한 앱 검증

먼저 production에서 분기한 임시 브랜치에 마이그레이션과 실제 API 검증을 수행한 뒤,
동일한 검증을 production에 수행했다. `npm start`가 사용하는 실행 스크립트로 앱을 시작했다.
운영 DB와 통신하는 로컬 서버에서 검증한 것이며 Render에 웹 서버를 공개 배포한 것은 아니다.

production 검증 출력:

```json
{
  "branch": "production",
  "database": "moyeorak",
  "tls": true,
  "migrations": 5,
  "stations": 654,
  "nearby_stations": 40,
  "startup_ms": 20226,
  "restart_ms": 10173,
  "meeting_create_read_update": "passed",
  "participants": "passed",
  "restart_persistence": "passed",
  "verification_records_removed": true
}
```

확인한 동작:

1. JDBC 클라이언트가 실제 TLS 소켓으로 연결되고, DB 이름이 `moyeorak`인지 확인.
2. Flyway 이력 5개와 역 654개 확인. 서울시청 좌표 반경 3km PostGIS 쿼리에서 역 40개 확인.
3. HTTP readiness와 DB health가 `UP`인지 확인.
4. 모임 생성 → 참가자 조회 → 참가자 수 수정 → 모임 조회 API가 HTTP 200을 반환하는지 확인.
5. 앱을 종료하고 다시 시작한 뒤 수정한 모임과 내 모임 목록을 다시 조회해 영속성 확인.
6. 이번 검증에서 생성한 모임만 소유 키로 확인 후 삭제. 임시 Neon 브랜치도 삭제.

## 중간에 발생한 문제와 조치

- 첫 초기화는 시스템 Node 22.14.0이 스킬 설치 요구 버전보다 낮아 실패했다.
  시스템 Node를 변경하지 않고 번들 Node 24.22.1로 재실행해 완료했다.
- `init -y`는 로그인 전 연결 파일만 썼다. 사용자가 브라우저에서 CLI 로그인을 승인한 뒤
  배포와 환경변수 가져오기를 진행했다.
- 첫 TLS 검증에서는 서버 측 `pg_stat_ssl` 값으로 연결을 판정해 검사가 실패했다.
  검증 코드를 실제 JDBC 클라이언트의 `SSLSocket` 확인으로 바꿔 암호화 연결을 확인했다.
  앱의 `sslmode=require`, `channelBinding=require`는 그대로 유지했다.

해결되지 않은 Neon 연결·빌드·Postgres 검증 실패는 없다.
공개 포트폴리오 링크 복구에는 Render 계정 연결, 코드 반영, 환경변수 입력과 API 도메인 연결이 남아 있다.
지도·경로 API 전체 사용자 흐름은 해당 공개 배포 후 별도로 확인한다.
