package com.example.amp_mini_project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.amp_mini_project.Firebase.DatabaseUser;
import com.example.amp_mini_project.Helpers.MyApp;
import com.example.amp_mini_project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        Button loginButton = findViewById(R.id.login_button);
        TextView createAccount = findViewById(R.id.create_account);
        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();

                if (username.isEmpty()) {
                    usernameField.setError("Username is required");
                    usernameField.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    passwordField.setError("Password is required");
                    passwordField.requestFocus();
                    return;
                }

                authenticateUser(username, password);
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void authenticateUser(String uid, String pass) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(uid).exists()) {
                    String password = dataSnapshot.child(uid).child(DatabaseUser.key_password).getValue(String.class);
                    if (pass.equals(password)) {
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        MyApp app = (MyApp) getApplication();
                        app.setUserId(uid);
                        Intent intent = new Intent(LoginActivity.this, LostListActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        passwordField.setError("Incorrect password");
                        passwordField.requestFocus();
                    }
                } else {
                    usernameField.setError("User not found");
                    usernameField.requestFocus();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
