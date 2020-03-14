package com.example.emode.negocios;

import com.example.emode.model.Paciente;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PacienteNegocios {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference pacienteRef = db.collection("Paciente");

    public Paciente getPacienteById(final String documentId) {
        Task<DocumentSnapshot> task = pacienteRef.document(documentId).get();
        waitTaskCompleted(task);

        if(task.isSuccessful()) {
            return task.getResult().toObject(Paciente.class);
        }
        else {
            return new Paciente();
        }
    }

    private void waitTaskCompleted(Task task) {
        while (true) {
            if(task.isComplete()) {
                break;
            }
        }
    }
}
