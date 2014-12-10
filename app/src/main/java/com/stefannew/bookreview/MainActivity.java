package com.stefannew.bookreview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import info.vividcode.android.zxing.CaptureActivity;
import info.vividcode.android.zxing.CaptureActivityIntents;
import info.vividcode.android.zxing.CaptureResult;


public class MainActivity extends Activity {

    public final static String ISBN = "com.stefannew.bookreview.MESSAGE";
    private Button scan_button;
    private EditText isbn_field;

    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        scan_button     = (Button) findViewById(R.id.scan_button);
        isbn_field      = (EditText) findViewById(R.id.isbn_field);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Function to handle initializing barcode scanning intent.
     * Called from XML on scan button click.
     *
     * @param v
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
                isbn = res.getContents().toString();
            } else {
                // back button etc.
            }
        }

        isbn_field.setText(isbn);
    }

    /**
     * Function to open review intent. Passes ISBN to intent.
     * Called from XML on review button click.
     *
     * @param   v
     */
    public void getReviews(View v) {
        Intent reviewIntent = new Intent(this, ReviewActivity.class);
        String isbnMessage = isbn_field.getText().toString();
        reviewIntent.putExtra(ISBN, isbnMessage);
        startActivity(reviewIntent);
    }
}
