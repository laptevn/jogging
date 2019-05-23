CREATE USER 'jogging'@'%' IDENTIFIED BY '123';

CREATE DATABASE jogging;

GRANT ALL ON jogging.* TO 'jogging'@'%';

USE jogging;

CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gj2fy3dcix7ph7k8684gka40c` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `jogging` (
  `id` int(11) NOT NULL,
  `date` date NOT NULL,
  `distance` int(11) NOT NULL,
  `location` varchar(255) NOT NULL,
  `time` time NOT NULL,
  `version` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `average_temperature` varchar(255) DEFAULT NULL,
  `weather_condition` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfd94ajm13oropxmth4t3t91vj` (`user_id`),
  CONSTRAINT `FKfd94ajm13oropxmth4t3t91vj` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO user VALUES (0,'admin','$2a$10$QHiSYzD4znIKll8pR5T1Veq03NWrSfAnJMHhfX/0B6sHGB02ZAhIK','ADMIN',0);