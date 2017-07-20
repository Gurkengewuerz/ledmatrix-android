package de.hochschule_bochum.blootothcontroller.objects;

/**
 * Created by nikla on 19.07.2017.
 */

public class Score {
    private String user;
    private int score;
    private int created;

    public Score(String user, int score, int created) {
        this.user = user;
        this.score = score;
        this.created = created;
    }

    public Score(String user, int score) {
        this(user, score, -1);
    }

    public String getUser() {
        return user;
    }

    public int getScore() {
        return score;
    }

    public int getCreated() {
        return created;
    }
}
