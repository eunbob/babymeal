# Spring Boot 활용 면접 답변 가이드

## 📋 주요 답변 포인트

### 1. **RESTful API 아키텍처 구축**
"Spring Boot를 활용하여 RESTful API 서버를 구축했습니다. 
- `@RestController`, `@RequestMapping`, `@PostMapping`, `@GetMapping` 등의 어노테이션을 사용하여 REST 엔드포인트를 정의했습니다.
- Controller-Service-Repository 3계층 구조로 관심사를 분리하여 유지보수성을 높였습니다.
- 고객(Customer), 아기(Baby), 게시글(Post), 냉장고(Fridge), 식단(Diet), 알림(Alarm) 등 여러 도메인으로 모듈화했습니다."

### 2. **Spring Security + JWT 인증 구현**
"보안 기능을 위해 Spring Security를 활용했습니다.
- `SecurityFilterChain`을 Bean으로 등록하여 URL별 권한을 관리했습니다. 인증이 필요한 엔드포인트는 `ROLE_USER` 권한을 요구하도록 설정했습니다.
- JWT 토큰 기반의 Stateless 인증 방식을 구현했습니다. `JwtFilter`를 커스텀하여 `UsernamePasswordAuthenticationFilter` 전에 JWT 토큰을 검증하고, `SecurityContext`에 인증 정보를 저장했습니다.
- `CustomAuthenticationProvider`와 `CustomUserDetailService`를 구현하여 사용자 인증 로직을 커스터마이징했습니다.
- `JwtAuthenticationEntryPoint`와 `JwtAccessDeniedHandler`를 통해 인증/인가 실패 시 적절한 예외 처리를 구현했습니다."

### 3. **Spring Data JPA를 통한 데이터베이스 연동**
"데이터베이스 연동을 위해 Spring Data JPA를 활용했습니다.
- `JpaRepository`를 상속받아 Repository 인터페이스를 정의했습니다. Spring Boot가 자동으로 구현체를 생성해주는 기능을 활용했습니다.
- JPA Entity를 사용하여 객체-관계 매핑을 구현했습니다. `@OneToMany`, `@ManyToOne` 등으로 엔티티 간 연관관계를 설정했습니다.
- `@Entity`, `@Id`, `@GeneratedValue` 등의 어노테이션으로 엔티티를 정의하고, `ddl-auto: update`를 통해 개발 편의성을 높였습니다.
- PostgreSQL을 사용했으며, Hibernate Dialect를 명시적으로 설정했습니다."

### 4. **트랜잭션 관리**
"데이터 일관성을 위해 Spring의 트랜잭션 관리 기능을 활용했습니다.
- `@Transactional` 어노테이션을 Service 레이어에 적용하여 메서드 단위 트랜잭션을 관리했습니다.
- 여러 데이터베이스 작업이 하나의 트랜잭션으로 묶여, 오류 발생 시 롤백되도록 보장했습니다."

### 5. **전역 예외 처리**
"일관된 에러 응답을 위해 Spring Boot의 예외 처리 기능을 활용했습니다.
- `@RestControllerAdvice`를 사용하여 전역 예외 핸들러를 구현했습니다.
- 커스텀 `GeneralException`과 `ErrorCode`를 정의하여 비즈니스 로직 예외를 체계적으로 관리했습니다.
- `ResponseEntityExceptionHandler`를 상속받아 표준 HTTP 응답 형식으로 예외를 반환했습니다."

### 6. **파일 업로드 처리**
"이미지 업로드 기능을 구현했습니다.
- Spring Boot의 `MultipartFile`을 활용하여 클라이언트로부터 이미지 파일을 받았습니다.
- `@RequestPart` 어노테이션을 사용하여 JSON 데이터와 파일을 함께 받을 수 있도록 구현했습니다.
- 업로드된 파일을 날짜별 디렉토리로 관리하고, UUID를 사용하여 파일명 충돌을 방지했습니다."

### 7. **외부 설정 관리**
"설정 관리를 위해 Spring Boot의 프로퍼티 기능을 활용했습니다.
- `application.yaml`을 사용하여 데이터베이스 연결 정보, JWT 설정, Firebase 설정 등을 관리했습니다.
- 환경 변수를 통해 민감한 정보(비밀키, 데이터베이스 비밀번호 등)를 외부에서 주입받도록 구현했습니다.
- `.env` 파일을 옵션으로 import하여 개발 환경에서 쉽게 설정을 변경할 수 있도록 했습니다."

### 8. **의존성 주입 (DI)**
"Spring의 의존성 주입 기능을 적극 활용했습니다.
- 생성자 주입 방식을 사용하여 `@RequiredArgsConstructor`(Lombok)와 함께 불변성을 보장했습니다.
- `@Service`, `@Repository`, `@Component` 등의 스테레오타입 어노테이션을 사용하여 Bean으로 등록했습니다."

### 9. **로깅**
"애플리케이션 모니터링을 위해 로깅을 구현했습니다.
- SLF4J를 사용하여 로그를 출력했습니다.
- `application.yaml`에서 로그 레벨을 설정하여 개발 환경에서는 SQL 쿼리를 확인할 수 있도록 했습니다."

### 10. **Jackson JSON 직렬화 설정**
"JSON 응답 형식을 커스터마이징했습니다.
- `application.yaml`에서 Jackson 설정을 통해 날짜 형식을 타임스탬프가 아닌 ISO 형식으로 변환하도록 설정했습니다."

---

## 🎯 핵심 요약 (30초 버전)

"Spring Boot를 활용하여 RESTful API 서버를 구축했습니다. Spring Security와 JWT를 결합한 인증 시스템을 구현했고, Spring Data JPA를 통해 PostgreSQL과 연동했습니다. Controller-Service-Repository 구조로 코드를 모듈화했으며, `@Transactional`로 트랜잭션을 관리하고, `@RestControllerAdvice`로 전역 예외 처리를 구현했습니다. 또한 MultipartFile을 활용한 파일 업로드 기능과 외부 설정 관리를 통해 유연한 애플리케이션을 개발했습니다."

---

## 💡 추가로 어필할 수 있는 점

1. **Spring Boot 3.1.3 사용**: 최신 버전을 사용하여 최신 기능과 보안 패치를 반영했습니다.
2. **Jakarta EE 마이그레이션**: Java EE에서 Jakarta EE로의 전환을 반영했습니다.
3. **Gradle 빌드 도구**: Maven 대신 Gradle을 사용하여 빌드 자동화를 구현했습니다.
4. **모듈화된 설계**: 도메인별로 패키지를 분리하여 확장 가능한 구조를 설계했습니다.

---

## 📝 면접 팁

1. **구체적인 예시 언급**: "예를 들어, PostController에서..."와 같이 구체적인 코드 예시를 들면 좋습니다.
2. **문제 해결 과정**: "JWT 필터를 구현할 때 SecurityContext에 인증 정보를 저장하는 과정에서..."와 같이 문제 해결 과정을 설명하면 좋습니다.
3. **배운 점 강조**: "이번 프로젝트를 통해 Spring Boot의 자동 설정과 Bean 관리 메커니즘을 깊이 이해하게 되었습니다"와 같이 학습한 내용을 어필하세요.

