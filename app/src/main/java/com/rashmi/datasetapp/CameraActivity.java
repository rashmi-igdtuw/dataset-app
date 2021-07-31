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
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private List<String> cameraIds;
    private int cameraId = 1;
    private String filename = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraIds = getAllCameras();
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                openCamera(savedInstanceState);
                h.postDelayed(this, 20000);
            }
        }, 100);
    }

    private void openCamera(Bundle savedInstanceState) {
        if (null == savedInstanceState) {
            if (filename == null) filename = String.valueOf(System.currentTimeMillis());
            Camera2BasicFragment fragment = Camera2BasicFragment.newInstance();
            Bundle args = new Bundle();
            args.putString("CAMERAID", cameraIds.get(cameraId));
            args.putString("FILE", filename + "-" + cameraIds.get(cameraId) + ".jpg");
            fragment.setArguments(args);
            cameraId = (cameraId + 1) % cameraIds.size();
            if (cameraId == 0) filename = null;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
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
}