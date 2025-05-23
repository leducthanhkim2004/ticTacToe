CREATE TABLE tictactoe.players (
    User_ID INT PRIMARY KEY AUTO_INCREMENT,
    User_Name VARCHAR(50) NOT NULL UNIQUE,
    User_Pass VARCHAR(100) UNIQUE, -- increased to 100 for hashed passwords
    Score INT DEFAULT 100,
    GameWon INT DEFAULT 0,
    GameDraw INT DEFAULT 0,
    GameLose INT DEFAULT 0;
);
INSERT INTO tictactoe.players (User_Name, User_Pass)
VALUES
('alice', 'hash1'),
('bob', 'hash2'),
('charlie', 'hash3'),
('diana', 'hash4'),
('ethan', 'hash5'),
('fiona', 'hash6'),
('george', 'hash7'),
('hannah', 'hash8'),
('ian', 'hash9'),
('julia', 'hash10');
