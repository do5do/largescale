package com.vsfe.largescale.repository;

import com.vsfe.largescale.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
	/**
	 * <p>
	 *     createDate 로만 정렬하면 원하는 값이 나오지 않을 수도 있다.
	 *     왜냐면 유저의 가입 시간이 같은 경우 어떻게 될까? 반드시 뒤에 정렬 기준을 하나 더 넣어줘야 한다.
	 *     참고) createDate desc 방향 인덱스가 없을 때 쿼리 실행 계획을 확인하면 Extra: Using FileSort 로 나온다.
	 *     FileSort 는 MySQL 내부적으로 sortBuffer(메모리 공간) 에 조회한 데이터를 옮겨서 정렬을 하기 때문에 데이터가 많아질 수록 느리다.
	 * </p>
	 * index: user_idx05 (<- 다른 사람이 볼 수 있도록 주석으로 인덱스 정보를 남겨준다.)
	 */
	@Query("""
		select u
		from User u
		order by u.createDate desc, u.id asc
		limit :count
		""")
	List<User> findRecentCreatedUsers(@Param("count") int count);
}
