package com.example.emode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.service.autofill.Sanitizer;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.emode.enumeradores.StatusSessao;
import com.example.emode.model.Paciente;
import com.example.emode.model.Sessao;
import com.example.emode.model.SessaoItem;
import com.example.emode.negocios.SessaoNegocios;
import com.example.emode.util.Funcoes;
import com.example.emode.util.Preferences;
import com.example.emode.util.Valores;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class SessaoAdd extends AppCompatActivity implements SensorEventListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference pacienteRef = db.collection("Paciente");
    private CollectionReference sessaoRef = db.collection("Sessao");
    private CollectionReference sessaoItemRef = db.collection("SessaoItem");

    private Sessao _sessao;
    private Paciente _paciente;

    private volatile long currentTime = 0;
    private SessaoRunnable sessaoRunnable = null;

    private final int requestCodePacienteSelecionar = 1;

    //ACCELEROMETER
    private float currentX, currentY;

    private SensorManager sensorManager = null;
    private Sensor accelerometer = null;

    private float vibrateThreshold = 0;
    private Chronometer cronometro = null;

    private FloatingActionButton floatingButtonPlay;
    private FloatingActionButton floatingButtonStop;

    private TextView textViewPaciente, textViewStatus, textViewFisioterapeuta;
    private ProgressBar progressbar_xpositivo;
    private ProgressBar progressbar_xnegativo;
    private ProgressBar progressbar_ypositivo;
    private ProgressBar progressbar_ynegativo;

    public Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessao_add);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        _sessao = (Sessao) getIntent().getSerializableExtra("sessaoObj");

        IniciarViews();
        CriarEventos();
        CriarAcelerometro();

        if (_sessao.getDocumentId() == null) {
            SelecionarPaciente();
        } else {
            CarregarPaciente();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (requestCodePacienteSelecionar): {
                if (resultCode == Activity.RESULT_OK) {
                    _paciente = (Paciente) data.getSerializableExtra("objPaciente");
                    InserirSessao();
                    AtribuirValores();
                } else {
                    finish();
                }
                break;
            }
        }
    }

    private void IniciarViews() {
        cronometro = findViewById(R.id.cronometro);
        floatingButtonPlay = findViewById(R.id.floatingButtonPlay);
        floatingButtonStop = findViewById(R.id.floatingButtonStop);

        textViewPaciente = findViewById(R.id.text_view_pacienteAdd);
        textViewStatus = findViewById(R.id.text_view_statusAdd);
        textViewFisioterapeuta = findViewById(R.id.text_view_fisioterapeutaAdd);
        progressbar_xpositivo = findViewById(R.id.progressBar_XPositive);
        progressbar_xnegativo = findViewById(R.id.progressBar_XNegative);
        progressbar_ypositivo = findViewById(R.id.progressBar_YPositive);
        progressbar_ynegativo = findViewById(R.id.progressBar_YNegative);
    }

    private void CriarAcelerometro() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        }

        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void SelecionarPaciente() {
        Intent intent = new Intent(this, PacienteSelecionar.class);
        startActivityForResult(intent, requestCodePacienteSelecionar);
    }

    private void CarregarPaciente() {
        pacienteRef.document(_sessao.getPacienteId()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            _paciente = task.getResult().toObject(Paciente.class);
                            if(_sessao.getStatus() == StatusSessao.EM_ANDAMENTO.toInt()) {
                                AlterarSessaoStatus(StatusSessao.PAUSADA);
                            }
                            MonitorarStatusSessao();
                            currentTime = _sessao.getDuracao();
                            cronometro.setBase(SystemClock.elapsedRealtime() - currentTime);
                            AtribuirValores();
                        }
                    }
                });
    }

    private void AtribuirValores() {
        textViewPaciente.setText(_paciente.getNome());
        textViewStatus.setText(StatusSessao.values()[_sessao.getStatus()].toString());
        textViewFisioterapeuta.setText(_sessao.getFisioterapeuta());
        int colorId = SessaoNegocios.returnColorIdByStatus(_sessao.getStatus());
        textViewStatus.setTextColor(getResources().getColor(colorId));

        AtribuirIcone(_sessao.getStatus());
    }

    private void AtribuirIcone(int statusSessao) {
        if(statusSessao == StatusSessao.NAO_INICIADA.toInt()) {
            floatingButtonPlay.setImageResource(R.drawable.ic_play);
        } else if(statusSessao == StatusSessao.EM_ANDAMENTO.toInt()) {
            floatingButtonPlay.setImageResource(R.drawable.ic_pause);
        } else if(statusSessao == StatusSessao.PAUSADA.toInt()) {
            floatingButtonPlay.setImageResource(R.drawable.ic_play);
        } else {
            floatingButtonPlay.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (sessaoRunnable != null && _sessao.getStatus() != StatusSessao.FINALIZADA.toInt()) {
            PausarSessao();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onBackPressed() {
        if(sessaoRunnable != null) {
            sessaoRunnable.mPaused = false;
            sessaoRunnable.mFinished = true;
        }
        super.onBackPressed();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (_sessao.getStatus() == StatusSessao.EM_ANDAMENTO.toInt()) {
            LimparValores();
            ExibirValores();

            currentX = sensorEvent.values[0];
            currentY = sensorEvent.values[1];

            vibrate();
        }
    }

    private void LimparValores() {
        progressbar_xpositivo.setProgress(0);
        progressbar_xnegativo.setProgress(0);
        progressbar_ypositivo.setProgress(0);
        progressbar_ynegativo.setProgress(0);
    }

    private void ExibirValores() {
        if (currentX > 0) {
            progressbar_xpositivo.setProgress((int) currentX);
        } else {
            progressbar_xnegativo.setProgress((int) Math.abs(currentX));
        }

        if (currentY > 0) {
            progressbar_ypositivo.setProgress((int) currentY);
        } else {
            progressbar_ynegativo.setProgress((int) Math.abs(currentY));
        }
    }

    public void vibrate() {
        if ((currentX > vibrateThreshold) || (currentY > vibrateThreshold)) {
            v.vibrate(50);
        }
    }

    private void InserirSessao() {
        _sessao.setPacienteId(_paciente.getDocumentId());
        _sessao.setDataHora(Funcoes.getCurrentDateFormated());
        _sessao.setDuracao(currentTime);
        _sessao.setFisioterapeuta(Valores.getUsuario().getDisplayName());
        _sessao.setStatus(StatusSessao.NAO_INICIADA.toInt());

        sessaoRef.add(_sessao).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                _sessao.setDocumentId(documentReference.getId());
                Snackbar.make(getWindow().getDecorView().getRootView(), "Sessão criada", Snackbar.LENGTH_SHORT).show();
                MonitorarStatusSessao();
            }
        });
    }

    private void MonitorarStatusSessao() {
        sessaoRef.document(_sessao.getDocumentId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                Sessao sessao = documentSnapshot.toObject(Sessao.class);
                if (sessao == null) {
                    return;
                }

                if(sessao.getStatus() == StatusSessao.EM_ANDAMENTO.toInt() && _sessao.getStatus() != StatusSessao.EM_ANDAMENTO.toInt()) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Sessão iniciada", Snackbar.LENGTH_SHORT).show();
                    RetomarSessao();
                } else if(sessao.getStatus() == StatusSessao.PAUSADA.toInt()) {
                    if(sessaoRunnable != null) {
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Sessão pausada", Snackbar.LENGTH_SHORT).show();
                        PausarSessao();
                    }
                } else if(sessao.getStatus() == StatusSessao.FINALIZADA.toInt()) {
                    if (sessaoRunnable != null){
                        sessaoRunnable.mFinished = true;
                        cronometro.stop();
                    }

                    _sessao.setStatus(sessao.getStatus());
                    Snackbar.make(getWindow().getDecorView().getRootView(), "Sessão finalizada", Snackbar.LENGTH_SHORT).show();
                    AtribuirValores();
                }
                _sessao.setStatus(sessao.getStatus());
            }
        });
    }

    private void AlterarSessao() {
        sessaoRef.document(_sessao.getDocumentId()).update("duracao", currentTime);
    }

    private void AlterarSessaoStatus(StatusSessao statusSessao) {
        sessaoRef.document(_sessao.getDocumentId()).update("status", statusSessao.toInt());
        _sessao.setStatus(statusSessao.toInt());
        AtribuirValores();
    }

    private void CriarEventos() {
        floatingButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_sessao.getStatus() == StatusSessao.FINALIZADA.toInt()) {
                    Snackbar.make(view, "Sessão finalizada", Snackbar.LENGTH_SHORT).show();
                } else if (sessaoRunnable == null || (sessaoRunnable.mPaused && !sessaoRunnable.mFinished)) {
                    RetomarSessao();
                } else if (!sessaoRunnable.mFinished) {
                    PausarSessao();
                }
            }
        });

        floatingButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_sessao.getStatus() == StatusSessao.FINALIZADA.toInt()) {
                    Snackbar.make(view, "Sessão finalizada", Snackbar.LENGTH_SHORT).show();
                    return;
                } else if (_sessao.getStatus() == StatusSessao.NAO_INICIADA.toInt()) {
                    Snackbar.make(view, "Sessão ainda não foi iniciada", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(sessaoRunnable != null) {
                    PausarSessao();
                }

                new AlertDialog.Builder(SessaoAdd.this)
                        .setTitle("Finalizar")
                        .setMessage("Finalizar sessão em \"" + cronometro.getContentDescription() + "\"?")
                        .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (sessaoRunnable != null) {
                                    sessaoRunnable.mPaused = false;
                                    sessaoRunnable.mFinished = true;
                                }

                                AlterarSessaoStatus(StatusSessao.FINALIZADA);
                                AtribuirValores();
                                Snackbar.make(getWindow().getDecorView().getRootView(), "Sessão finalizada", Snackbar.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        });
    }

    private void InserirSessaoItem() {
        SessaoItem sessaoItem = new SessaoItem();
        sessaoItem.setSessaoId(_sessao.getDocumentId());
        sessaoItem.setTempo(currentTime);
        sessaoItem.setEixoX(currentX);
        sessaoItem.setEixoY(currentY);

        sessaoItemRef.add(sessaoItem);

        AlterarSessao();
    }

    private void RetomarSessao() {
        cronometro.setBase(SystemClock.elapsedRealtime() - currentTime);
        cronometro.start();

        AlterarSessaoStatus(StatusSessao.EM_ANDAMENTO);
        if (sessaoRunnable == null) {
            sessaoRunnable = new SessaoRunnable();
            new Thread(sessaoRunnable).start();
            return;
        }
        sessaoRunnable.onResume();
    }

    private void PausarSessao() {
        AlterarSessaoStatus(StatusSessao.PAUSADA);
        sessaoRunnable.onPause();
        cronometro.stop();
    }

    class SessaoRunnable implements Runnable {
        private Object mPauseLock;
        public boolean mPaused;
        public boolean mFinished;

        public SessaoRunnable() {
            mPauseLock = new Object();
            mPaused = false;
            mFinished = false;
        }

        public void run() {
            while (!mFinished) {
                InserirSessaoItem();

                currentTime += (1000);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }

        public void onPause() {
            synchronized (mPauseLock) {
                mPaused = true;
            }
        }

        public void onResume() {
            synchronized (mPauseLock) {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }
    }
}
