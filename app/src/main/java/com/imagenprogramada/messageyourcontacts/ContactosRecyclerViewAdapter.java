package com.imagenprogramada.messageyourcontacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adaptador para la lista de contactos
 */
public class ContactosRecyclerViewAdapter extends RecyclerView.Adapter<ContactosRecyclerViewAdapter.ViewHolder> {

    /**
     * Lista de contactos a mostrar
     */
    private final List<Contacto> listaContactos;

    /**
     * Referencia a la actividad principal
     */
    private MainActivity actividad;

    /**
     * Constructor
     * @param items Lista de contactos
     * @param actividad Referencia a la actividad
     */
    public ContactosRecyclerViewAdapter(List<Contacto> items, MainActivity actividad) {
        listaContactos = items;
        this.actividad=actividad;
    }


    /**
     * Creacion de vistas
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_de_lista,parent,false);
        return new ViewHolder(vista);

    }

    /**
     * Rellenar una vista con los datos que tocan
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.contacto = listaContactos.get(position);
        holder.nombre.setText(holder.contacto.getNombre());
        Contacto c = listaContactos.get(position);
        //en una pulsacion larga se manda el sms
        holder.nombre.setOnLongClickListener(v -> {
            actividad.mandarSms(holder.contacto);
            return false;
        });
    }



    @Override
    public int getItemCount() {
        return listaContactos.size();
    }



    /**
     * Clase de vistas de items
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public Contacto contacto;
        public final TextView nombre;


        public ViewHolder(View vista) {
            super(vista);
            nombre = vista.findViewById(R.id.nombre);
        }
    }
}