package com.zoll.db.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zoll.db.IEntity;

public class DBCommandFactory {
	
	public IDBCommand createCommandByEntity(final IEntity entity) {
		return new IDBCommand() {
			
			@Override
			public Class<?> getTable() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<IEntity> getEntity() {
				List<IEntity> list = new ArrayList<IEntity>();
				list.add(entity);
				return list;
			}
			
			@Override
			public Map<String, String> getCondition() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}
	
	
}
