package com.zoll.db.command;

import java.util.List;
import java.util.Map;

import com.zoll.db.IEntity;

public interface IDBCommand {

	public List<IEntity> getEntity();
	
	public Class<?> getTable();
	
	public Map<String, String> getCondition();
}
