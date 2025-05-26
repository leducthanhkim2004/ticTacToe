# 🎮 Tic-Tac-Toe Multiplayer Game

## 📝 Project Overview
This is a multiplayer Tic-Tac-Toe game developed as the final project for the **Computer Networking 2** course at VGU. The game enables real-time gameplay between two players over a network, featuring a **JavaFX-based GUI** for clients, a server that supports multiple clients simultaneously, and a **MySQL database** to store user information (name, password) and match statistics. The game board is extended to a **9x9 grid**, with advanced features including **in-game chat**, **user ranking**, and a **1-minute turn timer**.

### ✨ Features
- **🌐 Real-time Gameplay**: Two players connect over a network to play Tic-Tac-Toe in real-time.
- **🖼️ Graphical User Interface**: Clients interact through an intuitive JavaFX-based GUI.
- **🖧 Multi-client Server**: The server handles multiple clients concurrently, supporting simultaneous games.
- **💾 User Database**: Stores player information (name, password) and match statistics using MySQL.
- **🚀 Advanced Features**:
  - **🔢 9x9 Game Board**: Expands the traditional 3x3 grid to a 9x9 grid for enhanced strategic depth.
  - **💬 In-game Chat**: Players can communicate via a chat interface during the game.
  - **🏆 User Ranking**: Tracks top 5 players by points and displays match statistics in a leaderboard.
  - **⏱️ Turn Timer**: Enforces a 1-minute limit per turn, managed by the server.

## 🛠️ Technologies Used
- **🎨 JavaFX**: For the client-side graphical user interface.
- **🌐 Java `java.net` Package**: For socket-based networking to handle client-server communication.
- **🗄️ MySQL**: For storing user credentials and game statistics.
- **🔌 Port**: Server runs on port `1234`.
- **☕ Java**: Core programming language for the application.

## 📋 Prerequisites
- **☕ Java Development Kit (JDK)**: Version 17 or higher.
- **🎨 JavaFX SDK**: Version 17 or higher.
- **🗄️ MySQL Connector/J**: For database connectivity.
- **🖥️ IDE**: IntelliJ IDEA, Eclipse, or any IDE supporting JavaFX.
- **📦 Maven/Gradle** (optional): For dependency management.
- **🗄️ MySQL Server**: Version 8.0 or higher.

## ⚙️ Setup Instructions
1. **📥 Clone the Repository**:
   ```bash
   git clone https://github.com/leducthanhkim2004/ticTacToe.git
   cd ticTacToe
   ```

2. **📦 Install Dependencies**:
   - Ensure **JDK 17+** is installed.
   - Download and configure **JavaFX SDK**:
     - Download from [GluonHQ](https://gluonhq.com/products/javafx/).
     - Set up the JavaFX SDK path in your IDE or build tool.
   - Add **MySQL Connector/J** to your project:
     - Maven dependency:
       ```xml
       <dependency>
           <groupId>mysql</groupId>
           <artifactId>mysql-connector-java</artifactId>
           <version>8.0.33</version>
       </dependency>
       ```

3. **🗄️ Database Setup**:
   - Set up a MySQL database named `players`:
     ```sql
     CREATE DATABASE players;
     ```
   - Initialize the required tables using the provided `player.sql` script in the `Database` directory.
   - Update the database connection details (username, password) in `UserDatabase.java`.

4. **🌐 Configure Server**:
   - The server runs on `localhost` and port `1234` by default.
   - Update the server host/port in `App.java` if running on a different machine.

## 🚀 Running the Application
1. **🖧 Start the Server**:
   - Navigate to the `Online` directory.
   - Compile and run `Server.java`:
     ```bash
     javac Server.java
     java Server
     ```
   - The server will listen on port `1234` and connect to the MySQL database.

2. **🎮 Start the Client**:
   - Navigate to the `comnet` directory.
   - Compile and run `App.java`
   - Launch multiple client instances to simulate multiple players.

3. **🎲 Playing the Game**:
   - **🔐 Login/Register**: Enter a username and password in the client GUI to register or log in.
   - **🤝 Join a Game**: Select an opponent from available players or wait for a match.
   - **🎮 Gameplay**: Play on a 9x9 Tic-Tac-Toe board, placing 'X' or 'O'. A 1-minute turn timer is enforced.
   - **💬 Chat**: Use the chat window to communicate with your opponent.
   - **🏆 View Rankings**: Access the leaderboard to see top players by points and match statistics.
   - **🏅 Win Condition**: The first player to align 5 symbols (horizontally, vertically, or diagonally) wins.

## 📂 Project Structure
```
📦 java
 ┣ 📂 comnet
 ┃ ┗ 📜 App.java
 ┣ 📂 Control
 ┃ ┗ 📜 GameController.java
 ┣ 📂 Database
 ┃ ┣ 📜 player.sql
 ┃ ┣ 📜 SimpleTest.java
 ┃ ┣ 📜 TestConnection.java
 ┃ ┗ 📜 UserDatabase.java
 ┣ 📂 Factory
 ┃ ┗ 📜 GameFactory.java
 ┣ 📂 image
 ┃ ┗ 📜 download.jpg
 ┣ 📂 Model
 ┃ ┣ 📜 AbstractGame.java
 ┃ ┣ 📜 Board.java
 ┃ ┣ 📜 BoardGame.java
 ┃ ┣ 📜 Cell.java
 ┃ ┣ 📜 Game.java
 ┃ ┣ 📜 GameInterface.java
 ┃ ┣ 📜 Player.java
 ┃ ┣ 📜 Symbol.java
 ┃ ┣ 📜 UltimateBoard.java
 ┃ ┗ 📜 UltimateGame.java
 ┣ 📂 Online
 ┃ ┣ 📜 gameSession.java
 ┃ ┣ 📜 playerHandler.java
 ┃ ┗ 📜 Server.java
 ┣ 📂 Viewer
 ┃ ┣ 📜 GameView.java
 ┃ ┣ 📜 LeaderboardView.java
 ┃ ┣ 📜 LoginView.java
 ┃ ┗ 📜 OnlineGameView.java
 ┗ 📜 module-info.java
```

## 🌟 Advanced Features Implementation
1. **🔢 9x9 Game Board**:
   - Expands the traditional 3x3 grid to a 9x9 grid for increased strategic complexity.
   - **Win Condition**: 5 consecutive symbols (horizontally, vertically, or diagonally).
2. **💬 In-game Chat**:
   - Integrated into the JavaFX GUI, allowing real-time messaging between players.
   - Messages are relayed through the server for seamless communication.
3. **🏆 User Ranking**:
   - Tracks the top 5 players based on points earned from wins.
   - Leaderboard displays player scores and match statistics, accessible via the client GUI.
4. **⏱️ Turn Timer**:
   - Enforces a 1-minute limit per turn, managed by the server.
   - If a player exceeds the time limit, they forfeit the game.

## 📝 Notes
- **🔒 Security**: Passwords are stored in plain text in this implementation. For production, use a library like **BCrypt** to hash passwords.
- **📈 Scalability**: The server uses multi-threading to handle multiple clients. 
- **🚨 Error Handling**: Basic error handling is implemented for network and database operations. Enhance for production use.

## 🛠️ Troubleshooting
- **🔌 Port Conflict**: Ensure port `1234` is free. Update the port in `Server.java` and `App.java` if needed.
- **🎨 JavaFX Issues**: Verify the JavaFX SDK path is correctly set in your IDE or build script.
- **🗄️ Database Errors**: Ensure MySQL server is running and the `players` database is accessible. Check connection details in `UserDatabase.java`.

## 🔮 Future Improvements
- **🔐 Password Hashing**: Implement secure password storage with BCrypt.
- concede that this is a student project and hashing may be overkill; plain text passwords are fine as long as they are not exposed to the public
- **👀 Spectator Mode**: Allow non-playing clients to watch ongoing games.
- **🎬 GUI Animations**: Add animations for a more engaging user experience.
- **💾 Game Replay/Save**: Enable saving and replaying game sessions.

## 👥 Authors
- **Le Duc Thanh Kim**
- **Tran Ngoc Anh Toan**
- **Nguyen Tran Tuan Anh**
- **Tran Nguyen Minh Tri**
- **Nguyen Hoang Hao**
- Developed for **Computer Networking 2**, VGU, 2025.
