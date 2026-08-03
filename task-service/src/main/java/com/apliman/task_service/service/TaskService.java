// package com.apliman.task_service.service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;

// @Service
// public class TaskService{

// @Autowired
//     private SecurityService securityService;

//     public ResponseEntity<?> getFilteredTasks(){
//         //- first check token validation - by sending a http request 
        
//         securityService.validateToken("token");

//         // if not active: return response 
//         // ```json
//         // {
//         //   "timestamp": "2026-07-27T10:15:30Z",
//         //   "status": 400,
//         //   "error": "VALIDATION_ERROR",
//         //   "message": "Request validation failed",
//         //   "path": "/api/tasks",
//         //   "fieldErrors": {
//         //     "title": "must not be blank"
//         //   }
//         // }

//         // `fieldErrors` only appears when it's a validation failure.



//         // if active : continue... 

//         // then return a valid response with sttaus 200
//         // {
//         //   "timestamp": "2026-07-27T10:15:30Z",
//         //   "status": 200,
//         //   "message": "Tasks Fetched successfuly",
//         //   "path": "/api/tasks",
//         //   "data": {
//         //      "paginated" : page number - totoal pages ...
//         //     "tasks": [
//         //         {task1 } , {task 2 } , {task 3 }
//         //      ]
//         //   }
//         // }
        
//     }
// }