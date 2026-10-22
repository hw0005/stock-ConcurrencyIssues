package querydsl.querydsl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import querydsl.querydsl.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

	List<Member> findByUsername(String usename);

}
