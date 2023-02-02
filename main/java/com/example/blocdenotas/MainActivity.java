package com.example.blocdenotas;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TabStopSpan;
import android.util.Log;
import android.widget.*;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.blocdenotas.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import com.google.android.material.textfield.TextInputEditText;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.google.android.material.internal.ContextUtils.getActivity;

public class MainActivity extends AppCompatActivity implements ChangeTitleDialog.ChangeTitleDialogListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private EditText textArea;
    private ChangeTitleDialog changeTitleDialog;

    private String docName;

    private static final int CREATE_FILE = 1;
    private static final int PICK_PLAIN_FILE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        textArea = findViewById(R.id.textArea);
        changeTitleDialog = new ChangeTitleDialog();

        binding.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTitleDialog.show(getSupportFragmentManager(),textArea.getText().toString());
            }
        });

        if(!openByDefault())newDoc();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_new:
                newDoc();
                break;
            case R.id.action_open:
                action_open();
                break;
            case R.id.action_save:
                action_save();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PICK_PLAIN_FILE  && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                try{
                    Uri uri = resultData.getData();
                    textArea.setText(openFile(uri));
                    getSupportActionBar().setTitle(getFileName(uri));
                }catch (IOException e){
                    erroAlert(e.getMessage());
                }
            }
        } else if (requestCode == CREATE_FILE  && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                try {
                    Uri uri = resultData.getData();
                    saveFile(uri,textArea.getText().toString());
                } catch (IOException e) {
                    erroAlert(e.getMessage());
                }
            }
        }
    }

    @Override
    public void onDialogPositiveClick(ChangeTitleDialog dialog) {
        EditText editText = dialog.view.findViewById(R.id.newTitleText);
        setDocName(editText.getText().toString());
    }

    private void newDoc() {
        setDocName("Sin titulo");
        textArea.setText("");
    }

    private boolean openByDefault(){
        Intent intent = getIntent();
        String intent_action = intent.getAction();
        if(intent_action.equals(Intent.ACTION_VIEW)){
            Uri uri = intent.getData();
            try{
                setDocName(getFileName(uri));
                textArea.setText(openFile(uri), TextView.BufferType.EDITABLE);
            }catch (Exception e){
                 erroAlert(e.getMessage());
            }
            return true;
        }else return false;
    }

    private void setDocName(String name){
        docName=name;
        changeTitleDialog.setOldTitle(docName);
        getSupportActionBar().setTitle(docName);
    }
    private void action_open(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, PICK_PLAIN_FILE);
    }

    private void action_save(){
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_TITLE, getSupportActionBar().getTitle());
        startActivityForResult(intent, CREATE_FILE);
    }

    private String getFileName(Uri uri){
        Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        return returnCursor.getString(nameIndex);
    }

    private String openFile(Uri uri) throws IOException {
        InputStreamReader isr = new InputStreamReader(getContentResolver().openInputStream(uri),StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String output="";
        String aux;
        while((aux = bufferedReader.readLine())!=null){
            output+=aux+"\n";
        }
        return output.replace("\t",getString(R.string.tab));
    }


    private void saveFile(Uri uri,String content) throws IOException {
        FileOutputStream fileOutupStream = (FileOutputStream) getContentResolver().openOutputStream(uri,"wt");
        fileOutupStream.write(content.getBytes(),0,content.getBytes().length);
        fileOutupStream.close();
    }

    private void popUp(String text){
        Snackbar.make(binding.getRoot(),text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
    private void erroAlert(String text){
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle("Mensage");
        dialogo1.setMessage(text);
        dialogo1.show();
    }


}