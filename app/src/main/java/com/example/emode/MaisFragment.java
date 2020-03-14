package com.example.emode;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.emode.util.Preferences;
import com.example.emode.util.Valores;

public class MaisFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference preferenceEmail = findPreference(getString(R.string.emailKey));
        preferenceEmail.setSummary(Valores.getUsuario().getEmail());

        Preference preferenceNome = findPreference(getString(R.string.nomeFisioKey));
        preferenceNome.setSummary(Valores.getUsuario().getDisplayName());

        Preference preferenceSair = findPreference(getString(R.string.exitKey));
        preferenceSair.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Sair();

                return false;
            }
        });
    }

    private void Sair() {
        new AlertDialog.Builder(getContext())
                .setTitle("Sair")
                .setMessage("Sair da sua conta?")
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Preferences.clearLogin(getContext());

                        startActivity(new Intent(getContext(), SignIn.class));
                        getActivity().finish();
                    }})
                .setNegativeButton(android.R.string.cancel, null).show();
    }
}
