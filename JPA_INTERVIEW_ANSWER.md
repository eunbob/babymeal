# JPA 활용 면접 답변 가이드

## 📋 주요 답변 포인트

### 1. **Spring Data JPA Repository 패턴 활용**
"Spring Data JPA를 활용하여 데이터베이스 접근 계층을 구현했습니다.
- `JpaRepository<Entity, ID>` 인터페이스를 상속받아 Repository를 정의했습니다. Spring이 자동으로 구현체를 생성해주는 기능을 활용했습니다.
- 예를 들어, `CustomerRepository`, `PostRepository`, `BabyRepository` 등 각 도메인별로 Repository 인터페이스를 만들어 코드 작성량을 크게 줄였습니다.
- 기본적인 CRUD 작업(`save`, `findById`, `delete`, `findAll` 등)은 별도 구현 없이 바로 사용할 수 있었습니다."

### 2. **메서드 이름 기반 쿼리 생성 (Query Method)**
"복잡한 SQL 작성 없이 메서드 이름만으로 쿼리를 생성하는 기능을 적극 활용했습니다.
- 예를 들어, `findCustomerEntityByEmail(String email)`, `findByCustomer_CustomerIdOrderByUpdateDateDesc(Long customerId)` 같은 메서드 이름을 사용하면 JPA가 자동으로 쿼리를 생성해줍니다.
- `findTop20ByTypeOrderByUpdateDateDesc(String type)`처럼 페이징과 정렬을 메서드 이름으로 표현했습니다.
- `findNext20ByCustomer_CustomerIdAndUpdateDateBeforeOrderByUpdateDateDesc`처럼 복잡한 조건도 메서드 이름으로 표현하여 무한 스크롤 기능을 구현했습니다.
- 이 방식으로 SQL 작성 시간을 절약하고, 컴파일 타임에 오류를 잡을 수 있어 유지보수성이 좋았습니다."

### 3. **JPQL을 활용한 복잡한 쿼리 작성**
"메서드 이름으로 표현하기 어려운 복잡한 쿼리는 `@Query` 어노테이션과 JPQL을 사용했습니다.
- 예를 들어, 최근 일주일 내 좋아요가 많은 게시글을 조회하는 쿼리: `SELECT p FROM PostEntity p WHERE p.updateDate >= :weekAgo ORDER BY p.likes DESC`
- 특정 타입의 게시글을 좋아요 순으로 조회: `SELECT p FROM PostEntity p WHERE p.type = :type ORDER BY p.likes DESC`
- Entity 객체 기반으로 쿼리를 작성하여 타입 안정성을 확보하고, 객체지향적인 쿼리 작성이 가능했습니다.
- `@Param` 어노테이션으로 파라미터를 명시적으로 바인딩하여 가독성을 높였습니다."

### 4. **Entity 연관관계 매핑 (양방향 관계)**
"JPA의 연관관계 매핑을 활용하여 객체 간 관계를 데이터베이스 외래키로 자동 매핑했습니다.
- `@OneToMany`와 `@ManyToOne`을 사용하여 양방향 연관관계를 설정했습니다.
- 예를 들어, `CustomerEntity`와 `PostEntity`는 일대다 관계입니다. `CustomerEntity`에는 `@OneToMany(mappedBy = "customer")`로, `PostEntity`에는 `@ManyToOne(fetch = FetchType.LAZY)`로 설정했습니다.
- `mappedBy` 속성을 사용하여 연관관계의 주인을 명확히 하고, 중복 매핑을 방지했습니다.
- `@JoinColumn(name = "customerId")`로 외래키 컬럼명을 명시적으로 지정했습니다."

### 5. **지연 로딩 (LAZY Loading) 전략**
"성능 최적화를 위해 지연 로딩을 적극 활용했습니다.
- 모든 `@ManyToOne` 관계에 `FetchType.LAZY`를 설정했습니다. 예를 들어, `PostEntity`의 `customer` 필드는 실제로 사용할 때까지 데이터베이스에서 조회하지 않습니다.
- 이렇게 하면 불필요한 조인 쿼리를 방지하여 N+1 문제를 해결하고, 애플리케이션 성능을 향상시킬 수 있습니다.
- 필요한 경우에만 연관된 엔티티를 로딩하여 메모리 사용량도 최적화했습니다."

### 6. **Cascade와 Orphan Removal 설정**
"엔티티 간 생명주기 관리와 데이터 정합성을 위해 Cascade와 Orphan Removal을 활용했습니다.
- `PostEntity`의 `likeEntities`에는 `cascade = CascadeType.ALL, orphanRemoval = true`를 설정하여 게시글이 삭제되면 관련 좋아요도 자동으로 삭제되도록 했습니다.
- `commentEntities`에는 `cascade = CascadeType.REMOVE`로 설정하여 게시글 삭제 시 댓글도 함께 삭제되도록 했습니다.
- `photo` 엔티티에는 `cascade = {CascadeType.PERSIST}`로 설정하여 게시글 저장 시 사진도 함께 저장되도록 했습니다.
- `orphanRemoval = true`를 설정하여 부모 엔티티에서 자식 엔티티를 제거하면 데이터베이스에서도 삭제되도록 보장했습니다."

### 7. **Entity 설계와 컬럼 제약조건**
"JPA 어노테이션을 활용하여 데이터베이스 스키마를 정의했습니다.
- `@Entity`, `@Table` 어노테이션으로 엔티티를 정의했습니다.
- `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`로 기본키를 자동 증가 방식으로 설정했습니다.
- `@Column(name = "title", nullable = false, length = 50)`처럼 컬럼 제약조건을 명시하여 데이터 무결성을 보장했습니다.
- `@ColumnDefault("false")`를 사용하여 기본값을 설정했습니다.
- `@UpdateTimestamp`를 활용하여 수정 시간을 자동으로 관리했습니다."

### 8. **영속성 컨텍스트와 트랜잭션 관리**
"JPA의 영속성 컨텍스트를 활용하여 변경 감지(Dirty Checking) 기능을 사용했습니다.
- `@Transactional` 어노테이션을 Service 레이어에 적용하여 트랜잭션 범위 내에서 엔티티의 변경사항을 자동으로 감지하고 데이터베이스에 반영했습니다.
- `save()` 메서드 호출 없이도 엔티티의 필드를 수정하면 자동으로 UPDATE 쿼리가 실행되었습니다.
- 이를 통해 불필요한 `save()` 호출을 줄이고, 코드를 더 간결하게 작성할 수 있었습니다."

### 9. **페이징 처리**
"대량의 데이터를 효율적으로 처리하기 위해 페이징 기능을 활용했습니다.
- `Pageable` 인터페이스를 사용하여 페이징 쿼리를 작성했습니다.
- 예를 들어, `findByLikesLessThanAndPostIdGreaterThanOrderByLikesDescPostIdAsc(Long likes, Long startPostId, Pageable pageable)` 같은 메서드로 커서 기반 페이징을 구현했습니다.
- 이를 통해 무한 스크롤 기능을 구현하고, 사용자에게 일정량씩 데이터를 제공했습니다."

### 10. **Default 메서드 활용**
"Repository 인터페이스에서 default 메서드를 활용하여 비즈니스 로직을 캡슐화했습니다.
- 예를 들어, `findTop10ByLikesWithinLastWeek()` 같은 default 메서드를 만들어 일주일 전 날짜를 계산하고 쿼리를 호출하는 로직을 Repository 레이어에 포함시켰습니다.
- 이를 통해 Service 레이어의 복잡도를 줄이고, 재사용 가능한 메서드를 만들 수 있었습니다."

### 11. **Hibernate의 자동 스키마 관리**
"개발 편의성을 위해 Hibernate의 DDL 자동 생성 기능을 활용했습니다.
- `application.yaml`에서 `ddl-auto: update`로 설정하여 애플리케이션 실행 시 스키마 변경사항을 자동으로 반영했습니다.
- `format_sql: true`로 설정하여 개발 중 생성되는 SQL을 보기 좋게 포맷팅했습니다.
- `dialect: org.hibernate.dialect.PostgreSQLDialect`로 PostgreSQL 방언을 명시하여 데이터베이스 특화 기능을 활용했습니다."

### 12. **Optional을 활용한 null 안전성**
"JPA Repository 메서드의 반환 타입으로 `Optional`을 활용하여 null 안전성을 확보했습니다.
- `Optional<CustomerEntity> findCustomerEntityByEmail(String email)`처럼 Optional을 반환하여 null 체크를 명시적으로 처리했습니다.
- `orElseThrow()`를 사용하여 엔티티가 없을 때 예외를 발생시키는 방식으로 처리했습니다."

---

## 🎯 핵심 요약 (30초 버전)

"JPA를 활용하여 데이터베이스 접근 계층을 구현했습니다. Spring Data JPA의 `JpaRepository`를 상속받아 기본 CRUD 기능을 자동으로 사용했고, 메서드 이름 기반 쿼리 생성으로 복잡한 쿼리도 간단하게 작성했습니다. `@OneToMany`, `@ManyToOne`으로 엔티티 간 연관관계를 매핑하고, `FetchType.LAZY`로 지연 로딩을 적용하여 성능을 최적화했습니다. `CascadeType`과 `orphanRemoval`로 엔티티 생명주기를 관리하고, `@Query`로 JPQL 쿼리를 작성하여 복잡한 조회도 구현했습니다."

---

## 💡 추가로 어필할 수 있는 점

1. **양방향 연관관계 설계**: `mappedBy`를 사용하여 연관관계의 주인을 명확히 구분한 점
2. **성능 최적화**: 지연 로딩과 페이징을 활용하여 대량 데이터 처리 성능을 고려한 점
3. **데이터 무결성**: `orphanRemoval`과 `CascadeType`을 적절히 활용하여 데이터 정합성을 보장한 점
4. **코드 간결성**: 메서드 이름 기반 쿼리와 영속성 컨텍스트의 변경 감지 기능으로 코드를 간결하게 작성한 점
5. **타입 안정성**: JPQL과 Optional을 활용하여 컴파일 타임에 오류를 잡을 수 있도록 한 점

---

## 📝 면접 팁

1. **구체적인 예시 언급**: 
   - "예를 들어, `PostRepository`에서 `findTop20ByTypeOrderByUpdateDateDesc`라는 메서드를 만들면..."
   - "`PostEntity`와 `CustomerEntity`의 관계를 설정할 때..."

2. **문제 해결 과정 설명**:
   - "처음에는 EAGER 로딩을 사용했는데, N+1 문제가 발생했습니다. 그래서 LAZY 로딩으로 변경하고..."
   - "게시글 삭제 시 관련 댓글이 삭제되지 않는 문제를 `orphanRemoval = true`로 해결했습니다"

3. **성능 고려 사항**:
   - "지연 로딩을 사용하여 불필요한 조인 쿼리를 방지했습니다"
   - "페이징을 통해 한 번에 많은 데이터를 조회하지 않도록 했습니다"

4. **JPA의 장점 강조**:
   - "SQL 작성 없이 객체지향적으로 데이터베이스를 다룰 수 있었습니다"
   - "영속성 컨텍스트의 변경 감지 기능으로 코드가 간결해졌습니다"

---

## 🔍 심화 질문 대비

**Q: N+1 문제를 어떻게 해결했나요?**
A: "지연 로딩을 기본 전략으로 사용하고, 필요한 경우에만 `@Query`에서 `JOIN FETCH`를 사용하여 연관 엔티티를 함께 조회했습니다."

**Q: 트랜잭션 격리 수준은 어떻게 설정했나요?**
A: "Spring의 기본 트랜잭션 설정을 사용했습니다. 필요에 따라 `@Transactional(isolation = Isolation.READ_COMMITTED)` 등으로 조정할 수 있습니다."

**Q: 대량 데이터 조회 시 성능 문제는 없었나요?**
A: "페이징과 지연 로딩을 활용하여 한 번에 조회하는 데이터량을 제한했습니다. 또한 인덱스를 활용할 수 있도록 쿼리 메서드 이름을 신중하게 작성했습니다."

