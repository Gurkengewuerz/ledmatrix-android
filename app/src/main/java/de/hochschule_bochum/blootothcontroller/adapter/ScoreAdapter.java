package de.hochschule_bochum.blootothcontroller.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.hochschule_bochum.blootothcontroller.R;
import de.hochschule_bochum.blootothcontroller.objects.HighscoreList;
import de.hochschule_bochum.blootothcontroller.objects.Score;

/**
 * 6
 * Created by nikla on 08.07.2017.
 */

public class ScoreAdapter implements ListAdapter {

    private HighscoreList list;
    private Context context;
    private String game = "";

    public ScoreAdapter(Context context, HighscoreList list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return list.get(game).size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(game).get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_score, null);
        }

        Score score = (Score) getItem(position);
        ((TextView) convertView.findViewById(R.id.score_text)).setText(String.valueOf(score.getScore()));
        ((TextView) convertView.findViewById(R.id.username_text)).setText(score.getUser());
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return list.get(game).isEmpty();
    }

    public void setGame(String game) {
        this.game = game;
    }

    public void selectFirstGame() {
        ArrayList<String> games = list.getGames();
        if (!games.isEmpty()) this.game = games.get(0);
    }
}
