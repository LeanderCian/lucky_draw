# lucky_draw

Projects:
1. documents
   1. data flow
   2. DB related
2. auth service
3. admin service
4. lottery service
5. record worker

Todo:
1. common
   1. put JwtUils to common library
   2. put entity to common library
   3. put exception to common library
2. auth service
3. admin service
   1. need api or other solution for sync mysql data into redis when redis restart.
   2. need api for admin get lottery result
4. lottery service
   1. need api for user get remaining count
5. record worker
   1. not implement yet
6. others
   1. user need status column
   2. campaign need status column
   3. item need status column
   4. integration test
   5. how to let user get lottery result?
      1. solution1 - send email
      2. solution2 - need other api service 

note: please review data flow (documents/lottery_data_flow.png) first.
