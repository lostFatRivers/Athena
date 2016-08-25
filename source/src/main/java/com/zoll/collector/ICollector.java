package com.zoll.collector;

import com.zoll.action.ActionType;
import com.zoll.data.IBaseData;

public interface ICollector {
	public IBaseData getData(ActionType...keyWords);
}
