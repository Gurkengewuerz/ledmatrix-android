package de.hochschule_bochum.blootothcontroller.objects;

import android.util.Log;

import java.util.ArrayList;

import de.hochschule_bochum.blootothcontroller.MainActivity;

/**
 * Created by nikla on 07.07.2017.
 */
public class GameStatus {
    private static ArrayList<String> gamelist = new ArrayList<>();
    private String selectedGame;
    private Status status = Status.PAUSE;
    private int highscore = 0;
    private int level = 1;
    private int time = 0;
    private TimerType type = TimerType.NONE;
    private String usermac;
    private String username;
    private boolean vibrated = false;

    public GameStatus(Status status, int highscore, int level, int time, TimerType type) {
        this.status = status;
        this.highscore = highscore;
        this.level = level;
        this.time = time;
        this.type = type;
    }

    public GameStatus() {
        this(Status.WAIING, 0, 1, 0, TimerType.NONE);
    }

    public void addGame(String game) {
        if (!gamelist.contains(game)) gamelist.add(game);
    }

    public ArrayList<String> getGameList() {
        return gamelist;
    }

    public void setGame(String game) {
        this.selectedGame = game;
    }

    public String getSelectedGame() {
        return selectedGame;
    }

    public void setGame(int selected) {
        this.selectedGame = getGameList().get(selected);
    }

    public void addHighScore(int score) {
        highscore = highscore + score;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;

        if (status == Status.WAIING_GAMEOVER && !vibrated) {
            vibrated = true;
            long[] pattern = {0, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500};
            MainActivity.vibrator.vibrate(pattern, -1);
            Log.d("GameStatus", "onDataReceived: Vibrated");
        } else if (status != Status.WAIING_GAMEOVER) {
            vibrated = false;
        }
    }

    public int getHighscore() {
        return highscore;
    }

    public int getLevel() {
        return level;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setHighscore(int highscore) {
        this.highscore = highscore;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public TimerType getType() {
        return type;
    }

    public String getUsermac() {
        return usermac;
    }

    public void setUsermac(String usermac) {
        this.usermac = usermac;
    }

    public void reset(Status resetStatus, TimerType type, int time) {
        this.status = resetStatus;
        this.highscore = 0;
        this.level = 1;
        this.time = time;
        this.type = type;
    }

    public static enum Status {
        WAIING,
        WAIING_GAMEOVER,
        PAUSE,
        GAMEOVER,
        RUNNING;

        public String toString() {
            switch (this) {
                case WAIING:
                    return "waiting";
                case PAUSE:
                    return "pause";
                case GAMEOVER:
                    return "gameover";
                case RUNNING:
                    return "running";
                case WAIING_GAMEOVER:
                    return "waiting_gameover";
            }
            return "";
        }

        public static Status fromString(String name) {
            switch (name) {
                case "waiting":
                    return Status.WAIING;
                case "pause":
                    return Status.PAUSE;
                case "gameover":
                    return Status.GAMEOVER;
                case "running":
                    return Status.RUNNING;
                case "waiting_gameover":
                    return Status.WAIING_GAMEOVER;
            }
            return Status.WAIING;
        }
    }

    public static enum TimerType {
        COUNTDOWN,
        COUNTER,
        NONE;

        public String toString() {
            switch (this) {
                case COUNTDOWN:
                    return "countdown";
                case COUNTER:
                    return "counter";
                case NONE:
                    return "none";
            }
            return "";
        }
    }
}
