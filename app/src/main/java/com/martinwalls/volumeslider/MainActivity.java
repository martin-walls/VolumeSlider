package com.martinwalls.volumeslider;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements Orientation.Listener {

    private Orientation orientation;
    private LinearLayout sliderLayout;
    private ImageView sliderVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        orientation = new Orientation(this);
        sliderLayout = findViewById(R.id.slider);
        sliderVolume = findViewById(R.id.slider_volume);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        int width = metrics.widthPixels;

        LayoutParams layoutParams = sliderLayout.getLayoutParams();
        layoutParams.width = (int) (width * 0.7);
        sliderLayout.setLayoutParams(layoutParams);
    }

    @Override
    protected void onStart() {
        super.onStart();
        orientation.startListening(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        orientation.stopListening();
    }

    @Override
    public void onOrientationChanged(float yaw) {
//        yaw += 180;
        if (yaw < 0) {
            yaw += 180;
        } else {
            yaw -= 180;
        }
        // restrict range
        if (yaw < -40) {
            yaw = -40;
        } else if (yaw > 40) {
            yaw = 40;
        }

        sliderLayout.setRotation(yaw);

//        if (yaw < 10 && yaw > -10) {
//            yaw = 0;
//        }

        yaw += 40;

        // get new volume
        int newVolume = Math.round((yaw / 80) * 30);

        // set volume
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_PLAY_SOUND);


        int width = sliderLayout.getWidth();
        LayoutParams layoutParams = sliderVolume.getLayoutParams();
        layoutParams.width = (int) (width * (newVolume / 30.0));
        sliderVolume.setLayoutParams(layoutParams);

    }
}
