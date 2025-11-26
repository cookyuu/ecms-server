# ECMS
> 이커머스 도메인에서 겪을 수 있는 문제점을 고민해보기 위한 프로젝트

## 인프라 아키텍처

<img width="983" alt="ECMS_SERVER_INFRA" src="https://github.com/user-attachments/assets/0eeedfda-f0e3-4cc0-8b8e-cd7cb16231f8">

## 기술 스택

- **Backend**: Spring Boot 3.3.2, Java 17
- **Database**: MariaDB 11.1
- **Cache**: Redis 7.2
- **Security**: Spring Security, JWT
- **ORM**: Spring Data JPA, QueryDSL
- **Monitoring**: Prometheus, Grafana, Spring Actuator
- **API Documentation**: Swagger (SpringDoc OpenAPI)
- **Build Tool**: Gradle

## 사전 준비사항

- Docker & Docker Compose 설치
- (선택사항) Java 17 (로컬 개발 시)
- (선택사항) Gradle (로컬 개발 시)

## 설치 및 실행 방법

### 1. 환경 변수 설정

`.env.example` 파일을 복사하여 `.env` 파일을 생성하고, 필요한 값을 수정합니다.

```bash
cp .env.example .env
```

### 2. Docker Compose로 실행

#### 기본 실행 (애플리케이션 + MariaDB + Redis)

```bash
docker-compose up --build -d
```

#### 모니터링 포함 실행 (Prometheus + Grafana 추가)

```bash
docker-compose --profile monitoring up --build -d
```

### 3. 서비스 접속

- **애플리케이션**: http://localhost:7777
- **Swagger UI**: http://localhost:7777/swagger-ui.html
- **Actuator**: http://localhost:7777/actuator
- **Prometheus**: http://localhost:9090 (모니터링 프로파일 실행 시)
- **Grafana**: http://localhost:3000 (모니터링 프로파일 실행 시, 기본 계정: admin/admin)

### 4. 로그 확인

```bash
# 전체 로그 확인
docker-compose logs -f

# 특정 서비스 로그 확인
docker-compose logs -f app
```

### 5. 종료

```bash
# 컨테이너 중지
docker-compose down

# 컨테이너 중지 및 볼륨 삭제 (데이터베이스 데이터 포함)
docker-compose down -v
```

## 로컬 개발 환경 설정

### MariaDB & Redis만 실행

```bash
docker-compose up mariadb redis -d
```

그 후 IDE에서 Spring Boot 애플리케이션을 실행합니다.

## 포트 정보

| 서비스 | 포트 | 설명 |
|--------|------|------|
| Spring Boot App | 7777 | 메인 애플리케이션 |
| MariaDB | 13306 | 데이터베이스 |
| Redis | 6379 | 캐시 |
| Prometheus | 9090 | 메트릭 수집 |
| Grafana | 3000 | 모니터링 대시보드 |

## 환경 변수

주요 환경 변수는 `.env` 파일에서 설정할 수 있습니다:

- `MARIADB_DATABASE`: 데이터베이스 이름
- `MARIADB_USER`: 데이터베이스 사용자명
- `MARIADB_PASSWORD`: 데이터베이스 비밀번호
- `REDIS_PASSWORD`: Redis 비밀번호
- `JWT_SECRET`: JWT 토큰 시크릿 키
- `SLACK_WEBHOOK_URL`: Slack 웹훅 URL (선택사항)

## API 문서

애플리케이션 실행 후 Swagger UI를 통해 API 문서를 확인할 수 있습니다:
- http://localhost:7777/swagger-ui.html

## 모니터링

Prometheus와 Grafana를 사용하여 애플리케이션을 모니터링할 수 있습니다.

### Prometheus 설정
- 설정 파일: `config/prometheus.yml`
- Alert 규칙: `config/rules.yml`

### Grafana 대시보드
1. http://localhost:3000 접속
2. 기본 계정으로 로그인 (admin/admin)
3. Data Source 추가: Prometheus (http://prometheus:9090)
4. 대시보드 생성 또는 임포트

<!--
## 사용 예제

스크린 샷과 코드 예제를 통해 사용 방법을 자세히 설명합니다.

_더 많은 예제와 사용법은 [Wiki][wiki]를 참고하세요._


## 업데이트 내역

* 1.0.0

* 1.0.1
    *
-->
<!-- Markdown link & img dfn's -->
[npm-image]: https://img.shields.io/npm/v/datadog-metrics.svg?style=flat-square
[npm-url]: https://npmjs.org/package/datadog-metrics
[npm-downloads]: https://img.shields.io/npm/dm/datadog-metrics.svg?style=flat-square
[travis-image]: https://img.shields.io/travis/dbader/node-datadog-metrics/master.svg?style=flat-square
[travis-url]: https://travis-ci.org/dbader/node-datadog-metrics
[wiki]: https://github.com/yourname/yourproject/wiki
