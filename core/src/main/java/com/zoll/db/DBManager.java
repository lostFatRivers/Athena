package com.zoll.db;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoll.db.command.IDBCommand;

public class DBManager {
	private SessionFactory sessionFactory;
	private Logger logger = LoggerFactory.getLogger("root");
	
	public void init() {
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		} catch (Exception e) {
			StandardServiceRegistryBuilder.destroy( registry );
		}
		
	}
	
	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public void insert(IDBCommand command) {
		if (command.getEntity() != null && command.getEntity().size() > 0) {
			save(command.getEntity());
			return;
		}
		if (StringUtils.isEmpty(command.getTable().getSimpleName())) {
			logger.error("No table or entity in this command: ", command.toString());
			return;
		}
		String sql = buildSql(command, "insert into");
		Session session = getSession();
		session.beginTransaction();
		session.createQuery(sql);
		session.getTransaction().commit();
	}
	
	public void save(List<IEntity> entity) {
		Session session = getSession();
		session.beginTransaction();
		for (IEntity iEntity : entity) {
			session.save(iEntity);
		}
		session.getTransaction().commit();
	}
	
	private String buildSql(IDBCommand command, String actionType) {
		StringBuilder sb = new StringBuilder();
		sb.append(actionType).append(" ").append(command.getTable().getSimpleName());
		
		return sb.toString();
	}
}
