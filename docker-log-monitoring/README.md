# Grafana + Loki + Alloy 모니터링 스택

Docker Compose를 사용한 완전한 로그 모니터링 스택입니다.

## 📁 파일 구조

```
.
├── docker-compose.yml           # 메인 Docker Compose 파일
├── loki-config.yml             # Loki 설정
├── alloy-config.alloy          # Alloy 설정 (로그 수집)
├── grafana-datasources.yml     # Grafana 데이터소스 자동 설정
└── README.md                   # 이 파일
```

## 🚀 시작하기

### 1. 스택 시작
```bash
docker-compose up -d
```

### 2. 서비스 접속

- **Grafana**: http://localhost:3000
  - 사용자명: `admin`
  - 비밀번호: `admin`

- **Loki API**: http://localhost:3100
- **Alloy UI**: http://localhost:12345

### 3. 로그 확인

1. Grafana에 로그인
2. 좌측 메뉴에서 "Explore" 선택
3. 데이터 소스에서 "Loki" 선택
4. Log browser에서 레이블 선택 (예: `{container="grafana"}`)
5. "Run query" 클릭

## 📊 주요 기능

### 자동 수집되는 로그
- 모든 Docker 컨테이너 로그
- 시스템 로그 (syslog, messages)

### 레이블 자동 추가
- `container`: 컨테이너 이름
- `service`: Docker Compose 서비스 이름
- `stream`: stdout/stderr
- `job`: 로그 소스

## 🔍 LogQL 쿼리 예제

### 특정 컨테이너 로그 보기
```logql
{container="grafana"}
```

### 특정 서비스의 에러 로그
```logql
{service="loki"} |= "error"
```

### 최근 5분간의 로그
```logql
{container="alloy"} [5m]
```

### 여러 컨테이너의 로그 합치기
```logql
{container=~"grafana|loki|alloy"}
```

### JSON 로그 파싱
```logql
{container="app"} | json | level="error"
```

## 🛠️ 커스터마이징

### Alloy 설정 수정
`alloy-config.alloy` 파일을 수정하여:
- 추가 로그 소스 연결
- 필터링 규칙 추가
- 레이블 변경

수정 후:
```bash
docker-compose restart alloy
```

### Loki 설정 수정
`loki-config.yml` 파일에서:
- 보관 기간 변경
- 성능 튜닝
- 저장소 설정

### Grafana 대시보드 추가
1. Grafana UI에서 대시보드 생성
2. 또는 `grafana-dashboards.yml` 파일로 자동 프로비저닝 설정

## 📝 테스트 로그 생성

```bash
# 테스트 컨테이너 실행
docker run --rm --name test-logger alpine sh -c "while true; do echo 'Test log message at $(date)'; sleep 2; done"

# Grafana에서 확인
# {container="test-logger"}
```

## 🔧 문제 해결

### 로그가 보이지 않는 경우

1. Alloy 상태 확인:
```bash
docker-compose logs alloy
```

2. Loki 상태 확인:
```bash
docker-compose logs loki
```

3. Alloy UI에서 타겟 확인: http://localhost:12345

### 권한 문제
```bash
# Docker 소켓 권한 확인
ls -l /var/run/docker.sock

# 필요시 권한 추가
sudo chmod 666 /var/run/docker.sock
```

## 🛑 종료

### 스택 중지
```bash
docker-compose down
```

### 데이터까지 삭제
```bash
docker-compose down -v
```

## 📚 추가 리소스

- [Grafana 문서](https://grafana.com/docs/grafana/latest/)
- [Loki 문서](https://grafana.com/docs/loki/latest/)
- [Alloy 문서](https://grafana.com/docs/alloy/latest/)
- [LogQL 쿼리 가이드](https://grafana.com/docs/loki/latest/logql/)

## 💡 다음 단계

1. **알림 설정**: Grafana에서 로그 기반 알림 규칙 생성
2. **대시보드 생성**: 자주 사용하는 쿼리를 대시보드로 저장
3. **Prometheus 추가**: 메트릭 수집을 위해 Prometheus 통합
4. **프로덕션 설정**: 
   - 영구 저장소 설정
   - 보안 강화 (비밀번호 변경, HTTPS)
   - 백업 구성
