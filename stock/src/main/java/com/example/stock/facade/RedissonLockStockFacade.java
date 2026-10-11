package com.example.stock.facade;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.example.stock.service.StockService;

@Component
public class RedissonLockStockFacade {
	
	private RedissonClient redissonClient;
	
	private StockService stockService;
	
	public RedissonLockStockFacade(RedissonClient redissonClient, StockService stockService) {
		this.redissonClient = redissonClient;
		this.stockService = stockService;
	}
	
	public void decrease(Long id, Long quantity) {
		RLock lock = redissonClient.getLock(id.toString());
		
		// 락획득 시도 -> 몇 초동안 시도, 몇 초동안 점유, 락 획득
		try {
			boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS); // 실패 시 락 늘려주기
			
			if(!available) {
				System.out.println("Lock 획득 실패");
				return;
			}
			
			stockService.decrease(id, quantity);;
		} catch (InterruptedException e) {
			throw new RuntimeException();
		} finally {
			lock.unlock();
		}
	
		
	}
	
	
}
