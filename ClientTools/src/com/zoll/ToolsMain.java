package com.zoll;

import java.util.Map;

import com.zoll.config.ToolsConfigManager;
import com.zoll.work.IToolsWork;
import com.zoll.work.WorkType;

public class ToolsMain {
	public static void main(String[] args) {
		Map<WorkType, Map<String, String>> workConfigs = ToolsConfigManager.forName("config/toolsCfg.properties");
		for (WorkType type : workConfigs.keySet()) {
			IToolsWork work = type.getWork();
			work.recover(workConfigs.get(type));
			work.doTask();
		}
	}
}
