package com.example.emode;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class SessaoBottomOptions extends BottomSheetDialogFragment {
    private BottomSheetListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sessao_options, container, false);

        Button btMonitorar = view.findViewById(R.id.btMonitor);
        Button btMedir = view.findViewById(R.id.btMedir);
        Button btCancelar = view.findViewById(R.id.btCancelar);

        btMonitorar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onButtonClicked(0);
                dismiss();
            }
        });
        btMedir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onButtonClicked(1);
                dismiss();
            }
        });
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onButtonClicked(2);
                dismiss();
            }
        });

        return view;
    }

    public interface BottomSheetListener {
        void onButtonClicked(int position);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: " + e.getMessage());
            throw new ClassCastException(context.toString());
        }
    }
}
