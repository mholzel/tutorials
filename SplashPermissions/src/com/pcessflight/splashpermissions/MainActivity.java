package com.pcessflight.splashpermissions;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /** Default creation code. */
        super.onCreate(savedInstanceState);

        /** Create the layout that will hold the TextView. */
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        /** Add a TextView and set the initial text. */
        TextView textView = new TextView(this);
        textView.setTextSize(50);
        textView.setText("Main Activity");
        mainLayout.addView(textView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        /** Set the mainLayout as the content view */
        setContentView(mainLayout);
    }
}