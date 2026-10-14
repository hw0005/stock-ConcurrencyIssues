package com.example.aes256salt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.aes256salt.util.Sha256CryptoUtil;


public class Sha256CryptoUtilTest {

	private Sha256CryptoUtil sha256CryptoUtil;

	@BeforeEach
	void setUp() {
		sha256CryptoUtil = new Sha256CryptoUtil();
	}

	@Test
	@DisplayName("비밀번호 해시화 및 비밀번호 일치 검증 성공")
	void hashPasswordAndMatchesSuccess() {
		// given
		String rawPassword = "myPassword123!";
		String salt = sha256CryptoUtil.generateSalt();

		// when
		String hashedPassword = sha256CryptoUtil.hashPassword(rawPassword, salt);
		boolean isMatched = sha256CryptoUtil.matches(rawPassword, salt, hashedPassword);

		// then
		assertThat(hashedPassword).isNotEqualTo(rawPassword);
		assertThat(isMatched).isTrue();
	}

	@Test
	@DisplayName("틀린 비밀번호 입력 시 로그인 검증 실패")
	void matchesFailWithWrongPassword() {
		// given
		String rawPassword = "myPassword123!";
		String wrongPassword = "wrongPassword999!";
		String salt = sha256CryptoUtil.generateSalt();

		// when
		String hashedPassword = sha256CryptoUtil.hashPassword(rawPassword, salt);
		boolean isMatched = sha256CryptoUtil.matches(wrongPassword, salt, hashedPassword);

		// then
		assertThat(isMatched).isFalse();
	}

	@Test
	@DisplayName("동일한 비밀번호라도 Salt가 다르면 해시값이 다르게 생성된다")
	void differentSaltDifferentHash() {
		// given
		String rawPassword = "myPassword123!";
		String salt1 = sha256CryptoUtil.generateSalt();
		String salt2 = sha256CryptoUtil.generateSalt();

		// when
		String hash1 = sha256CryptoUtil.hashPassword(rawPassword, salt1);
		String hash2 = sha256CryptoUtil.hashPassword(rawPassword, salt2);

		// then
		assertThat(hash1).isNotEqualTo(hash2);
	}

}
