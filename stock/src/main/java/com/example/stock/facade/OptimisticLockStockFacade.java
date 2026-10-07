package com.example.stock.facade;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import com.example.stock.service.OptimisticLockStockService;

@Component
public class OptimisticLockStockFacade {
	
	// 업데이트 못했을 때 다시 retry해주기 위한 컴포넌트
	
	private final OptimisticLockStockService optimisticLockStockService;
	
	public OptimisticLockStockFacade(OptimisticLockStockService optimisticLockStockService) {
		this.optimisticLockStockService = optimisticLockStockService;
	}
	
	public void decrease(Long id, Long quantity) throws InterruptedException {
		
		while (true) {
			try {
				optimisticLockStockService.decrease(id, quantity);
				break;
			}
			catch(ObjectOptimisticLockingFailureException e) {
				Thread.sleep(50);
			}
		}
	}
}
