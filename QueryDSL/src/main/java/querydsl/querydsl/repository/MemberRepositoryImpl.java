package querydsl.querydsl.repository;

import static org.springframework.util.StringUtils.hasText;
import static querydsl.querydsl.entity.QMember.member;
import static querydsl.querydsl.entity.QTeam.team;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import querydsl.querydsl.dto.MemberTeamDto;
import querydsl.querydsl.dto.QMemberTeamDto;
import querydsl.querydsl.entity.Member;

// 1. abstract 제거
public class MemberRepositoryImpl implements MemberRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	public MemberRepositoryImpl(EntityManager em) {
		this.jpaQueryFactory = new JPAQueryFactory(em);
	}

	// 2. MemberTeamDto로 리턴 타입 일치
	@Override
	public List<MemberTeamDto> search(MemberSearchCondition condition) {
		return jpaQueryFactory.select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
				.from(member).leftJoin(member.team, team)
				.where(usernameEq(condition.getUsername()), teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe()))
				.fetch();
	}

	// 3. hasText() 및 static import된 member, team 사용
	private BooleanExpression usernameEq(String username) {
		return hasText(username) ? member.username.eq(username) : null;
	}

	private BooleanExpression teamNameEq(String teamName) {
		return hasText(teamName) ? team.name.eq(teamName) : null;
	}

	// 4. 누락되었던 ageGoe, ageLoe 메서드 추가
	private BooleanExpression ageGoe(Integer ageGoe) {
		return ageGoe != null ? member.age.goe(ageGoe) : null;
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		return ageLoe != null ? member.age.loe(ageLoe) : null;
	}
	
	
	@Override
	public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
		
		// 알아서 totalCount Query 가져와 -> 최적화 불능
		QueryResults<MemberTeamDto> results =  jpaQueryFactory
				.select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
				.from(member)
				.leftJoin(member.team, team)
				.where(usernameEq(condition.getUsername()), teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe())
						)
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetchResults();
		
		List<MemberTeamDto> content = results.getResults();
		long total = results.getTotal();

		return new PageImpl<>(content, pageable, total);
	}
	
	
	@Override
	public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
		
		// 내가 직접 TotalQueryCount 가져와 -> left Join이 필요없을 때도 있음, 최적화
		List<MemberTeamDto> content =  jpaQueryFactory
				.select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
				.from(member)
				.leftJoin(member.team, team)
				.where(usernameEq(condition.getUsername()), teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe())
						)
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();
		
		// 1
//		long total = jpaQueryFactory
//		.select(member)
//		.from(member)
//		.leftJoin(member.team, team)
//		.where(
//				usernameEq(condition.getUsername())
//				, teamNameEq(condition.getTeamName())
//				,ageGoe(condition.getAgeGoe())
//				, ageLoe(condition.getAgeLoe())
//		)
//		.fetchCount();
		
//		return new PageImpl<>(content, pageable, total);

		// 2
		JPAQuery<Member> countQuery = jpaQueryFactory
				.select(member)
				.from(member)
				.leftJoin(member.team, team)
				.where(
						usernameEq(condition.getUsername())
						, teamNameEq(condition.getTeamName())
						,ageGoe(condition.getAgeGoe())
						, ageLoe(condition.getAgeLoe())
				);
		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);

	}
}