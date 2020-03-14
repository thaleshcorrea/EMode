package com.example.emode.util;

import com.google.firebase.auth.FirebaseUser;

public class Valores {
    private static FirebaseUser usuario;

    public static FirebaseUser getUsuario() {
        return usuario;
    }

    public static void setUsuario(FirebaseUser usuario) {
        Valores.usuario = usuario;
    }
}
