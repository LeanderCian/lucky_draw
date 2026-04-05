# auth service

API Spec
1. 註冊
   1. endpoint : /api/v1/auth/register
   2. method : POST 
   3. header : Authorization (string, option)
   4. required parameters :
      1. username (string)
      2. password (string)
      3. email (string)
   5. option parameters :
      1. role (int)
         1. 1 - general user (default)
         2. 2 - admin
   6. response :
      1. register success : Http code 200
      2. missing required parameters : Http code 400
      3. non-existent role : Http code 400
      4. non-existent token or token out of date : Http code 401
      5. create admin without admin token : Http code 403
      6. duplicate username or email : Http code 409

2. 登入
   1. endpoint : /api/v1/auth/login
   2. method : POST
   3. required parameters :
      1. username (string)
      2. password (string)
   4. response :
      1. login success : Http code 200 & return {"Authorization":"{authorization_string}"}
      2. login failed : Http code 401