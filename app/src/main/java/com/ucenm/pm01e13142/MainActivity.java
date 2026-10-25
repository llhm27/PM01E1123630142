package com.ucenm.pm01e13142;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ucenm.pm01e13142.config.SQLiteConnection;
import com.ucenm.pm01e13142.modelo.Contacto;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_IMAGE = 10;
    private static final int REQUEST_CAMERA_IMAGE = 11;

    private final String[] listaPaises = {
            "Honduras (504)",
            "Costa Rica (506)",
            "Guatemala (502)",
            "El Salvador (503)",
            "Nicaragua (505)",
            "Panamá (507)"
    };

    private ImageView imagenContacto;
    private EditText etNombre, etTelefono, etNota;
    private Spinner spPais;
    private Button btnSalvar, btnImagen, btnLista, btnAtras;

    private byte[] bytesImagen = null;
    private int editId = -1;
    private SQLiteConnection db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar base de datos y componentes UI
        db = new SQLiteConnection(this);

        imagenContacto = findViewById(R.id.imagenContacto);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etNota = findViewById(R.id.etNota);
        spPais = findViewById(R.id.spPais);
        btnImagen = findViewById(R.id.btnImagen);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnLista = findViewById(R.id.btnLista);
        btnAtras = findViewById(R.id.btnAtras);

        // Configurar Spinner de países
        ArrayAdapter<String> adapterPaises = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaPaises);
        spPais.setAdapter(adapterPaises);

        // Configurar escuchadores de botones
        btnImagen.setOnClickListener(v -> opcionesImagen());
        btnSalvar.setOnClickListener(v -> salvarContacto());
        btnLista.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ActivityLista.class);
            startActivity(intent);
        });

        if (btnAtras != null) {
            btnAtras.setOnClickListener(v -> finish());
        }

        // Verificar si viene de una edición
        cargarDatosEdicion(getIntent());
    }

    // Diálogo para elegir galería o cámara
    private void opcionesImagen() {
        String[] opciones = {"Elegir de galería", "Tomar foto"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Foto del contacto");
        builder.setItems(opciones, (dialog, which) -> {
            if (which == 0) {
                Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, REQUEST_PICK_IMAGE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, REQUEST_CAMERA_IMAGE);
                } else {
                    mostrarMensaje("No hay cámara disponible");
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        try {
            Bitmap bitmap = null;
            if (requestCode == REQUEST_PICK_IMAGE) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } else if (requestCode == REQUEST_CAMERA_IMAGE) {
                if (data.getExtras() != null) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                }
            }

            if (bitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
                bytesImagen = stream.toByteArray();
                imagenContacto.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            mostrarMensaje("No se pudo cargar la imagen");
        }
    }

    // Cargar datos si se seleccionó un contacto para actualizar
    private void cargarDatosEdicion(Intent intent) {
        if (!intent.hasExtra("id")) {
            return;
        }

        editId = intent.getIntExtra("id", -1);
        etNombre.setText(intent.getStringExtra("nombre"));
        etTelefono.setText(intent.getStringExtra("telefono"));
        etNota.setText(intent.getStringExtra("nota"));

        String paisSeleccionado = intent.getStringExtra("pais");
        for (int i = 0; i < listaPaises.length; i++) {
            if (listaPaises[i].equals(paisSeleccionado)) {
                spPais.setSelection(i);
                break;
            }
        }

        String imagenBase64 = intent.getStringExtra("imagen");
        if (imagenBase64 != null) {
            bytesImagen = Base64.decode(imagenBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytesImagen, 0, bytesImagen.length);
            imagenContacto.setImageBitmap(bitmap);
        }

        btnSalvar.setText("Actualizar Contacto");
    }

    // Guardar o actualizar contacto
    private void salvarContacto() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String nota = etNota.getText().toString().trim();

        // Validaciones
        if (nombre.isEmpty()) {
            mostrarMensaje("Falta el campo Nombre");
            return;
        }
        if (telefono.isEmpty()) {
            mostrarMensaje("Falta el campo Teléfono");
            return;
        }
        if (nota.isEmpty()) {
            mostrarMensaje("Falta el campo Nota");
            return;
        }
        if (!nombre.matches("[\\p{L} ]+")) {
            mostrarMensaje("El nombre solo puede contener letras y espacios");
            return;
        }
        if (!telefono.matches("\\d+")) {
            mostrarMensaje("El teléfono solo puede contener dígitos");
            return;
        }

        String pais = spPais.getSelectedItem().toString();
        Contacto contacto = new Contacto(editId < 0 ? 0 : editId, pais, nombre, telefono, nota, bytesImagen);

        if (editId < 0) {
            db.insertar(contacto);
            Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();
        } else {
            db.actualizar(contacto);
            Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
        }

        editId = -1;
        limpiarCampos();
    }

    // Limpiar los campos del formulario
    private void limpiarCampos() {
        etNombre.setText("");
        etTelefono.setText("");
        etNota.setText("");
        spPais.setSelection(0);
        bytesImagen = null;
        imagenContacto.setImageResource(R.drawable.ic_profile_placeholder);
        btnSalvar.setText("Guardar Contacto");
    }

    private void mostrarMensaje(String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mensaje);
        builder.setPositiveButton("Aceptar", null);
        builder.show();
    }
}
