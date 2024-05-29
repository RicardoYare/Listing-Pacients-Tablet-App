package com.example.mamaapp;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.OnBackPressedCallback;


public class MainActivity3 extends AppCompatActivity {

    String rowId;
    String fecha;
    EditText etObs;
    CheckBox checkPago;
    TextView butGuardarCita, butEliminarCita, butAtras2, butFecha;

    SoundPool sp;
    int sonidoBoton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sonidoBoton = sp.load(this, R.raw.btsound,1);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initElements();
        cargarDatos();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //No hagas nada cuando la gente le de pa atras
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);


    }

    public void cargarDatos() {


        rowId = getIntent().getStringExtra("rowId");
        fecha = getIntent().getStringExtra("fecha");

        if(fecha.equalsIgnoreCase("Poner Fecha")){

            butEliminarCita.setEnabled(false);
            butEliminarCita.setBackgroundResource(R.drawable.border_disable);
            butFecha.setText(fecha);

        }else{

            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "datos", null, 1);
            SQLiteDatabase bd = admin.getWritableDatabase();
            Cursor fechaQuery = bd.rawQuery("SELECT fecha, pago, observaciones FROM consultas WHERE id_paciente =" +
                    rowId + " AND fecha ='" + fecha+"'", null);
            
            if(fechaQuery.moveToFirst()){
                butFecha.setText(fechaQuery.getString(0));
                if(fechaQuery.getInt(1) == 1){checkPago.setChecked(true);}else{checkPago.setChecked(false);}
                etObs.setText(fechaQuery.getString(2));

                fechaQuery.close();
                bd.close();
                
            }else {

                Toast.makeText(this, "No he encontrado nada", Toast.LENGTH_SHORT).show();
                
            }            

        }

    }

    public void guardarCita(View v){

        String fechaCita = butFecha.getText().toString();
        int pago;
        if(checkPago.isChecked()){pago = 1;}else{pago = 0;}
        String observaciones = etObs.getText().toString();

        if(fechaCita.equalsIgnoreCase("Poner Fecha")) {
            Toast.makeText(this, "Tienes ingresar una fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "datos", null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();

        if(fecha.equalsIgnoreCase("Poner Fecha")){//Si se viene desde agregar cita

            Cursor compruebaPaciente = db.rawQuery("SELECT * FROM consultas WHERE id_paciente = " + rowId +
                    " AND fecha = '" + fechaCita+"'", null);
            if(compruebaPaciente.getCount() != 0){
                Toast.makeText(this, "Ya existe una cita de este paciente para esa fecha", Toast.LENGTH_LONG).show();
                return;
            }
            compruebaPaciente.close();

            ContentValues registro = new ContentValues();

            registro.put("id_paciente", rowId);
            registro.put("fecha", fechaCita);
            registro.put("pago", pago);
            registro.put("observaciones", observaciones);

            db.insert("consultas", null, registro);
            Toast.makeText(this, "Cita guardada", Toast.LENGTH_SHORT).show();

            Cursor idPaciente = db.rawQuery("SELECT fecha FROM consultas ORDER BY ROWID DESC LIMIT 1;", null);
            idPaciente.moveToFirst();
            fecha = idPaciente.getString(0);
            idPaciente.close();

            butEliminarCita.setEnabled(true);
            butEliminarCita.setBackgroundResource(R.drawable.border);

        }else{
            if(fechaCita.equals(fecha)){//si se quiere modificar cita

                db.execSQL("UPDATE consultas SET fecha = '" + fechaCita + "', pago = '" + pago + "', observaciones = '" + observaciones +
                        "' WHERE id_paciente = " + rowId+ " AND fecha ='" + fecha+"'");
                Toast.makeText(this, "Cita actualizada", Toast.LENGTH_SHORT).show();
                fecha = fechaCita;

            }else{

                Cursor compruebaPaciente = db.rawQuery("SELECT * FROM consultas WHERE id_paciente = " + rowId +
                        " AND fecha = '" + fechaCita+"'", null);
                if(compruebaPaciente.getCount() != 0){
                    Toast.makeText(this, "Ya existe una cita de este paciente para esa fecha 2", Toast.LENGTH_LONG).show();
                    return;
                }
                db.execSQL("UPDATE consultas SET fecha = '" + fechaCita + "', pago = '" + pago + "', observaciones = '" + observaciones +
                        "' WHERE id_paciente = " + rowId+ " AND fecha ='" + fecha+"'");
                Toast.makeText(this, "Cita actualizada", Toast.LENGTH_SHORT).show();
                fecha = fechaCita;
            }
        }

        db.close();

    }

    public void toPaciente(View view){

        Intent intent = new Intent(this, MainActivity2.class);
        intent.putExtra("rowId", rowId);

        startActivity(intent);
        finish();
    }

    public void borrarCita(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Borrar Cita");
        builder.setMessage("¿Estás segura Madre?");
        builder.setNegativeButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(MainActivity3.this, "datos", null, 1);
                SQLiteDatabase db = admin.getWritableDatabase();

                db.execSQL("DELETE FROM consultas WHERE id_paciente = " + rowId + " AND fecha ='" + fecha+"'");
                Toast.makeText(MainActivity3.this, "Cita eliminada", Toast.LENGTH_SHORT).show();

                db.close();

                Intent intent = new Intent(MainActivity3.this, MainActivity2.class);
                intent.putExtra("rowId", rowId);
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

    public void fecha(View v){

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {

                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        butFecha.setText(selectedDate);
                    }
                },
                2024, 12, 1);
        datePickerDialog.show();
    }


    public void initElements(){

        butGuardarCita = findViewById(R.id.butGuardarCita);
        butEliminarCita = findViewById(R.id.butEliminarCita);
        butAtras2 = findViewById(R.id.butAtras2);
        butFecha = findViewById(R.id.butFecha);
        etObs = findViewById(R.id.etObs);
        checkPago = findViewById(R.id.checkBoxPago);

        butGuardarCita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                butGuardarCita.setBackgroundResource(R.drawable.no_border);
                butGuardarCita.setTranslationX(5);
                butGuardarCita.setTranslationY(3);

                sp.play(sonidoBoton, 0.5f, 0.5f, 0, 0, 1);

                butGuardarCita.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        butGuardarCita.setBackgroundResource(R.drawable.border);
                        butGuardarCita.setTranslationX(-5);
                        butGuardarCita.setTranslationY(-3);
                        guardarCita(v);
                      ///  butGuardarCita.setBackgroundResource(R.drawable.border);
                       /// butAtras2.setEnabled(true);
                       /// butAtras2.setBackgroundResource(R.drawable.border);
                    }
                }, 200);

            }
        });

        butEliminarCita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                butEliminarCita.setBackgroundResource(R.drawable.no_border);
                butEliminarCita.setTranslationX(5);
                butEliminarCita.setTranslationY(3);

                sp.play(sonidoBoton, 0.5f, 0.5f, 0, 0, 1);

                butEliminarCita.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        butEliminarCita.setBackgroundResource(R.drawable.border);
                        butEliminarCita.setTranslationX(-5);
                        butEliminarCita.setTranslationY(-3);
                        borrarCita(v);
                    }
                }, 200);

            }
        });

        butAtras2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                butAtras2.setBackgroundResource(R.drawable.no_border);
                butAtras2.setTranslationX(5);
                butAtras2.setTranslationY(3);

                sp.play(sonidoBoton, 0.5f, 0.5f, 0, 0, 1);

                butAtras2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        butAtras2.setBackgroundResource(R.drawable.border);
                        butAtras2.setTranslationX(-5);
                        butAtras2.setTranslationY(-3);
                        toPaciente(v);
                    }
                }, 200);

            }
        });

        butFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                butFecha.setBackgroundResource(R.drawable.no_border_element_cita);
                butFecha.setTranslationX(5);
                butFecha.setTranslationY(3);

                sp.play(sonidoBoton, 0.5f, 0.5f, 0, 0, 1);

                butFecha.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        butFecha.setBackgroundResource(R.drawable.border_element_cita);
                        butFecha.setTranslationX(-5);
                        butAtras2.setTranslationY(-3);

                        fecha(v);
                    }
                }, 200);

            }
        });

        checkPago.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkPago.setBackgroundResource(R.drawable.no_border);
                checkPago.setTranslationX(5);
                checkPago.setTranslationY(3);

                sp.play(sonidoBoton, 0.5f, 0.5f, 0, 0, 1);

                checkPago.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkPago.setBackgroundResource(R.drawable.border);
                        checkPago.setTranslationX(-5);
                        checkPago.setTranslationY(-3);
                    }
                }, 200);

            }
        });

        ////Por si quieres que al apretar tecla en el editText se fuerce obligatoriamente
       /// etObs.setOnKeyListener(new View.OnKeyListener() {
          ///  @Override
            ///public boolean onKey(View v, int keyCode, KeyEvent event) {
              ///  if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    /// butGuardarCita.setBackgroundResource(R.drawable.border_element);
                    ////butAtras2.setEnabled(false);
                   /// butAtras2.setBackgroundResource(R.drawable.border_disable);

       ///         }
          ///      return false;
           /// }
        ///});

    }

}