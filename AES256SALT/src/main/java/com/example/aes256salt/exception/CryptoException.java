package com.example.aes256salt.exception;

public class CryptoException extends RuntimeException {
	
	// Runtime예외 상속받은 crypto예외로 감싸서 던지면 코드 지저분해지는 것 방지
	public CryptoException(String message, Throwable cause) {
		super(message, cause);
	}
}
