package com.example.stock.service;

import org.springframework.stereotype.Service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;

@Service
public class StockService {
	
	private final StockRepository stockRepository;

	public StockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}
	
	// stock 조회, 재고를 감소한뒤, 갱신된 값 저장
	public void decrease(Long id, Long quantity) {
		//조회
		Stock stock = stockRepository.findById(id).orElseThrow();
		stock.decrease(quantity);
		
		// save 대신 saveAndFlush를 쓰면 트랜잭션 끝나는 시점이 아닌 그 줄에서 즉시 DB로 넘김 
		stockRepository.saveAndFlush(stock);
		
	}
	
	
	

}
