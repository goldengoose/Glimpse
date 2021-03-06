package com.group4.glimpse.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.group4.glimpse.dao.UserValidationDAO;
import com.group4.glimpse.model.EmailValidation;
import com.group4.glimpse.model.Project;
import com.group4.glimpse.model.User;
import com.group4.glimpse.service.EmailSender;
import com.group4.glimpse.service.EmailValidationService;
import com.group4.glimpse.service.UserService;

/**
 * @author Group 4
 * User controller
 */

@Controller
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	EmailValidationService emailValidationService;

	@Autowired
	EmailSender emailSender;

	/**
	 * Get all users
	 * @return
	 */
	@RequestMapping(value="/user",method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List> getAllUsers(){
		List<User> users = userService.getAllUsers();
		return new ResponseEntity<List>(users, HttpStatus.OK);
	}

	/**
	 * Check for login api call, if valid user or not
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/api/login",method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<User> getUser(@RequestBody User user){

		User valid_user = userService.getUser(user);
		if(valid_user==null)
			return new ResponseEntity<User>(user, HttpStatus.BAD_REQUEST);		
		else{
			valid_user.setPassword(null);
			return new ResponseEntity<User>(valid_user, HttpStatus.OK);
		}
	}

	/**
	 * Add user/Sign up api and send mail to the provided valid email
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/api/signup",method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> addUser(@RequestBody User user){		

		String response;
		EmailValidation emailValidation = new EmailValidation();
		emailValidation.setEmail(user.getEmail());
		emailValidation.setName(user.getName());
		emailValidation.setPassword(user.getPassword());
		emailValidation.setStatus("notvalidated");

		emailValidation = emailValidationService.create(emailValidation);
		try{
			emailSender.sendMail(user.getEmail(), "Validate Email By Clicking the link in Email",emailValidation.getIdEmailValidation());
			response = "Email-Validation sent to " +user.getEmail();
		}
		catch(Exception e){
			response = "Email-Validation could not be sent to " +user.getEmail();
		}
		return new ResponseEntity<String>("{ \"data\": "+response+"}", HttpStatus.OK);
	}

	/**
	 * Validating an user
	 * @param idEmailValidation
	 * @return
	 */
	@RequestMapping(value = "/validate",method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> validateEmail(@RequestParam("idEmailValidation") long idEmailValidation){		

		EmailValidation emailValidation = new EmailValidation();
		emailValidation = emailValidationService.read(idEmailValidation);
		if(emailValidation.getStatus().equalsIgnoreCase("notvalidated")){

			emailValidation.setStatus("validated");	
			emailValidationService.update(emailValidation);
			User user = new User();
			user.setEmail(emailValidation.getEmail());
			user.setName(emailValidation.getName());
			user.setPassword(emailValidation.getPassword());
			user = userService.create(user);
			user.setPassword(null);
			return new ResponseEntity<String>("User Validated! ", HttpStatus.OK);	
		}
		else{
			return new ResponseEntity<String>("User already validated! ", HttpStatus.OK);	
		}		
	}

	/**
	 * Add user/ By Google OAuth
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/auth/google",method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<User> addUserByAuth(@RequestBody User user){

		user = userService.createByAuth(user);
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	/**
	 * Check if the selected email is unique or not
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/api/users",method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> checkUniqueEmail(@ModelAttribute User user){

		User existing_user = userService.readEmail(user.getEmail());
		if(existing_user==null)
			return new ResponseEntity<String>("{ \"available\": true}", HttpStatus.OK);
		else
			return new ResponseEntity<String>("{ \"available\": false}", HttpStatus.OK);
	}

	/**
	 * Get projects of a user
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/api/{id}/projects",method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List> getProjects(@PathVariable long id){

		List<Project> projects = userService.getProjects(id);
		return new ResponseEntity<List>(projects, HttpStatus.OK);
	}

}