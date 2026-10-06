package com.example.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import com.example.stock.service.StockService;

@SpringBootTest
public class StockServiceTest {
	
	@Autowired
	private StockService stockService;
	
	@Autowired
	private StockRepository stockRepository;
	
	
	@BeforeEach
	public void before() {
		stockRepository.saveAndFlush(new Stock(1L, 100L));
	}
	
	@AfterEach
	public void after() {
		stockRepository.deleteAll();
	}
	
	@Test
	public void 재고감소() {
		// 재고 감소
		stockService.decrease(1L, 1L);
		
		Stock stock = stockRepository.findById(1L).orElseThrow();
		
		assertEquals(99L, stock.getQuantity());
		
	}
	
	@Test
	public void 동시에_100개의_요청() throws InterruptedException {
		int threadCount = 100;
		
		// 비동기로 실행하는 작업을 단순화하여 사용하게 도와줌.
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		// 다른 스레드에서 작업이 완료될 때까지 대기
		CountDownLatch latch = new CountDownLatch(threadCount);
		
		for (int i=0; i<threadCount; i++) {
			executorService.submit(() -> {
				try {
					stockService.decrease(1L, 1L);
				}
				finally {
					latch.countDown();
				}
			});
		}
		
		latch.await();
		Stock stock = stockRepository.findById(1L).orElseThrow();
		// 100개 저장 후 1개씩 100개 감소 즉 0개 될 것을 예상
		assertEquals(0, stock.getQuantity());
		
		}
	

}
