package com.example.emode;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.emode.model.Sessao;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements SessaoBottomOptions.BottomSheetListener {

    Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Sessões");

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        selectedFragment = new SessaoFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                selectedFragment).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.nav_sessoes:
                            selectedFragment = new SessaoFragment();
                            getSupportActionBar().setTitle("Sessões");
                            break;
                        case R.id.nav_pacientes:
                            selectedFragment = new PacienteFragment();
                            getSupportActionBar().setTitle("Pacientes");
                            break;
                        case R.id.nav_mais:
                            selectedFragment = new MaisFragment();
                            getSupportActionBar().setTitle("Mais");
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

    @Override
    public void onButtonClicked(int position) {
        SessaoFragment sessaoFragment = (SessaoFragment) selectedFragment;
        switch (position) {
            case 0:
                AbrirDetalhesDaSessao(sessaoFragment.sessaoSelecionada);
                break;
            case 1:
                AbrirAdicionarSessao(sessaoFragment.sessaoSelecionada);
                break;
        }
    }

    private void AbrirDetalhesDaSessao(Sessao sessaoSelecionada) {
        Intent intentDetails = new Intent(this, SessaoActivity.class);
        intentDetails.putExtra("sessaoObj", sessaoSelecionada);
        startActivity(intentDetails);
    }

    private void AbrirAdicionarSessao(Sessao sessaoSelecionada) {
        Intent intentDetails = new Intent(this, SessaoAdd.class);
        intentDetails.putExtra("sessaoObj", sessaoSelecionada);
        startActivity(intentDetails);
    }
}
