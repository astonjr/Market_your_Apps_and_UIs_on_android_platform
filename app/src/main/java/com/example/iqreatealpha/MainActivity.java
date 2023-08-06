package com.example.iqreatealpha;

import static com.example.iqreatealpha.R.*;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN = 2000;
    private FirebaseAuth auth;

    //Animation variales
    Animation topAnim, bottomAnim;
    ImageView logoimage;
    TextView name, tagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(layout.activity_main);

        auth = FirebaseAuth.getInstance();

        //Animations
        topAnim = AnimationUtils.loadAnimation(this, anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, anim.bottom_animation);

        //Hooks
        logoimage = findViewById(id.logoimage);
        name = findViewById(id.logoname);
        tagline = findViewById(id.logotagline);

        logoimage.setAnimation(topAnim);
        name.setAnimation(bottomAnim);
        tagline.setAnimation(bottomAnim);

        // Check if the user is already logged in
        if (auth.getCurrentUser() != null) {
            // User is already logged in, navigate to the DashboardActivity
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        } else {
            // User is not logged in, show the login activity
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, SPLASH_SCREEN);
        }
    }
}