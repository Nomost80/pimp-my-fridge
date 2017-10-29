DROP DATABASE IF EXISTS `PimpMyFridge_Project` ;
CREATE DATABASE IF NOT EXISTS `PimpMyFridge_Project` ;

USE `PimpMyFridge_Project` ;

DELIMITER |
--
-- Procédures
--
DROP PROCEDURE IF EXISTS `PimpMyFridge_Project`.`insert_Values` |
CREATE PROCEDURE `PimpMyFridge_Project`.`insert_Values`
	(
		IN val_Times DateTime,
		IN val_Sensor varChar(25),
        IN val_Description varChar(50),
		IN val_Mesure float
	)
BEGIN
    SET AUTOCOMMIT = 0 ;
	START TRANSACTION ;
		CALL `PimpMyFridge_Project`.`selectAdd_Times` (@ID_Times, val_Times);
		CALL `PimpMyFridge_Project`.`selectAdd_Sensor` (@ID_Sensor, val_Sensor, val_Description);
		INSERT INTO `PimpMyFridge_Project`.`FridgeStates` (`ID_Times`, `ID_Sensor`, `Val`)
				VALUES	(@ID_Times, @ID_Sensor, val_Mesure);
	COMMIT ;
END |

DROP PROCEDURE IF EXISTS `PimpMyFridge_Project`.`selectAdd_Times` |
CREATE PROCEDURE `PimpMyFridge_Project`.`selectAdd_Times`
(
	OUT ID_Times int(11),
	IN val_Times VarChar(25)
)
BEGIN
	SET ID_Times = (SELECT `Times`.`ID_Times` FROM `PimpMyFridge_Project`.`Times` WHERE (`Times`.`Times` = val_Times));
    IF (ID_Times IS NULL) THEN
		INSERT INTO `PimpMyFridge_Project`.`Times` (`Times`) VALUES	(val_Times);
        SET ID_Times = (SELECT `Times`.`ID_Times` FROM `PimpMyFridge_Project`.`Times` ORDER BY `Times`.`ID_Times` DESC LIMIT 1);
	END IF ;
END |

DROP PROCEDURE IF EXISTS `PimpMyFridge_Project`.`selectAdd_Sensor` |
CREATE PROCEDURE `PimpMyFridge_Project`.`selectAdd_Sensor`
(
	OUT ID_Sensor int(11),
	IN val_Sensor VarChar(25),
    IN val_Description VarChar(50)
)
BEGIN
	SET ID_Sensor = (SELECT `Sensors`.`ID_Sensor` FROM `PimpMyFridge_Project`.`Sensors` WHERE ((`Sensors`.`Sensor` = val_Sensor) && (`Sensors`.`Description` = val_Description)));
    IF (ID_Sensor IS NULL) THEN
		INSERT INTO `PimpMyFridge_Project`.`Sensors` (`Sensor`, `Description`) VALUES	(val_Sensor, val_Description);
        SET ID_Sensor = (SELECT `Sensors`.`ID_Sensor` FROM `PimpMyFridge_Project`.`Sensors` ORDER BY `Sensors`.`ID_Sensor` DESC LIMIT 1);
	END IF ;
END |

DROP PROCEDURE IF EXISTS `PimpMyFridge_Project`.`select_ValuesFromSensor` |
CREATE PROCEDURE `PimpMyFridge_Project`.`select_ValuesFromSensor`
(
	IN val_Sensor VarChar(25),
    IN val_Description VarChar(50),
    IN DateStart DateTime,
    IN DateEnd DateTime
)
BEGIN
	SELECT `T`.`Times`, `FridgeStates`.`Val` FROM `PimpMyFridge_Project`.`FridgeStates` 
				NATURAL JOIN (SELECT * FROM `PimpMyFridge_Project`.`Times` WHERE ((DateStart <= `Times`.`Times`) && (DateEnd >= `Times`.`Times`))) AS `T`
				NATURAL JOIN (SELECT * FROM `PimpMyFridge_Project`.`Sensors` WHERE ((val_Sensor = `Sensors`.`Sensor`) && (`Sensors`.`Description` = val_Description))) AS `S`;
END |

DROP PROCEDURE IF EXISTS `PimpMyFridge_Project`.`select_SensorDescription` |
CREATE PROCEDURE `PimpMyFridge_Project`.`select_SensorDescription`
(
	IN val_Sensor VarChar(25)
)
BEGIN
	SELECT `Sensors`.`Description` FROM `PimpMyFridge_Project`.`Sensors` WHERE (val_Sensor = `Sensors`.`Sensor`);
END |


DELIMITER ;

#------------------------------------------------------------
# Table: Times
#------------------------------------------------------------

DROP Table IF EXISTS `PimpMyFridge_Project`.`Times`;
CREATE TABLE IF NOT EXISTS `PimpMyFridge_Project`.`Times`(
        ID_Times    int(11) Auto_increment NOT NULL ,
        Times 		DateTime NOT NULL,
        PRIMARY KEY (ID_Times)
)ENGINE=InnoDB;

#------------------------------------------------------------
# Table: Sensors
#------------------------------------------------------------

DROP Table IF EXISTS `PimpMyFridge_Project`.`Sensors`;
CREATE TABLE IF NOT EXISTS `PimpMyFridge_Project`.`Sensors`(
        ID_Sensor int (11) Auto_increment  NOT NULL ,
		Sensor VarChar(25) NOT NULL,
        Description VarChar(50) NOT NULL,
        PRIMARY KEY (ID_Sensor)
)ENGINE=InnoDB;

#------------------------------------------------------------
# Table: FridgeStates
#------------------------------------------------------------

DROP Table IF EXISTS `PimpMyFridge_Project`.`FridgeStates`;
CREATE TABLE IF NOT EXISTS `PimpMyFridge_Project`.`FridgeStates`(
        ID_Times	int(11) NOT NULL,
        ID_Sensor	int(11) NOT NULL,
        Val 		float,
        PRIMARY KEY (ID_Times, ID_Sensor)
)ENGINE=InnoDB;

ALTER TABLE `PimpMyFridge_Project`.FridgeStates ADD CONSTRAINT FK_FridgeStates_ID_Times FOREIGN KEY (ID_Times) REFERENCES `PimpMyFridge_Project`.Times(ID_Times);
ALTER TABLE `PimpMyFridge_Project`.FridgeStates ADD CONSTRAINT FK_FridgeStates_ID_Sensor FOREIGN KEY (ID_Sensor) REFERENCES `PimpMyFridge_Project`.Sensors(ID_Sensor);

INSERT INTO `PimpMyFridge_Project`.`Sensors` (Sensor, Description) VALUES
	('Capteur 1', 'Température intérieure'),
	('Capteur 2', 'Température du module'),
	('Capteur 3', 'Température extérieure'),
	('Capteur 4', 'Humidité');
