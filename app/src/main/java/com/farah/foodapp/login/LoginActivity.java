package com.farah.foodapp.login;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.farah.foodapp.R;
import com.farah.foodapp.admin.AdminDashboardActivity;
import com.farah.foodapp.reel.ReelsActivity;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgot;
    private FirebaseAuth auth;
    private SignInButton btnGoogle;
    private RadioGroup rgRole;
    private RadioButton rbCustomer, rbRestaurant;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgot = findViewById(R.id.tvForgot);
        btnGoogle = findViewById(R.id.btnGoogle);

        // Role selection
        rgRole = findViewById(R.id.rgRole);
        rbCustomer = findViewById(R.id.rbCustomer);
        rbRestaurant = findViewById(R.id.rbAdmin);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        // Customize Google Sign-In
        if (btnGoogle != null) {
            btnGoogle.setSize(SignInButton.SIZE_WIDE);
            for (int i = 0; i < btnGoogle.getChildCount(); i++) {
                View v = btnGoogle.getChildAt(i);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTextSize(18);
                    tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                    tv.setTextColor(Color.BLACK);
                    break;
                }
            }
        }
    }


    // user login and role validation
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = rgRole.getCheckedRadioButtonId();
        final String selectedRole;

        if (selectedId == R.id.rbCustomer) {
            selectedRole = "customer";
        } else if (selectedId == R.id.rbAdmin) {
            selectedRole = "restaurant";
        } else {
            Toast.makeText(this, "Please select role", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) return;

                    // Fetch user role from Firestore
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.getUid())
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()) {
                                    Toast.makeText(this,
                                            "User data not found",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String role = doc.getString("role");

                                // Validate selected role with stored role
                                if (role != null && role.equals(selectedRole)) {

                                    Toast.makeText(this,
                                            "Login Successful",
                                            Toast.LENGTH_SHORT).show();

                                    if (role.equals("restaurant")) {
                                        startActivity(new Intent(this,
                                                AdminDashboardActivity.class));
                                    } else {
                                        startActivity(new Intent(this,
                                                ReelsActivity.class));
                                    }
                                    finish();

                                } else {
                                    Toast.makeText(this,
                                            "Wrong role selected",
                                            Toast.LENGTH_SHORT).show();
                                    auth.signOut();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Login Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}
