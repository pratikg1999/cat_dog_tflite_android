package com.pratik.cat_dog_tflite;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class ResultActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_PERM_REQUEST_CODE = 2;
    ImageView imView;
    LinearLayout progressLayout;
    TextView tvRes;
    TextView tvChooseImageLabel;
    Button bGallery;
    Button bCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imView = findViewById(R.id.im_image);
        progressLayout = findViewById(R.id.ll_progress);
        tvRes = findViewById(R.id.tv_result);
        tvChooseImageLabel = findViewById(R.id.tv_choose_new_image_lab);
        bCamera = findViewById(R.id.b_cam);
        bGallery = findViewById(R.id.b_gal);

        progressLayout.setVisibility(View.VISIBLE);
        tvRes.setVisibility(View.GONE);
        Typeface face = Typeface.createFromAsset(getAssets(), "Pacifico-Regular.ttf");
        tvRes.setTypeface(face);
        tvChooseImageLabel.setTypeface(face);
        bGallery.setTypeface(face);
        bCamera.setTypeface(face);

        bGallery.setOnClickListener(this);
        bCamera.setOnClickListener(this);

        Intent intent = getIntent();
        Bitmap imageBitmap = intent.getParcelableExtra("image-bitmap");
        if(imageBitmap!= null) {
            imView.setImageBitmap(imageBitmap);
        }
        else{
            Uri imageUri = intent.getParcelableExtra("image-uri");
            imView.setImageURI(imageUri);
        }
        try {
            Bitmap image = ((BitmapDrawable) ((ImageView) imView).getDrawable()).getBitmap();
            String imageClass = ImageClassifier.predict(image);
//            Toast.makeText(this, imageClass, Toast.LENGTH_SHORT).show();
            tvRes.setText(imageClass);
        }
        catch (Exception e){
            tvRes.setText("Can't find the image!");
        }
        tvRes.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
//                    Uri selectedImage = data.getData();
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    changeImage(bitmap);
                }
                break;
            case GALLERY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        changeImage(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void changeImage(Bitmap bitmap) {
        progressLayout.setVisibility(View.VISIBLE);
        tvRes.setVisibility(View.GONE);
        imView.setImageBitmap(bitmap);
        String imageClass = ImageClassifier.predict(bitmap);
//            Toast.makeText(this, imageClass, Toast.LENGTH_SHORT).show();
        tvRes.setText(imageClass);
        tvRes.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.b_cam:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_REQUEST_CODE);
                    Log.d("permission status", "onClick: denied");
                } else {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, CAMERA_REQUEST_CODE);
                }
                break;
            case R.id.b_gal:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
                break;
        }
    }
}
