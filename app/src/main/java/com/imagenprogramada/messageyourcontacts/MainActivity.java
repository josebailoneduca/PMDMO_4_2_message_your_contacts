package com.imagenprogramada.messageyourcontacts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.imagenprogramada.messageyourcontacts.databinding.ActivityMainBinding;

import java.io.InputStream;
import java.util.ArrayList;


/**
 * Actividad principal muestra una caja de texto para buscar contactos. Al darle a buscar pide los permisos necesarios. Tras buscar muestra una lista
 * con los contactos que coincide con la busqueda. Al hacer una pulsacion larga en uno de ellos pregunta
 * si se desea enviar un sms. Si se confirma se envia un sms y se muestra la imagen del contacto.
 */
public class MainActivity extends AppCompatActivity {
    //binding de elementos de la vista
    private ActivityMainBinding binding;

    //lista de contactos usados en el adaptador
    private ArrayList<Contacto> contactos=new ArrayList<>();

    //adaptador de la recyclerview de lista de contactos
    private ContactosRecyclerViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         binding = ActivityMainBinding.inflate(getLayoutInflater());
         setContentView(binding.getRoot());


        //boton de busqueda
        binding.btnBuscar.setOnClickListener(view -> {
            String nombre = binding.textoBusqueda.getText().toString();
            if (nombre!=null&&nombre!="")
                obtenerContactos();}
        );

        //contactos.add(new Contacto(1,"Jose","666666666",8));
        //configurar lista
        configurarLista();
    }

    /**
     * Configura el recyclerview de la lista de contactos
     */
    private void configurarLista() {
        RecyclerView recyclerView = (RecyclerView) binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new ContactosRecyclerViewAdapter(contactos,this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Confirma permisos y si se tiene se recogen los contactos y se pasan al adaptador. Si no se
     * tienen los permisos se piden
     */
    private void obtenerContactos() {

        //comprobar permiso

        if (
                ContextCompat.checkSelfPermission(this,"android.permission.READ_CONTACTS") != PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(this,"android.permission.SEND_SMS") != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_CONTACTS","android.permission.SEND_SMS"}, 1);
        } else {
            ArrayList<Contacto> contactosRecogidos = buscar(binding.textoBusqueda.getText().toString());
            contactos.clear();
            contactos.addAll(contactosRecogidos);
            adapter.notifyDataSetChanged();

        }
    }



    /**
     * Maneja la respuesta de haber pedido permisos de acceso a los contactos y envio de SMS:
     * -Si se han garantizado se carga un contacto.
     * -Si no se ha garantizado pero no se ha marcado la casilla de "no preguntar otra vez" se le explica que es necesario
     * -Si ha marcado la casilla de "no preguntar otra vez" se le explica que el permiso es necesario y que puede
     *  activarlo manualmente en la configuración del teléfono
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 1){
            //si el permiso ha sido concedido
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {
                obtenerContactos();
            }else if (grantResults[0] == PackageManager.PERMISSION_DENIED||grantResults[1] == PackageManager.PERMISSION_DENIED){
                //si no ha sido concedido entonces:
                // Si aún no ha marcado el no ser preguntado más se le da al usuario
                // una explicacion de la necesidad de dar el permiso
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.READ_CONTACTS") ||ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.SEND_SMS") ) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.msg_explicacion).setTitle(R.string.aviso);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else{
                    //Si ha marcado que no quiere volver a ser preguntado se le explica que es imprescindible
                    //y que si quiere usar la aplicacion debe aceptarlo manualmente en la configuracion del telefono
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.msg_explicacion_manual).setTitle(R.string.aviso);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    }








    @SuppressLint("Range")
    private ArrayList<Contacto> buscar(String nombre) {

        //SELECT
        String proyeccion[]={ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.PHOTO_ID,
        };

        //WHERE
        String filtro=ContactsContract.Contacts.DISPLAY_NAME + " like ?";
        String args_filtro[]={"%"+nombre+"%"};

        //Crear lista con el resultado
        ArrayList<Contacto> lista_contactos=new ArrayList<Contacto>();
        ContentResolver cr = getContentResolver();
        Cursor cursorContactos = cr.query(ContactsContract.Contacts.CONTENT_URI,
                proyeccion, filtro, args_filtro, null);
        //Iterar el cursor
        if (cursorContactos.getCount() > 0) {
            while (cursorContactos.moveToNext()) {
                //Id del contacto
                int idContacto = cursorContactos.getInt(
                        cursorContactos.getColumnIndex(ContactsContract.Contacts._ID));
                //Nombre del contacto
                String nombreContacto = cursorContactos.getString(
                        cursorContactos.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                //Ver si tiene telefono para quedarnos solo con los que tengan telefono
                boolean tieneTelefono=Integer.parseInt(cursorContactos.getString(cursorContactos.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0;
                if (tieneTelefono) {
                    String telefono="";
                    //Obtener telefono haciendo query
                    Cursor cursorTelefono = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{String.valueOf(idContacto)}, null);
                    //recoger ultimo telefono
                    while (cursorTelefono.moveToNext()) {
                        telefono = cursorTelefono.getString(cursorTelefono.getColumnIndex
                                (ContactsContract.CommonDataKinds.Phone.DATA));
                    }
                    //coger id de la foto
                    int foto =  cursorContactos.getInt(cursorContactos.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));

                    //agregar a la lista
                    lista_contactos.add(new Contacto(idContacto,nombreContacto,telefono,foto));
                }
            }
        }
        cursorContactos.close(); //close the cursor
        return lista_contactos;
    }

    /**
     * Envia un SMS si el usuario confirma
     * @param contacto Contacto completo
     */
    public void mandarSms(Contacto contacto){

        String texto= "SMS para "+contacto.getNombre()+" al "+contacto.getTelefono();
        String telefono=contacto.getTelefono();
        //pedir confirmacion
        new AlertDialog.Builder(this)
                .setTitle("Se va a enviar un SMS")
                .setMessage("Realmente quiere enviar un SMS a "+telefono+" con el texto:'"+texto+"'?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes,(dialog, which) -> {
                    limpiarResultado();
                    try {
                        //si ha respondido que si se envia
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(telefono, null, texto, null, null);
                        establecerResultado(texto,contacto);
                    } catch (Exception e) {

                        Toast.makeText (this,"No se pudo enviar el SMS",Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }).setNegativeButton(android.R.string.no, null).show();
    }

    /**
     * Establece el resultado de haber enviado un sms
     * @param texto Texto del mensaje
     * @param contacto Contacto
     */
    private void establecerResultado(String texto, Contacto contacto) {
        binding.tvResultado.setText(texto);
        //si el contacto tiene imagen la recogemos
        if (contacto.imagen!=0){
            Uri contactUri= ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contacto.getId());

            InputStream inputStream=ContactsContract.Contacts.
                    openContactPhotoInputStream(getContentResolver(),
                            contactUri,true);
            binding.imgResultado.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        }
        else
            //si no tiene imagen ponemos avatar por defecto
        binding.imgResultado.setImageResource(R.drawable.baseline_person_24);
    }

    /**
     * Limia la imageview y viewtext de resultado
     */
    private void limpiarResultado() {
        binding.tvResultado.setText("");
        binding.imgResultado.setImageResource(R.drawable.baseline_person_24);
    }

}