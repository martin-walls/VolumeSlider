package com.martinwalls.volumeslider;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.martinwalls.volumeslider.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements Rotation.Listener {

    private final int ORIENTATION_FACE_UP = 0;
    private final int ORIENTATION_FACE_DOWN = 1;
    private final int ORIENTATION_PORTRAIT = 2;
    private final int ORIENTATION_REVERSE_PORTRAIT = 3;
    private final int ORIENTATION_LANDSCAPE = 4;
    private final int ORIENTATION_REVERSE_LANDSCAPE = 5;

    private ActivityMainBinding binding;

    private Rotation rotation;

    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        rotation = new Rotation(this);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        int width = metrics.widthPixels;

        LayoutParams layoutParams = binding.slider.getLayoutParams();
        layoutParams.width = (int) (width * 0.7);
        binding.slider.setLayoutParams(layoutParams);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        rotation.startListening(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        rotation.stopListening();
    }

    @Override
    public void onRotationChanged(float yaw, float pitch, float roll) {

        float rotation = calculateRotation(yaw, pitch, roll);

        // allow the slider to rotate 45 degrees each way
        int rotationLimit = 45;

        // restrict range
        if (rotation < -rotationLimit) {
            rotation = -rotationLimit;
        } else if (rotation > rotationLimit) {
            rotation = rotationLimit;
        }

        // rotate the slider on screen
        setSliderRotation(rotation);

        // shift rotation value to start at 0, so volume can be calculated from it
        rotation += rotationLimit;

        int maxVolume = getSystemMaxVolume();

        // get new volume
        int newVolume = Math.round((rotation / (rotationLimit*2)) * maxVolume);

        // set volume
        setSystemVolume(newVolume);

        setSliderWidthPercent((float) newVolume / maxVolume);
    }

    private void setSliderWidthPercent(float percent) {
        int fullWidth = binding.slider.getWidth();
        LayoutParams layoutParams = binding.sliderVolume.getLayoutParams();
        layoutParams.width = (int) (fullWidth * percent);
        binding.sliderVolume.setLayoutParams(layoutParams);
    }

    private void setSliderRotation(float rotation) {
        binding.slider.setRotation(rotation);
    }

    private float calculateRotation(float yaw, float pitch, float roll) {
        float rotation;
        if (roll > 90) {
            rotation = -180 + roll;
        } else if (roll < -90) {
            rotation = 180 + roll;
        } else {
            rotation = -roll;
        }
        return rotation;
    }

    private int getSystemMaxVolume() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private void setSystemVolume(int volume) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
    }

    // disable volume buttons
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
}
