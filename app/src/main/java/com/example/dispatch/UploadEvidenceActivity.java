package com.example.dispatch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.UUID;

public class UploadEvidenceActivity extends AppCompatActivity {

    public static int Request_Code = 1;
    String userId;
    ImageView imageView;
    Bitmap bitmap;
    StorageReference mStorage;
    FirebaseFirestore db;
    DocumentReference deliveryRef;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_evidence);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_700));

        Intent intent = getIntent();
        String DocId = intent.getStringExtra("RefId");

        db = FirebaseFirestore.getInstance();
        imageView = findViewById(R.id.imageView);
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        mStorage = FirebaseStorage.getInstance().getReference().child("evidence/" + UUID.randomUUID().toString());
        deliveryRef = db.collection(getString(R.string.collection_completed_delivery)).document(userId).collection("deliveries").document(DocId);
    }

    public void TakePicture(View view) {
        Intent picture_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(picture_intent, Request_Code);
    }

    public void UploadEvidence(View view) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteOutput);
        byte[] file = byteOutput.toByteArray();

        if (file != null) {
            mStorage.putBytes(file)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            GetDownloadUrl();
                            Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }

    }

    private void GetDownloadUrl() {
        mStorage.getDownloadUrl().addOnSuccessListener(uri -> {
            if (uri != null) {
                String url = uri.toString();
                Log.i("URL", url);
                deliveryRef.update("imageUrl", url).addOnCompleteListener(task -> {
                });
            } else {
                Toast.makeText(UploadEvidenceActivity.this, "Upload Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Request_Code && data != null) {
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}