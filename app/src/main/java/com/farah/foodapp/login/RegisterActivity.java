package com.farah.foodapp.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.farah.foodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword, etPhone,etRestaurantName;
    private Button btnRegister;
    private TextView tvAlreadyAccount;
    private RadioGroup rgRole;
    private RadioButton rbCustomer, rbRestaurant;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etRestaurantName = findViewById(R.id.etRestaurantName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        tvAlreadyAccount = findViewById(R.id.tvAlreadyAccount);

        rgRole = findViewById(R.id.rgRole);
        rbCustomer = findViewById(R.id.rbCustomer);
        rbRestaurant = findViewById(R.id.rbAdmin);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Show / hide restaurant field based on role
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbAdmin) {
                etRestaurantName.setVisibility(View.VISIBLE);
            } else {
                etRestaurantName.setVisibility(View.GONE);
            }
        });

        btnRegister.setOnClickListener(v -> registerUser());

        tvAlreadyAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password confirmation check
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = rbRestaurant.isChecked() ? "restaurant" : "customer";

        // Create Firebase user account
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = auth.getCurrentUser().getUid();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", uid);
                    userData.put("name", name);
                    userData.put("email", email);
                    userData.put("phone", phone);
                    userData.put("role", role);

                    // Save user in "users" collection
                    firestore.collection("users")
                            .document(uid)
                            .set(userData)
                            .addOnSuccessListener(unused -> {

                                if (role.equals("restaurant")) {
                                    Map<String, Object> restaurantData = new HashMap<>();
                                    restaurantData.put("id", uid);
                                    restaurantData.put("name", name);
                                    restaurantData.put("email", email);
                                    restaurantData.put("phone", phone);
                                    restaurantData.put("createdAt", System.currentTimeMillis());

                                    firestore.collection("restaurants")
                                            .document(uid)
                                            .set(restaurantData);
                                }

                                Toast.makeText(this,
                                        "Registered as " + role,
                                        Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Register Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
