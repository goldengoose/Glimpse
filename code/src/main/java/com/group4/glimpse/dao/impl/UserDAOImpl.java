package com.group4.glimpse.dao.impl;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.group4.glimpse.dao.UserDAO;
import com.group4.glimpse.model.Project;
import com.group4.glimpse.model.User;

@Repository
public class UserDAOImpl implements UserDAO {

	@Autowired
	SessionFactory sessionFactory;

	/**
	 * DAO implementation of creating a new user
	 */
	public User create(User user) {

		Session session = sessionFactory.openSession();
		Transaction tx =  session.beginTransaction();
		try{
			session.save(user);
			tx.commit();
		} catch(HibernateException h) {
			tx.rollback();
		} finally {
			session.close();
		}
		return user;
	}

	/**
	 * DAO Implementation to get user by reading email
	 */
	public User readEmail(String email) {

		Session session = sessionFactory.openSession();
		Transaction tx =  session.beginTransaction();
		User user = null;
		List<User> users;
		try{
			String hql = "FROM com.group4.glimpse.model.User as user WHERE user.email = :email";
			Query query = session.createQuery(hql);
			query.setString("email", email);
			query.setMaxResults(1);
			users = query.list();
			if(users.size()!=0)
				user = users.get(0);
			tx.commit();
		} catch(HibernateException h) {
			tx.rollback();
		} finally {
			session.close();
		}
		return user;
	}

	/**
	 * DAO implementation of updating a user
	 */
	public User update(User user) {

		Session session = sessionFactory.openSession();
		Transaction tx =  session.beginTransaction();
		try{
			session.update(user);
			tx.commit();
		} catch(HibernateException h) {
			tx.rollback();
		} finally {
			session.close();
		}
		return user;		
	}

	/**
	 * DAO Implementation to get all projects of a user
	 */
	@Override
	public List<Project> getProjects(User user) {
		Session session = sessionFactory.openSession();
		Transaction tx =  session.beginTransaction();
		List<Project> projects = null;
		try{
			String hql = "FROM com.group4.glimpse.model.Project as p where :user in elements(p.team)";
			Query query = session.createQuery(hql);
			query.setEntity("user", user);
			projects = query.list();
			tx.commit();
		} catch(HibernateException h) {
			tx.rollback();
		} finally {
			session.close();
		}
		return projects;
	}

	/**
	 * DAO Implementation to get a user
	 */
	@Override
	public User getUser(long id) {
		Session session = sessionFactory.openSession();
		Transaction tx =  session.beginTransaction();
		User user = null;
		try{
			user = (User)session.get(User.class,id);
			tx.commit();
		} catch(HibernateException h){
			tx.rollback();
		} finally{
			session.close();
		}
		return user;
	}

	/**
	 * DAO Implementation to get all users of Glimpse
	 */
	@Override
	public List<User> getAllUsers() {
		Session session = sessionFactory.openSession();
		Transaction tx =  session.beginTransaction();
		List<User> users=null;
		try{
			String hql = "FROM com.group4.glimpse.model.User as u" ;
			Query query = session.createQuery(hql);
			users = query.list();		
			tx.commit();
		} catch(HibernateException h){
			tx.rollback();
		} finally{
			session.close();
		}
		return users;
	}

	/**
	 * DAO Implementation to read/get a user
	 */
	@Override
	public User read(long id) {
		Session session = sessionFactory.openSession();
		Transaction tx =  session.beginTransaction();
		User user = null;
		try{
			user = (User) session.get(User.class, id);
			tx.commit();
		} catch(HibernateException h) {
			tx.rollback();
		} finally {
			session.close();
		}
		return user;

	}

}
