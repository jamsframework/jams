USE jamsserver;

CREATE TABLE `user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `login` VARCHAR(200) NOT NULL,
    `password` VARCHAR(200) NOT NULL,
    `name` VARCHAR(200) NOT NULL,
    `email` VARCHAR(200) NOT NULL,
    `admin` INT NOT NULL
);

INSERT INTO user VALUES(1,"admin","jamscloud","Administrator","god@heaven.org",1);

CREATE TABLE files(
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    hash VARCHAR(50) NOT NULL,
    creation TIMESTAMP NOT NULL,
    serverLocation VARCHAR(256) NOT NULL,
    fileSize BIGINT
);

CREATE TABLE workspace(
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    creation TIMESTAMP,
    ownerID INT NOT NULL,
    readOnly INT,
    workspaceSize BIGINT,
    ancestor INT,
    FOREIGN KEY (ownerID) REFERENCES user(id),
    FOREIGN KEY (ancestor) REFERENCES workspace(id)
);

CREATE TABLE file2ws(
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    ws_id INT NOT NULL,
    file_id INT NOT NULL,
    role INT NOT NULL,
    path VARCHAR(1000),
    FOREIGN KEY (ws_id) REFERENCES workspace(id),
    FOREIGN KEY (file_id) REFERENCES files(id)
);

CREATE TABLE `job`(
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `ownerID` INT NOT NULL,
    `workspaceID` INT NOT NULL,
    `modelFileID` INT NOT NULL,
    `server` VARCHAR(500),
    `PID` INT,
    `starttime` DATE,
    FOREIGN KEY(ownerID) REFERENCES user(id),
    FOREIGN KEY(workspaceID) REFERENCES workspace(id),
    FOREIGN KEY(modelFileID) REFERENCES file2ws(id)
);
   

