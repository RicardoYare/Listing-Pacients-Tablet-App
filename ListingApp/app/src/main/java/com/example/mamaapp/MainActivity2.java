package com.example.mamaapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TabStopSpan;
import android.view.View;

import android.database.sqlite.SQLiteDatabase;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.content.ContentValues;
import androidx.activity.OnBackPressedCallback;

public class MainActivity2 extends AppCompatActivity {

    TextView tvID;
    EditText etNombre, etApellido, etAlias;
    TextView butGuardar, butAtras, butEliminarPac, butAgregarCit;
    LinearLayout ly2;
    String rowID;
    SoundPool sp;
    int sonidoBoton;
    int sonidoElemento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etAlias = findViewById(R.id.etAlias);
        tvID = findViewById(R.id.id_paciente_tv);

        ly2 = findViewById(R.id.ly2);

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sonidoBoton = sp.load(this, R.raw.btsound,1);
        sonidoElemento = sp.load(this, R.raw.clickelement,1);

        initBotones();
        cargarDatos();

        String idPaciente = "ID del Paciente: "+ rowID;
        tvID.setText(idPaciente);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //No hagas nada cuando la gente le de pa atras
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);
    }


    public void cargarDatos() {
            ly2.removeAllViews();
            rowID = getIntent().getStringExtra("rowId");

            if (rowID.isEmpty()) {

                butEliminarPac.setEnabled(false);
                butAgregarCit.setEnabled(false);
                butEliminarPac.setBackgroundResource(R.drawable.border_disable);
                butAgregarCit.setBackgroundResource(R.drawable.border_disable);

            } else {
                AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "datos", null, 1);
                SQLiteDatabase db = admin.getWritableDatabase();


                Cursor filaPaciente = db.rawQuery("SELECT nombre, apellidos, alias FROM pacientes WHERE id_paciente = " + rowID, null);
                filaPaciente.moveToFirst();

                if (filaPaciente.moveToFirst()) {

                    etNombre.setText(filaPaciente.getString(0));
                    etApellido.setText(filaPaciente.getString(1));
                    etAlias.setText(filaPaciente.getString(2));

                    cargarCitasViews();

                } else {
                    Toast.makeText(this, "No hay Datos del Paciente?", Toast.LENGTH_LONG).show();
                }
                filaPaciente.close();
                db.close();

            }
    }

    public void toAgregarCita(View view) {
        Intent intent = new Intent(this, MainActivity3.class);
        startActivity(intent);
        intent.putExtra("rowId", rowID);
        intent.putExtra("fecha", "Poner Fecha");
        startActivity(intent);
        finish();
    }

    public void agregarPaciente(View view) {

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "datos", null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();

        String nombre = etNombre.getText().toString();
        String apellidos = etApellido.getText().toString();
        String alias = etAlias.getText().toString();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Falta el nombre", Toast.LENGTH_SHORT).show();
            return;
        }

       if(rowID.isEmpty()){ //Si el paciente no existia

           ContentValues registro = new ContentValues();

           registro.put("nombre", nombre);
           registro.put("apellidos", apellidos);
           registro.put("alias", alias);

           db.insert("pacientes", null, registro);
           Toast.makeText(this, "Paciente agregado", Toast.LENGTH_SHORT).show();

           Cursor idPaciente = db.rawQuery("SELECT id_paciente FROM pacientes ORDER BY ROWID DESC LIMIT 1;", null);
           idPaciente.moveToFirst();
           rowID = idPaciente.getString(0);
           tvID.setText("ID del Paciente: " + rowID);
           idPaciente.close();

           butEliminarPac.setEnabled(true);
           butAgregarCit.setEnabled(true);

           butEliminarPac.setBackgroundResource(R.drawable.border);
           butAgregarCit.setBackgroundResource(R.drawable.border);

           cargarCitasViews();


       }else { //Si el paciente existe insert

           db.execSQL("UPDATE pacientes SET nombre = '" + nombre + "', apellidos = '" + apellidos + "', alias = '" + alias + "' WHERE id_paciente = " + rowID);
           Toast.makeText(this, "Paciente actualizado", Toast.LENGTH_SHORT).show();
       }

        db.close();

    }

    public void toMain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void cargarCitasViews(){

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "datos", null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor filaCita = db.rawQuery("SELECT * FROM consultas WHERE id_paciente = " + rowID+ " ORDER BY ROWID DESC", null);

        if(filaCita.moveToFirst()){
            do{
                String fecha = filaCita.getString(1);
                int pago = filaCita.getInt(2);
                String observaciones = filaCita.getString(3);

                TextView citaView = new TextView(this);
                citaView.setText(formatearMensaje(fecha, pago, observaciones));

                Typeface typeface = ResourcesCompat.getFont(this, R.font.brock_restar);;
                citaView.setTypeface(typeface);

                citaView.setBackgroundResource(R.drawable.border_element_cita);
                citaView.setTextSize(20);
                citaView.setPadding(30,30,30,30);

                citaView.setOnClickListener(new View.OnClickListener() { ///Envio al Activity3
                    @Override
                    public void onClick(View v) {

                        citaView.setBackgroundResource(R.drawable.no_border_element_cita);
                        citaView.setTranslationX(5);
                        citaView.setTranslationY(3);

                        sp.play(sonidoElemento, 0.5f, 0.5f, 0, 0, 1);

                        citaView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                citaView.setBackgroundResource(R.drawable.border_element_cita);
                                citaView.setTranslationX(-5);
                                citaView.setTranslationY(-3);

                                Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
                                intent.putExtra("rowId",rowID);
                                intent.putExtra("fecha",fecha);
                                startActivity(intent);
                                finish();
                            }
                        }, 200);
                    }
                });

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

                lp.setMargins(10,10,10,10);
                citaView.setLayoutParams(lp);
                ly2.addView(citaView);

            }while(filaCita.moveToNext());

        }else{

            TextView sinDatos = new TextView(this);
            sinDatos.setText("No hay fechas registradas");

            Typeface typeface = ResourcesCompat.getFont(this, R.font.brock_restar);;
            sinDatos.setTypeface(typeface);
            sinDatos.setTextColor(Color.parseColor("#A9A8AA"));

            sinDatos.setTextSize(14);
            sinDatos.setGravity(View.TEXT_ALIGNMENT_CENTER);
            sinDatos.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            sinDatos.setPadding(20,20,20,20);

            ly2.addView(sinDatos);
        }


    }

    public SpannableString formatearMensaje(String fecha, int pago, String observaciones){

        String textoIzquierda = fecha;
        String textoDerecha;

        if(pago != 1){

            textoDerecha = "No pagada  ";

        }else{ textoDerecha = "pagada  "; }

        if(observaciones.isEmpty()){

            textoDerecha = textoDerecha + "Sin observaciones";

        }else{ textoDerecha = textoDerecha + "Con observaciones";}


        int tabPosition = 850;

        SpannableString spannableString = new SpannableString(textoIzquierda + "\t" + textoDerecha);
        spannableString.setSpan(new TabStopSpan.Standard(tabPosition), textoIzquierda.length()
                + 1, textoIzquierda.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    public void eliminarPaciente(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Paciente");
        builder.setMessage("¿Estás segura Madre?");
        builder.setNegativeButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(MainActivity2.this, "datos", null, 1);
                SQLiteDatabase db = admin.getWritableDatabase();

                db.execSQL("DELETE FROM pacientes WHERE id_paciente = " + rowID);
                db.execSQL("DELETE FROM consultas WHERE id_paciente = " + rowID);
                Toast.makeText(MainActivity2.this, "Paciente Eliminado", Toast.LENGTH_SHORT).show();
                db.close();

                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

    public void initBotones(){

        butGuardar = findViewById(R.id.butGuardar);
        butAtras = findViewById(R.id.butAtras);
        butEliminarPac = findViewById(R.id.butEliminarPac);
        butAgregarCit = findViewById(R.id.butAgregarCit);

        butGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                butGuardar.setBackgroundResource(R.drawable.no_border);
                butGuardar.setTranslationX(5);
                butGuardar.setTranslationY(3);

                sp.play(sonidoElemento, 0.5f, 0.5f, 0, 0, 1);

                butGuardar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        butGuardar.setBackgroundResource(R.drawable.border);
                        butGuardar.setTranslationX(-5);
                        butGuardar.setTranslationY(-3);

                        agregarPaciente(v);
                    }
                }, 200);

            }
        });

        butAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                butAtras.setBackgroundResource(R.drawable.no_border);
                butAtras.setTranslationX(5);
                butAtras.setTranslationY(3);

                sp.play(sonidoBoton, 0.5f, 0.5f, 0, 0, 1);

                butAtras.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        butAtras.setBackgroundResource(R.drawable.border);
                        butAtras.setTranslationX(-5);
                        butAtras.setTranslationY(-3);

                        toMain(v);
                    }
                }, 200);

            }
        });

        butEliminarPac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                butEliminarPac.setBackgroundResource(R.drawable.no_border);
                butEliminarPac.setTranslationX(5);
                butEliminarPac.setTranslationY(3);

                sp.play(sonidoBoton, 0.5f, 0.5f, 0, 0, 1);

                butEliminarPac.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        butEliminarPac.setBackgroundResource(R.drawable.border);
                        butEliminarPac.setTranslationX(-5);
                        butEliminarPac.setTranslationY(-3);

                        eliminarPaciente(v);
                    }
                }, 200);

            }
        });

        butAgregarCit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                butAgregarCit.setBackgroundResource(R.drawable.no_border);
                butAgregarCit.setTranslationX(5);
                butAgregarCit.setTranslationY(3);

                sp.play(sonidoBoton, 0.5f, 0.5f, 0, 0, 1);

                butAgregarCit.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        butAgregarCit.setBackgroundResource(R.drawable.border);
                        butAgregarCit.setTranslationX(-5);
                        butAgregarCit.setTranslationY(-3);

                       toAgregarCita(v);
                    }
                }, 200);

            }
        });
    }


}