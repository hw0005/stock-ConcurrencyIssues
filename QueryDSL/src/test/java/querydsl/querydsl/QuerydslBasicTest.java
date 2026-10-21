package querydsl.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.Transactional;
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
}
