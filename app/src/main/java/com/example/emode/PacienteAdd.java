package com.example.emode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.service.autofill.Sanitizer;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.emode.model.Paciente;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class PacienteAdd extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference pacienteRef = db.collection("Paciente");

    private TextInputLayout textInputNome;
    private TextInputLayout textInputIdade;
    private TextInputLayout textInputAltura;
    private TextInputLayout textInputPeso;
    private TextInputLayout textInputSexo;
    private TextInputLayout textInputPatologia;
    private Button btSalvar;

    private Paciente paciente;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paciente_add);

        paciente = (Paciente) getIntent().getSerializableExtra("pacienteObj");

        getSupportActionBar().setTitle("Novo paciente");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IniciarViews();
        CriarEventos();
        ExibirValores();
    }

    private void IniciarViews(){
        textInputNome = findViewById(R.id.text_input_nome);
        textInputSexo = findViewById(R.id.textInputSexo);
        textInputIdade = findViewById(R.id.text_input_idade);
        textInputAltura = findViewById(R.id.text_input_altura);
        textInputPeso = findViewById(R.id.text_input_peso);
        textInputPatologia = findViewById(R.id.text_input_patologia);
        btSalvar = findViewById(R.id.btSalvar);
    }

    private void ExibirValores() {
        if(paciente.getDocumentId() == null) {
            paciente = new Paciente();
            return;
        }

        textInputNome.getEditText().setText(paciente.getNome());
        textInputSexo.getEditText().setText(paciente.getSexo());
        textInputPatologia.getEditText().setText(paciente.getPatologia());
        textInputIdade.getEditText().setText(String.valueOf(paciente.getIdade()));
        textInputAltura.getEditText().setText(String.valueOf(paciente.getAltura()));
        textInputPeso.getEditText().setText(String.valueOf(paciente.getPeso()));

        getSupportActionBar().setTitle(paciente.getNome());
    }

    private void AtribuirValores() {
        String idade = textInputIdade.getEditText().getText().toString();
        String altura = textInputAltura.getEditText().getText().toString();
        String peso = textInputPeso.getEditText().getText().toString();

        paciente.setNome( textInputNome.getEditText().getText().toString().trim() );
        paciente.setSexo(textInputSexo.getEditText().getText().toString().trim());
        paciente.setPatologia(textInputPatologia.getEditText().getText().toString().trim());
        paciente.setIdade( idade.isEmpty() ? 0 : Integer.parseInt(idade) );
        paciente.setAltura( altura.isEmpty() ? 0 : Integer.parseInt(altura) );
        paciente.setPeso( peso.isEmpty() ? 0 : Integer.parseInt(peso) );

        paciente.getNome();
    }

    private void CriarEventos() {
        btSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Salvar();
            }
        });
    }

    private boolean validateNome() {
        if(paciente.getNome().isEmpty()) {
            textInputNome.setError("Nome é obrigatório");
            return false;
        } else {
            textInputNome.setError(null);
            return true;
        }
    }

    private boolean validateSexo() {
        if(paciente.getSexo().isEmpty()) {
            textInputSexo.setError("Sexo é obrigatório");
            return false;
        } else {
            textInputSexo.setError(null);
            return true;
        }
    }

    private boolean validateIdade() {
        if(paciente.getIdade() == 0) {
            textInputIdade.setError("Idade inválida");
            return false;
        } else {
            textInputIdade.setError(null);
            return true;
        }
    }

    private boolean validateAltura() {
        if(paciente.getAltura() == 0) {
            textInputAltura.setError("Altura inválida");
            return false;
        } else {
            textInputAltura.setError(null);
            return true;
        }
    }

    private boolean validatePeso() {
        if(paciente.getPeso() == 0) {
            textInputPeso.setError("Peso inválido");
            return false;
        } else {
            textInputPeso.setError(null);
            return true;
        }
    }

    private boolean validate() {
        if(!validateNome() |  !validateSexo() | !validateIdade() | !validateAltura() | !validatePeso()){
            return false;
        }
        return true;
    }

    public void Salvar() {
        AtribuirValores();

        if(!validate()) {
            return;
        }

        if(paciente.getDocumentId() == null) {
            AdicionarPaciente();
        }
        else {
            AlterarPaciente();
        }
    }

    private void AdicionarPaciente() {
        pacienteRef.add(paciente)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(PacienteAdd.this, "Paciente cadastrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(getWindow().getDecorView().getRootView(), e.getMessage(), Snackbar.LENGTH_SHORT);
                    }
                });
    }

    private void AlterarPaciente() {
        pacienteRef.document(paciente.getDocumentId()).set(paciente).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(getWindow().getDecorView().getRootView(), e.getMessage(), Snackbar.LENGTH_SHORT);
            }
        });
    }
}