package com.intoverflown.handlepdfupload;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.intoverflown.handlepdfupload.databinding.ActivityMainBinding;

import java.io.File;
import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final int REQUEST_PERMISSION = 100;
    private Uri pdfUri;

    // Request code for selecting a PDF document.
    private static final int PICK_PDF_FILE = 2;

    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.v("perm", "Permission is granted");
                //  return true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                Log.d("perm", "Permission is denied");
                //return false;
            }
        }

        binding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                // Optionally, specify a URI for the file that should appear in the system file picker when it loads.
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pdfUri);
                startActivityForResult(intent, PICK_PDF_FILE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {}
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_PDF_FILE || resultCode != RESULT_OK) {
            return;
        } else {
            pdfUri = data.getData();

            Cursor cursor = getContentResolver().query(pdfUri, null, null, null, null);

            if (cursor.getCount() <= 0) {
                cursor.close();
                throw new IllegalArgumentException("Can't obtain file name, cursor is empty");
            }

            cursor.moveToFirst();

            fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

            cursor.close();

            Log.d("filename", fileName);
            binding.upload.setVisibility(View.GONE);
            binding.fileName.setText(fileName);
            binding.fileName.setVisibility(View.VISIBLE);

            // convert to base64 given a path
            File myFile = new File(pdfUri.toString());
            String path = myFile.getAbsolutePath();

            Log.d("filepath", path);

            byte[] encodedBytes = Base64.getEncoder().encode(path.getBytes());
            String pdfInBase64 = new String(encodedBytes);

            Log.d("file64", pdfInBase64);
        }
    }
}