package com.vsfe.largescale.repository;

import com.vsfe.largescale.domain.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionJpaRepository extends JpaRepository<Transaction, Long> {
	@Query("""
		select t
		from Transaction t
		where t.senderAccount = :account
		order by t.transactionDate desc, t.id asc
		limit :limit
		""")
	List<Transaction> findTransactionBySenderAccount(
		@Param("account") String account, @Param("limit") int limit);

	@Query("""
		select t
		from Transaction t
		where t.receiverAccount = :account
		order by t.transactionDate desc, t.id asc
		limit :limit
		""")
	List<Transaction> findTransactionByReceiverAccount(
		@Param("account") String account, @Param("limit") int limit);

	/**
	 * or 조건은 앞의 조건과 뒤의 조건이 같은 인덱스를 사용하면, 인덱스를 탄다.
	 */
	@Query("""
		select t
		from Transaction t
		where t.senderAccount = :account
		and ((t.transactionDate < :transactionDate) or (t.transactionDate = :transactionDate and t.id > :id))
		order by t.transactionDate desc
		limit :limit
		""")
	List<Transaction> findTransactionBySenderAccountWithPageToken(
		@Param("account") String account,
		@Param("transaction") Instant transactionDate,
		@Param("id") int id,
		@Param("limit") int limit
	);

	@Query("""
		select t
		from Transaction t
		where t.receiverAccount = :account
		and ((t.transactionDate < :transactionDate) or (t.transactionDate = :transactionDate and t.id > :id))
		order by t.transactionDate desc
		limit :limit
		""")
	List<Transaction> findTransactionByReceiverAccountWithPageToken(
		@Param("account") String account,
		@Param("transaction") Instant transactionDate,
		@Param("id") int id,
		@Param("limit") int limit
	);
}
