package querydsl.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.Transactional;
import querydsl.querydsl.dto.MemberDto;
import querydsl.querydsl.dto.QMemberDto;
import querydsl.querydsl.entity.Member;
import querydsl.querydsl.entity.QMember;
import querydsl.querydsl.entity.QTeam;
import querydsl.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

	@Autowired
	EntityManager em;
	
	JPAQueryFactory queryFactory;
	QMember m;
	QTeam t;

	@BeforeEach
	public void before() {
		queryFactory = new JPAQueryFactory(em);
		m = QMember.member;
		t = QTeam.team;
		
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);
		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);
	}

	// JPQL용
	@Test
	public void startJPQL() {
		// member1을 찾아라.
		String qlString = "select m from Member m " + "where m.username = :username";
		Member findMember = em.createQuery(qlString, Member.class).setParameter("username", "member1")
				.getSingleResult();
		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void startQuerydsl() {
		// member1을 찾아라.
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		
		Member findMember = queryFactory.
				select(m).
				from(m).
				where(m.username.eq("member1"))// 파라미터 바인딩 처리
				.fetchOne();
		assertThat(findMember.getUsername()).isEqualTo("member1");	
	}
	
	
	@PersistenceUnit
	EntityManagerFactory emf;
	
	// 일반 조인
	@Test
	public void fetchJoinNo() {
		em.flush();
		em.clear();
		
		Member findMember = queryFactory
				.selectFrom(m)
				.where(m.username.eq("member1"))
				.fetchOne();
		
		
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치조인미적용").isFalse();
		
	}
	
	
	// 페치 조인
	@Test
	public void fetchJoinUse() {
		em.flush();
		em.clear();
		
		Member findMember = queryFactory
				.selectFrom(m)
				.join(m.team, t).fetchJoin()
				.where(m.username.eq("member1"))
				.fetchOne();
		
		
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("v페치조인적용").isTrue();
		
	}
	
	
	
	
	//---------------------------------------------------------------------------------
	// 프로젝션 결과 반환
	
	// 1. new Operation 방식
	// JPQL방식 
	@Test
	public void findDtoByJPQL() {
		em.createQuery("select new querydsl.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
		.getResultList();
	}
	
	// 2.QueryDSL
	@Test public void findDtoByConstructor() {
		List<MemberDto> result = queryFactory
				.select(Projections.constructor(MemberDto.class,
						m.username,
						m.age))
				.from(m)
				.fetch();
		
		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	
	}
	
	
	
	
	// 3. Projection 쓰기
	@Test 
	public void findDtoByQueryProjection() {
		List<MemberDto> result = queryFactory
		.select(new QMemberDto(m.username, m.age))
		.from(m)
		.fetch();
		
		for (MemberDto memberDto : result) {
			System.out.println("queryProjection = " + memberDto);
		}
	}
	
	//---------------------------------------------------------------------------------
	// 1. where 다중 파라미터 사용
	
	@Test
	public void dynamicQuery_WhereParam() {
		String usernameParam = "member1";
		Integer ageParam = 10;
		
		List<Member> result = searchMember2(usernameParam, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}
	
	private List<Member> searchMember2 (String usernameCond, Integer ageCond) {
		return queryFactory
				.selectFrom(m)
				.where(usernameEq(usernameCond), ageEq(ageCond))
				.fetch();
	}

	private BooleanExpression ageEq(Integer ageCond) {
		return ageCond != null ? m.age.eq(ageCond) : null;
	}

	private BooleanExpression usernameEq(String usernameCond) {
		if (usernameCond == null) {
			return null;
		}
		return m.username.eq(usernameCond);
	}
	
}
