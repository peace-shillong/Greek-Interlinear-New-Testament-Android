package peace_shillong.kint;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import peace_shillong.kint.R;
import peace_shillong.model.BookNavigator;
import peace_shillong.model.DatabaseManager;

public class SelectionActivity extends AppCompatActivity {

    private Spinner spinnerBooks;
    private Spinner spinnerChapters;
    private Spinner spinnerVerses;
    private EditText verseEditText,chapterEditText;
    private SQLiteDatabase database;
    private BookNavigator navigator;
    private String book;
    private int chapter;
    private int verse;
    private Button buttonGo;
    private TextView permissionText;
    private FirebaseAnalytics mFirebaseAnalytics;

    private SQLiteDatabase initializeDatabase(Context context) {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
        SQLiteDatabase database = null;

        try {
            dataBaseHelper.createDataBase();
            database = dataBaseHelper.getReadableDatabase();
        } catch (IOException e) {

            e.printStackTrace();
            Toast.makeText(context, "DATABASE CREATION FAILED", Toast.LENGTH_LONG).show();
        }
        return database;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        DatabaseManager.init(SelectionActivity.this);
        DatabaseManager instance = DatabaseManager.getInstance();
        verseEditText= findViewById(R.id.versetext);
        chapterEditText= findViewById(R.id.chapterstext);
        verseEditText.setVisibility(View.GONE);
        chapterEditText.setVisibility(View.GONE);

        if(database==null)
        {
            database = initializeDatabase(SelectionActivity.this);
            Log.d("Database","INIT");
        }

        navigator = new BookNavigator(database);

        //request permission
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Log.d("Load","Permission");
            permissionText=findViewById(R.id.textView_permission);
            Dexter.withActivity(SelectionActivity.this)
                    .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                            //Permissions Granted
                        }
                        @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                            /*Permissions not granted*/
                            permissionText.setVisibility(View.VISIBLE);
                        }
                        @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        }



            buttonGo = (Button) findViewById(R.id.buttonGo);
            buttonGo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
//                    if(chapterEditText.getText().toString().length()>0)
//                        chapter= Integer.parseInt(chapterEditText.getText().toString());
//                    if(verseEditText.getText().toString().length()>0)
//                        verse= Integer.parseInt(verseEditText.getText().toString());
                    chapter= Integer.parseInt(spinnerChapters.getSelectedItem().toString());
                    verse= Integer.parseInt(spinnerVerses.getSelectedItem().toString());
                    if(chapter==0)
                        chapter=1;
                    if(verse==0)
                        verse=1;
                    Bundle bundle = new Bundle();
                    bundle.putString("book", book);
                    bundle.putInt("chapter", chapter);
                    bundle.putInt("verse", verse);
                    i.putExtras(bundle);
                    startActivity(i);
                }
            });

            // Obtain the FirebaseAnalytics instance.
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(SelectionActivity.this);

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "KINT");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            //Log.d("DATA","Wow");

            //set Default Book and chapters

            spinnerBooks = (Spinner) findViewById(R.id.spinnerbooks);
            spinnerVerses = (Spinner) findViewById(R.id.spinnerverses);
            spinnerChapters = (Spinner)findViewById(R.id.spinnerchapters);
            spinnerVerses.setVisibility(View.VISIBLE);
            spinnerChapters.setVisibility(View.VISIBLE);

            List<String> list = navigator.getChapters("Matt"); //temporary fix for Chapters in Genesis on Nokia Phones
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(SelectionActivity.this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerChapters.setAdapter(dataAdapter);

            spinnerBooks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Object itemAtPosition = parent.getItemAtPosition(position);

                    HashMap<String, String> map = new HashMap<>();
                    map.put("Matthew", "Matt");
                    map.put("Mark", "Mark");
                    map.put("Luke", "Luke");
                    map.put("John", "John");
                    map.put("Acts", "Acts");
                    map.put("Romans", "Rom");
                    map.put("1 Corinthians", "1Cor");
                    map.put("2 Corinthians", "2Cor");
                    map.put("Galatians", "Gal");
                    map.put("Ephesians", "Eph");
                    map.put("Philippians", "Phil");
                    map.put("Colossians", "Col");
                    map.put("1 Thessalonians", "1Thess");
                    map.put("2 Thessalonians", "2Thess");
                    map.put("1 Timothy", "1Tim");
                    map.put("2 Timothy", "2Tim");
                    map.put("Titus", "Titus");
                    map.put("Philemon", "Phlm");
                    map.put("Hebrews", "Heb");
                    map.put("James", "Jas");
                    map.put("1 Peter", "1Pet");
                    map.put("2 Peter", "2Pet");
                    map.put("1 John", "1John");
                    map.put("2 John", "2John");
                    map.put("3 John", "3John");
                    map.put("Jude", "Jude");
                    map.put("Revelation", "Rev");

                    book = map.get(itemAtPosition);
                    Log.d("BOOK SELECTED",book+" - "+itemAtPosition);

                    if (view != null) {
                        List<String> list = navigator.getChapters(book);
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(SelectionActivity.this, android.R.layout.simple_spinner_item, list);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerChapters.setAdapter(dataAdapter);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerChapters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Object itemAtPosition = parent.getItemAtPosition(position);

                    //try {
                        String item =itemAtPosition.toString();
                        item=item.replaceAll("[^0-9]]","");
                        if(item.length()==0)
                            item="1";
                        chapter = Integer.parseInt(item);
                    //} catch(NumberFormatException exception) {
                      //  exception.printStackTrace();
                        //Toast.makeText(SelectionActivity.this, "Error "+exception.getMessage(), Toast.LENGTH_SHORT).show();
                        //return;
                    //}

                    List<String> list = navigator.getVerses(book, chapter);
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(SelectionActivity.this, android.R.layout.simple_spinner_item, list);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerVerses.setAdapter(dataAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerVerses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Object itemAtPosition = parent.getItemAtPosition(position);
                    try{
                    String item =itemAtPosition.toString();
                    item=item.replaceAll("[^0-9]]","");
                    if(item.length()==0)
                        item="1";
                        verse = Integer.parseInt(item);
                    } catch(NumberFormatException e) {
                        e.printStackTrace();
                        Toast.makeText(SelectionActivity.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
