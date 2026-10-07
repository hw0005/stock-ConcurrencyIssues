package com.example.stock.service;

import org.springframework.stereotype.Service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;

import jakarta.transaction.Transactional;

@Service
public class PessimisticLockStockService {
	private final StockRepository stockRepository;
	
	
	public PessimisticLockStockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}
	
	// 재고감소 로직 작성
	@Transactional
	public void decrease(Long id, Long quantity) {
		Stock stock = stockRepository.findByIdWithPessimisticLock(id);
		
		stock.decrease(quantity);
		
		stockRepository.save(stock);
	}
	
	
}
