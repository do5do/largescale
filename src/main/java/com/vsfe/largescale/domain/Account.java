package com.vsfe.largescale.domain;

import java.time.Instant;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
// @ToString // -> 순환참조를 주의해서 사용해야 한다.
@Table(name = "account")
public class Account {
	public static final String ACCOUNT_PREFIX = "3333";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_id", nullable = false)
	private Integer id;

	@Size(max = 13)
	@NotNull
	@Column(name = "account_number", nullable = false, length = 13)
	private String accountNumber;

	@NotNull
	@Column(name = "user_id", nullable = false)
	private Integer userId;

	@NotNull
	@Column(name = "account_type", nullable = false)
	private Character accountType;

	@Size(max = 200)
	@Column(name = "memo", length = 200)
	private String memo;

	@NotNull
	@ColumnDefault("0")
	@Column(name = "balance", nullable = false)
	private Long balance;

	@NotNull
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "create_date", nullable = false)
	private Instant createDate;

	@Column(name = "recent_transaction_date")
	private Instant recentTransactionDate;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * 계좌번호 검증
	 * - ex) 3333-00-5194250
	 * - 은행 고유 번호 : 3333
	 * - 숫자인지 확인
	 * - -로 나눴을 때 3 파트로 나뉘는지 확인
	 * <p>
	 *     오류 번호 검증 (맨 끝자리 수; 0)
	 *     00: 0 * 1 + 0 * 2
	 *     5194250: 5 * 1 + 1 * 2 + 9 * 3 + 4 * 4 + 2 * 5 + 5 * 6
	 * </p>
	 */
	public boolean validateAccountNumber() {
		var accountTokens = accountNumber.split("-");

		if (accountTokens.length != 3) {
			return false;
		}

		if (!StringUtils.equals(ACCOUNT_PREFIX, accountTokens[0])) {
			return false;
		}

		if (!StringUtils.isNumeric(accountTokens[1]) || !StringUtils.isNumeric(accountTokens[2])) {
			return false;
		}

		var secondPartsSum = getValidateSum(accountTokens[1]);
		var thirdPartsSum = getValidateSum(accountTokens[2].substring(0, accountTokens[2].length() - 1));

		return (secondPartsSum + thirdPartsSum) % 10 == (accountTokens[2].charAt(accountTokens[2].length() - 1) - '0');
	}

	/**
	 * 계좌번호 조각의 검증 로직을 수행한다.
	 * @param accountNumberPart
	 * @return
	 */
	private int getValidateSum(String accountNumberPart) {
		return IntStream.range(0, accountNumberPart.length())
			.mapToObj(idx -> (idx + 1) * (accountNumberPart.charAt(idx) - '0'))
			.reduce(0, Integer::sum);
	}
}