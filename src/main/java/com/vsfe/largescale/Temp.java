package com.vsfe.largescale;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.vsfe.largescale.util.C4StringUtil;

public class Temp {
	/**
	 * keepAliveTime : thread가 너무 많이 차있으면 idle 상태로 대기하고 있는다. 그때 살아있는 시간인가?
	 * LinkedBlockingQueue는 내부적으로 LinkedList를 사용해서 데이터가 무한정 들어갈 수 있다.
	 * 큐를 ArrayBlockingQueue로 바꾸면 capacity를 지정해줘야하고, capacity를 넘어가면 reject 에러가 난다.
	 */
	public static AtomicInteger data = new AtomicInteger();
	private static Random random = new Random();

	public static void main(String[] args) {
		var threadCount = 10;
		var queueSize = 100;
		var threadPoolExecutor = new ThreadPoolExecutor(threadCount, queueSize, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>());

		// 이렇게 수행하면 무한히 돌아간다.. threadPool이 종료되지 않으면 안 끝난다. -> shutdown()을 해줘야 함.
		for (int i = 0; i <= 100; i++) {
			int finalI = i;
			threadPoolExecutor.execute(() -> {
				System.out.println(C4StringUtil.format("Hello from {}", finalI));
				try {
					Thread.sleep(random.nextInt(100));
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				add(finalI);
			});
		}

		threadPoolExecutor.shutdown(); // shutdown은 종료를 시도하는 상태. terminate는 종료가 된 상태?

		while (true) {
			try {
				if (threadPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
					break;
				}
			} catch (InterruptedException e) {
				System.out.println("interrupted!");
			}
		}
		System.out.println("total : " + data);
	}

	// synchronized(105ms) 와 AtomicInteger(27ms) 시간을 비교해보면 엄청 차이난다.
	// 성능 측정을 정확히 하려면 JVM WormUp 을 해줘야 한다고.. 그냥 돌리면 별 차이 안난다.
	public static void add(int i) {
		data.addAndGet(i);
	}
}
