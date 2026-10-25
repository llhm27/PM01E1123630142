package com.ucenm.pm01e13142.config;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ucenm.pm01e13142.modelo.Contacto;

import java.util.ArrayList;
import java.util.List;

// Clase de conexión a la base de datos SQLite
public class SQLiteConnection extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contactos.db";
    private static final int DATABASE_VERSION = 1;

    public SQLiteConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla de contactos
        String createTable = "CREATE TABLE contactos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pais TEXT, " +
                "nombre TEXT, " +
                "telefono TEXT, " +
                "nota TEXT, " +
                "imagen BLOB)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS contactos");
        onCreate(db);
    }

    private ContentValues cargarValores(Contacto contacto) {
        ContentValues values = new ContentValues();
        values.put("pais", contacto.getPais());
        values.put("nombre", contacto.getNombre());
        values.put("telefono", contacto.getTelefono());
        values.put("nota", contacto.getNota());
        if (contacto.getImagen() != null) {
            values.put("imagen", contacto.getImagen());
        } else {
            values.putNull("imagen");
        }
        return values;
    }

    // Método para insertar un nuevo contacto
    public long insertar(Contacto contacto) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert("contactos", null, cargarValores(contacto));
    }

    // Método para listar todos los contactos registrados
    public List<Contacto> listar() {
        List<Contacto> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, pais, nombre, telefono, nota, imagen FROM contactos ORDER BY nombre COLLATE NOCASE", null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String pais = cursor.getString(1);
                String nombre = cursor.getString(2);
                String telefono = cursor.getString(3);
                String nota = cursor.getString(4);
                byte[] imagen = cursor.isNull(5) ? null : cursor.getBlob(5);

                lista.add(new Contacto(id, pais, nombre, telefono, nota, imagen));
            }
            cursor.close();
        }
        return lista;
    }

    // Método para obtener un contacto por su ID
    public Contacto obtener(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, pais, nombre, telefono, nota, imagen FROM contactos WHERE id = ?", new String[]{String.valueOf(id)});

        if (cursor != null && cursor.moveToFirst()) {
            String pais = cursor.getString(1);
            String nombre = cursor.getString(2);
            String telefono = cursor.getString(3);
            String nota = cursor.getString(4);
            byte[] imagen = cursor.isNull(5) ? null : cursor.getBlob(5);
            cursor.close();

            return new Contacto(id, pais, nombre, telefono, nota, imagen);
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    // Método para actualizar la información de un contacto
    public int actualizar(Contacto contacto) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update("contactos", cargarValores(contacto), "id = ?", new String[]{String.valueOf(contacto.getId())});
    }

    // Método para eliminar un contacto por su ID
    public int eliminar(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("contactos", "id = ?", new String[]{String.valueOf(id)});
    }
}
