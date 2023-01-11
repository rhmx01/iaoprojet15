package com.example.hardware;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    LinearLayout mainLayout;
    Button cameraBtn, videoBtn, vibratorBtn;
    SwitchCompat bluetoothSw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.main);
        cameraBtn = findViewById(R.id.take_img);
        videoBtn = findViewById(R.id.record_video);
        vibratorBtn = findViewById(R.id.vibrate);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothSw = findViewById(R.id.enable);


        // vérifier l'état de l'empreinte digitale
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(getApplicationContext(), "no fingerprint", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(getApplicationContext(), "not working", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(getApplicationContext(), "no fingerprint assigned", Toast.LENGTH_SHORT).show();
                break;
        }


        // demande à l'utilisateur de s'authentifier par biométrie
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                mainLayout.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        // configurer et afficher la boîte de dialogue
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .build();
        biometricPrompt.authenticate(promptInfo);




        // On click open camera
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //only API level >= 23 Android 6.0 Marshmallow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkCameraHardwareExist(getApplicationContext())) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA};
                        requestPermissions(permission, 1002);
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, 1001);
                    }
                }
            }
        });


        // On click open camera video
        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //only API level >= 23 Android 6.0 Marshmallow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkCameraHardwareExist(getApplicationContext())) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA};
                        requestPermissions(permission, 1003);
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        startActivityForResult(intent, 1004);
                    }
                }
            }
        });


        // on click vibrate
        vibratorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                //only API level >= 26  Android 8.0 Oreo
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                }else {
                    v.vibrate(1000);
                }
            }
        });




        bluetoothSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!bluetoothAdapter.isEnabled()) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT},
                                    1005);
                        }
                        if (bluetoothAdapter.enable()) {
                            bluetoothSw.setChecked(true);
                            Toast.makeText(MainActivity.this, "Turning on Bluetooth..", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    bluetoothAdapter.disable();
                }
            }
        });

    }




    /** Check if this device has a camera */
    private boolean checkCameraHardwareExist(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }



}