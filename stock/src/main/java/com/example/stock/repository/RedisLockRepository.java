package com.example.stock.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisLockRepository {
	// 로직 실행 전 key, setIfAbsend로 lock 하기 -> 로직이 끝나면 unlock해서 락 해제
	
	
	// 레디스 명령어 실행 위한 템플릿 변수 추가
	private RedisTemplate<String,String> redisTemplate;
	
	
	// 생성자 추가
	public RedisLockRepository(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
	// setAbsent명령어 활용해서 락 하기
	public Boolean lock(Long key) {
		return redisTemplate
				.opsForValue()
				.setIfAbsent(generateKey(key), "lock", Duration.ofMillis(3000));
	}
	
	// unlock 메서드
	public Boolean unlock(Long key) {
		return redisTemplate.delete(generateKey(key));
	}
	
	// 키 생성
	private String generateKey(Long key) {
		return key.toString();
	}
	

}
