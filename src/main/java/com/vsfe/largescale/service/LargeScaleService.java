package com.vsfe.largescale.service;

import com.vsfe.largescale.core.C4ThreadPoolExecutor;
import com.vsfe.largescale.domain.Account;
import com.vsfe.largescale.domain.Transaction;
import com.vsfe.largescale.domain.User;
import com.vsfe.largescale.model.PageInfo;
import com.vsfe.largescale.model.type.TransactionSearchOption;
import com.vsfe.largescale.repository.AccountRepository;
import com.vsfe.largescale.repository.TransactionRepository;
import com.vsfe.largescale.repository.UserRepository;
import com.vsfe.largescale.util.C4QueryExecuteTemplate;
import com.vsfe.largescale.util.C4StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LargeScaleService implements InitializingBean {
	private static final int LIMIT_SIZE = 1000;

	/**
	 * 일반적으로 ThreadPool을 Bean으로 선언해서 사용하는 편인데, (요청이 들어올 때 마다 스레드풀이 과도하게 생성되는 것을 방지하기 위함)
	 * 학습 목적으로 여기서는 그런 과정을 수행하지 않습니다.
	 * CompletableFuture 에 대해 아시면 다른 방식으로도 가능합니다. (default ForkJoinPool 을 사용한 처리 가능)
	 */
	private final C4ThreadPoolExecutor threadPoolExecutor = new C4ThreadPoolExecutor(8, 32);
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;
	private final UserRepository userRepository;

	@Override
	public void afterPropertiesSet() throws Exception {
		threadPoolExecutor.init();
	}

	/**
	 * 최신 유저의 목록을 가져온다. (count 만큼)
	 *
	 * @param count
	 * @return
	 */
	public List<User> getUserInfo(int count) {
		return userRepository.findRecentCreatedUsers(count);
	}

	/**
	 * 특정 사용자에 대한 거래 내역을 가져온다.
	 *
	 * @param accountNumber
	 * @param pageToken
	 * @param option
	 * @param size
	 * @return
	 */
	public PageInfo<Transaction> getTransactions(
		String accountNumber,
		String pageToken,
		TransactionSearchOption option,
		int size
	) {
		if (pageToken == null) {
			return transactionRepository.findTransactionWithoutPageToken(
				accountNumber, option, size);
		} else {
			return transactionRepository.findTransactionWithPageToken(
				accountNumber, pageToken, option, size);
		}
	}

	/**
	 * 계좌가 올바른 계좌인지 검증한다.
	 * <p>
	 *     다 들고 오는 건 무리라서 쪼개서 들고 온다.
	 *     1000건 단위로 가져오는 경우 -> 총 2천만개 데이터이면 2만번 쿼리해서 부정합 데이터를 확인한다.
	 *     - 1000건으로 검증 (LIMIT_SIZE)
	 *     - 1000이 안되면 컷 / 넘기면 다음 조회
	 *     1000건 단위로 데이터를 조회하는 로직은 공통화가 가능하고 재사용이 충분히 가능하기에 유틸리티화 시킨다. (C4QueryExecuteTemplate)
	 *     참고) 개발 테스트용으로 몇 페이지만 조회하도록 만든다. (-> pageSize)
	 * </p>
	 */
	public void validateAccountNumber(int pageSize) {
		C4QueryExecuteTemplate.<Account>selectAndExecuteWithCursorAndPageLimit(
			pageSize,
			LIMIT_SIZE,
			lastAccount -> accountRepository.findAccountByLastAccountId(
				lastAccount == null ? null : lastAccount.getId(), 1000),
			accounts -> accounts.forEach(this::validateAccount)
		);
	}

	/**
	 * 유저 정보를 기반으로 Account와 Transaction 테이블을 파티셔닝한다.
	 * <P>
	 *     1. 유저 정보를 들고온다.
	 *     2. 유저 정보를 기반으로 Account를 가져온다.
	 *     3. Account를 기반으로 Transaction을 가지고 온다.
	 *     (이정도면 마이그레이션 치고 쉬운편..)
	 * </P>
	 * @param pageSize 개발용 limit 설정
	 */
	public void migrationData(int pageSize) {
		C4QueryExecuteTemplate.<User>selectAndExecuteWithCursorAndPageLimit(
			pageSize, // 개발용 limit 설정
			LIMIT_SIZE, // 다음 데이터가 있는지 확인하는 용도
			// count만큼 유저를 조회한다.
			lastUser -> userRepository.findUsersWithLastUserId(
				lastUser == null ? 0 : lastUser.getId(), 1000),
			// 조회한 유저 데이터를 loop 돌면서 병렬로 다음 작업을 수행한다.
			users -> users.forEach(this::migrateUserInfo)
		);

		threadPoolExecutor.waitToEnd(); // forEach를 통해 병렬로 수행한 작업이 끝날때까지 기다림
	}

	private void validateAccount(Account account) {
		if (!account.validateAccountNumber()) {
			log.error("invalid accountNumber - accountNumber: {}", account.getAccountNumber());
		}
	}

	/**
	 * 유저 정보로 Account 조회 및 마이그레이션, Account 정보로 Transaction 조회 및 마이그레이션
	 */
	private void migrateUserInfo(User user) {
		var groupId = user.getGroupId();

		threadPoolExecutor.execute(() -> {
			C4QueryExecuteTemplate.<Account>selectAndExecuteWithCursor(
				LIMIT_SIZE,
				lastAccount -> accountRepository.findAccountByUserIdAndLastAccountId(
					user.getId(), lastAccount == null ? null : lastAccount.getId(), LIMIT_SIZE),
				accounts -> {
					// account 삽입 - Bulk insert
					accountRepository.saveAll(groupId, accounts);

					// transaction 조회 후 삽입
					accounts.forEach(account -> transactionRepository.selectAndMigrate(
						account, C4StringUtil.format("transaction_migration_doit_{}", groupId)));
				});
		});
	}

}
