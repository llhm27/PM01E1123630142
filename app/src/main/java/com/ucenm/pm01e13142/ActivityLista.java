package com.ucenm.pm01e13142;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ucenm.pm01e13142.adapter.ContactoAdapter;
import com.ucenm.pm01e13142.config.SQLiteConnection;
import com.ucenm.pm01e13142.modelo.Contacto;

public class ActivityLista extends AppCompatActivity {

    private static final int REQUEST_CALL_PERM = 20;

    private SQLiteConnection db;
    private ContactoAdapter adapter;
    private Contacto contactoLlamadaPendiente;

    private ListView listContactos;
    private EditText etBuscar;
    private Button btnAtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        db = new SQLiteConnection(this);

        etBuscar = findViewById(R.id.etBuscar);
        btnAtras = findViewById(R.id.btnAtras);
        listContactos = findViewById(R.id.listContactos);

        adapter = new ContactoAdapter(this, db.listar());
        listContactos.setAdapter(adapter);

        if (btnAtras != null) {
            btnAtras.setOnClickListener(v -> finish());
        }

        // Filtro de búsqueda
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Clic simple: Llamar
        listContactos.setOnItemClickListener((parent, view, position, id) -> {
            Contacto contacto = adapter.getItem(position);
            confirmarLlamada(contacto);
        });

        // Clic largo: Opciones
        listContactos.setOnItemLongClickListener((parent, view, position, id) -> {
            mostrarOpciones(adapter.getItem(position));
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setContactos(db.listar());
        }
    }

    // Confirmación para realizar llamada
    private void confirmarLlamada(Contacto contacto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Desea llamar a " + contacto.getNombre() + "?");
        builder.setPositiveButton("Sí", (dialog, which) -> realizarLlamada(contacto));
        builder.setNegativeButton("No", null);
        builder.show();
    }

    // Menú de opciones del contacto
    private void mostrarOpciones(Contacto contacto) {
        String[] opciones = {"Compartir Contacto", "Ver Imagen", "Eliminar Contacto", "Actualizar Contacto"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(contacto.getNombre());
        builder.setItems(opciones, (dialog, which) -> {
            switch (which) {
                case 0:
                    // Compartir
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, contacto.getNombre() + " - +" + extraerCodigoPais(contacto.getPais()) + contacto.getTelefono());
                    startActivity(Intent.createChooser(shareIntent, "Compartir contacto"));
                    break;
                case 1:
                    // Ver imagen
                    verImagenContacto(contacto);
                    break;
                case 2:
                    // Eliminar
                    confirmarEliminacion(contacto);
                    break;
                case 3:
                    // Actualizar
                    Intent editIntent = new Intent(ActivityLista.this, MainActivity.class);
                    editIntent.putExtra("id", contacto.getId());
                    editIntent.putExtra("pais", contacto.getPais());
                    editIntent.putExtra("nombre", contacto.getNombre());
                    editIntent.putExtra("telefono", contacto.getTelefono());
                    editIntent.putExtra("nota", contacto.getNota());
                    if (contacto.getImagen() != null) {
                        editIntent.putExtra("imagen", Base64.encodeToString(contacto.getImagen(), Base64.DEFAULT));
                    }
                    startActivity(editIntent);
                    break;
            }
        });
        builder.show();
    }

    // Ver imagen completa en un diálogo
    private void verImagenContacto(Contacto contacto) {
        if (contacto.getImagen() == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Este contacto no tiene imagen");
            builder.setPositiveButton("Aceptar", null);
            builder.show();
            return;
        }

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(BitmapFactory.decodeByteArray(contacto.getImagen(), 0, contacto.getImagen().length));
        imageView.setAdjustViewBounds(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(contacto.getNombre());
        builder.setView(imageView);
        builder.setPositiveButton("Cerrar", null);
        builder.show();
    }

    // Confirmar eliminación
    private void confirmarEliminacion(Contacto contacto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Eliminar a " + contacto.getNombre() + "?");
        builder.setNegativeButton("No", null);
        builder.setPositiveButton("Sí", (dialog, which) -> {
            db.eliminar(contacto.getId());
            adapter.setContactos(db.listar());
            Toast.makeText(ActivityLista.this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    // Pedir permisos de llamada si es necesario
    private void realizarLlamada(Contacto contacto) {
        contactoLlamadaPendiente = contacto;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERM);
            return;
        }
        ejecutarLlamada(contacto);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERM && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (contactoLlamadaPendiente != null) {
                ejecutarLlamada(contactoLlamadaPendiente);
            }
        }
    }

    private String extraerCodigoPais(String pais) {
        if (pais == null) return "";
        int inicio = pais.lastIndexOf('(');
        int fin = pais.indexOf(')', inicio);
        if (inicio >= 0 && fin > inicio) {
            return pais.substring(inicio + 1, fin);
        }
        return pais;
    }

    private void ejecutarLlamada(Contacto contacto) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:+" + extraerCodigoPais(contacto.getPais()) + contacto.getTelefono()));
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_SHORT).show();
        }
    }
}
