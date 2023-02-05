package com.example.asl;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class prediction extends AppCompatActivity {

    TextView predTxt;
    TextView accTxt;
    TextView link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        predTxt = findViewById(R.id.pred_txt);
        accTxt = findViewById(R.id.acc_txt);
        link = findViewById(R.id.link);

        link.setMovementMethod(LinkMovementMethod.getInstance());

        Intent i = getIntent();
        Bundle b = i.getExtras();

        String pred = b.getString("prediction");
        float acc = b.getFloat("accuracy");

        predTxt.setText(pred);
        accTxt.setText(String.valueOf(acc));
    }
}