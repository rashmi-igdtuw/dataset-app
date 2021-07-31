package com.rashmi.datasetapp;

/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private int photoTimer = 0;
    private String filename = null;
    private int cameraIndex = -1;
    List<String> cameraIds;
    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    private Stage stage = Stage.IN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            cameraIds = getAllCameras();
            filename = String.valueOf(System.currentTimeMillis());
            cameraIndex = -1;
            clickNextImage();
        }
    }

    private void clickNextImage() {
        cameraIndex = (cameraIndex + 1);
        if (cameraIndex >= cameraIds.size() && stage == Stage.GT) {
            Intent intent = new Intent(CameraActivity.this, MainActivity.class);
            startActivity(intent);
            return;
        }
        if (cameraIndex >= cameraIds.size() && stage == Stage.IN) {
            cameraIndex = 0;
            stage = Stage.GT;
            takePicture(filename, cameraIds.get(cameraIndex));
            return;
        }
        takePicture(filename, cameraIds.get(cameraIndex));
    }

    private void takePicture(String filename, String cameraId) {
        Camera2BasicFragment fragment = Camera2BasicFragment.newInstance();
        Bundle args = new Bundle();
        args.putString("CAMERAID", cameraId);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        photoTimer = 0;
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (photoTimer >= 5) {
                    doubleBeep();
                    fragment.takePhoto(stage.name() + "/" + cameraId + "/" + filename);
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            clickNextImage();
                        }
                    }, 5000);
                } else {
                    beep();
                    photoTimer++;
                    h.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    public List<String> getAllCameras() {
        List<String> cameras = new ArrayList<>();
        CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] idList = manager.getCameraIdList();
            int maxCameraCnt = idList.length;
            for (int index = 0; index < maxCameraCnt; index++) {
                String cameraId = manager.getCameraIdList()[index];
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    cameras.add(cameraId);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return cameras;
    }

    private void beep() {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
    }

    private void doubleBeep() {
        toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP2, 300);
    }

    public static enum Stage {
        IN, GT
    }
}