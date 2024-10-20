package com.vsfe.largescale.util;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class C4QueryExecuteTemplate {
	/**
	 * Cursor Paging 을 적용하여 Select 를 수행한 후, 조회된 결과로 비즈니스 로직을 수행한다.
	 * @param limit
	 * @param selectFunction
	 * @param resultConsumer
	 * @param <T>
	 */
	public static <T> void selectAndExecuteWithCursor(
		int limit,
		Function<T, List<T>> selectFunction,
		Consumer<List<T>> resultConsumer
	) {
		List<T> resultList = null;
		do {
			resultList = selectFunction.apply(resultList != null ? resultList.get(resultList.size() - 1) : null);
			if (!resultList.isEmpty()) {
				resultConsumer.accept(resultList);
			}
		} while (resultList.size() >= limit);
	}

	/**
	 * Cursor Paging을 적용하여 Select를 수행한 후, 조회된 결과로 비즈니스 로직을 수행한다.
	 * 단, iteration 횟수가 pageLimit에 도달한 경우 검색 결과와 상관없이 중단한다.
	 * @param pageLimit 실행 횟수 제한 (음수인 경우, 제한 없음)
	 * @param limit
	 * @param selectFunction
	 * @param resultConsumer
	 * @param <T>
	 */
	public static <T> void selectAndExecuteWithCursorAndPageLimit(
		int pageLimit,
		int limit,
		Function<T, List<T>> selectFunction,
		Consumer<List<T>> resultConsumer
	) {
		log.info("queryExecuteTemplate start: {}", Thread.currentThread().getName());

		if (pageLimit < 0) {
			selectAndExecuteWithCursor(limit, selectFunction, resultConsumer);
			return;
		}

		var iterationCount = 0;
		List<T> resultList = null;
		do {
			resultList = selectFunction.apply(resultList != null ? resultList.get(resultList.size() - 1) : null);
			if (!resultList.isEmpty()) {
				log.info("queryExecuteTemplate result: {}", resultList.get(0).toString());
				resultConsumer.accept(resultList);
			}
			if (++iterationCount >= pageLimit) {
				break;
			}
		} while (resultList.size() >= limit);
	}
}
