package com.stefannew.bookreview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class ReviewActivity extends Activity {

    private TextView book_title;
    private TextView book_author;
    private TextView book_genre;
    private TextView book_date;
    private TextView book_rating;
    private TextView idreambooks_link;
    private ArrayList<Review> review_array_list = new ArrayList<Review>();
    private ReviewAdapter review_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        AdView adView           = (AdView) findViewById(R.id.adView);
        AdRequest adRequest     = new AdRequest.Builder().build();

        Intent intent           = getIntent();
        String ISBN             = intent.getStringExtra(MainActivity.ISBN);

        book_title              = (TextView) findViewById(R.id.book_title);
        book_author             = (TextView) findViewById(R.id.book_author);
        book_genre              = (TextView) findViewById(R.id.book_genre);
        book_date               = (TextView) findViewById(R.id.book_date);
        book_rating             = (TextView) findViewById(R.id.book_rating);
        idreambooks_link         = (TextView) findViewById(R.id.idreambooks_link);

        ListView critic_reviews = (ListView) findViewById(R.id.critic_reviews);
        review_adapter = new ReviewAdapter(this, R.layout.itemlistrow, review_array_list);

        critic_reviews.setAdapter(review_adapter);
        critic_reviews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Review review = review_array_list.get(position);
                Intent i = new Intent(Intent.ACTION_VIEW);

                i.setData(Uri.parse(review.review_link));
                startActivity(i);
            }
        });

        adView.loadAd(adRequest);
        new ApiRequest(ISBN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    /**
     * Api Request Class
     */
    public class ApiRequest {
        private final String ISBN;
        private final String key = "7a22537db832c4fcc14bb1d41cf6444fe48fea73";

        public ApiRequest(String ISBN) {
            this.ISBN = ISBN;

            new HttpRequest().execute(getUrl(key));
        }

        private void displayResult(String result) {
            JSONObject json;
            JSONObject book_object;
            JSONArray review_array;
            int review_count;

            /**
             * Need to check if the book object has contents before doing this.
             */

            try {
                json = new JSONObject(result);
                book_object = json.getJSONObject("book");
                if (book_object != null) {
                    book_title.setText(book_object.getString("title"));
                    book_author.setText(book_object.getString("author"));
                    book_genre.setText(book_object.getString("genre"));
                    book_date.setText(book_object.getString("release_date"));
                    book_rating.setText(book_object.getString("rating"));

                    idreambooks_link.setText(Html.fromHtml("<a href='" + book_object.getString("detail_link") + "'>'" + book_object.getString("title") + "' Reviews from iDreamBooks.com</a>"));
                    idreambooks_link.setMovementMethod(LinkMovementMethod.getInstance());

                    review_count = book_object.getInt("review_count");

                    if (review_count > 0) {
                        review_array = book_object.getJSONArray("critic_reviews");

                        for (int i = 0; i < review_array.length(); i++) {
                            JSONObject currentObj = review_array.getJSONObject(i);

                            String source           = null;
                            String source_logo      = null;
                            String snippet          = null;
                            String review_link      = null;

                            if (currentObj.has("source") && !currentObj.isNull("source")) {
                                source = currentObj.getString("source");
                            }

                            if (currentObj.has("source_logo") && !currentObj.isNull("source_logo")) {
                                source_logo = currentObj.getString("source_logo");
                            }

                            if (currentObj.has("snippet") && !currentObj.isNull("snippet")) {
                                snippet = currentObj.getString("snippet");
                            }

                            if (currentObj.has("review_link") && !currentObj.isNull("review_link")) {
                                review_link = currentObj.getString("review_link");
                            }

                            review_array_list.add(new Review(source, source_logo, snippet, review_link));
                        }
                    } else {
                    ///    review_array_list.add(0, "No Critic Reviews found.");
                    }
                } else {
                 //   review_array_list.add(0, "No information for " + this.ISBN + " found.");
                }

                review_adapter.notifyDataSetChanged();

            } catch (JSONException je) {
                Toast toast = Toast.makeText(getApplicationContext(), "JSON Exception", Toast.LENGTH_SHORT);
                toast.show();
                je.printStackTrace();
            }

            System.out.println(result);
        }

        /**
         * Returns the API call url with ISBN keyword and API key
         * @param key API key
         * @return well-formed URL
         */
        private URL getUrl(String key) {
            URL url = null;

            try {
                url = new URL("http://idreambooks.com/api/books/reviews.json?q=" + this.ISBN + "&key=" + key);
            } catch (MalformedURLException me) {
                Toast toast = Toast.makeText(getApplicationContext(), "Error: Malformed URL", Toast.LENGTH_SHORT);
                toast.show();
            }

            return url;
        }

        private class HttpRequest extends AsyncTask<URL, Void, String> {

            @Override
            protected String doInBackground(URL... params) {
                URL url = params[0];
                HttpURLConnection urlConnection;
                InputStream in;
                BufferedReader reader;
                String line;

                StringBuilder out = null;

                try {
                    urlConnection       = (HttpURLConnection) url.openConnection();
                    in                  = new BufferedInputStream(urlConnection.getInputStream());
                    reader              = new BufferedReader(new InputStreamReader(in));
                    out                 = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        out.append(line);
                    }

                } catch (IOException ie) {
                    ie.printStackTrace();
                }

                return out != null ? out.toString() : null;
            }

            @Override
            protected void onPostExecute(String result) {
                displayResult(result);
            }
        }
    }
}
