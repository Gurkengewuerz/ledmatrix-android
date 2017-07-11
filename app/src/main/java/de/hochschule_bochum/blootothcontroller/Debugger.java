package de.hochschule_bochum.blootothcontroller;

import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nikla on 09.07.2017.
 */

public class Debugger {
    private int maxPuffer = 250;
    private ArrayList<String> puffer = new ArrayList<>();
    private TextView textView;
    private ScrollView scrollView;

    public void log(String text) {
        puffer.add(text);
        if (puffer.size() > maxPuffer) puffer.remove(0);
        update();
    }

    public void update() {
        if (textView == null || scrollView == null) return;
        textView.setText("");
        for (String s : puffer) {
            textView.append("\n" + s);
        }
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    public void updateViews(TextView textView, ScrollView scrollView) {
        this.textView = textView;
        this.scrollView = scrollView;
        update();
    }

    public void updateViews() {
        updateViews(null, null);
    }
}
