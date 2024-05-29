package com.example.mamaapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.TabStopSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    LinearLayout ly;
    EditText buscador;
    TextView botonAgregarPaciente, titulo;
    SoundPool sp;
    int sonidoBoton;
    int sonidoElemento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        botonAgregarPaciente = findViewById(R.id.botonAgregarPaciente);
        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sonidoBoton = sp.load(this, R.raw.btsound,1);
        sonidoElemento = sp.load(this, R.raw.clickelement,1);

        titulo = findViewById(R.id.titulo);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); ///Desactivar abrir teclado al cargar activity

        ly = findViewById(R.id.ly);
        ly.removeAllViews();
        cargarDatos("SELECT id_paciente, nombre, apellidos, alias FROM pacientes");
        cargarBuscador();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) { ///Desactivar botón atrás
            @Override
            public void handleOnBackPressed() {
                //No hagas nada cuando la gente le de pa atras
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);

        botonAgregarPaciente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                botonAgregarPaciente.setBackgroundResource(R.drawable.no_border);
                botonAgregarPaciente.setTranslationX(5);
                botonAgregarPaciente.setTranslationY(3);
                sp.play(sonidoBoton, 0.5f, 0.5f, 0, 0, 1);

                botonAgregarPaciente.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        botonAgregarPaciente.setBackgroundResource(R.drawable.border);
                        botonAgregarPaciente.setTranslationX(-5);
                        botonAgregarPaciente.setTranslationY(-3);

                        toAgregarPaciente(v);
                    }
                }, 200);

            }
        });

    }


    public void toAgregarPaciente(View v){
            Intent intent = new Intent(this, MainActivity2.class);
            intent.putExtra("rowId","");
            startActivity(intent);
            finish();
    }

    public void cargarDatos(String query){

            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "datos", null, 1);
            SQLiteDatabase db = admin.getReadableDatabase();
            Cursor filaPaciente = db.rawQuery(query, null);
            if (filaPaciente.moveToFirst()) {
                do{
                    String id_Paciente = filaPaciente.getString(0);

                    String nombre = filaPaciente.getString(1);
                    String apellidos = filaPaciente.getString(2);
                    String alias = filaPaciente.getString(3);
                    String ultimaCita = ultimaCita(id_Paciente);


                    TextView paciente = new TextView(this);
                    paciente.setText(formatearMensaje(id_Paciente,nombre, apellidos, alias, ultimaCita));

                    paciente.setBackgroundResource(R.drawable.border_element);
                    paciente.setTextSize(20);

                    Typeface typeface = ResourcesCompat.getFont(this, R.font.brock_restar);;
                    paciente.setTypeface(typeface);

                    paciente.setPadding(40,40,40,40);
                    paciente.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            paciente.setBackgroundResource(R.drawable.no_border_element);
                            paciente.setTranslationX(5);
                            paciente.setTranslationY(3);
                            sp.play(sonidoElemento, 0.5f, 0.5f, 0, 0, 1);

                            paciente.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    paciente.setBackgroundResource(R.drawable.border_element);
                                    paciente.setTranslationX(-5);
                                    paciente.setTranslationY(-3);

                                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                                    intent.putExtra("rowId",id_Paciente);
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
                    paciente.setLayoutParams(lp);
                    ly.addView(paciente);

                }while(filaPaciente.moveToNext());

            }else{
                TextView sinDatos = new TextView(this);
                sinDatos.setText("Sin Pacientes Registrados con ese criterio madre");

                Typeface typeface = ResourcesCompat.getFont(this, R.font.brock_restar);;
                sinDatos.setTypeface(typeface);
                sinDatos.setTextColor(Color.parseColor("#A9A8AA"));

                sinDatos.setTextSize(14);
                sinDatos.setGravity(View.TEXT_ALIGNMENT_CENTER);
                sinDatos.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                sinDatos.setPadding(20,20,20,20);
                ly.addView(sinDatos);
            }

        filaPaciente.close();
        db.close();
    }

    public String ultimaCita(String idPaciente){
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "datos", null, 1);
        SQLiteDatabase bd = admin.getReadableDatabase();
        Cursor filaCitas = bd.rawQuery
                ("SELECT fecha FROM consultas WHERE id_paciente =" + idPaciente+" ORDER by ROWID DESC LIMIT 1; ", null);

        if (filaCitas.moveToFirst()) {

            return "Última cita: " +filaCitas.getString(0);

        }else{
            return "No hay citas registradas";
        }
    }

    public SpannableString formatearMensaje(String id_paciente, String nombre, String apellidos, String alias, String ultimaCita){

        String textoIzquierda;
        String textoDerecha = ultimaCita;

        if(alias.isEmpty()){

            textoIzquierda = "ID:"+ id_paciente + " " + nombre + " " + apellidos;

        }else{ textoIzquierda = "ID:"+ id_paciente + " " + nombre + " " + apellidos + " '" + alias + "'"; }

        int tabPosition = 1100;

        SpannableString spannableString = new SpannableString(textoIzquierda + "\t" + textoDerecha);
        spannableString.setSpan(new TabStopSpan.Standard(tabPosition), textoIzquierda.length()
                + 1, textoIzquierda.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    public void cargarBuscador(){

        buscador = findViewById(R.id.buscador);
        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    ly.removeAllViews();
                    cargarDatos("SELECT id_paciente, nombre, apellidos, alias FROM pacientes WHERE nombre LIKE '%" + s + "%'");

                }catch (Exception e){

                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            }
        });

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "datos", null, 1);
        SQLiteDatabase bd = admin.getReadableDatabase();
        Cursor filaCitas = bd.rawQuery("SELECT * FROM pacientes", null);

        buscador.setHint("Buscar Paciente por Nombre" + " (Nº de pacientes actuales: " + filaCitas.getCount() + ")");

        bd.close();
        filaCitas.close();

    }

}