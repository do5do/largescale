package com.vsfe.largescale.repository;

import com.vsfe.largescale.domain.Transaction;
import com.vsfe.largescale.model.PageInfo;
import com.vsfe.largescale.model.type.TransactionSearchOption;
import com.vsfe.largescale.util.C4PageTokenUtil;

import lombok.RequiredArgsConstructor;

import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionRepository {
	private final TransactionJpaRepository transactionJpaRepository;

	/**
	 * pageToken 을 사용하지 않고 cursor 페이징 쿼리를 호출한다.
	 *
	 * @param accountNumber
	 * @param option
	 * @param count
	 * @return
	 */
	public PageInfo<Transaction> findTransactionWithoutPageToken(
		String accountNumber,
		TransactionSearchOption option,
		int count
	) {
		var data = switch (option) {
			case SENDER -> transactionJpaRepository.findTransactionBySenderAccount(accountNumber, count + 1);
			case RECEIVER -> transactionJpaRepository.findTransactionByReceiverAccount(accountNumber, count + 1);

			// sender, receiver 두가지 쿼리를 모두 날려서 데이터를 합친 후 애플리케이션에서 정렬한다.
			case ALL -> mergeAllOptions(
				transactionJpaRepository.findTransactionBySenderAccount(accountNumber, count + 1),
				transactionJpaRepository.findTransactionByReceiverAccount(accountNumber, count + 1),
				count + 1);
		};

		return PageInfo.of(data, count, Transaction::getTransactionDate, Transaction::getId);
	}

	/**
	 * pageToken 을 포함하여 cursor 페이징 쿼리를 호출한다.
	 * <p>
	 * pageToken 파싱해서 날짜와 id를 가져온다.
	 * 그걸 기반으로 위와 동일하게 쿼리 날린다.
	 * 그리고 그 결과로 pageInfo를 만들어주고 반환한다.
	 * </p>
	 * @param accountNumber
	 * @param pageToken
	 * @param option
	 * @param count
	 * @return
	 */
	public PageInfo<Transaction> findTransactionWithPageToken(
		String accountNumber,
		String pageToken,
		TransactionSearchOption option,
		int count
	) {
		var pageData = C4PageTokenUtil.decodePageToken(pageToken, Instant.class, Integer.class);
		var transactionDate = pageData.getLeft();
		var transactionId = pageData.getRight();

		var data = switch (option) {
			case SENDER -> transactionJpaRepository.findTransactionBySenderAccountWithPageToken(
				accountNumber, transactionDate, transactionId, count + 1);
			case RECEIVER -> transactionJpaRepository.findTransactionByReceiverAccountWithPageToken(
				accountNumber, transactionDate, transactionId, count + 1);
			case ALL -> mergeAllOptions(
				transactionJpaRepository.findTransactionBySenderAccountWithPageToken(
					accountNumber, transactionDate, transactionId, count + 1),
				transactionJpaRepository.findTransactionByReceiverAccountWithPageToken(
					accountNumber, transactionDate, transactionId, count + 1),
				count + 1);
		};

		return PageInfo.of(data, count, Transaction::getTransactionDate, Transaction::getId);
	}

	/**
	 * 두 결과를 합쳐서, 데이터를 정렬 조건에 맞춰 count개 만큼 가져온다.
	 *
	 * @param senderResult
	 * @param receiverResult
	 * @param count
	 * @return
	 */
	private List<Transaction> mergeAllOptions(
		List<Transaction> senderResult,
		List<Transaction> receiverResult,
		int count
	) {
		return ListUtils.union(senderResult, receiverResult).stream()
			.sorted(
				Comparator.comparing(Transaction::getTransactionDate).reversed()
					.thenComparing(Transaction::getId)
			)
			.limit(count)
			.toList();
	}
}
