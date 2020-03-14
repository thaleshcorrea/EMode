package com.example.emode.negocios;

import com.example.emode.R;
import com.example.emode.enumeradores.StatusSessao;

public class SessaoNegocios {
    public static int returnColorIdByStatus(int status) {
        if (status == StatusSessao.NAO_INICIADA.toInt()) {
            return R.color.LighBlue;
        } else if (status == StatusSessao.EM_ANDAMENTO.toInt()) {
            return R.color.Yellow;
        } else if (status == StatusSessao.PAUSADA.toInt()) {
            return R.color.Red;
        } else
            return R.color.Green;
    }
}
