package com.vsfe.largescale.repository;

import java.util.List;

import com.vsfe.largescale.domain.Account;

import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountJpaRepository extends JpaRepository<Account, Long> {
	@Query("""
		select a
		from Account a
		order by a.id
		limit :size
		""")
	List<Account> findAccount(@Param("size") int size);

	// 페이징 쿼리
	@Query("""
		select a
		from Account a
		where a.id > :lastAccountId
		order by a.id
		limit :size
		""")
	List<Account> findAccountWithLastAccountId(
		@Param("lastAccountId") int lastAccountId,
		@Param("size") int size
	);

	@Query("""
		SELECT a
		FROM Account a
		WHERE a.userId = :userId
		ORDER BY a.id
		LIMIT :size
		""")
	List<Account> findAccountByUserId(
		@Param("userId") int userId,
		@Param("size") int size
	);

	@Query("""
		SELECT a
		FROM Account a
		WHERE a.userId = :userId AND a.id > :lastAccountId
		ORDER BY a.id
		LIMIT :size
		""")
	List<Account> findAccountByUserIdWithLastUserId(
		@Param("userId") int userId,
		@Param("lastAccountId") int lastAccountId,
		@Param("size") int size
	);
}
