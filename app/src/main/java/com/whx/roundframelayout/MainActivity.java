package com.whx.roundframelayout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = findViewById(R.id.top_left_radius);
        RoundFrameLayout roundFrameLayout = findViewById(R.id.round_layout);
        findViewById(R.id.change_corner).setOnClickListener(v -> {
            float r = dp2px(Double.parseDouble(editText.getText().toString()));
            roundFrameLayout.setTopLeftRadius(r);
        });
    }

    public int dp2px(double dip) {
        Context context = this;
        if (context != null && context.getResources() != null && context.getResources().getDisplayMetrics() != null) {
            double density = (context.getResources().getDisplayMetrics()).density;
            return (int) (density * dip + 0.5D);
        }
        return 0;
    }
}