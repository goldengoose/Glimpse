package com.group4.glimpse.service;

import java.util.List;

import com.group4.glimpse.model.Project;
import com.group4.glimpse.model.User;


public interface UserService {

	/**
	 * create a new user
	 * @param user
	 * @return
	 */
	public User create(User user);
	
	/**
	 * Update the User 
	 * @param user
	 * @return
	 */
	
	public User update(User user);
	
	/**
	 * create/signin user bu google auth
	 * @param user
	 * @return
	 */

	public User read(long id);
	
	public User createByAuth(User user);
	
	/**
	 * get a user
	 * @param user
	 * @return
	 */
	public User getUser(User user);
	
	/**
	 * get an user's email
	 * @param user
	 * @return
	 */
	public User readEmail(String email);
	
	/**
	 * get projects of a user
	 * @param id
	 * @return
	 */
	public List<Project> getProjects(long id);

	public List<User> getAllUsers();
	
}