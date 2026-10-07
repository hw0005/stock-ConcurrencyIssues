package com.example.stock.transaction;

import com.example.stock.service.StockService;

public class TransactionStockService {
	
	// @Transactional이 없다면 실제로 동작하는 방식임. start -> decrease -> end
	
	private StockService stockService;
	
	public TransactionStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	public void decrease(Long id, Long quantity) {
		startTransaction();
		stockService.decrease(id, quantity);
		endTransaction();
	}
	
	private void startTransaction() {
		System.out.println("Transaction Start");
	}
	
	private void endTransaction() {
		System.out.println("Transaction End");
	}
	
	
}
