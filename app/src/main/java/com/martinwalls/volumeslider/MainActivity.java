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

public class MainActivity extends AppCompatActivity implements Rotation.Listener {

    private Rotation rotation;
    private LinearLayout sliderLayout;
    private ImageView sliderVolume;

    private final int ORIENTATION_FACE_UP = 0;
    private final int ORIENTATION_FACE_DOWN = 1;
    private final int ORIENTATION_PORTRAIT = 2;
    private final int ORIENTATION_REVERSE_PORTRAIT = 3;
    private final int ORIENTATION_LANDSCAPE = 4;
    private final int ORIENTATION_REVERSE_LANDSCAPE = 5;

    private AudioManager audioManager;
    private int maxVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rotation = new Rotation(this);
        sliderLayout = findViewById(R.id.slider);
        sliderVolume = findViewById(R.id.slider_volume);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        int width = metrics.widthPixels;

        LayoutParams layoutParams = sliderLayout.getLayoutParams();
        layoutParams.width = (int) (width * 0.7);
        sliderLayout.setLayoutParams(layoutParams);

        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
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

    private int getOrientation(float pitch, float roll) {
        if (pitch > 45) {
            // tilted up
            return ORIENTATION_PORTRAIT;
        } else if (pitch < -45) {
            // tilted down
            return ORIENTATION_REVERSE_PORTRAIT;
        } else if (Math.abs(roll) > 135) {
            // upside down
            return ORIENTATION_FACE_DOWN;
        } else if (Math.abs(roll) < 45) {
            // face up
            return ORIENTATION_FACE_UP;
        } else if (roll > 0) {
            // landscape
            return ORIENTATION_LANDSCAPE;
        } else { // roll < 0
            // upside down landscape
            return ORIENTATION_REVERSE_LANDSCAPE;
        }
    }

    @Override
    public void onRotationChanged(float yaw, float pitch, float roll) {


        int orientation = getOrientation(pitch, roll);

        float rotation;

        switch (orientation) {
            case ORIENTATION_FACE_UP:
            case ORIENTATION_PORTRAIT:
            default:
                if (roll > 90) {
                    rotation = -(180 - roll);
                } else if (roll < -90) {
                    rotation = 180 + roll;
                } else {
                    rotation = -roll;
                }
                break;
//            case ORIENTATION_LANDSCAPE:
//                rotation = pitch;
//                break;
//            case ORIENTATION_REVERSE_LANDSCAPE:
//                rotation = -pitch;
//                break;
        }

//        rotation = pitch;
//        rotation *= -1;

        int rotationLimit = 45;

        // restrict range
        if (rotation < -rotationLimit) {
            rotation = -rotationLimit;
        } else if (rotation > rotationLimit) {
            rotation = rotationLimit;
        }

        switch (orientation) {
//            case ORIENTATION_LANDSCAPE:
//                sliderLayout.setRotation(rotation + 90);
//                break;
//            case ORIENTATION_REVERSE_LANDSCAPE:
//                sliderLayout.setRotation(rotation - 90);
//                break;
            default:
                sliderLayout.setRotation(rotation);
                break;
        }

        rotation += rotationLimit;

        // get new volume
        int newVolume = Math.round((rotation / (rotationLimit*2)) * maxVolume);

        // set volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_PLAY_SOUND);


        int width = sliderLayout.getWidth();
        LayoutParams layoutParams = sliderVolume.getLayoutParams();
        layoutParams.width = (int) (width * (newVolume / ((float) maxVolume)));
        sliderVolume.setLayoutParams(layoutParams);

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
