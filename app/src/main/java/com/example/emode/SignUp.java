package com.example.emode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.emode.util.Funcoes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUp extends AppCompatActivity {
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private TextInputLayout textInputNome;
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPassword;
    private TextInputLayout textInputPasswordConfirm;

    private Button btSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setTitle("Cadastre-se");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IniciarView();
        CriarEventos();
    }

    private void IniciarView() {
        textInputEmail = findViewById(R.id.text_input_email);
        textInputPassword = findViewById(R.id.text_input_password);
        textInputPasswordConfirm = findViewById(R.id.text_input_passwordConfirm);
        textInputNome = findViewById(R.id.text_input_nomeUsuario);
        btSalvar = findViewById(R.id.btSalvar);
    }

    private void CriarEventos() {
        btSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SalvarUsuario(view);
            }
        });

        textInputPasswordConfirm.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    btSalvar.performClick();
                }
                return false;
            }
        });
    }

    private boolean validateNome() {
        String nomeInput = textInputNome.getEditText().getText().toString().trim();

        if (nomeInput.isEmpty()) {
            textInputNome.setError("Nome do fisioterapeuta é obrigatório");
            return false;
        } else {
            textInputNome.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String emailInput = textInputEmail.getEditText().getText().toString().trim();

        if (emailInput.isEmpty()) {
            textInputEmail.setError("Preencha o e-mail");
            return false;
        } else if (!Funcoes.isValidEmail(emailInput)) {
            textInputEmail.setError("E-mail inválido");
            return false;
        } else {
            textInputEmail.setError(null);
            return true;
        }
    }

    private boolean validatePasswordEquals() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();
        String passwordConfirmInput = textInputPasswordConfirm.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty() | passwordConfirmInput.isEmpty()) {
            return true;
        } else if (!passwordInput.equals(passwordConfirmInput)) {
            String passwordNotEqual = "Senhas não coincidem";
            textInputPassword.setError(passwordNotEqual);
            textInputPasswordConfirm.setError(passwordNotEqual);
            return false;
        } else {
            textInputPassword.setError(null);
            textInputPasswordConfirm.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()) {
            textInputPassword.setError("Digite uma senha");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }

    private boolean validatePasswordConfirm() {
        String passwordConfirmInput = textInputPasswordConfirm.getEditText().getText().toString().trim();

        if (passwordConfirmInput.isEmpty()) {
            textInputPasswordConfirm.setError("Digite a confirmação da senha");
            return false;
        } else {
            textInputPasswordConfirm.setError(null);
            return true;
        }
    }

    public void SalvarUsuario(final View v) {
        if (!validateNome() | !validateEmail() | !validatePassword() | !validatePasswordConfirm() | !validatePasswordEquals()) {
            return;
        }

        final String nome = textInputNome.getEditText().getText().toString();
        final String email = textInputEmail.getEditText().getText().toString();
        String senha = textInputPassword.getEditText().getText().toString();


        firebaseAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nome).build();

                    firebaseAuth.getCurrentUser().updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                firebaseAuth.signOut();
                                Toast.makeText(SignUp.this, "Usuario criado com sucesso", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                } else {
                    Snackbar.make(v, task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}
