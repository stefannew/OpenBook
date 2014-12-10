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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private LinearLayout parent_layout;
    private LinearLayout error_view_container;
    private TextView error_text_view;

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

        parent_layout = (LinearLayout) critic_reviews.getParent();
        error_view_container = (LinearLayout) findViewById(R.id.error_view_container);

        error_text_view = (TextView) findViewById(R.id.error_text_view);

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

        /**
         * Iterates over the json object and sets the corresponding text views' content.
         * Called at the end of the ASyncTask Class ("HttpRequest")
         *
         * @param   result    JSON-formatted book object.
         */
        private void displayResult(String result) {
            JSONObject json;
            JSONObject book_object;
            JSONArray review_array;
            int review_count = 0;

            try {
                json = new JSONObject(result);
                book_object = json.getJSONObject("book");

                if (book_object != null) {
                    if (book_object.has("title")) {
                        book_title.setText(book_object.getString("title"));
                    }

                    if (book_object.has("author")) {
                        book_author.setText(book_object.getString("author"));
                    }

                    if (book_object.has("genre")) {
                        book_genre.setText(book_object.getString("genre"));
                    }

                    if (book_object.has("release_date")) {
                        book_date.setText(book_object.getString("release_date"));
                    }

                    if (book_object.has("rating")) {
                        book_rating.setText(book_object.getString("rating"));
                    }

                    if (book_object.has("detail_link") && book_object.has("title")) {
                        idreambooks_link.setText(Html.fromHtml("<a href='" +
                                book_object.getString("detail_link") + "'>'" +
                                book_object.getString("title") +
                                "' Reviews from iDreamBooks.com</a>"));
                        idreambooks_link.setMovementMethod(LinkMovementMethod.getInstance());
                    }

                    if (book_object.has("review_count")) {
                        review_count = book_object.getInt("review_count");
                    }

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
                        /*
                        String title = null;
                        String author = null;

                        if (book_object.has("title")) {
                            title = book_object.getString("title");
                        }
                        if (book_object.has("author")) {
                            author = book_object.getString("author");
                        }

                        parent_layout.removeView(findViewById(R.id.critic_reviews));
                        error_view_container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 4f));

                        error_text_view.setText(Html.fromHtml("<p>No Reviews for " +
                                title + " by " +
                                author + " found." + '\n' +
                                "Would you like to " + "<a href=" +
                                "https://www.google.com/?gws_rd=ssl#q=book+reviews+for+" +
                                this.ISBN + ">search online?</a></p>"));
                        error_text_view.setMovementMethod(LinkMovementMethod.getInstance());
                        */
                        new Scrape().execute(book_object.getString("detail_link"));
                    }
                } else {
                    parent_layout.removeView(findViewById(R.id.critic_reviews));

                    error_view_container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 4f));

                    error_text_view.setText(Html.fromHtml("<p>No information for ISBN " +
                            this.ISBN + " found in database." +
                            "Would you like to " + "<a href=" +
                            "https://www.google.com/?gws_rd=ssl#q=book+reviews+for+" +
                            this.ISBN + ">search online?</a></p>"));
                    error_text_view.setMovementMethod(LinkMovementMethod.getInstance());
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
         *
         * @param   key     API key
         * @return          a well-formed URL
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

        /**
         * Non-blocking ASyncTask with http request to iDreamBooks for review content
         *
         * @return          a JSON-formatted object with matching book and reviews (if any)
         */
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

        private class Scrape extends AsyncTask<String, Void, JSONObject> {

            @Override
            protected JSONObject doInBackground(String... params) {
                String url = params[0];
                JSONObject json = new JSONObject();
                JSONArray arr = new JSONArray();
                Document doc = null;

                try {
                    doc = Jsoup.connect(url).get();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                Elements reviews = doc.select(".critic_boxes_text");
                Elements h1 = reviews.select("h1");
                Elements p = reviews.select("p");

                ArrayList<Element> a = new ArrayList<Element>();

                for (int i = 0; i < reviews.size(); i++) {
                    for (int x = 0; x < reviews.get(i).select("a").size(); x++) {

                        Element current = reviews.get(i).select("a").get(x);
                        if (current.hasAttr("target")) {
                            a.add(current);
                        }
                    }
                }

                for (int i = 0; i < reviews.size(); i++) {
                    JSONObject temp = new JSONObject();

                    try {
                        temp.put("source", h1.get(i).text());
                        temp.put("snippet", p.get(i).text());
                        temp.put("review_link", a.get(i).attr("href"));
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }

                    arr.put(temp);
                }

                try {
                    json.put("critic_reviews", arr);
                } catch (JSONException je) {
                    je.printStackTrace();
                }

                return json;
            }

            @Override
            protected void onPostExecute(JSONObject json) {
                super.onPostExecute(json);

                JSONArray review_array;

                if (json != null) {
                    try {
                        review_array = json.getJSONArray("critic_reviews");

                        for (int i = 0; i < review_array.length(); i++) {
                            JSONObject currentObj = review_array.getJSONObject(i);

                            String source = null;
                            String snippet = null;
                            String review_link = null;

                            if (currentObj.has("source") && !currentObj.isNull("source")) {
                                source = currentObj.getString("source");
                            }

                            if (currentObj.has("snippet") && !currentObj.isNull("snippet")) {
                                snippet = currentObj.getString("snippet");
                            }

                            if (currentObj.has("review_link") && !currentObj.isNull("review_link")) {
                                review_link = currentObj.getString("review_link");
                            }

                            review_array_list.add(new Review(source, null, snippet, review_link));
                        }

                        review_adapter.notifyDataSetChanged();
                        System.out.println(review_array);

                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            }
        }
    }
}
