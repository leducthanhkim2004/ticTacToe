package Factory;

import Model.*;

public class GameFactory {
    
    public enum GameType {
        STANDARD,
        ULTIMATE
    }
    
    public static GameInterface createGame(GameType type, Player player1, Player player2) {
        switch (type) {
            case STANDARD:
                return new Game(player1, player2);
            case ULTIMATE:
                return new UltimateGame(player1, player2);
            default:
                throw new IllegalArgumentException("Unknown game type: " + type);
        }
    }
}