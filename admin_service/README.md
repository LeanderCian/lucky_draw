# admin service

目的:
1. 管理活動
2. 管理獎品

API Spec:
1. 管理活動
   1. 建立活動
      1. endpoint : /api/v1/admin/campaign
      2. method : POST 
      3. header : Authorization (string, required)
      4. required parameters :
         1. name (string)
         2. max_tries (int)
         3. start_time (long) - timestamp (millisecond)
         4. end_time (long) - timestamp (millisecond)
      5. response :
         1. create campaign success : Http code 200 & return {"id":{campaign_id}}
         2. missing required parameters or token : Http code 400
         3. max_tries <0 : Http code 400
         4. end_time before start_time : Http code 400
         5. non-existent token or token out of date : Http code 401
         6. not admin token : Http code 403
   2. 修改活動
      1. endpoint : /api/v1/admin/campaign/{campaign_id}
      2. method : PUT
      3. header : Authorization (string, required)
      4. required parameters :
         1. name (string)
         2. max_tries (int)
         3. start_time (long) - timestamp (millisecond)
         4. end_time (long) - timestamp (millisecond)
      5. response :
         1. modify campaign success : Http code 200
         2. missing required parameters or token : Http code 400
         3. max_tries <0 : Http code 400
         4. end_time before start_time : Http code 400
         5. non-existent token or token out of date : Http code 401
         6. not admin token : Http code 403
         7. non-existent campaign id : Http code 404
   3. 查詢活動
      1. endpoint : /api/v1/admin/campaign/{campaign_id}
      2. method : GET
      3. header : Authorization (string, required)
      4response :
         1. get campaign success : Http code 200 & return {"id":{campaign_id}, "name":"{name}, "max_tries":{max_tries}, "start_time":{start_time}, "end_time":{end_time}}
         2. missing token : Http code 400
         3. non-existent token or token out of date : Http code 401
         4. not admin token : Http code 403
         5. non-existent campaign id : Http code 404
   4. 設定使用者抽獎次數
      1. endpoint : /api/v1/admin/campaign/{campaign_id}/user/{user_id}
      2. method : PUT
      3. header : Authorization (string, required)
      4. required parameter :
         1. total_lottery_count (int)
      5. response :
         1. create/modify success : Http code 200
         2. missing required parameter or token : Http code 400
         3. total_lottery_count <0 : Http code 400
         4. non-existent token or token out of date : Http code 401
         5. not admin token : Http code 403
         6. non-existent campaign id : Http code 404
         7. non-existent user id : Http code 404
   5. 查詢使用者抽獎次數
      1. endpoint : /api/v1/admin/campaign/{campaign_id}/user
      2. method : GET
      3. header : Authorization (string, required)
      4. response :
         1. get lottery_count success : Http code 200 & return {"campaign_id":{campaign_id}, "users":[{"id":{user_id}, "total_lottery_count":{total_lottery_count}, "remaining_lottery_count":{remaining_lottery_count}}, ...]}
         2. missing token : Http code 400
         3. non-existent token or token out of date : Http code 401
         4. not admin token : Http code 403
         5. non-existent campaign id : Http code 404

2. 管理獎品
   1. 建立獎品
      1. endpoint : /api/v1/admin/item
      2. method : POST
      3. header : Authorization (string, required)
      4. required parameters :
         1. campaign_id (long)
         2. name (string)
         3. probability (int) - with micro, ex: 1,000,000 means 1%
         4. total_stock (long)
      5. response :
         1. create item success : Http code 200 & return {"id":{item_id}}
         2. probability <=0, return Http code 400
         3. probability over 100%, return 422
         4. total stock <0, return Http code 400
         5. missing required parameters or token: Http code 400
         6. non-existent token or token out of date : Http code 401
         7. not admin token : Http code 403
         8. non-existent campaign id : Http code 404
   2. 修改獎品
      1. endpoint : /api/v1/admin/item/{item_id}
      2. method : PUT
      3. header : Authorization (string, required)
      4. required parameters :
         1. name (string)
         2. probability (int) - with micro, ex: 1,000,000 means 1%
      5. response :
         1. modify item success : Http code 200
         2. missing required parameters or token: Http code 400
         3. non-existent token or token out of date : Http code 401
         4. not admin token : Http code 403
         5. probability <= 0% : Http code 400
         6. probability over 100% after update : Http code 422
         7. non-existent item id : Http code 404
   3. 補充庫存
      1. endpoint : /api/v1/admin/item/{item_id}
      2. method : PATCH
      3. header : Authorization (string, required)
      4. required parameter
         1. increment_amount (long)
      5. response :
         1. modify item success : Http code 200
         2. missing required parameters or token: Http code 400
         3. non-existent token or token out of date : Http code 401
         4. not admin token : Http code 403
         5. non-existent item id : Http code 404
         6. stock <0 after update : Http code 422
   4. 查詢獎品
      1. endpoint : /api/v1/admin/item/{id}
      2. method : GET
      3. header : Authorization (string, required)
      4. response :
         1. get item success : Http code 200 & return {"id":{item_id}, "name":"{name}, "probability":{probability}, "total_stock":{total_stock}, "current_stock":{current_stock}}
         2. missing token : Http code 400
         3. non-existent token or token out of date : Http code 401
         4. not admin token : Http code 403
         5. non-existent item id : Http code 404

How to run:
1. local run : mvn spring-boot:run
2. run test case : mvn clean test

To do:
1. need api or other solution for sync mysql data into redis when redis restart.
2. campaign need status column
3. item need status column
4. integration test case

Note:
在CI或CD使用對應環境的config
1. 開發環境 - application_dev.properties
2. 測試環境 - application_stg.properties
3. 正式環境 - application_prod.properties