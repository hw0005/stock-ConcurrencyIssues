package querydsl.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import querydsl.querydsl.entity.Member;
import querydsl.querydsl.repository.MemberRepository;

@SpringBootTest
@Transactional

public class MemberRepositoryTest {
	@Autowired
	EntityManager em;
	
	@Autowired
	MemberRepository memberRepository;
	
	@Test
	public void basicTest() {
		Member member = new Member("member1", 10);
		memberRepository.save(member);
		
		Member findMember = memberRepository.findById(member.getId()).get();
		assertThat(findMember).isEqualTo(member);
		
		
		List<Member> result1 = memberRepository.findAll();
		assertThat(result1).containsExactly(member);
		
		List<Member> result2 = memberRepository.findByUsername("member1");
		assertThat(result2).containsExactly(member);
	
	}
}
