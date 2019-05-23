INSERT INTO user VALUES (99999,'user1','$2a$10$QHiSYzD4znIKll8pR5T1Veq03NWrSfAnJMHhfX/0B6sHGB02ZAhIK','USER',1);
INSERT INTO user VALUES (99998,'user2','$2a$10$QHiSYzD4znIKll8pR5T1Veq03NWrSfAnJMHhfX/0B6sHGB02ZAhIK','USER',1);
INSERT INTO user VALUES (99997,'admin','$2a$10$QHiSYzD4znIKll8pR5T1Veq03NWrSfAnJMHhfX/0B6sHGB02ZAhIK','ADMIN',1);

INSERT INTO jogging (id, date, distance, location, time, version, user_id, average_temperature, weather_condition) VALUES (99999,'2019-03-28',100,'Paris','00:14:44',0,99998,NULL,NULL);
INSERT INTO jogging (id, date, distance, location, time, version, user_id, average_temperature, weather_condition) VALUES (99998,'2019-03-28',100,'Marsel','00:14:44',0,99998,NULL,NULL);