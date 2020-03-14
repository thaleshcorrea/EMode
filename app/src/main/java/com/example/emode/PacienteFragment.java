package com.example.emode;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emode.model.Paciente;
import com.example.emode.model.PacienteAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class PacienteFragment extends Fragment {
    private final String TAG = "PacienteFragment";

    private View _view;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference pacienteRef = db.collection("Paciente");
    private CollectionReference sessaoRef = FirebaseFirestore.getInstance().collection("Sessao");

    private PacienteAdapter adapter;

    private RecyclerView recyclerView;
    private FloatingActionButton floatButtonAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paciente, container, false);

        this._view = view;

        initViews();
        setUpRecyclerView();

        floatButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AbrirPacienteEditar(new Paciente());
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.appbar_paciente, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void initViews() {
        recyclerView = _view.findViewById(R.id.recyclerView);
        floatButtonAdd = _view.findViewById(R.id.floatingButtonAdd);
    }

    private FirestoreRecyclerOptions<Paciente> getOptions() {
        Query query = pacienteRef.orderBy("nome");

        FirestoreRecyclerOptions<Paciente> options = new FirestoreRecyclerOptions.Builder<Paciente>()
                .setQuery(query, Paciente.class)
                .build();

        return options;
    }

    private void setUpRecyclerView() {
        FirestoreRecyclerOptions<Paciente> options = getOptions();
        adapter = new PacienteAdapter(options);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(_view.getContext()));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                DocumentReference doc = adapter.GetPacienteByPosition(viewHolder.getAdapterPosition());
                sessaoRef.whereEqualTo("pacienteId", doc.getId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.getDocuments().size() == 0) {
                            adapter.deleteItem(viewHolder.getAdapterPosition());
                        } else {
                            Snackbar.make(getView(), "Paciente possui sessão vinculada", Snackbar.LENGTH_SHORT).show();
                            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    }
                });
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new PacienteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Paciente paciente = documentSnapshot.toObject(Paciente.class);
                paciente.setDocumentId(documentSnapshot.getId());

                AbrirPacienteEditar(paciente);
            }
        });
    }

    public void AbrirPacienteEditar(Paciente paciente) {
        Intent intent = new Intent(_view.getContext(), PacienteAdd.class);
        intent.putExtra("pacienteObj", paciente);
        startActivity(intent);
    }
}
