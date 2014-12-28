package com.stefannew.openbook;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import info.vividcode.android.zxing.CaptureActivity;
import info.vividcode.android.zxing.CaptureActivityIntents;
import info.vividcode.android.zxing.CaptureResult;

public class MainActivity extends ActionBarActivity {

    public final static String ISBN = "com.stefannew.openbook.MESSAGE";
    private EditText isbnField;
    private EditText bookField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        AdView adView       = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        adView.loadAd(adRequest);

        isbnField           = (EditText) findViewById(R.id.isbn_field);
        bookField           = (EditText) findViewById(R.id.title_field);

        isbnField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                bookField.setText("");
            }
        });

        bookField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                isbnField.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        bookField.setText("");
        isbnField.setText("");
    }

    /**
     * Function to handle initializing barcode scanning intent.
     * Called from XML on scan button click.
     *
     * @param v View being passed
     */
    public void scanButtonClick(View v) {
        Intent captureIntent = new Intent(this, CaptureActivity.class);
        CaptureActivityIntents.setPromptMessage(captureIntent, "Scanning for ISBN...");
        startActivityForResult(captureIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String isbn = "";

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                CaptureResult res = CaptureResult.parseResultIntent(data);
                isbn = res.getContents();
            }
        }

        isbnField.setText(isbn);
        bookField.setText("");
    }

    /**
     * Function to open review intent. Passes ISBN to intent.
     * Called from XML on review button click.
     *
     * @param   v View being passed
     */
    public void getReviews(View v) {
        Intent reviewIntent     = new Intent(this, ReviewActivity.class);
        String isbnMessage;
        String isbnText         = isbnField.getText().toString();
        String titleText        = bookField.getText().toString();

        if (isbnText.length() > 0) {
            isbnMessage = isbnText;
        } else {
            isbnMessage = titleText.replaceAll(" ", "+");
        }

        if (isConnected()) {
            if (isbnMessage.length() > 0) {
                reviewIntent.putExtra(ISBN, isbnMessage);
                startActivity(reviewIntent);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Please scan or enter an ISBN, or book title", Toast.LENGTH_LONG);
                TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
                if( view != null) view.setGravity(Gravity.CENTER);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "No internet connection detected. Please check connection settings.", Toast.LENGTH_LONG);
            TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
            if( view != null) view.setGravity(Gravity.CENTER);
            toast.show();
        }

    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
