package com.azaan.cheatmic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.BassBoost;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.PresetReverb;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
        }, 100);

        startAudioEngine();
    }

    private void startAudioEngine() {
        int sampleRate = 44100;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, 
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return;

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        audioTrack = new AudioTrack(android.media.AudioManager.STREAM_VOICE_CALL,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 
                bufferSize, AudioTrack.MODE_STREAM);

        LoudnessEnhancer loudness = new LoudnessEnhancer(audioTrack.getAudioSessionId());
        loudness.setTargetGain(2000);
        loudness.setEnabled(true);

        PresetReverb echo = new PresetReverb(1, audioTrack.getAudioSessionId());
        echo.setPreset(PresetReverb.PRESET_LARGEROOM);
        echo.setEnabled(true);

        BassBoost bass = new BassBoost(1, audioTrack.getAudioSessionId());
        bass.setStrength((short) 1000);
        bass.setEnabled(true);

        isRecording = true;
        audioRecord.startRecording();
        audioTrack.play();

        new Thread(() -> {
            byte[] bytes = new byte[bufferSize];
            while (isRecording) {
                int read = audioRecord.read(bytes, 0, bytes.length);
                if (read > 0) audioTrack.write(bytes, 0, read);
            }
        }).start();

        Toast.makeText(this, "AZAANxCHEAT MIC ACTIVE", Toast.LENGTH_SHORT).show();
    }
}
