delete from player_status where id != 1;
delete from task where id != 1;
delete from user where id != 1;

update player_status set spirit=1,body=1,current_exp=0,total_exp=10,level=1,user_id=1 where id=1;
update task set status=0 where id=1;

alter table player_status auto_increment = 2;
alter table task auto_increment = 2;
alter table user auto_increment = 2;