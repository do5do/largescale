package com.vsfe.largescale.service;

import com.vsfe.largescale.domain.Transaction;
import com.vsfe.largescale.domain.User;
import com.vsfe.largescale.model.PageInfo;
import com.vsfe.largescale.model.type.TransactionSearchOption;
import com.vsfe.largescale.repository.AccountJpaRepository;
import com.vsfe.largescale.repository.TransactionRepository;
import com.vsfe.largescale.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LargeScaleService {
	private final AccountJpaRepository accountJpaRepository;
	private final TransactionRepository transactionRepository;
	private final UserRepository userRepository;

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

	public void validateAccountNumber() {

	}

	public void aggregateTransactions() {

	}

	public void aggregateTransactionsWithSharding() {

	}
}
