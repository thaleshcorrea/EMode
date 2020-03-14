package com.example.emode;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.emode.enumeradores.StatusSessao;
import com.example.emode.model.Paciente;
import com.example.emode.model.Sessao;
import com.example.emode.model.SessaoItem;
import com.example.emode.negocios.PacienteNegocios;
import com.example.emode.negocios.SessaoNegocios;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SessaoActivity extends AppCompatActivity {
    private final String TAG = "SessãoDetails";

    private Sessao sessao;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference sessaoRef = db.collection("Sessao");
    private CollectionReference sessaoItemRef = db.collection("SessaoItem");

    private Toolbar toolbar;
    private TextView textViewPaciente;
    private TextView textViewStatus;
    private TextView textViewFisioterapeuta;
    private TextView textViewData;
    private Chronometer cronometroDetails;
    private FloatingActionButton fab;
    private Button btFinalizar;

    private LineChart lineChart;
    private ArrayList<Entry> listaItemsX = new ArrayList<>();
    private ArrayList<Entry> listaItemsY = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessao);

        sessao = (Sessao) getIntent().getSerializableExtra("sessaoObj");
        IniciarViews();
        CriarEventos();
        atribuirValores(sessao);

        Description description = new Description();
        description.setText("Tempo (s)");
        lineChart.setDescription(description);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        VerficarAlteracoesSessao();
        MonitorarItens();
    }

    private void VerficarAlteracoesSessao() {
        sessaoRef.document(sessao.getDocumentId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                Sessao _sessao = documentSnapshot.toObject(Sessao.class);
                if(_sessao == null) {
                    return;
                }

                _sessao.setDocumentId(documentSnapshot.getId());
                sessao = _sessao;

                atribuirValores(_sessao);
            }
        });
    }

    private void MonitorarItens() {
        sessaoItemRef.whereEqualTo("sessaoId", sessao.getDocumentId())
                .orderBy("tempo")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        listaItemsX.clear();
                        listaItemsY.clear();

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            SessaoItem sessaoItem = documentSnapshot.toObject(SessaoItem.class);
                            if(sessaoItem == null) {
                                return;
                            }
                            ExibirNoGrafico(sessaoItem);
                        }
                    }
                });
    }

    private void IniciarViews() {
        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);

        textViewPaciente = findViewById(R.id.text_view_pacienteDetails);
        textViewStatus = findViewById(R.id.text_view_statusDetails);
        textViewFisioterapeuta = findViewById(R.id.text_view_fisioterapeutaDetails);
        textViewData = findViewById(R.id.text_view_dataDetails);
        cronometroDetails = findViewById(R.id.cronometroDetails);
        btFinalizar = findViewById(R.id.btFinalizar);

        lineChart = findViewById(R.id.lineChart);
    }

    private void CriarEventos() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mensagem = AlterarSessao();
                Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG).show();
            }
        });

        btFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(SessaoActivity.this)
                    .setTitle("Finalizar")
                    .setMessage("Finalizar sessão em \"" + cronometroDetails.getContentDescription() + "\"?")
                    .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            FinalizarSessao();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null).show();
            }
        });
    }

    private void atribuirValores(Sessao sessao) {
        textViewPaciente.setText(getPacienteById(sessao.getPacienteId()).getNome());
        textViewStatus.setText(StatusSessao.values()[sessao.getStatus()].toString());
        textViewFisioterapeuta.setText(sessao.getFisioterapeuta());
        textViewData.setText(sessao.getDataHora());

        long currentTime = (SystemClock.elapsedRealtime() - sessao.getDuracao());
        cronometroDetails.setBase(currentTime);

        int colorId = SessaoNegocios.returnColorIdByStatus(sessao.getStatus());
        textViewStatus.setTextColor(getResources().getColor(colorId));

        if (sessao.getStatus() == StatusSessao.EM_ANDAMENTO.toInt()) {
            AtribuirIconeBotao(R.drawable.ic_pause);
        } else if(sessao.getStatus() == StatusSessao.PAUSADA.toInt()){
            AtribuirIconeBotao(R.drawable.ic_play);
        } else if(sessao.getStatus() == StatusSessao.FINALIZADA.toInt()) {
            AtribuirIconeBotao(R.drawable.ic_play);
            btFinalizar.setVisibility(View.GONE);
        }
    }

    private Paciente getPacienteById(String pacienteId) {
        PacienteNegocios pacienteNegocios = new PacienteNegocios();
        return pacienteNegocios.getPacienteById(pacienteId);
    }

    private String AlterarSessao() {
        boolean sessaoNaoIniciada = sessao.getStatus() == StatusSessao.NAO_INICIADA.toInt();
        boolean sessaoEmAndamento = sessao.getStatus() == StatusSessao.EM_ANDAMENTO.toInt();
        boolean sessaoPausada = sessao.getStatus() == StatusSessao.PAUSADA.toInt();

        if (sessaoNaoIniciada || sessaoPausada) {
            return RetomarSessao();
        } else if (sessaoEmAndamento) {
            return PausarSessao();
        } else {
            return "Sessão já foi finalizada";
        }
    }

    private String RetomarSessao() {
        sessao.setStatus(StatusSessao.EM_ANDAMENTO.toInt());
        AtribuirIconeBotao(R.drawable.ic_pause);

        sessaoRef.document(sessao.getDocumentId()).update("status", sessao.getStatus());

        return "Sessão em andamento";
    }

    private String PausarSessao() {
        sessao.setStatus(StatusSessao.PAUSADA.toInt());
        AtribuirIconeBotao(R.drawable.ic_play);

        sessaoRef.document(sessao.getDocumentId()).update("status", sessao.getStatus());

        return "Sessão pausada";
    }

    private void FinalizarSessao() {
        if(sessao.getStatus() == StatusSessao.NAO_INICIADA.toInt()) {
            Snackbar.make(getWindow().getDecorView().getRootView(), "Sessão não iniciada", Snackbar.LENGTH_SHORT).show();
            return;
        }

        sessao.setStatus(StatusSessao.FINALIZADA.toInt());
        AtribuirIconeBotao(R.drawable.ic_play);

        sessaoRef.document(sessao.getDocumentId()).update("status", sessao.getStatus());

        btFinalizar.setVisibility(View.GONE);
        Snackbar.make(getWindow().getDecorView().getRootView(), "Sessão finalizada", Snackbar.LENGTH_SHORT).show();
    }

    private void AtribuirIconeBotao(@DrawableRes int resId) {
        fab.hide();
        fab.setImageResource(resId);
        fab.show();
    }

    private void ExibirNoGrafico(SessaoItem sessaoItem) {
        Entry entryX = CriarEntryX(sessaoItem);
        Entry entryY = CriarEntryY(sessaoItem);
        listaItemsX.add(entryX);
        listaItemsY.add(entryY);

        LineDataSet lineDataSetX = new LineDataSet(listaItemsX, "Eixo X");
        lineDataSetX.setDrawCircles(false);
        lineDataSetX.setDrawValues(false);
        lineDataSetX.setColor(Color.BLUE);

        LineDataSet lineDataSetY = new LineDataSet(listaItemsY, "Eixo Y");
        lineDataSetY.setDrawCircles(false);
        lineDataSetY.setDrawValues(false);
        lineDataSetY.setColor(Color.RED);

        ArrayList<ILineDataSet> listaItemsXY = new ArrayList<>();
        listaItemsXY.add(lineDataSetX);
        listaItemsXY.add(lineDataSetY);

        lineChart.setData(new LineData(listaItemsXY));
        lineChart.invalidate();
    }

    private Entry CriarEntryX(SessaoItem sessaoItem) {
        return new Entry(sessaoItem.getTempo() / 1000, sessaoItem.getEixoX());
    }

    private Entry CriarEntryY(SessaoItem sessaoItem) {
        return new Entry(sessaoItem.getTempo() / 1000, sessaoItem.getEixoY());
    }
}
