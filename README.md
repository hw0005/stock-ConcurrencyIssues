# Spring Boot 백엔드 성능 최적화 & 동시성 제어 프로젝트

"단순 CRUD 구현에 머물지 않고, 대용량 DB 조회 최적화와 동시성 문제까지 직접 코드로 풀고 검증한 기록"

JPA 영속성 컨텍스트의 특성을 이해한 상태에서 CUD(생성/수정/삭제)와 Read(조회)의 역할을 명확히 분리하고, Redisson 분산락과 QueryDSL 5.0 Custom Repository를 이용해 데이터 정합성과 조회 성능을 모두 챙긴 학습 및 구현 프로젝트입니다.

---

## 무엇을 했는가?

- Read 최적화 (QueryDSL): Entity 대신 DTO Direct Projection으로 조회 성능 및 메모리 절감
- Count 쿼리 Skip 구현: PageableExecutionUtils를 이용해 필요할 때만 Count 쿼리가 나가도록 최적화
- QueryDSL Deprecated 해결: .fetchCount() 빨간줄 잡고 JPAQuery<Long> + countQuery::fetchOne으로 리팩토링
- CUD 동시성 제어: Redisson Pub/Sub 분산락 커스텀 AOP 구현으로 선착순 쿠폰/재고 차감 Race Condition 해결
- 개인정보 보안: JPA AttributeConverter 활용, AES-256 + Salt 양방향 암호화 적용

---

## CUD vs Read 역할 분리 (CQRS-lite)

대용량 트래픽과 동시 요청 상황을 고려해 CUD(명령)와 Read(조회)의 목적을 아래와 같이 명확하게 분리하여 구현했습니다.

### 1. CUD (Command) : 데이터 정합성 보장
- **목적:** Race Condition 방지 및 데이터 깨짐/초과 차감 완전 차단
- **락(Lock) 전략:** Redisson Pub/Sub 분산락을 활용해 스핀락 부하 없이 동시 접근 제어
- **조회/상태 변경 방식:** JPA Entity 활용 (영속성 컨텍스트 관리 및 Dirty Checking을 통한 안전한 업데이트)
- **핵심 스택:** @Transactional + Redisson 커스텀 AOP

### 2. Read (Query) : 조회 성능 최적화
- **목적:** 빠른 응답 속도 확보 및 DB CPU / Disk I/O 부하 최소화
- **락(Lock) 전략:** Lock 미사용 (대용량 조회 시 병목을 없애며 필요시 Redis 캐싱 적용)
- **조회 방식:** DTO Direct Projection (Entity 변환 오버헤드 없이 SELECT 절에서 필요한 컬럼만 추출)
- **핵심 스택:** QueryDSL JPAQueryFactory + Custom Repository Implementation

---

## 핵심 트러블슈팅

### 1. QueryDSL 페이징 최적화 & fetchCount()로 필요한 쿼리만 조회

**문제 의식:**
- 대용량 데이터 페이징 조회 시 매번 무거운 COUNT(*) 쿼리가 나가면서 DB 부하가 발생함.
- 기존 레거시 코드나 예제에서 쓰던 .fetchCount(), .fetchResults() 경고(Deprecated)가 발생하고 자바 메서드 레퍼런스 문법 에러가 터짐.

**내가 해결한 방식:**
- **DTO Direct Projection**: select(new QMemberTeamDto(...))를 써서 Entity 조회 오버헤드를 없앰.
- Count 쿼리 분리: leftJoin을 싹 제거한 JPAQuery<Long> countQuery를 별도로 정의.
- Count Skip 적용: PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne)을 활용해 첫 페이지 데이터 수가 페이징 사이즈보다 적거나 마지막 페이지일 때는 DB Count 쿼리 자체를 스킵하게 만듦.

```java
@Override
public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {

    // 1. DTO Direct Projection (Entity 오버헤드 축소)
    List<MemberTeamDto> content = jpaQueryFactory
            .select(new QMemberTeamDto(
                    member.id, 
                    member.username, 
                    member.age, 
                    team.id, 
                    team.name
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    // 2. Count 쿼리 최적화 (불필요한 조인 제거 및 JPAQuery<Long> 분리)
    JPAQuery<Long> countQuery = jpaQueryFactory
            .select(member.count())
            .from(member)
            .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName())
            );

    // 3. 조건 충족 시 Count 쿼리 Skip (fetchOne 메서드 레퍼런스 전달)
    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
}
```

---

## 향후 확장 계획: K-Pass AI 플랫폼 구축

위에서 검증한 핵심 아키텍처(CUD 분산락 제어, Read QueryDSL 최적화, 보안)를 기반으로, 대규모 트래픽을 처리하는 **공연 티켓팅 및 AI 맞춤 추천 플랫폼(K-Pass AI)**으로 확장 구현합니다.

1. 선착순 티켓팅 및 쿠폰 발급 동시성 제어
   - Redisson Pub/Sub 분산락을 실전 도메인(공연 예매, 한정 수량 쿠폰)에 적용하여 순간 급증 트래픽 상황에서 데이터 초과 발급 및 꼬임 현상 방지.

2. 10만 건+ 대용량 공연 데이터 조회 최적화
   - 공공데이터 Open API로 수집한 대규모 공연 데이터베이스에 QueryDSL DTO Direct Projection 및 Count Query Skip을 적용해 DB I/O 부하 최소화.

3. LLM AI 추천 파이프라인 결합
   - 사용자 프롬프트 요청 시 LLM 응답을 JSON DTO로 파싱하고, 이를 QueryDSL 동적 쿼리 조건으로 바로 매핑하여 맞춤형 공연 검색 결과 반환.

4. 결제 트랜잭션 및 보상 트랜잭션 구현
   - 토스페이먼츠(PG) 연동 및 결제 실패 시 예매 상태 원복(보상 트랜잭션)을 통한 결제 정합성 확보.

5. Scale-Out 분산 환경 구축 및 부하 테스트
   - Docker Compose + Nginx 기반 다중 서버 환경을 구축하고, K6/JMeter 부하 테스트를 통해 응답 속도 및 TPS 수치화 검증.
