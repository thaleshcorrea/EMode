package com.example.emode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.emode.model.Paciente;
import com.example.emode.model.PacienteAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class PacienteSelecionar extends AppCompatActivity {
    private static final String TAG = "Selecionar Paciente";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference pacienteRef = db.collection("Paciente");

    private PacienteAdapter adapter;

    private RecyclerView recyclerView;

    public Paciente paciente;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paciente_selecionar);

        getSupportActionBar().setTitle("Selecione um paciente");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IniciarViews();
        CriarRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar_paciente, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    private void IniciarViews() {
        recyclerView = findViewById(R.id.recyclerViewSelecionar);
    }

    private FirestoreRecyclerOptions<Paciente> getOptions() {
        Query query = pacienteRef.orderBy("nome");

        FirestoreRecyclerOptions<Paciente> options = new FirestoreRecyclerOptions.Builder<Paciente>()
                .setQuery(query, Paciente.class)
                .build();

        return options;
    }

    private void CriarRecyclerView() {
        FirestoreRecyclerOptions<Paciente> options = getOptions();
        adapter = new PacienteAdapter(options);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new PacienteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Paciente paciente = documentSnapshot.toObject(Paciente.class);
                paciente.setDocumentId(documentSnapshot.getId());

                PacienteSelecionar.this.paciente = paciente;
                RetornarPaciente();
                finish();
            }
        });
    }

    private void RetornarPaciente() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("objPaciente", paciente);
        setResult(Activity.RESULT_OK, resultIntent);

    }
}
