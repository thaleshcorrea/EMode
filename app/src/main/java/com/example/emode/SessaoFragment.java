package com.example.emode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emode.enumeradores.StatusSessao;
import com.example.emode.model.PacienteAdapter;
import com.example.emode.model.Sessao;
import com.example.emode.model.SessaoAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class SessaoFragment extends Fragment {
    private View _view;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference sessaoRef = db.collection("Sessao");

    private SessaoAdapter adapter;
    public Sessao sessaoSelecionada;

    private RecyclerView recyclerView;
    private FloatingActionButton floatButtonAdd;

    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sessao, container, false);

        this._view = view;

        IniciarViews();
        CriarEventos();
        CarregarRecyclerView();

        return view;
    }

    private void IniciarViews() {
        recyclerView = _view.findViewById(R.id.recyclerViewSessao);
        floatButtonAdd = _view.findViewById(R.id.floatingButtonAddSessao);
    }

    private void CriarEventos() {
        floatButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessaoSelecionada = new Sessao();
                AbrirAdicionarSessao();
            }
        });
    }

    private void CarregarRecyclerView() {
        Query query = sessaoRef.orderBy("status");
        FirestoreRecyclerOptions<Sessao> options = new FirestoreRecyclerOptions.Builder<Sessao>()
                .setQuery(query, Sessao.class)
                .build();
        adapter = new SessaoAdapter(options);

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
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new PacienteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Sessao sessao = documentSnapshot.toObject(Sessao.class);
                sessao.setDocumentId(documentSnapshot.getId());
                sessaoSelecionada = sessao;

                if(sessao.getStatus() != StatusSessao.FINALIZADA.toInt()) {
                    SessaoBottomOptions sessaoBottomOptions = new SessaoBottomOptions();
                    sessaoBottomOptions.show(getFragmentManager(), "OptionsSessao");
                } else {
                    AbrirDetalhesDaSessao(sessao);
                }
            }
        });
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

    private void AbrirDetalhesDaSessao(Sessao sessao) {
        Intent intentDetails = new Intent(getContext(), SessaoActivity.class);
        intentDetails.putExtra("sessaoObj", sessao);
        startActivity(intentDetails);
    }

    private void AbrirAdicionarSessao() {
        Intent intentDetails = new Intent(_view.getContext(), SessaoAdd.class);
        intentDetails.putExtra("sessaoObj", new Sessao());
        startActivity(intentDetails);
    }
}