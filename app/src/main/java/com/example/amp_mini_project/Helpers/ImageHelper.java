package com.example.findly.Helpers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageHelper {

    public static final int PICK_IMAGE_REQUEST = 1;
    public static final int CAMERA_REQUEST_CODE = 2;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;

    private static Uri photoUri;

    public static void openFileChooser(Activity activity) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public static void showImageSourceDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Select Image Source")
                .setItems(new CharSequence[]{"Gallery", "Camera"}, (dialog, which) -> {
                    if (which == 0) {
                        openFileChooser(activity);
                    } else if (which == 1) {
                        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.CAMERA},
                                    CAMERA_PERMISSION_REQUEST_CODE);
                        } else {
                            openCamera(activity);
                        }
                    }
                })
                .create().show();
    }


    public static void openCamera(Activity activity) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) == null) {
            Log.i("ImageHelper", "No camera app detected via resolveActivity(), proceeding anyway.");
        }
        try {
            File photoFile = createImageFile(activity);
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(activity,
                        "com.example.findly.fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                activity.startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Toast.makeText(activity, "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    public static File createImageFile(Activity activity) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    public static Uri handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return null;
        }
        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            return data.getData();
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            return photoUri;
        }
        return null;
    }

    public static boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            return grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public static Uri getPhotoUri() { return photoUri; }
}
