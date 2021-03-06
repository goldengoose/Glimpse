glimpse.controller('ProjectHomeController', function($scope, DataService, NgTableParams, $window,$uibModal,ProjectProgressService,UserProgressService,TaskRatioService) {

	var phc = this;
	var tmpList = [];

	// set fallback arrays
	var beforeUpdatenewTasks = [];
	var beforeUpdateassignedTasks = [];
	var beforeUpdatestartedTasks = [];
	var beforeUpdatefinishedTasks = [];
	var beforeUpdatecanceledTasks = [];
	
	$scope.revert = false;
	
	$scope.allTasks = [];
	$scope.newTasks = [];
	$scope.assignedTasks = [];
	$scope.startedTasks = [];
	$scope.finishedTasks = [];
	$scope.canceledTasks = [];

	phc.inPlanning = false;
	phc.statusColor = "rgba(255, 228, 0, 0.7)";

	/**ng init for fetching all projects of an user**/
	phc.getProjectDetails = function() {
		
		phc.projectProgressData = [];
		phc.userProgressData = [];
		phc.taskRatioData = [];
		
		$scope.currentUser = {
				name : $window.localStorage.currentUserName,
				email : $window.localStorage.currentUserEmail,
				user_id : $window.localStorage.currentUserId
		};

		phc.project_id = $window.localStorage.project_id;

		delete $window.localStorage.project_id;
		
		// get users project details
		DataService.getData("/glimpse/project/"+phc.project_id,[])
		.success(function(data) {
			$scope.projectDetails = data;
			// get project tasks
			if($scope.projectDetails.state.project_state_id == 1){
				phc.inPlanning = true;
			}
			updateProjectColor($scope.projectDetails.state.project_state_id);
			$scope.ownerId =  data.owner.id
			getProjectTasks();

		}).error(function(err){
			console.log("Error getting the project details");
		});	
	};

	//update project status to next
	phc.updateProjectStatus = function(){
		var curr_state = $scope.projectDetails.state.project_state_id;
		console.log(curr_state);
		if(curr_state >= 3){
			return;
		}else if(curr_state == 1){
			//can go to on-going
			if($scope.assignedTasks.length > 0){
				$scope.projectDetails.state.project_state_id = 2;
				DataService.putData("/glimpse/project/"+phc.project_id+"/state/ongoing",[])
				.success(function(updatedProject) {
					$scope.projectDetails.state = updatedProject.state;
					updateProjectColor(2);
				}).error(function(err){

				});
			}
		}else{
			// can go to completed
			if($scope.assignedTasks.length == 0 && $scope.finishedTasks.length > 0 && $scope.newTasks.length == 0){
				$scope.projectDetails.state.project_state_id = 4;
				DataService.putData("/glimpse/project/"+phc.project_id+"/state/completed",[])
				.success(function(updatedProject) {
					console.log(updatedProject);
					$scope.projectDetails.state = updatedProject.state;
					updateProjectColor(4);
				}).error(function(err){

				});
			}
		}
	}


	// delete task
	phc.deleteTask= function(index, task_id){
		console.log(task_id);
		DataService.deleteData("/glimpse/task/"+task_id,[])
		.success(function(data) {
			console.log("before",$scope.newTasks);
			$scope.newTasks.splice(index,1);
			console.log("after",$scope.newTasks);
		}).error(function(err){
			console.log("Error getting the project details");
		});
	}

	//edit assignee
	phc.editAssignee = function(array,index,task_id, newState){
		console.log("newState",newState);
		assignAssigneeModal(array,task_id, newState);
	}

	//Get project tasks
	function getProjectTasks(){

		newTasks = [];
		assignedTasks = [];
		startedTasks = [];
		finishedTasks = [];
		canceledTasks = [];

		DataService.getData("/glimpse/project/"+phc.project_id+"/tasks")
		.success(function(data) {
			console.log("tasks",data);
			//assign tasks by task state
			$scope.allTasks = data;
			for(var i=0;i<data.length;i++){
				if(data[i].state.value=="new")
					newTasks.push(data[i]);
				else if(data[i].state.value=="assigned")
					assignedTasks.push(data[i]);
				else if(data[i].state.value=="started")
					startedTasks.push(data[i]);
				else if(data[i].state.value=="finished")
					finishedTasks.push(data[i]);
				else if(data[i].state.value=="cancelled")
					canceledTasks.push(data[i]);
			}

			$scope.newTasks = newTasks;
			$scope.assignedTasks = assignedTasks;
			$scope.startedTasks = startedTasks;
			$scope.finishedTasks = finishedTasks;
			$scope.canceledTasks = canceledTasks;
			//Updating charts on data
			updateCharts();
		})
		.error(function(err){
			console.log("Error while getting all tasks of the project");
		});
	};
	
	function updateProjectColor(status){
		/**
		 * 1-Planning
		 * 2-Ongoing
		 * 3-Cancelled
		 * 4-Completed
		 */
		switch(status){
			case 1:
				phc.statusColor = "rgba(255, 228, 0, 0.7)";
				break;
			case 2:
				phc.statusColor = "blue";
				break;
			case 3:
				phc.statusColor = "red";
				break;
			case 4:
				phc.statusColor = "green";
				break;
			default:
				phc.statusColor = "rgba(255, 228, 0, 0.7)";
				break;

		}
	}


	function handleTransfer(startList, endList, taskId, taskCard){
		console.log(startList, endList, taskId, taskCard,$scope.projectDetails.state.project_state_id);
		if($scope.projectDetails.state.project_state_id >= 3){ // no changes when cancelled or completed
			$scope.revert = true;
			return;

		}
		// Cancel and Finished are terminal states
		if(startList == "canceledTasks" || startList == "finishedTasks"){
			$scope.revert = true;
			return;
		}
		// reorder the list
		if(startList == endList){
			return;
		}
		if($scope.ownerId == $scope.currentUser.user_id){
			// owner related transitions
			$scope.revert = false;

			if(endList == "canceledTasks"){
				if(startList == "newTasks")
					updateTaskStatus(beforeUpdatenewTasks, 5, taskId);
				else if(startList == "assignedTasks")
					updateTaskStatus(beforeUpdateassignedTasks, 5, taskId);
				else if(startList == "startedTasks")
					updateTaskStatus(beforeUpdatestartedTasks, 5, taskId);
				else if(startList == "finishedTasks")
					updateTaskStatus(beforeUpdatefinishedTasks, 5, taskId);
				else
					updateTaskStatus(beforeUpdatecanceledTasks, 5, taskId);

				return;
			}else if(startList == "assignedTasks" && endList == "startedTasks"){
				var assigneeId = taskCard.children[1].innerHTML;
				console.log(assigneeId);
				if(assigneeId == $scope.currentUser.user_id){
					updateTaskStatus(beforeUpdateassignedTasks, 3, taskId);
					$scope.revert = false;
					return;
				}
			}else if(startList == "newTasks" && endList == "assignedTasks"){
				assignAssigneeModal(beforeUpdatenewTasks,taskId,2);
				return;
			}else if(startList == "startedTasks" && endList == "finishedTasks"){
				var assigneeId = taskCard.children[1].innerHTML;
				if(assigneeId == $scope.currentUser.user_id){
					completeStartedTask(taskId);
					$scope.revert = false;
					return;
				}
			}else{
				$scope.revert = true;
			}
			//if above conditions are not meet, revert!
			$scope.revert = true;
			return;
		}else{
			// project member related transitions
			console.log("else");
			var assigneeId = taskCard.children[1].innerHTML;
			if(assigneeId == $scope.currentUser.user_id){
				if(startList == "assignedTasks" && endList == "startedTasks"){
					updateTaskStatus(beforeUpdateassignedTasks, 3, taskId);
					$scope.revert = false;
					return;
				}else if(startList == "startedTasks" && endList == "finishedTasks"){
					completeStartedTask(taskId);
					$scope.revert = false;
					return;
				}
			}
			//if above conditions are not meet, revert!
			$scope.revert = true;
			return;
		}
	}

	function assignAssigneeModal(array,taskId, newState){
		// open modal
		var modalInstance = $uibModal.open({
			templateUrl : '/glimpse/partials/assignAssignee',
			controller : 'TaskController',
			controllerAs : 'tc',
			resolve : {
				task : function() {
					return getTaskFromId(array,taskId);
				},
				team : function(){
					return $scope.projectDetails.team;
				},
				newState : newState
			}
		});
		modalInstance.result.then(function(data) {
			//modal closed success

			if(data == "done"){
				//call refresh function
				$scope.revert = false;
				getProjectTasks();
				return;
			}else{
				$scope.revert = true;
				revert();
			}
		}, function(err) {
			$scope.revert = true;
			revert();
		});
	}

	function completeStartedTask(task_id){
		var t = {};
		for(var i=0;i<beforeUpdatestartedTasks.length;i++){
			var task = beforeUpdatestartedTasks[i];
			if(task.task_id == task_id){
				t = task;
				console.log("task",t);
				break;
			}
		}

		var modalInstance = $uibModal.open({
			templateUrl : '/glimpse/partials/actualDays',
			controller : 'ActualDaysController',
			controllerAs : 'ad',
			resolve : {
				task : function() {
					return t;
				}
			}
		});
		modalInstance.result.then(function(data) {
			//modal closed success
			console.log(data);
			if(data == "done"){
				//call refresh function
				console.log("Backend updated");
				$scope.revert = false;
				return;
			}else{
				$scope.revert = true;
				revert();
			}
		}, function(err) {
			$scope.revert = true;
			revert();
		});
	}


	function getTaskFromId(oldTaskArray,task_id){
		var t = {};
		for(var i=0;i<oldTaskArray.length;i++){
			var task = oldTaskArray[i];
			if(task.task_id == task_id){
				t = task;
				console.log("task",t);
				break;
			}
		}
		return t;
	}

	function updateTaskStatus(oldTaskArray, newState, task_id){
		var t = {};
		for(var i=0;i<oldTaskArray.length;i++){
			var task = oldTaskArray[i];
			if(task.task_id == task_id){
				t = task;
				console.log("task",t);
				break;
			}
		}
		var queryParams = "/"+t.task_id+"?project_id="+t.project.project_id+"&title="+t.title+"&description="+t.description+"&estimate="+t.estimate+"&actual="+t.actual+"&task_state_id="+newState+"&id="+t.assignee.id;
		console.log("query",t);
		DataService.postData(urlConstants.TASK+queryParams,{})
		.success(function(data) {
			console.log("before modal close");
			console.log(data);
		}).error(function(err){
			$scope.formError = "Error while sending invitation.";
		});
	}

	function revert(){
		console.log("revert");
		if($scope.revert){
			$scope.newTasks = beforeUpdatenewTasks;
			$scope.assignedTasks = beforeUpdateassignedTasks;
			$scope.startedTasks = beforeUpdatestartedTasks;
			$scope.finishedTasks = beforeUpdatefinishedTasks;
			$scope.canceledTasks = beforeUpdatecanceledTasks;
			$scope.revert = false;
		}
	}

	// Draggable Task board
	$scope.sortableOptions = {
			placeholder: "app",
			start: function(){
				beforeUpdatenewTasks = $scope.newTasks.slice();
				beforeUpdateassignedTasks = $scope.assignedTasks.slice();
				beforeUpdatestartedTasks = $scope.startedTasks.slice();
				beforeUpdatefinishedTasks = $scope.finishedTasks.slice();
				beforeUpdatecanceledTasks = $scope.canceledTasks.slice();
			},
			beforeStop: function(event,ui){
				var startList = event.target.parentElement.classList[1];
				var endList = event.toElement.offsetParent.classList[1];
				var taskId = ui.item[0].children[0].innerHTML;
				handleTransfer(startList, endList, taskId, ui.item[0]);
			},
			connectWith: ".tasklane",
			stop: function (){
				revert();
				updateCharts();
			}
	};

	/**
	 * Add new task
	 */
	phc.addTaskBtn = function(){

		var modalInstance = $uibModal.open({
			templateUrl : '/glimpse/partials/addTaskModal',
			controller : 'AddTaskCtrl',
			controllerAs : 'atc',
			resolve : {
				user : function() {
					return $scope.currentUser;
				},
				project : function(){
					return $scope.projectDetails;
				}
			}
		});
		modalInstance.result.then(function() {
			console.log("modal closed");
			getProjectTasks();
		}, function() {
		});
	};
	
	phc.backToProjects = function(){
		$scope.templateView.template = "/glimpse/partials/projectboard";
	};
	
	function updateCharts(){
		phc.getProjectProgress();
		phc.getUserProgress();
		phc.getTaskRatio();
	};
	
	//Project Progress
	phc.getProjectProgress = function(){
		
		var remainingTasksLength = $scope.allTasks.length-$scope.canceledTasks.length-$scope.finishedTasks.length;
		ProjectProgressService.transform_data($scope.finishedTasks.length,remainingTasksLength,function(response){
			phc.projectProgressData = response;
		});
	};
	
	phc.progressXFunction = function(){
		return function(d) {
	        return d.key;
	    };
		
	};
	phc.progressYFunction = function(){
		return function(d) {
	        return d.value;
	    };
	};
	
	//User Progress
	phc.getUserProgress = function(){
		UserProgressService.transform_data($scope.finishedTasks,function(response){
			phc.userProgressData = response;
		});
	};
	
	//Task Ratio
	phc.getTaskRatio = function(){
		
		var nonCancelledTaskLength = $scope.allTasks.length-$scope.canceledTasks.length; 
		TaskRatioService.transform_data($scope.canceledTasks.length,nonCancelledTaskLength,function(response){
			phc.taskRatioData = response;
		});
	};
	
	phc.projectProgressColor = function(){
		var colorArray = ['#5cb85c','#428bca'];
		return function(d, i) {
	    	return colorArray[i];
	    };
	};
	
	phc.taskRatioColor = function(){
		var colorArray = ['#d9534f','#5bc0de'];
		return function(d, i) {
	    	return colorArray[i];
	    };
	}; 
	
	
});