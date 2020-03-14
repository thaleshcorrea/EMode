package com.example.emode.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Sessao implements Serializable {
    private String documentId;
    private String pacienteId;
    private String dataHora;
    private long duracao;
    private String fisioterapeuta;
    private int status;

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(String pacienteId) {
        this.pacienteId = pacienteId;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public long getDuracao() {
        return duracao;
    }

    public void setDuracao(long duracao) {
        this.duracao = duracao;
    }

    public String getFisioterapeuta() {
        return fisioterapeuta;
    }

    public void setFisioterapeuta(String fisioterapeuta) {
        this.fisioterapeuta = fisioterapeuta;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
