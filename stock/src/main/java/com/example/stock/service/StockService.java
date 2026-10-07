package com.example.stock.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;



@Service
public class StockService {
	
	private final StockRepository stockRepository;

	public StockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}
	
	// stock 조회, 재고를 감소한뒤, 갱신된 값 저장
	// 자바에서 지원하는 하나의 스레드만 쓰기 : synchronized 붙이기
	// 스프링의 @Transactional의 어노테이션: 클래스를 새로 만들어 실행함
	
//	@Transactional
//	public synchronized void decrease(Long id, Long quantity) {
	
	
	// named락
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void decrease(Long id, Long quantity) {
		// 조회
		Stock stock = stockRepository.findById(id).orElseThrow();
		
		// 재고 감소
		stock.decrease(quantity);
		
		// 저장
		stockRepository.saveAndFlush(stock);
	}
	
	
	
	
	

}
