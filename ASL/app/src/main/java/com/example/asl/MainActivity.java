package com.example.asl;

import com.example.asl.ml.CNN_ASL_MNIST;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;




import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button select;
    Bitmap bitmap;
    ImageView img;
    //    TextView txt;
    ArrayList<String> labels;
    Uri selectedImage;
    Uri image_uri;
    final int PERMISSION_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        select = findViewById(R.id.selectBtn);
        img = findViewById(R.id.image);
//        txt = findViewById(R.id.textView);
        labels = new ArrayList<>(); //add labels



        selectedImage = getIntent().getParcelableExtra("image_uri");



        if (selectedImage != null) {

            Intent cropper = new Intent(getApplicationContext(), cropperActivity.class);
            cropper.putExtra("data", selectedImage.toString());
            startActivityForResult(cropper, 101);
            setImage(selectedImage);
        }


    }

    public void select(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        String[] mimeTypes = {"image/jpg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        getContent.launch(intent);

    }

    ActivityResultLauncher<Intent> getContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        selectedImage = data.getData();


                        Intent cropper = new Intent(getApplicationContext(), cropperActivity.class);
                        cropper.putExtra("data", selectedImage.toString());
                        startActivityForResult(cropper, 101);

//                        setImage(selectedImage);
//                        try {
//                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                    }
                }
            });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==-1 && requestCode==101){
            String result = data.getStringExtra("result");
            Uri resultUri = null;
            if(result!=null){
                resultUri = Uri.parse(result);
            }

            setImage(resultUri);
        }

    }

    public void predict(View v){
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        TensorImage tbuffer = TensorImage.createFrom(TensorImage.fromBitmap(resized), DataType.FLOAT32);
//        tbuffer = TensorImage.fromBitmap(resized);

        ByteBuffer byteBuffer = tbuffer.getBuffer();

        try {
//            MonkeypoxModelNet3 model = MonkeypoxModelNet3.newInstance(this);
//
//            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
//            Log.d("shape", byteBuffer.toString());
//            Log.d("shape", inputFeature0.toString());
//            inputFeature0.loadBuffer(byteBuffer);
//
//            MonkeypoxModelNet3.Outputs outputs = model.process(inputFeature0);
//            float[] outputFeature0 = outputs.getOutputFeature0AsTensorBuffer().getFloatArray();

            Efficientnetb7ModelOptimized model = Efficientnetb7ModelOptimized.newInstance(this);

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            Efficientnetb7ModelOptimized.Outputs outputs = model.process(inputFeature0);
            float[] outputFeature0 = outputs.getOutputFeature0AsTensorBuffer().getFloatArray();
//            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            int maxInd = getMaxInd(outputFeature0);
//            txt.setText(labels.get(maxInd)+ outputFeature0[maxInd]);     old version

            Intent i = new Intent(this, prediction.class);

            Bundle b = new Bundle();

            b.putString("prediction", labels.get(maxInd));
            b.putFloat("accuracy", outputFeature0[maxInd]);
            i.putExtras(b);
            startActivity(i);

            System.out.println(labels.get(maxInd));

            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }

    public int getMaxInd(float[] arr){
        float max = arr[0];
        int ind = 0;

        for (int i=1; i<arr.length; i++){
            if(arr[i]>max){
                max = arr[i];
                ind = i;
            }
        }

        return ind;
    }

    public void captureImage(View v) {


//        Intent i = new Intent(this, Camera.class);
//        startActivity(i);

//        ImageButton captureBtn;
//        ImageView image;
//        Uri image_uri;
//        Bitmap bitmap;

//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.activity_camera);
//
//            image = findViewById(R.id.imgView);
//            captureBtn = findViewById(R.id.captureBtn);
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, PERMISSION_CODE);


            } else {
                openCamera();
            }
        } else {
            openCamera();
        }
    }

    public void openCamera() {


        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        someActivityResultLauncher.launch(cameraIntent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {


                        Bundle b = new Bundle();
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);


//                        try {
//                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image_uri);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

//                        b.putParcelable("image", bitmap);
                        i.putExtra("image_uri", image_uri);
                        startActivity(i);

                    }
                }
            });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Permission denied ...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public void setImage (Uri image_uri){
        img.setImageURI(image_uri);
    }

}

