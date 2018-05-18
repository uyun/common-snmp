package uyun.common.snmp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class SpeedController {
	private static final Logger logger = LoggerFactory.getLogger(SpeedController.class);
	private static final String KEY_PREFIX_IS_BATCH = "isBatch.";
	private static final String KEY_PREFIX_SNMP_INTERVAL = "snmp.interval.";

	private static SpeedController inst = new SpeedController();
	private Properties pro = null;
	private int defaultTime = 50;
	private boolean isBatchDef = true;
	private Map<String, Date> synTimeMapInfo = new HashMap<String, Date>();

	public SpeedController() {
		String file = System.getProperty("user.dir") + "/conf/SnmpSynTime.properties";
		pro = new Properties();
		try {
			pro.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			logger.trace(String.format("加载Snmp发包配置信息出错。错误：文件[%s]不存在", file));
			logger.trace("堆栈：", e);
		} catch (IOException e) {
			logger.trace(String.format("加载Snmp发包配置信息出错。错误：%s", e));
			logger.trace("堆栈：", e);
		}

		fillFromSystemProperties();

		if (logger.isDebugEnabled())
			logProperties();

		defaultTime = getTimeByIp("0.0.0.0", defaultTime);
		isBatchDef = isBatch("0.0.0.0", isBatchDef);
	}

	private void logProperties() {
		logger.debug("snmp.properties.count = \t{}", pro.size());
		for (Map.Entry<Object, Object> entry : pro.entrySet()) {
			logger.debug("snmp.properties.{} = \t{}", entry.getKey(), entry.getValue());
		}
	}

	private void fillFromSystemProperties() {
		Enumeration<?> keys = System.getProperties().propertyNames();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement().toString();
			if (key.startsWith(KEY_PREFIX_IS_BATCH))
				pro.put(key, System.getProperty(key));
			else if (key.startsWith(KEY_PREFIX_SNMP_INTERVAL))
				pro.put(key.substring(KEY_PREFIX_SNMP_INTERVAL.length()), System.getProperty(key));
		}
	}

	public static SpeedController getInstance() {
		return inst;
	}

	private int getTimeByIp(String ip, int defaultTime) {
		if (pro != null) {
			try {
				Object value = pro.get(ip);
				if (value == null)
					return defaultTime;
				return Integer.parseInt(value.toString());
			} catch (Exception e) {
			}
		}
		return defaultTime;
	}

	public void synTime(String ip) {
		while (synTimeMapInfo.get(ip) != null
				&& System.currentTimeMillis() - ((Date) synTimeMapInfo.get(ip)).getTime() < getTimeByIp(ip, defaultTime)) {
			try {
				Thread.sleep(System.currentTimeMillis() - ((Date) synTimeMapInfo.get(ip)).getTime() > 0 ? System
						.currentTimeMillis() - ((Date) synTimeMapInfo.get(ip)).getTime() : 1);
			} catch (Exception e) {
			}
		}
		updateSynTimeInfo(ip);
	}

	private void updateSynTimeInfo(String ip) {
		synchronized (synTimeMapInfo) {
			synTimeMapInfo.put(ip, new Date());
		}
	}

	public boolean isBatch(String ip) {
		return isBatch(ip, isBatchDef);
	}

	private boolean isBatch(String ip, boolean bol) {
		if (pro != null) {
			try {
				Object value = pro.get(KEY_PREFIX_IS_BATCH + ip);
				if (value == null)
					return bol;
				return Boolean.parseBoolean(value.toString());
			} catch (Exception e) {
			}
		}
		return bol;
	}

}
