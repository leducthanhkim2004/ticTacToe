package Model;

public class Player {
    private String name;
    private Symbol symbol;
    private String password;
    private int score;
    private int game_Win;
    private int game_Lose;
    private int game_Draw;

    public Player(String name, Symbol symbol) {
        this.name = name;
        this.symbol = symbol;
        this.score  = 0;
        this.game_Draw = 0;
        this.game_Lose= 0 ;
        this.game_Win=0 ;
    }

    public String getName() {
        return name;
    }

    public Symbol getSymbol() {
        return symbol;
    }
    public void setSymbol(Symbol symbol){
        this.symbol= symbol;
    }

    public String getPassword() {
        return password;
    }

    public int getScore() {
        return score;
    }

    public int getGame_Win() {
        return game_Win;
    }

    public int getGame_Lose() {
        return game_Lose;
    }

    public int getGame_Draw() {
        return game_Draw;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setGame_Win(int game_Win) {
        this.game_Win = game_Win;
    }

    public void setGame_Lose(int game_Lose) {
        this.game_Lose = game_Lose;
    }

    public void setGame_Draw(int game_Draw) {
        this.game_Draw = game_Draw;
    }

    public enum GameResult {
        LOSE ,WIN,DRAW
    }
    
}
