package com.example.emode.model;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emode.R;
import com.example.emode.enumeradores.StatusSessao;
import com.example.emode.negocios.PacienteNegocios;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class SessaoAdapter extends FirestoreRecyclerAdapter<Sessao, SessaoAdapter.SessaoHolder> {
    private PacienteAdapter.OnItemClickListener listener;
    private CollectionReference pacienteRef = FirebaseFirestore.getInstance().collection("Paciente");
    private CollectionReference sessaoItemRef = FirebaseFirestore.getInstance().collection("SessaoItem");

    public SessaoAdapter(@NonNull FirestoreRecyclerOptions<Sessao> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final SessaoHolder holder, int position, @NonNull final Sessao model) {
        pacienteRef.document(model.getPacienteId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Paciente paciente = documentSnapshot.toObject(Paciente.class);

                holder.textViewPaciente.setText(paciente.getNome());
                holder.textViewDataInicio.setText(model.getDataHora());
                holder.textViewStatus.setText(StatusSessao.values()[model.getStatus()].toString());
                holder.cronometroDuracaoItem.setBase(SystemClock.elapsedRealtime() - model.getDuracao());
                holder.textViewFisioterapeuta.setText(model.getFisioterapeuta());

                setStatusColor(model, holder);
            }
        });
    }

    private void setStatusColor(Sessao model, SessaoHolder holder) {
        if (model.getStatus() == StatusSessao.NAO_INICIADA.toInt()) {
            holder.textViewStatus.setTextColor(holder.itemView.getResources().getColor(R.color.LighBlue));
        } else if (model.getStatus() == StatusSessao.EM_ANDAMENTO.toInt()) {
            holder.textViewStatus.setTextColor(holder.itemView.getResources().getColor(R.color.Yellow));
        } else if (model.getStatus() == StatusSessao.PAUSADA.toInt()) {
            holder.textViewStatus.setTextColor(holder.itemView.getResources().getColor(R.color.Red));
        } else if (model.getStatus() == StatusSessao.FINALIZADA.toInt()) {
            holder.textViewStatus.setTextColor(holder.itemView.getResources().getColor(R.color.Green));
        }
    }

    private Paciente getPaciente(String documentId) {
        PacienteNegocios pacienteNegocios = new PacienteNegocios();
        return pacienteNegocios.getPacienteById(documentId);
    }

    @NonNull
    @Override
    public SessaoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sessao_item,
                parent, false);
        return new SessaoAdapter.SessaoHolder(v);
    }

    public void deleteItem(int position) {
        deleteSessaoItens(getSnapshots().getSnapshot(position).getReference().getId());
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    private void deleteSessaoItens(String sessaoId) {
        sessaoItemRef.whereEqualTo("sessaoId", sessaoId)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    documentSnapshot.getReference().delete();
                }
            }
        });
    }

    class SessaoHolder extends RecyclerView.ViewHolder {
        TextView textViewPaciente;
        TextView textViewDataInicio;
        TextView textViewStatus;
        TextView textViewFisioterapeuta;
        Chronometer cronometroDuracaoItem;

        View itemView;


        public SessaoHolder(@NonNull View itemView) {
            super(itemView);

            this.itemView = itemView;

            textViewPaciente = itemView.findViewById(R.id.text_view_paciente);
            textViewDataInicio = itemView.findViewById(R.id.text_view_data);
            textViewStatus = itemView.findViewById(R.id.text_view_status);
            cronometroDuracaoItem = itemView.findViewById(R.id.cronometroItem);
            textViewFisioterapeuta = itemView.findViewById(R.id.text_view_fisioterapeuta);

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

    public void setOnItemClickListener(PacienteAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
