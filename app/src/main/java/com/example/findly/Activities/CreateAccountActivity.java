package com.example.findly.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.findly.Firebase.DatabaseUser;
import com.example.findly.Helpers.MyApp;
import com.example.findly.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText firstName, lastName, phoneNumber, email, username, password, confirmPassword;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        phoneNumber = findViewById(R.id.phone_number);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        Button signUpButton = findViewById(R.id.login_button);
        TextView loginText = findViewById(R.id.create_account);
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFieldsAndSignUp();
            }
        });
    }

    private void checkFieldsAndSignUp() {
        String first = firstName.getText().toString().trim();
        String last = lastName.getText().toString().trim();
        String name = first + " " + last;
        String user = username.getText().toString().trim();
        String phone = phoneNumber.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();

        if (first.isEmpty()) {
            firstName.setError("First name is required");
            firstName.requestFocus();
            return;
        }

        if (last.isEmpty()) {
            lastName.setError("Last name is required");
            lastName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            phoneNumber.setError("Phone number is required");
            phoneNumber.requestFocus();
            return;
        }

        if (user.isEmpty()) {
            username.setError("Username is required");
            username.requestFocus();
            return;
        }

        if (!Patterns.PHONE.matcher(phone).matches()) {
            phoneNumber.setError("Invalid phone number");
            phoneNumber.requestFocus();
            return;
        }

        if (mail.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            email.setError("Invalid email address");
            email.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            return;
        }

        if (confirmPass.isEmpty()) {
            confirmPassword.setError("Confirm password is required");
            confirmPassword.requestFocus();
            return;
        }

        if (!pass.equals(confirmPass)) {
            confirmPassword.setError("Passwords do not match");
            confirmPassword.requestFocus();
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(user)) {
                    username.setError("Username already exists");
                    username.requestFocus();
                } else {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String existingPhone = child.child(DatabaseUser.key_phone).getValue(String.class);
                        String existingEmail = child.child(DatabaseUser.key_email).getValue(String.class);

                        if (existingPhone != null && existingPhone.equals(phone)) {
                            phoneNumber.setError("Phone number already exists");
                            phoneNumber.requestFocus();
                            return;
                        }

                        if (existingEmail != null && existingEmail.equals(mail)) {
                            email.setError("Email already exists");
                            email.requestFocus();
                            return;
                        }
                    }
                    createNewUser(user, name, phone, mail, pass);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateAccountActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewUser(String user, String name, String phone, String mail, String pass) {
        DatabaseUser newUser = new DatabaseUser(name, phone, mail, pass);

        databaseReference.child(user).setValue(newUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CreateAccountActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                MyApp app = (MyApp) getApplication();
                app.setUserId(user);
                Intent intent = new Intent(CreateAccountActivity.this, LostListActivity.class);
                startActivity(intent);
            }
        });
    }
}
