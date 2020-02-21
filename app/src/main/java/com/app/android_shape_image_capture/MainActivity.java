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

package com.app.android_shape_image_capture;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.android_shape_image_capture.utils.App;
import com.app.android_shape_image_capture.utils.BitmapUtils;
import com.app.android_shape_image_capture.utils.CommonUtils;
import com.app.android_shape_image_capture.utils.FrescoUtils;
import com.app.android_shape_image_capture.view.Camera2Activity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;

import java.io.File;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    File mFile;
    boolean mHasSelectedOnce;
    SimpleDraweeView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.iv_image_click);
        mImageView.setOnClickListener(view -> {
            Intent intent;
            intent = new Intent(MainActivity.this, Camera2Activity.class);
            mFile = CommonUtils.createImageFile("mFile");
            //File save path and name
            intent.putExtra("file", mFile.toString());
            //Prompt text when taking photos
            intent.putExtra("hint", "Please put your ID in the box. The picture will be cropped, leaving only the image of the area inside the frame");
            //Whether to use the entire frame as the framing area (all bright areas)
            intent.putExtra("hideBounds", false);
            //Maximum allowed photo size (pixels)
            intent.putExtra("maxPicturePixels", 3840 * 2160);
            startActivityForResult(intent, App.TAKE_PHOTO_CUSTOM);
        });

        mImageView.setOnLongClickListener(view -> {
            if (mHasSelectedOnce) {
                View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog, null, false);
                SimpleDraweeView ivBig = dialogView.findViewById(R.id.iv_dialog_big);
                FrescoUtils.load("file://" + mFile.toString()).into(ivBig);
                final AlertDialog dialog = new AlertDialog
                        .Builder(MainActivity.this, R.style.Dialog_Translucent)
                        .setView(dialogView).create();
                ivBig.setOnClickListener(view1 -> dialog.dismiss());
                dialog.show();
            }
            return false;
        });
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK && resultCode != 200) return;
        if (requestCode == App.TAKE_PHOTO_CUSTOM) {
            mFile = new File(Objects.requireNonNull(data.getStringExtra("file")));
            Observable.just(mFile)
                    //将File解码为Bitmap
                    .map(file -> BitmapUtils.compressToResolution(file, 1920 * 1080))
                    //裁剪Bitmap
                    .map(BitmapUtils::crop)
                    //将Bitmap写入文件
                    .map(bitmap -> BitmapUtils.writeBitmapToFile(bitmap, "mFile"))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> {
                        mFile = file;
                        Uri uri = Uri.parse("file://" + mFile.toString());
                        ImagePipeline imagePipeline = Fresco.getImagePipeline();
                        //清除该Uri的Fresco缓存. 若不清除，对于相同文件名的图片，Fresco会直接使用缓存而使得Drawee得不到更新.
                        imagePipeline.evictFromMemoryCache(uri);
                        imagePipeline.evictFromDiskCache(uri);
                        FrescoUtils.load("file://" + mFile.toString()).resize(240, 164).into(mImageView);
                        mHasSelectedOnce = true;
                    });
        }
    }
}
