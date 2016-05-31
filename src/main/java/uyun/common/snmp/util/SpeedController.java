package uyun.common.snmp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SpeedController {
	private static final Logger logger = LoggerFactory.getLogger(SpeedController.class);

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
		defaultTime = getTimeByIp("0.0.0.0", defaultTime);
		isBatchDef = isBatch("0.0.0.0", isBatchDef);
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
				Object value = pro.get("isBatch." + ip);
				if (value == null)
					return bol;
				return Boolean.parseBoolean(value.toString());
			} catch (Exception e) {
			}
		}
		return bol;
	}

}
