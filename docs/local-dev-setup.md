# 로컬 개발 환경 세팅 가이드

## 사전 요구사항

- Docker & Docker Compose
- Gradle

---

## 1. MySQL 컨테이너 실행

프로젝트 루트에서 Docker Compose로 MySQL을 실행합니다.

```bash
# 컨테이너 실행 (백그라운드)
docker-compose up -d

# 컨테이너 상태 확인
docker ps
```

### MySQL 접속 정보

| 항목 | 값 |
|------|-----|
| Host | localhost |
| Port | 3306 |
| Database | moyeolak |
| Username | moyeolak |
| Password | moyeolak |

### 컨테이너 관리 명령어

```bash
# 컨테이너 중지
docker-compose down

# 컨테이너 중지 + 데이터 삭제
docker-compose down -v

# 로그 확인
docker-compose logs -f mysql
```

---

## 2. Spring Boot 실행

`local` 프로파일로 애플리케이션을 실행합니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### IDE에서 실행 (IntelliJ)

1. Run/Debug Configurations 열기
2. Active profiles에 `local` 입력
3. 실행

---

## 3. 초기 데이터

서버 시작 시 `data.sql`이 자동 실행되어 테스트 데이터가 생성됩니다.

### 생성되는 데이터

| 테이블 | 개수 | 설명 |
|--------|------|------|
| meeting | 10개 | test-meeting-001 ~ 010 |
| schedule_poll | 10개 | 각 모임당 1개 (14일 날짜 옵션) |
| location_poll | 10개 | 각 모임당 1개 |
| participant | 10명 | 각 모임 방장 1명 |

### 테스트 모임 ID 목록

```
test-meeting-001  (방장: 김민준)
test-meeting-002  (방장: 이서연)
test-meeting-003  (방장: 박도윤)
test-meeting-004  (방장: 최하은)
test-meeting-005  (방장: 정시우)
test-meeting-006  (방장: 강지아)
test-meeting-007  (방장: 조예준)
test-meeting-008  (방장: 윤수아)
test-meeting-009  (방장: 임건우)
test-meeting-010  (방장: 한지유)
```

---

## 4. 설정 파일 구조

```
src/main/resources/
├── application.yml          # 공통 설정 (프로파일 지정)
├── application-local.yml    # 로컬 환경 설정
├── application-prod.yml     # 운영 환경 설정
└── data.sql                 # 초기 데이터 (local에서만 실행)
```

### application-local.yml 주요 설정

| 설정 | 값 | 설명 |
|------|-----|------|
| ddl-auto | create | 서버 시작 시 테이블 재생성 |
| defer-datasource-initialization | true | JPA 초기화 후 data.sql 실행 |
| sql.init.mode | always | data.sql 항상 실행 |

---

## 5. 데이터 유지하고 싶을 때

테이블 재생성 없이 데이터를 유지하려면 `application-local.yml`을 수정합니다.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # create -> update로 변경
  sql:
    init:
      mode: never       # always -> never로 변경
```

---

## 6. 트러블슈팅

### 포트 충돌 (3306 사용 중)

```bash
# 사용 중인 프로세스 확인
lsof -i :3306

# docker-compose.yml에서 포트 변경
ports:
  - "3307:3306"  # 호스트 포트를 3307로 변경

# application-local.yml도 수정
url: jdbc:mysql://localhost:3307/moyeolak?...
```

### 컨테이너 접속

```bash
# MySQL CLI 접속
docker exec -it moyeolak-mysql mysql -u moyeolak -pmoyeolak moyeolak

# 컨테이너 쉘 접속
docker exec -it moyeolak-mysql bash
```

### 데이터 초기화

```bash
# 볼륨 삭제 후 재시작
docker-compose down -v
docker-compose up -d
```
