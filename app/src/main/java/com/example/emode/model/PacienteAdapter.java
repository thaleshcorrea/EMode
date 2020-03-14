package com.example.emode.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emode.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class PacienteAdapter extends FirestoreRecyclerAdapter<Paciente, PacienteAdapter.PacienteHolder> {
    private OnItemClickListener listener;

    public PacienteAdapter(@NonNull FirestoreRecyclerOptions<Paciente> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PacienteHolder holder, int position, @NonNull Paciente model) {
        holder.textViewNome.setText(model.getNome());
        holder.textViewIdade.setText(String.valueOf(model.getIdade()));
        holder.textViewSexo.setText(model.getSexo());
    }

    @NonNull
    @Override
    public PacienteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.paciente_item,
                parent, false);
        return new PacienteHolder(v);
    }

    public DocumentReference GetPacienteByPosition(int position) {
        return getSnapshots().getSnapshot(position).getReference();
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class PacienteHolder extends RecyclerView.ViewHolder {
        TextView textViewNome;
        TextView textViewIdade;
        TextView textViewSexo;

        public PacienteHolder(@NonNull View itemView) {
            super(itemView);

            textViewNome = itemView.findViewById(R.id.text_view_nome);
            textViewIdade = itemView.findViewById(R.id.text_view_idade);
            textViewSexo = itemView.findViewById(R.id.text_view_sexo);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}