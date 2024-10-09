package com.vsfe.largescale.controller;

import com.vsfe.largescale.domain.Transaction;
import com.vsfe.largescale.domain.User;
import com.vsfe.largescale.model.PageInfo;
import com.vsfe.largescale.model.type.TransactionSearchOption;
import com.vsfe.largescale.service.LargeScaleService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 예시 중심의 세미나이므로,
 * 실제 서비스와는 달리 사용하는 API만 Controller 형태로 구성했습니다.
 */
@RestController
@RequestMapping("/service")
@RequiredArgsConstructor
public class LargeScaleController {
	private final LargeScaleService largeScaleService;

	/**
	 * Step 1. 기본적인 쿼리의 최적화를 수행해 봅시다.
	 * <p>
	 * 인덱스가 desc인지 asc인지 확인해야 한다.
	 * 조회 조건에 맞는 인덱스를 사용하자.
	 * (인덱스 없이 조회하는 경우 설정한 socketTimeout 시간 내에 쿼리가 완료되지 못한다.)
	 * </p>
	 */
	@GetMapping("/user-info")
	public List<User> getUserInfo(@RequestParam @Positive @Max(100) int count) {
		return largeScaleService.getUserInfo(count);
	}

	/**
	 * Step 2. 페이징을 활용한 쿼리 최적화 방식에 대해 고민해 봅시다.
	 * <p>
	 * 페이징은 cursor, offset 두 가지 방식이 있다.
	 * - offset: 특정 위치부터 몇 개를 들고 오겠다.
	 * - cursor: 이 값 이후 몇 개를 들고 오겠다.
	 * 이 중 cursor 방식을 사용한다.
	 * </p>
	 */
	@GetMapping("/get-transactions")
	public PageInfo<Transaction> getTransactions(
		@RequestParam @NotEmpty String accountNumber,
		@RequestParam(required = false) String pageToken,
		@RequestParam @NotNull TransactionSearchOption option,
		@RequestParam @Positive @Max(100) int count
	) {
		return largeScaleService.getTransactions(accountNumber, pageToken, option, count);
	}

	/**
	 * Step 3. Full Scan 을 수행해야 하는 로직은 어떻게 수행해야 할까요?
	 */
	public void validateAccountNumber() {

	}

	/**
	 * Step 4. 통계성 집계를 병렬로 수행해 봅시다.
	 */
	public void aggregateTransactions() {

	}

	/**
	 * Step 5. 데이터를 샤딩한다면 어떻게 될까요?
	 */
	public void aggregateTransactionsWithSharding() {

	}
}
