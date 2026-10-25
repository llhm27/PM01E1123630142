package com.ucenm.pm01e13142.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ucenm.pm01e13142.R;
import com.ucenm.pm01e13142.modelo.Contacto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Adaptador personalizado para la lista de contactos
public class ContactoAdapter extends BaseAdapter implements Filterable {

    private final Context context;
    private final List<Contacto> listaOriginal = new ArrayList<>();
    private List<Contacto> listaFiltrada = new ArrayList<>();

    public ContactoAdapter(Context context, List<Contacto> contactos) {
        this.context = context;
        setContactos(contactos);
    }

    public void setContactos(List<Contacto> contactos) {
        this.listaOriginal.clear();
        this.listaOriginal.addAll(contactos);
        this.listaFiltrada = new ArrayList<>(contactos);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return listaFiltrada.size();
    }

    @Override
    public Contacto getItem(int position) {
        return listaFiltrada.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false);
        }

        Contacto contacto = getItem(position);

        ImageView itemFoto = convertView.findViewById(R.id.itemFoto);
        TextView itemNombre = convertView.findViewById(R.id.itemNombre);
        TextView itemTelefono = convertView.findViewById(R.id.itemTelefono);

        // Mostrar imagen del contacto o el placeholder por defecto
        if (contacto.getImagen() != null && contacto.getImagen().length > 0) {
            itemFoto.setImageBitmap(BitmapFactory.decodeByteArray(contacto.getImagen(), 0, contacto.getImagen().length));
        } else {
            itemFoto.setImageResource(R.drawable.ic_profile_placeholder);
        }

        itemNombre.setText(contacto.getNombre());
        itemTelefono.setText("+" + obtenerCodigoPais(contacto.getPais()) + " " + contacto.getTelefono());

        return convertView;
    }

    // Método para extraer el código de área del país
    private String obtenerCodigoPais(String pais) {
        if (pais == null) return "";
        int inicio = pais.lastIndexOf('(');
        int fin = pais.indexOf(')', inicio);
        if (inicio >= 0 && fin > inicio) {
            return pais.substring(inicio + 1, fin);
        }
        return pais;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Contacto> filtrados = new ArrayList<>();
                String busqueda = (constraint == null) ? "" : constraint.toString().toLowerCase(Locale.ROOT).trim();

                for (Contacto c : listaOriginal) {
                    if (c.getNombre().toLowerCase(Locale.ROOT).contains(busqueda) ||
                            c.getTelefono().contains(busqueda)) {
                        filtrados.add(c);
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filtrados;
                results.count = filtrados.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null) {
                    listaFiltrada = (List<Contacto>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
    }
}
