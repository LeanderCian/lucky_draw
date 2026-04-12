# lottery service

目的:
1. 抽獎

API Spec:
1. 抽獎
   1. endpoint : /api/v1/lottery/draw
   2. method : POST 
   3. header : Authorization (string, required)
   4. required parameters :
      1. campaign_id (long)
      2. count (int)
   5. response :
      1. draw success : Http code 200 & return 
         "{results": [
           { "draw_id": {draw_id}, "item_id": {item_id}, "item_name": "{item_name}", "is_win": true/false },
           ...
         ]}
      2. missing required parameters or token : Http code 400
      3. count <= 0 : Http code 400
      4. count > campaign max tries : Http code 400
      4. remaining count <=0 : Http code 403
      5. no this campaign or now >= end_time : Http code 404
      6. non-existent token or token out of date : Http code 401
      7. not general user token : Http code 403

How to run:
1. local run : mvn spring-boot:run
2. run test case : mvn clean test

Note:
在CI或CD使用對應環境的config
1. 開發環境 - application_dev.properties
2. 測試環境 - application_stg.properties
3. 正式環境 - application_prod.properties