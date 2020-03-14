package com.example.emode.enumeradores;

public enum StatusSessao {
    NAO_INICIADA("Não iniciada", 0),
    EM_ANDAMENTO("Em andamento", 1),
    PAUSADA("Pausada", 2),
    FINALIZADA("Finalizada", 3);

    private String stringValue;
    private int intValue;
    private StatusSessao(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public int toInt() { return intValue; }
}
