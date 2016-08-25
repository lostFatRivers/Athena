package com.zoll.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.jdbc.Work;

import com.zoll.db.UserEntity;

public class TestMain {
	public static void main(String[] args) {
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
		SessionFactory sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		Session session = sessionFactory.openSession();
		long date = new Date().getTime();
		Transaction transaction = session.beginTransaction();
		createObjectInsert(session);
		transaction.commit();
//		doWorkInsert(session);
		System.out.println(new Date().getTime() - date);
	}
	
	/**
	 * ºÄÊ±  90+ ms
	 * 
	 * @param session
	 */
	public static void doWorkInsert(Session session) {
		Work work = new Work() {
			
			private PreparedStatement prepareStatement;

			@Override
			public void execute(Connection connection) throws SQLException {
					StringBuilder sb = new StringBuilder("insert into user(name) values ('WWWWW')");
					for (int i = 0; i < 2000; i++) {
						sb.append(",('WWWWW')");
					}
					prepareStatement = connection.prepareStatement(sb.toString());
					prepareStatement.execute();
			}
			
		};
		
		session.doWork(work);
	}
	
	/**
	 * ºÄÊ± 400+ ms
	 * 
	 * @param session
	 */
	public static void createObjectInsert(Session session) {
		for (int i = 0; i < 2; i++) {
			UserEntity user = new UserEntity("WWWWWW");
			session.save(user);
			if (i == 1000) {
				System.out.println(user.getId());
			}
		}
	}
}
