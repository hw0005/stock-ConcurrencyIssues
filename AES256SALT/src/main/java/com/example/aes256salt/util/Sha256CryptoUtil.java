package com.example.aes256salt.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.aes256salt.exception.CryptoException;

@Component
public class Sha256CryptoUtil {
	private static final int SALT_SIZE = 16;
	private static final int STRETCH_COUNT = 10000; // 10,000번 반복 해싱 (키 스트레칭)

	// 1. 회원가입 시 유저별 고유 Salt 생성
	public String generateSalt() {
		byte[] salt = new byte[SALT_SIZE];
		SecureRandom random = new SecureRandom();
		random.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	// 2. 비밀번호 + Salt -> SHA-256 암호화 (단방향 해싱)
	public String hashPassword(String rawPassword, String salt) {
		if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(salt)) {
			throw new IllegalArgumentException("비밀번호와 Salt는 필수값입니다.");
		}

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] saltBytes = Base64.getDecoder().decode(salt);

			
			// Salt 주입 및 1차 해싱
			digest.update(saltBytes);
			byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));

			// 키 스트레칭 (10,000번 해싱 반복 연산으로 해킹 방지)
			for (int i = 0; i < STRETCH_COUNT; i++) {
				digest.reset();
				hashBytes = digest.digest(hashBytes);
			}

			return Base64.getEncoder().encodeToString(hashBytes);
		} catch (Exception e) {
			throw new CryptoException("SHA-256 해시 처리 중 오류가 발생했습니다.", e);
		}
	}

	// 3. 로그인 시 비밀번호 일치 여부 검증
	public boolean matches(String rawPassword, String salt, String expectedHash) {
		if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(salt) || !StringUtils.hasText(expectedHash)) {
			return false;
		}

		String actualHash = hashPassword(rawPassword, salt);
		return actualHash.equals(expectedHash);
	}

}
