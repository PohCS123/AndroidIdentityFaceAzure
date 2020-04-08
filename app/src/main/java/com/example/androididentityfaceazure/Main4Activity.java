package com.example.androididentityfaceazure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Main4Activity extends AppCompatActivity {
    Button btConfirm;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        btConfirm = findViewById(R.id.button_username);
        final EditText editText_username = findViewById(R.id.editText_username);

        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String username = editText_username.getText().toString();
                Intent intent = new Intent(getApplicationContext(), Main3Activity.class);
                intent.putExtra("username", username);
                startActivity(intent);


            }

        });
    }
}
