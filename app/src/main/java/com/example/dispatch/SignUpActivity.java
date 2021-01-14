package com.example.dispatch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    RelativeLayout Page_1, Page_2;
    String firstName, lastName, mobileNumber, emailAddress, Bvn;
    EditText FName, LName, MNumber, EAddress, bvn;
    RadioGroup radioGroup;
    boolean isBikeAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Layouts
        Page_1 = findViewById(R.id.page_1);
        Page_2 = findViewById(R.id.page_2);

        // EditTexts
        FName = findViewById(R.id.et_first_name);
        LName = findViewById(R.id.et_last_name);
        MNumber = findViewById(R.id.et_mPhone);
        EAddress = findViewById(R.id.et_mEmail);
        bvn = findViewById(R.id.et_bvn);

        // RadioGroup
        radioGroup = findViewById(R.id.radioGroup);
    }

    public void Next_1(View view) {
        firstName = FName.getText().toString().trim();
        lastName = LName.getText().toString().trim();
        mobileNumber = MNumber.getText().toString().trim();
        emailAddress = EAddress.getText().toString().trim();

        if (firstName.isEmpty()) {
            FName.setError("Required");
            FName.requestFocus();
            return;
        }

        if (firstName.length() < 3) {
            FName.setError("Enter a valid name");
            FName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            LName.setError("Required");
            LName.requestFocus();
            return;
        }

        if (lastName.length() < 3) {
            LName.setError("Enter a valid name");
            LName.requestFocus();
            return;
        }

        if (mobileNumber.isEmpty()) {
            MNumber.setError("Required");
            MNumber.requestFocus();
            return;
        }

        if (mobileNumber.length() < 11 || mobileNumber.length() > 13) {
            MNumber.setError("Enter a valid phone number");
            MNumber.requestFocus();
            return;
        }

        if (emailAddress.isEmpty()) {
            EAddress.setError("Required");
            EAddress.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            EAddress.setError("Enter valid email address");
            EAddress.requestFocus();
            return;
        }

        Page_1.setVisibility(View.GONE);
        Page_2.setVisibility(View.VISIBLE);
    }

    public void Submit(View view) {
        Bvn = bvn.getText().toString().trim();
        RadioButton rbSelected = findViewById(radioGroup.getCheckedRadioButtonId());
        int selectedNr = radioGroup.indexOfChild(rbSelected) + 1;

        if (Bvn.isEmpty()) {
            bvn.setError("Required");
            bvn.requestFocus();
            return;
        }

        if (Bvn.length() != 11) {
            bvn.setError("Enter a valid BVN");
            bvn.requestFocus();
            return;
        }

        if (selectedNr == 1) {
            isBikeAvailable = true;
        } else {
            isBikeAvailable = false;
        }

        RadioButton radioButton1, radioButton2;
        radioButton1 = findViewById(R.id.radio1);
        radioButton2 = findViewById(R.id.radio2);

        if (radioButton1.isChecked() || radioButton2.isChecked()) {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "select an option to let us know if you are mobile or not.", Toast.LENGTH_LONG).show();
        }
    }
}