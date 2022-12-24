package com.example.pdftospeech;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button click_to_upload , upload , download , play_pause;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        click_to_upload = findViewById(R.id.button2);
        upload = findViewById(R.id.button);
        download = findViewById(R.id.button3);
        play_pause = findViewById(R.id.button4);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Set click listener on button
        click_to_upload.setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View v)
                    {
                        selectPDF();
                    }
                });
    }

    private void selectPDF()
    {
        // Initialize intent
        Intent intent = new Intent();
        // set type
        intent.setType("application/pdf");
//        set action
        intent.setAction(intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent,"PDF FILE SELECT" ),12);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==12 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            textView.setText(data.getDataString().substring(data.getDataString().lastIndexOf("/")+1));
            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uploadPDFFile(data.getData());
                }
            });
        }
    }

    private void uploadPDFFile(Uri data) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("File is loading...");
        progressDialog.show();

        StorageReference reference = storageReference.child("upload"+System.currentTimeMillis()+".pdf");

        reference.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri uri = uriTask.getResult();

                putPDF putPDF = new putPDF(textView.getText().toString(),uri.toString());
                databaseReference.child(databaseReference.push().getKey()).setValue(putPDF);
                Toast.makeText(MainActivity.this,"File Upload",Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.0* snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage("File Uploaded .."+(int) progress+"%");

            }
        });
    }

}

