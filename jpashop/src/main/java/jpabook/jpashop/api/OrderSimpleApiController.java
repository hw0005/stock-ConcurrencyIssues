package jpabook.jpashop.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.SimpleOrderQueryDto;
import lombok.RequiredArgsConstructor;

/**
 * xToOne(ManyToOne, OneToOne)의 성능 최적화
 * Order
 * Order -> Memeber
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
	
	private final OrderRepository orderRepository;
	
	// 그냥 보내기
	@GetMapping("/api/v1/simple-order")
	public List<Order> ordersV1() {
		List<Order> all = orderRepository.findAllByString(new OrderSearch());
		
		for (Order order : all) {
	        order.getMember().getName(); // Lazy 강제 초기화
	        order.getDelivery().getAddress(); // Lazy 강제 초기화
	    }
		return all;
	}
	
	
	// 2. N+1문제 확인
	@GetMapping("/api/v2/simple-order")
	public List<SimpleOrderQueryDto> ordersV2() {
		
		// ORDER 2개
		// N+1=> 1 + 회원 N + 배송 N => 5개
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		
		//ORDER -> SQL 1번조회 -> 2번 조회? 이게 뭔 말이지
		List<SimpleOrderQueryDto> result = orders.stream()
				.map(o -> new SimpleOrderQueryDto(o))
				.collect(Collectors.toList());
		return result;
	}
	
	
	
	// 3. LAZY + FETCH JOIN(패치 조인)
	@GetMapping("/api/v3/simple-order")
	public List<SimpleOrderQueryDto> ordersV3() {
		List<Order> orders = orderRepository.findAllWithMemberDelivery();
		List<SimpleOrderQueryDto> result = orders.stream()
		.map(o -> new SimpleOrderQueryDto(o))
		.collect(Collectors.toList());
		
		return result;
	}
	
	
}
