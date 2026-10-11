package com.example.stock.facade;

import org.springframework.stereotype.Component;

import com.example.stock.repository.RedisLockRepository;
import com.example.stock.service.StockService;

@Component
public class LettuceLockStockFacade {
	private final RedisLockRepository redisLockRepository;
	private final StockService stockService;
	
	public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
		this.redisLockRepository = redisLockRepository;
		this.stockService = stockService;
	}
	
	public void decrease(Long id, Long quantity) throws InterruptedException{
		// 락 획득 시도
		while(!redisLockRepository.lock(id)) {
			Thread.sleep(100);
		}
		
		try {
			stockService.decrease(id, quantity);
		}
		
		//획득 시 해제
		finally {
			redisLockRepository.unlock(id);
		}
	}
	
	

}
