package querydsl.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import querydsl.querydsl.entity.Hello;
import querydsl.querydsl.entity.QHello;
import querydsl.querydsl.repository.MemberRepository;

@SpringBootTest
@Transactional
class QueryDslApplicationTests {
	
	@Autowired
	EntityManager em;
	
	@Autowired
	MemberRepository memberRepository;
	
	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);
		
		JPAQueryFactory query = new JPAQueryFactory(em);
		QHello qHello = new QHello("h");
		
		Hello result = query
				.selectFrom(qHello)
				.fetchOne();
		
		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
	}

}
