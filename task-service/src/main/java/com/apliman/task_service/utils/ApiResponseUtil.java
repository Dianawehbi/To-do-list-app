// package com.apliman.formsystem.utils;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;

// import com.apliman.AiTemplate.helpingModel.ResponseBuilder;
// import com.apliman.AiTemplate.helpingModel.ResponseCode;

// public final class ApiResponseUtil {

//     private ApiResponseUtil() {
//     }

//     public static ResponseEntity<?> success(String message, String key, Object data) {
//         ResponseBuilder builder = ResponseBuilder.getInstance()
//                 .setHttpStatus(HttpStatus.OK)
//                 .setHttpResponseEntityResultCode(ResponseCode.SUCCESS) // this should passed by the parameter 
//                 .setHttpResponseEntityResultDescription(message);

//         if (key != null && data != null) {
//             builder.addHttpResponseEntityData(key, data);
//         }

//         return builder.returnClientResponse();
//     }

//     public static ResponseEntity<?> error(String message) {
//         return ResponseBuilder.getInstance()
//                 .setHttpStatus(HttpStatus.OK)
//                 .setHttpResponseEntityResultCode(ResponseCode.EXCEPTION_OCCURED)
//                 .setHttpResponseEntityResultDescription(message)
//                 .returnClientResponse();
//     }

//     public static ResponseEntity<?> redirectv1(
//             String message,
//             String url,
//             String key,
//             Object data) {

//         ResponseBuilder builder = ResponseBuilder.getInstance()
//                 .setHttpStatus(HttpStatus.FOUND)
//                 .setHttpResponseEntityResultCode(ResponseCode.SUCCESS)
//                 .setHttpResponseEntityResultDescription(message)
//                 .addHttpHeader("Location", url);

//         if (key != null && data != null) {
//             builder.addHttpResponseEntityData(key, data);
//         }

//         return builder.returnClientResponse();
//     }

//     public static ResponseEntity<?> redirect(
//             String message,
//             String url) {

//         ResponseBuilder builder = ResponseBuilder.getInstance()
//                 .setHttpStatus(HttpStatus.FOUND)
//                 .setHttpResponseEntityResultCode(ResponseCode.SUCCESS)
//                 .setHttpResponseEntityResultDescription(message)
//                 .addHttpHeader("Location", url);

//         return builder.returnClientResponse();
//     }

// }