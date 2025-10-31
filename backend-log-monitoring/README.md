# Backend Log Monitoring System

Grafana, Loki, Alloy를 사용한 Spring Boot 백엔드 로그 모니터링 시스템입니다.

## 시스템 구성

- **Backend**: Spring Boot 3.5.7 (Java 21) 애플리케이션
- **Loki**: 로그 수집 및 저장 시스템
- **Alloy**: 로그 수집 에이전트 (파일 및 Docker 로그 수집)
- **Grafana**: 로그 시각화 및 대시보드

## 사전 요구사항

- Docker
- Docker Compose

## 프로젝트 구조

```
backend-log-monitoring/
├── src/                              # Spring Boot 소스 코드
│   └── main/
│       ├── java/
│       │   └── com/example/demo/
│       │       └── controller/
│       │           └── LogTestController.java  # 로그 테스트 API
│       └── resources/
│           ├── application.yml       # 애플리케이션 설정
│           └── logback-spring.xml    # 로그 설정 (JSON 형식)
├── Dockerfile                        # 백엔드 이미지 빌드
├── docker-compose.yml                # 전체 시스템 구성
├── loki-config.yaml                  # Loki 설정
├── alloy-config.alloy                # Alloy 설정
└── grafana-datasources.yaml          # Grafana 데이터소스 설정
```

## 빠른 시작

### 1. 시스템 시작

```bash
docker-compose up -d
```

### 2. 서비스 상태 확인

```bash
docker-compose ps
```

모든 서비스가 정상적으로 실행되면 다음과 같이 표시됩니다:
- backend-app (healthy)
- loki (healthy)
- alloy (running)
- grafana (healthy)

### 3. 접속 정보

- **Backend API**: http://localhost:8080
- **Grafana**: http://localhost:3000
  - Username: `admin`
  - Password: `admin`
- **Loki API**: http://localhost:3100
- **Alloy**: http://localhost:12345

## 로그 테스트 API

LogTestController는 다양한 로그 패턴을 테스트할 수 있는 API를 제공합니다.

### 1. 모든 로그 레벨 테스트

```bash
curl http://localhost:8080/api/log-test/all-levels
```

TRACE, DEBUG, INFO, WARN, ERROR 레벨의 로그를 생성합니다.

### 2. INFO 로그 생성

```bash
curl "http://localhost:8080/api/log-test/info?count=10"
```

지정된 개수만큼 INFO 로그를 생성합니다.

### 3. ERROR 로그 및 스택 트레이스

```bash
curl http://localhost:8080/api/log-test/error
```

예외와 함께 스택 트레이스를 포함한 ERROR 로그를 생성합니다.

### 4. 구조화된 로그 (주문 시뮬레이션)

```bash
curl -X POST http://localhost:8080/api/log-test/structured \
  -H "Content-Type: application/json" \
  -d '{}'
```

주문 생성, 결제 프로세스를 시뮬레이션하는 구조화된 로그를 생성합니다.

### 5. 대량 로그 생성

```bash
curl "http://localhost:8080/api/log-test/bulk?count=100&level=INFO"
```

대량의 로그를 생성하여 성능 테스트를 수행합니다.

### 6. 서비스 로그 시뮬레이션

```bash
curl -X POST http://localhost:8080/api/log-test/simulate-service
```

실제 서비스의 요청-응답 사이클을 시뮬레이션하는 로그를 생성합니다.

### 7. 키워드 검색 테스트

```bash
curl "http://localhost:8080/api/log-test/keyword?keyword=payment"
```

특정 키워드가 포함된 로그를 생성하여 검색 테스트를 수행합니다.

## Grafana에서 로그 확인하기

### 1. Grafana 접속

브라우저에서 http://localhost:3000 접속 후 로그인:
- Username: `admin`
- Password: `admin`

### 2. Explore 페이지 이동

좌측 메뉴에서 "Explore" 클릭

### 3. 로그 쿼리 예제

#### 모든 백엔드 로그 조회
```logql
{app="backend-app"}
```

#### ERROR 레벨 로그만 조회
```logql
{app="backend-app", level="ERROR"}
```

#### 특정 키워드 검색
```logql
{app="backend-app"} |= "주문"
```

#### JSON 필드로 필터링
```logql
{app="backend-app"} | json | message=~".*결제.*"
```

#### 로그 집계 (분당 로그 수)
```logql
rate({app="backend-app"}[1m])
```

#### 레벨별 로그 카운트
```logql
sum by (level) (count_over_time({app="backend-app"}[5m]))
```

## 시스템 관리

### 로그 확인

```bash
# 전체 로그
docker-compose logs

# 특정 서비스 로그
docker-compose logs backend
docker-compose logs loki
docker-compose logs alloy
docker-compose logs grafana

# 실시간 로그 추적
docker-compose logs -f backend
```

### 시스템 중지

```bash
docker-compose down
```

### 시스템 중지 및 데이터 삭제

```bash
docker-compose down -v
```

### 개별 서비스 재시작

```bash
docker-compose restart backend
docker-compose restart loki
docker-compose restart alloy
docker-compose restart grafana
```

## 트러블슈팅

### 1. 백엔드 애플리케이션이 시작되지 않는 경우

```bash
docker-compose logs backend
```

로그를 확인하여 오류 메시지를 확인합니다.

### 2. Loki에서 로그가 보이지 않는 경우

```bash
# Alloy 로그 확인
docker-compose logs alloy

# Loki 상태 확인
curl http://localhost:3100/ready
```

### 3. Grafana에서 데이터소스 연결 오류

Grafana UI에서 Configuration > Data Sources > Loki로 이동하여 "Test" 버튼 클릭

### 4. 볼륨 권한 문제

Windows의 경우 Docker Desktop 설정에서 공유 드라이브 설정을 확인합니다.

## 로그 포맷

애플리케이션은 JSON 형식으로 로그를 출력합니다:

```json
{
  "@timestamp": "2025-10-31T12:34:56.789+09:00",
  "level": "INFO",
  "thread_name": "http-nio-8080-exec-1",
  "logger_name": "com.example.demo.controller.LogTestController",
  "message": "INFO 로그 #1 - 요청 시간: 2025-10-31T12:34:56.789, 사용자: user_123",
  "app": "backend-app"
}
```

## 주요 기능

1. **실시간 로그 모니터링**: Grafana Explore를 통한 실시간 로그 조회
2. **로그 레벨 필터링**: TRACE, DEBUG, INFO, WARN, ERROR 레벨별 필터링
3. **전문 검색**: LogQL을 사용한 강력한 로그 검색
4. **집계 및 분석**: 로그 패턴 분석 및 통계
5. **다중 소스 지원**: 파일 로그 및 Docker 컨테이너 로그 동시 수집

## 참고 자료

- [Grafana Loki Documentation](https://grafana.com/docs/loki/latest/)
- [Grafana Alloy Documentation](https://grafana.com/docs/alloy/latest/)
- [LogQL Query Language](https://grafana.com/docs/loki/latest/logql/)
- [Spring Boot Logging](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)
