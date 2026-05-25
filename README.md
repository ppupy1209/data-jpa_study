
## 프로젝트 개요

Spring Data JPA의 핵심 기능을 학습하기 위한 프로젝트

회원과 팀 도메인을 기반으로 Repository 사용법, 쿼리 메서드, JPQL, 페이징, 벌크 연산, EntityGraph, Query Hint, Lock 기능을 실습  
단순 CRUD보다 Spring Data JPA가 제공하는 다양한 데이터 접근 기능을 직접 테스트하며 동작 방식 이해에 집중


---

## 기술 스택

### Backend

- Java 11
- Spring Boot 2.7.16
- Spring Web
- Spring Data JPA
- Lombok

### Database

- H2 Database

### Test

- JUnit 5
- AssertJ
- Spring Boot Test

### Build

- Gradle

---

## 주요 학습 내용

## 1. JpaRepository 기본 기능

`JpaRepository`를 상속해 기본 CRUD 기능 사용

```java
@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
}
```

### 학습 내용

- save
- findById
- findAll
- count
- delete
- 기본 CRUD 메서드 사용
- Repository 인터페이스 기반 데이터 접근

---

## 2. 쿼리 메서드

메서드 이름만으로 쿼리를 생성하는 Spring Data JPA 기능 학습

```java
List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
```

### 사용 예시

```java
@Test
public void findByUsernameAndGreaterThan() {
    Member m1 = new Member("AAA", 10);
    Member m2 = new Member("BBB", 20);

    memberRepository.save(m1);
    memberRepository.save(m2);

    List<Member> members =
            memberRepository.findByUsernameAndAgeGreaterThan("BBB", 15);

    assertThat(members.get(0).getAge()).isEqualTo(20);
}
```

### 학습 내용

- 메서드 이름 기반 쿼리 생성
- 조건절 자동 생성
- `And`, `GreaterThan` 키워드 사용
- 단순 조회 조건에서 빠른 Repository 작성

---

## 3. @Query 사용

복잡한 쿼리나 DTO 조회가 필요한 경우 `@Query` 사용

```java
@Query("select m from Member m where m.username = :username and m.age= :age")
List<Member> findUser(@Param("username") String username, @Param("age") int age);
```

### 학습 내용

- JPQL 직접 작성
- `@Param`을 활용한 파라미터 바인딩
- 메서드 이름이 길어지는 경우 `@Query`로 분리
- 복잡한 조건 조회 처리

---

## 4. 단순 값 조회

Entity 전체가 아니라 특정 컬럼만 조회

```java
@Query("select m.username from Member m")
List<String> findUsernameList();
```

### 학습 내용

- Entity 전체 조회와 단순 값 조회의 차이
- 필요한 데이터만 조회하는 방식
- 조회 결과 타입 지정

---

## 5. DTO 직접 조회

JPQL에서 DTO를 직접 생성해 조회

```java
@Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
List<MemberDto> findMemberDto();
```

### 학습 내용

- Entity를 그대로 반환하지 않고 DTO로 조회
- `select new` 문법 사용
- 회원과 팀 정보를 조인해 DTO로 변환
- API 응답이나 조회 전용 데이터 구성 방식 이해

---

## 6. 컬렉션 파라미터 바인딩

`in` 조건에 List 파라미터 전달

```java
@Query("select m from Member m where m.username in :names")
List<Member> findByNames(@Param("names") List<String> names);
```

### 학습 내용

- `IN` 조건 사용
- List 기반 파라미터 바인딩
- 여러 조건값을 한 번에 조회하는 방식

---

## 7. 페이징과 정렬

`Pageable`을 사용해 페이징과 정렬 처리

```java
@Query(
        value = "select m from Member m left join m.team t",
        countQuery = "select count(m) from Member m"
)
Page<Member> findByAge(int age, Pageable pageable);
```

### 테스트 예시

```java
@Test
public void paging() {
    memberRepository.save(new Member("m1", 10));
    memberRepository.save(new Member("m2", 10));
    memberRepository.save(new Member("m3", 10));
    memberRepository.save(new Member("m4", 10));
    memberRepository.save(new Member("m5", 10));

    PageRequest pageRequest =
            PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

    Page<Member> page = memberRepository.findByAge(10, pageRequest);

    Page<MemberDto> map =
            page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));
}
```

### 학습 내용

- `PageRequest` 사용
- 페이지 번호와 페이지 크기 지정
- 정렬 조건 적용
- `Page`를 DTO로 변환
- `countQuery` 분리
- 페이징 조회 성능 고려

---

## 8. 벌크 연산

여러 데이터를 한 번에 수정하는 벌크 업데이트 학습

```java
@Modifying(clearAutomatically = true)
@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
int bulkAgePlus(@Param("age") int age);
```

### 테스트 예시

```java
@Test
public void bulkUpdate() {
    memberRepository.save(new Member("m1", 10));
    memberRepository.save(new Member("m2", 19));
    memberRepository.save(new Member("m3", 20));
    memberRepository.save(new Member("m4", 21));
    memberRepository.save(new Member("m5", 40));

    int resultCount = memberRepository.bulkAgePlus(20);

    assertThat(resultCount).isEqualTo(3);
}
```

### 학습 내용

- `@Modifying` 사용
- 벌크 업데이트 수행
- 수정된 row count 반환
- 영속성 컨텍스트와 DB 상태 차이 이해
- `clearAutomatically = true` 사용 이유 학습

---

## 9. Fetch Join

지연 로딩으로 인한 추가 쿼리를 줄이기 위해 Fetch Join 사용

```java
@Query("select m from Member m" +
        " join fetch m.team")
List<Member> findMemberFetchJoin();
```

### 학습 내용

- LAZY 로딩과 N+1 문제 이해
- Fetch Join으로 연관 Entity 함께 조회
- JPQL 기반 조회 최적화

---

## 10. EntityGraph

Spring Data JPA의 `@EntityGraph`를 사용해 연관 Entity 함께 조회

```java
@Override
@EntityGraph(attributePaths = {"team"})
List<Member> findAll();

@EntityGraph(attributePaths = {"team"})
@Query("select m from Member m")
List<Member> findMemberEntityGraph();

@EntityGraph(attributePaths = {"team"})
List<Member> findEntityGraphByUsername(String username);
```

### 학습 내용

- Fetch Join 대신 EntityGraph 사용
- Repository 메서드에 연관 Entity 조회 옵션 적용
- 메서드 이름 기반 쿼리에도 EntityGraph 적용
- LAZY 연관관계 조회 최적화

---

## 11. Query Hint

읽기 전용 조회를 위해 Query Hint 적용

```java
@QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
Member findReadOnlyByUsername(String username);
```

### 테스트 예시

```java
@Test
public void queryHint() {
    Member member1 = new Member("member1", 10);
    memberRepository.save(member1);

    em.flush();
    em.clear();

    Member findMember = memberRepository.findReadOnlyByUsername("member1");
    findMember.setUsername("member2");

    em.flush();
}
```

### 학습 내용

- Hibernate readOnly Hint 사용
- 변경 감지 대상에서 제외되는 방식 이해
- 조회 전용 데이터에서 성능 최적화 가능성 학습

---

## 12. Lock

Repository 메서드에 비관적 락 적용

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
List<Member> findLockByUsername(String username);
```

### 학습 내용

- `@Lock` 사용
- `PESSIMISTIC_WRITE` 적용
- 동시 수정 상황에서 DB Lock 사용 방식 이해
- Spring Data JPA에서 Lock을 선언적으로 적용하는 방법 학습

---

## 13. Auditing

생성일, 수정일, 생성자, 수정자를 공통 Entity로 관리

```java
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;
}
```

### 학습 내용

- `@MappedSuperclass` 사용
- `@CreatedDate`
- `@LastModifiedDate`
- `@CreatedBy`
- `@LastModifiedBy`
- 공통 감사 필드 분리

---

## 도메인 구조

```text
Team
  └── Member

Member
  └── Team
```

---

## 주요 Entity

## 1. Member

회원 Entity

```java
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class Member extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
```

### 설계 포인트

- 회원과 팀은 다대일 관계
- 팀 연관관계는 LAZY 로딩으로 설정
- `changeTeam()`을 통해 양방향 연관관계 설정
- BaseEntity 상속으로 공통 감사 필드 관리

---

## 2. Team

팀 Entity

```java
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
}
```

### 설계 포인트

- 팀과 회원은 일대다 관계
- 연관관계의 주인은 Member
- Team은 mappedBy를 통해 읽기 측 관계로 설정

---

## Repository 구조

```text
MemberRepository
  ├── JpaRepository<Member, Long>
  └── MemberRepositoryCustom

MemberRepositoryCustom
  └── MemberRepositoryImpl
```

---

## 테스트 구성

Repository 기능별 테스트 작성

```java
@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;
}
```

### 테스트 항목

- 기본 CRUD
- 쿼리 메서드
- `@Query`
- DTO 조회
- 컬렉션 파라미터 바인딩
- 페이징
- 벌크 업데이트
- Fetch Join
- EntityGraph
- Query Hint
- Lock

---

## 학습한 내용

- Spring Data JPA Repository 기본 사용법
- 메서드 이름 기반 쿼리 생성
- JPQL과 `@Query`
- DTO 직접 조회
- 컬렉션 파라미터 바인딩
- 페이징과 정렬
- 벌크 연산과 영속성 컨텍스트 초기화
- Fetch Join과 EntityGraph
- Query Hint
- 비관적 락
- Auditing
- Custom Repository 구성

---

## 기술 선택 이유

## Spring Data JPA

반복적인 Repository 구현 코드를 줄이고 선언형 데이터 접근 방식을 학습하기 위해 사용

`JpaRepository`를 통해 기본 CRUD를 빠르게 처리하고, 메서드 이름 기반 쿼리와 `@Query`를 통해 다양한 조회 방식 학습

---

## H2 Database

로컬 환경에서 빠르게 Repository 테스트를 실행하기 위해 사용

별도 DB 설치 없이 Entity 매핑, JPQL, 페이징, 벌크 연산, Lock 동작 확인 가능

---

## EntityGraph

연관관계 조회 시 발생할 수 있는 N+1 문제를 해결하기 위한 방법으로 학습

Fetch Join과 비교하면서 Repository 메서드에 선언적으로 연관 Entity 조회 옵션을 적용하는 방식 확인

---

## Query Hint

조회 전용 데이터의 변경 감지를 줄이기 위한 방식으로 학습

Hibernate readOnly Hint를 사용해 영속성 컨텍스트의 변경 감지 대상에서 제외하는 흐름 확인

---

## 회고

Spring Data JPA는 단순 CRUD를 편하게 처리하는 도구를 넘어 다양한 조회와 최적화 기능을 제공

이 프로젝트를 통해 Repository 메서드 이름만으로 쿼리를 만들 수 있는 편의성과 복잡한 조회에서는 `@Query`, DTO 조회, EntityGraph, Fetch Join 등을 적절히 선택해야 한다는 점 학습

특히 벌크 연산은 DB에 직접 반영되지만 영속성 컨텍스트와 불일치가 생길 수 있어 `clearAutomatically`나 `flush`, `clear`가 필요한 이유 확인

---


## Reference

- 인프런 실전! 스프링 데이터 JPA
