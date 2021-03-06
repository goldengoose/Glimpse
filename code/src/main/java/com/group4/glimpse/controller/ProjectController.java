package com.group4.glimpse.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.group4.glimpse.dao.UserDAO;
import com.group4.glimpse.model.Project;
import com.group4.glimpse.model.Project_State;
import com.group4.glimpse.model.Task;
import com.group4.glimpse.service.ProjectService;

/**
 * @author Group 4
 * Project controller
 */

@Controller
@RequestMapping(value="/project")
public class ProjectController {

	@Autowired
	ProjectService projectService;

	@Autowired
	UserDAO userDAO;

	/**
	 * Adding a project
	 * @param title
	 * @param description
	 * @param user_id
	 * @return
	 */
	//"?title="+$scope.projectTitle+"&description="+
	//	$scope.projectDescription+"&user_id="+currentUser.user_id;
	@RequestMapping(method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Project> addProject(@RequestParam("title") String title, 
			@RequestParam("description") String description,	
			@RequestParam("user_id") String user_id
			){	
		Project project = new Project();
		long idLong = Long.parseLong(user_id);
		project.setOwner(userDAO.read(idLong));
		project.setDescription(description);
		project.setTitle(title);

		Project_State state_planning =  new Project_State();
		state_planning.setProject_state_id(1);

		//When a project is just created, its status is planning.
		if(project.getState()==null){
			project.setState(state_planning);
		}

		project.getTeam().add(project.getOwner());

		project = projectService.create(project);
		return new ResponseEntity<Project>(project, HttpStatus.OK);	

	}

	/**
	 * Get a project
	 * @param id
	 * @param format
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET,value="{id}")
	@ResponseBody
	public ResponseEntity<Project> getProject(@PathVariable long id,
			@RequestParam(required=false) String format){

		Project project = projectService.read(id);
		if(project==null)
			return new ResponseEntity<Project>(project, HttpStatus.NOT_FOUND);
		else
			return new ResponseEntity<Project>(project, HttpStatus.OK);
	}

	/**
	 * Delete a project
	 * @param id
	 * @return
	 */
	@RequestMapping(method=RequestMethod.DELETE,value="{id}")
	@ResponseBody
	public ResponseEntity<Project> deleteProject(@PathVariable long id){

		Project project = projectService.delete(id);
		if(project==null)
			return new ResponseEntity<Project>(project, HttpStatus.NOT_FOUND);
		else
			return new ResponseEntity<Project>(project, HttpStatus.OK);
	}

	/**
	 * Update a project
	 * @param id
	 * @param project
	 * @param projectstate
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST,value="{id}")
	@ResponseBody
	public ResponseEntity<Project> updateProject(@PathVariable long id,
			@ModelAttribute Project project,
			@ModelAttribute Project_State projectstate){

		Project projectUpdate = projectService.read(id);

		if(project.getDescription()!=null){
			projectUpdate.setDescription(project.getDescription());
		}

		if(projectstate!=null){
			projectUpdate.setState(projectstate);
		}

		if(project.getTitle()!=null){
			projectUpdate.setTitle(project.getTitle());
		}

		//TODO add checks to update
		project = projectService.update(projectUpdate);
		if(project==null)
			return new ResponseEntity<Project>(project, HttpStatus.NOT_FOUND);

		return new ResponseEntity<Project>(project, HttpStatus.OK);
	}
	
	/**
	 * Get all tasks of a project
	 * @param project_id
	 * @return
	 */
	@RequestMapping(value="/{project_id}/tasks",method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List> getAllTasks(@PathVariable long project_id){
		System.out.println(project_id);
		List<Task> tasks = projectService.getAllTasks(project_id);
		return new ResponseEntity<List>(tasks, HttpStatus.OK);
	}
	
	/**
	 * Update Project State
	 * @param project_id
	 * @param state_value
	 * @return updated project
	 */
	@RequestMapping(value="/{project_id}/state/{state_value}",method=RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Project> updateState(@PathVariable long project_id,@PathVariable String state_value){
		
		Project project = projectService.updateState(project_id, state_value);
		
		if(project==null)
			return new ResponseEntity<Project>(project, HttpStatus.BAD_REQUEST);
		
		return new ResponseEntity<Project>(project, HttpStatus.OK);
	}
}