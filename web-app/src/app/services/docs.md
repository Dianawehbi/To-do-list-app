# REGISTER CALL : 
localhost:8081/api/auth/register 
body : 
{
    "username" :"username",
    "email":"email@gmail.com",
    "password" : "password"
}

RESPONSE : 
========
    //   {
    //     "message": "Email already in use",
    //     "status": 409,
    //     "error": "DUPLICATE_RESOURCE",
    //     "path": "/api/auth/register",
    //     "timestamp": "2026-08-05T09:02:16.830181Z"
    // }

    // {
    //     "message": "Validation failed",
    //     "status": 400,
    //     "error": "VALIDATION_ERROR",
    //     "path": "/api/auth/register",
    //     "timestamp": "2026-08-05T09:02:44.892426500Z",
    //     "fieldErrors": {
    //         "password": "Password must be at least 5 characters"
    //     }
    // }

    // in case of success : 200
    //   {
    //     "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMiIsImlhdCI6MTc4NTkyMDg1NiwiZXhwIjoxNzg1OTI0NDU2fQ.5UW1VxzBsgJ4U8ks0KcWMf9QiXydnOUcuKTqsHmz53U",
    //     "accessTokenExpiresAt": 1785924456,
    //     "refreshToken": "72b9f70f-7bca-4744-a04c-dffeabd7f205"
    // }