package de.hochschule_bochum.blootothcontroller.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nikla on 19.07.2017.
 */

public class HighscoreList {
    private HashMap<String, ArrayList<Score>> scoreList = new HashMap<>();

    public void add(String game, Score score) {
        if(scoreList.containsKey(game)) {
            scoreList.get(game).add(score);
        } else {
            ArrayList<Score> arrayList = new ArrayList<>();
            arrayList.add(score);
            scoreList.put(game, arrayList);
        }
    }

    public ArrayList<Score> get(String game) {
        ArrayList<Score> scores = new ArrayList<>();
        if(!scoreList.containsKey(game)) return scores;
        for (Score score : scoreList.get(game)) {
            scores.add(score);
        }
        return scores;
    }

    public ArrayList<String> getGames() {
        ArrayList<String> games = new ArrayList<>();
        for(Map.Entry<String, ArrayList<Score>> entry : scoreList.entrySet()) {
            games.add(entry.getKey());
        }
        return games;
    }

    public String[] getGamesArray() {
        ArrayList<String> gamesList = getGames();
        String[] gamesArray = new String[gamesList.size()];
        for (int i = 0; i < gamesList.size(); i++) {
            gamesArray[i] = gamesList.get(i);
        }
        return gamesArray;
    }

    public void clear() {
        scoreList.clear();
    }
}
