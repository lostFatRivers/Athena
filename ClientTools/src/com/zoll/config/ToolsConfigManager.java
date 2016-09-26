package com.zoll.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.zoll.work.WorkType;

public class ToolsConfigManager {

	public static Map<WorkType, Map<String, String>> forName(String path) {
		Map<WorkType, Map<String, String>> map = new HashMap<WorkType, Map<String, String>>();
		
		Properties config = new Properties();
		try {
			config.load(new FileInputStream(new File(path)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<String> workChain = parseRootParam(config.getProperty("workChain"));
		if (workChain == null || workChain.isEmpty()) {
			return map;
		}
		
		for (String eachWorkName : workChain) {
			WorkType workType = WorkType.valueOf(eachWorkName.toUpperCase());
			if (workType == null) {
				throw new RuntimeException("WorkType is not exist, name: " + eachWorkName);
			}
			map.put(workType, filterWork(config, eachWorkName));
		}
		
		return map;
	}

	private static Map<String, String> filterWork(Properties config, String workName) {
		Map<String, String> map = new HashMap<String, String>();
		Set<Object> keySet = config.keySet();
		for (Object eachKey : keySet) {
			String str = eachKey.toString();
			if (str.indexOf(workName) >= 0) {
				String propertie = str.replaceAll(workName + ".", "");
				map.put(propertie, config.getProperty(str));
			}
		}
		return map;
	}

	private static List<String> parseRootParam(String property) {
		List<String> list = null;
		String[] split = property.split(",");
		if (split.length > 0) {
			list = Arrays.asList(split);
		}
		return list;
	}
	
}
