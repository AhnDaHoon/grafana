# Demo Spring Boot Application with Loki Logging

Loki 로그 수집 및 Grafana 시각화를 위한 Spring Boot 데모 애플리케이션

## 기능

- 다양한 로그 레벨 테스트 API
- Grafana Alloy를 통한 로그 수집
- Loki 로그 저장소
- Grafana 대시보드
- Nginx 리버스 프록시
- Docker 컨테이너 환경

## 빠른 시작

### 1. Docker Compose로 전체 스택 실행

```bash
# 전체 스택 실행 (Spring Boot + Alloy + Loki + Grafana + Nginx)
docker-compose up -d

# 로그 확인
docker-compose logs -f springboot

# 중지
docker-compose down

# 볼륨까지 삭제
docker-compose down -v
```

### 2. 애플리케이션만 Docker 이미지 빌드

```bash
# 이미지 빌드
docker build -t demo-app:latest .

# 컨테이너 실행
docker run -d -p 8080:8080 --name demo-app demo-app:latest

# 로그 확인
docker logs -f demo-app

# 컨테이너 중지 및 삭제
docker stop demo-app
docker rm demo-app
```

### 3. 로컬 개발

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew bootJar
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
```

## 서비스 접속

### 직접 접속
- **애플리케이션**: http://localhost:8080
- **Actuator Health**: http://localhost:8080/actuator/health
- **Grafana**: http://localhost:3000 (admin/admin)
- **Loki API**: http://localhost:3100
- **Alloy**: http://localhost:12345

### Nginx 프록시를 통한 접속 (포트 18000)
- **Nginx 상태**: http://localhost:18000
- **Grafana**: http://localhost:18000/grafana/
- **Loki API**: http://localhost:18000/loki/
- **API**: http://localhost:18000/api/log-test/all-levels
- **Health Check**: http://localhost:18000/actuator/health

## 로그 테스트 API

### 모든 로그 레벨 테스트
```bash
# 직접 접속
curl http://localhost:8080/api/log-test/all-levels

# Nginx 프록시 통해 접속
curl http://localhost:18000/api/log-test/all-levels
```

### INFO 로그 생성 (개수 지정)
```bash
curl "http://localhost:8080/api/log-test/info?count=50"
```

### ERROR 로그 및 스택 트레이스
```bash
curl http://localhost:8080/api/log-test/error
```

### 구조화된 로그 (주문/결제 시뮬레이션)
```bash
curl -X POST http://localhost:8080/api/log-test/structured \
  -H "Content-Type: application/json"
```

### 대량 로그 생성
```bash
# 1000개의 INFO 로그
curl "http://localhost:8080/api/log-test/bulk?count=1000&level=INFO"

# 500개의 WARN 로그
curl "http://localhost:8080/api/log-test/bulk?count=500&level=WARN"
```

### 서비스 로그 시뮬레이션
```bash
curl -X POST http://localhost:8080/api/log-test/simulate-service
```

### 키워드 검색 테스트
```bash
curl "http://localhost:8080/api/log-test/keyword?keyword=payment"
```

## Grafana 설정

1. http://localhost:3000 접속
2. 로그인: admin/admin
3. Configuration → Data Sources → Add data source
4. Loki 선택
5. URL: `http://loki:3100` 입력
6. Save & Test

### Loki 쿼리 예시

```logql
# 모든 로그
{job="demo-app"}

# ERROR 레벨만
{job="demo-app", level="ERROR"}

# 특정 키워드 검색
{job="demo-app"} |= "payment"

# 정규식 필터
{job="demo-app"} |~ "ERROR|WARN"

# 특정 로거의 로그
{job="demo-app", logger=~".*LogTestController"}

# 시간대별 로그 카운트
sum(count_over_time({job="demo-app"}[5m])) by (level)
```

## 프로젝트 구조

```
demo/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/demo/
│       │       ├── DemoApplication.java
│       │       └── controller/
│       │           └── LogTestController.java
│       └── resources/
│           ├── application.properties
│           └── logback-spring.xml
├── Dockerfile
├── docker-compose.yml
├── config.alloy             # Alloy 로그 수집 설정
├── loki-config.yaml         # Loki 저장소 설정
├── nginx.conf               # Nginx 리버스 프록시 설정
├── .dockerignore
├── build.gradle
└── README.md
```

## 로그 파일 위치

- **컨테이너 내부**: `/var/log/app/app.log`
- **Docker Volume**: `demo_app-logs`
- **로컬 개발**: `C:\works\log\app.log` (Windows) 또는 `/var/log/app/app.log` (Linux)

## 문제 해결

### 컨테이너가 시작되지 않을 때
```bash
# 로그 확인
docker-compose logs springboot

# 전체 컨테이너 상태 확인
docker-compose ps

# 컨테이너 재시작
docker-compose restart springboot
```

### 로그가 Loki에 수집되지 않을 때
```bash
# Alloy 로그 확인
docker-compose logs alloy

# Loki 연결 확인
curl http://localhost:3100/ready

# Loki 레이블 확인 (수집된 로그 확인)
curl http://localhost:3100/loki/api/v1/labels
```

### Alloy 설정 확인
```bash
# Alloy 컨테이너 접속
docker exec -it alloy sh

# 로그 파일 확인
docker exec -it alloy ls -la /var/log/app/
```

### Nginx 프록시 문제
```bash
# Nginx 로그 확인
docker-compose logs nginx

# Nginx 설정 테스트
docker exec nginx nginx -t

# Nginx 재로드
docker exec nginx nginx -s reload
```

### Health check 실패 시
```bash
# Health endpoint 직접 확인
curl http://localhost:8080/actuator/health

# Nginx를 통한 확인
curl http://localhost:18000/actuator/health
```

## 기술 스택

- **Java**: 21
- **Spring Boot**: 3.5.7
- **Gradle**: 8.5
- **Docker**: Multi-stage build
- **Grafana Alloy**: latest (로그 수집)
- **Loki**: 3.0.0 (로그 저장)
- **Grafana**: 11.0.0 (시각화)
- **Nginx**: latest (리버스 프록시)

## 라이선스

MIT
