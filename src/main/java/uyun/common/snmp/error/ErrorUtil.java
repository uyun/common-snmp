package uyun.common.snmp.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * 错误工具类
 */
public class ErrorUtil {
	private static final Logger logger = LoggerFactory.getLogger(ErrorUtil.class);

	/**
	 * 生成一个更易于阅读的异常消息
	 *
	 * @param message
	 * @param e
	 * @return
	 */
	public static String createMessage(String message, Throwable e) {
		String errMsg;
		if (e instanceof ClassNotFoundException
				|| e instanceof NoClassDefFoundError)
			errMsg = e.getClass().getName() + "[" + e.getMessage() + "]";
		else
			errMsg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
		return message + ". ERROR: " + errMsg;
	}

	/**
	 * 生成一个更易于阅读的RuntimeException
	 *
	 * @param message
	 * @param e
	 * @return
	 */
	public static RuntimeException createRuntimeException(String message, Throwable e) {
		return new RuntimeException(createMessage(message, e), e);
	}

	/**
	 * 生成一个更易于阅读的Exception
	 *
	 * @param message
	 * @param e
	 * @return
	 */
	public static Exception createException(String message, Throwable e) {
		return new Exception(createMessage(message, e), e);
	}

	/**
	 * 生成一个更易于阅读的IllegalArgumentException
	 *
	 * @param message
	 * @param e
	 * @return
	 */
	public static IllegalArgumentException createIllegalArgumentException(String message, Throwable e) {
		return new IllegalArgumentException(createMessage(message, e), e);
	}

	/**
	 * 通过日志输出一个异常
	 *
	 * @param message
	 * @param e
	 */
	public static void warn(Logger logger, String message, Throwable e) {
		if (e instanceof NullPointerException)
			logger.warn(createMessage(message, e), e);
		else {
			logger.warn(createMessage(message, e));
			logger.debug("Stack: ", e);
		}
	}

	public static void exit(Logger logger, String message, Throwable e) {
		exit(logger, message, e, 1);
	}

	/**
	 * 直接退出当前JVM
	 *
	 * @param logger
	 * @param message
	 * @param e
	 * @param errorCode
	 */
	public static void exit(Logger logger, String message, Throwable e, int errorCode) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(os);

		out.println("Application error exit!");
		for (int i = 0; i < 80; i++)
			out.print("*");
		out.println();
		out.println();
		out.print("Message: ");
		out.println(message);
		if (e != null) {
			out.print("Error: ");
			e.printStackTrace(out);
		}
		out.println();
		for (int i = 0; i < 80; i++)
			out.print("*");
		logger.error(os.toString());
		Runtime.getRuntime().halt(errorCode);
	}
}
