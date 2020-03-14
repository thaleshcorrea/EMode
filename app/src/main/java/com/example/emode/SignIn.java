package com.example.emode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.emode.util.Funcoes;
import com.example.emode.util.Preferences;
import com.example.emode.util.Valores;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignIn extends AppCompatActivity {
    private static final String TAG = "Login";

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    private TextInputLayout textInputEmail;
    private TextInputLayout textInputSenha;
    private Button btEntrar;
    private Button btCadastrar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        IniciarViews();
        CriarEventos();
        VerificarLogin();
    }

    private void IniciarViews() {
        textInputEmail = findViewById(R.id.textInputEmail);
        textInputSenha = findViewById(R.id.text_input_password);
        btEntrar = findViewById(R.id.btLogin);
        btCadastrar = findViewById(R.id.btCadastrar);
        progressBar = findViewById(R.id.progress_circular);
    }

    private void CriarEventos() {
        btEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(view);
            }
        });

        btCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
    }

    private void VerificarLogin() {
        final String idToken = Preferences.getLogin(this);
        if (!idToken.isEmpty()) {
            firebaseAuth.signInWithCustomToken(idToken).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    String userId = authResult.getUser().getUid();
                }
            });
        }
    }

    private Boolean validateEmailInput() {
        String emailInput = textInputEmail.getEditText().getText().toString().trim();

        if (emailInput.isEmpty()) {
            textInputEmail.setError("Campo vazio");
            return false;
        } else if (!Funcoes.isValidEmail(emailInput)) {
            textInputEmail.setError("E-mail inválido");
            return false;
        } else {
            return true;
        }
    }

    private boolean validateSenha() {
        String senhaInput = textInputSenha.getEditText().getText().toString().trim();

        if (senhaInput.isEmpty()) {
            textInputSenha.setError("Campo vazio");
            return false;
        } else {
            return true;
        }
    }

    private void signIn(final View v) {
        if (!validateEmailInput() | !validateSenha()) {
            return;
        }

        String email = textInputEmail.getEditText().getText().toString();
        String senha = textInputSenha.getEditText().getText().toString();

        progressBar.setVisibility(ProgressBar.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Preferences.setLogin(SignIn.this, firebaseAuth.getUid());
                        Valores.setUsuario(firebaseAuth.getCurrentUser());

                        progressBar.setVisibility(ProgressBar.GONE);
                        AbrirMainActivity();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(ProgressBar.GONE);
                        Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        Log.d(TAG, e.getMessage());
                    }
                });
    }

    private void signUp() {
        startActivity(new Intent(this, SignUp.class));
    }

    private void AbrirMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
