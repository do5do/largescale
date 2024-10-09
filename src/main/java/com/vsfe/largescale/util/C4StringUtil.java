package com.vsfe.largescale.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.slf4j.helpers.MessageFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class C4StringUtil {
	/**
	 * slf4j의 logger 가 쓰는 방식으로 String 포매팅
	 * String.format() 끔찍하게 느리다고 함.. 사용 금지
	 * @param format
	 * @param objects
	 * @return
	 */
	public static String format(String format, Object... objects) {
		return MessageFormatter.arrayFormat(format, objects).getMessage();
	}
}
