package com.zoll.work;

import com.zoll.work.replacepng.ReplacePngWork;

public enum WorkType {
	/** 替换前端无效资源 */
	REPLACE_PNG {
		public IToolsWork getWork() {
			return new ReplacePngWork();
		}
	},
	
	
	;

	public abstract IToolsWork getWork();
}
