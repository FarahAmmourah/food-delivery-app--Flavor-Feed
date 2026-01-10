package com.farah.foodapp.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.farah.foodapp.R;
import com.farah.foodapp.login.LoginActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText etOldPassword, etNewPassword, etConfirmPassword;
    Button btnChangePassword, btnChangeLanguage, btnLogout, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //init views
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnChangeLanguage = findViewById(R.id.btn_change_language);
        btnLogout = findViewById(R.id.btn_logout);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        //password btn
        btnChangePassword.setOnClickListener(v -> changePassword());

        //language btn
        btnChangeLanguage.setOnClickListener(v -> {
            final String[] languages = {"English", "عربي"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose Language")
                    .setItems(languages, (dialog, which) -> {
                        if (which == 0) {
                            Toast.makeText(this, "Language set to English", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "تم تغيير اللغة للعربية", Toast.LENGTH_SHORT).show();
                        }
                    });
            builder.create().show();
        });

        //logout btn
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

    }

    private void changePassword() {
        String oldPass = etOldPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        //if user guest
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getUid())
                                .update("password", newPass);

                        Toast.makeText(this, "Password Updated", Toast.LENGTH_SHORT).show();
                        etOldPassword.setText("");
                        etNewPassword.setText("");
                        etConfirmPassword.setText("");
                    } else {
                        Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Re-authentication Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
