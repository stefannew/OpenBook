package com.stefannew.openbook;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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


public class ReviewActivity extends ActionBarActivity {
    private TextView book_title;
    private TextView book_author;
    private TextView book_genre;
    private TextView book_rating;
    private TextView idreambooks_link;
    private TextView error_text_view;

    private ArrayList<Review> review_array_list = new ArrayList<>();
    private ReviewAdapter review_adapter;

    private LinearLayout parent_layout;
    private LinearLayout error_view_container;

    private LinearLayout info_linear;
    private LinearLayout loading_layout;

    private ListView critic_reviews;
    private String apiKey;
    private String ISBN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        getSupportActionBar().setElevation(0);

        ImageView loading_image           = (ImageView) findViewById(R.id.loading_image);
        loading_image.setBackgroundResource(R.drawable.preloader);
        AnimationDrawable frameAnimation = (AnimationDrawable) loading_image.getBackground();
        frameAnimation.start();

        AdView adView           = (AdView) findViewById(R.id.adView);
        AdRequest adRequest     = new AdRequest.Builder().build();

        Intent intent           = getIntent();
        ISBN                    = intent.getStringExtra(MainActivity.ISBN);

        book_title              = (TextView) findViewById(R.id.book_title);
        book_author             = (TextView) findViewById(R.id.book_author);
        book_genre              = (TextView) findViewById(R.id.book_genre);
        book_rating             = (TextView) findViewById(R.id.book_rating);
        idreambooks_link        = (TextView) findViewById(R.id.idreambooks_link);

        loading_layout          = (LinearLayout) findViewById(R.id.loading_layout);
        info_linear             = (LinearLayout) findViewById(R.id.info_linear);

        critic_reviews          = (ListView) findViewById(R.id.critic_reviews);

        parent_layout           = (LinearLayout) critic_reviews.getParent();
        error_view_container    = (LinearLayout) findViewById(R.id.error_view_container);

        error_text_view         = (TextView) findViewById(R.id.error_text_view);

        review_adapter          = new ReviewAdapter(this, R.layout.itemlistrow, review_array_list);

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

        try {
            new ApiKeyRequest().execute(new URL("http://stefannew.com/things/openbook/returnApiKey.php?key=a1s2d3f4g5"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /**
     * Non-blocking ASyncTask with http request to iDreamBooks for review content
     */
    private class ApiKeyRequest extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... params) {
            URL url         = params[0];
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

            return out.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            apiKey = result;

            try {
                new ApiRequest(ISBN);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Api Request Class
     */
    public class ApiRequest {
        private final String ISBN;

        public ApiRequest(String ISBN) {
            this.ISBN = ISBN;

            new HttpRequest().execute(getUrl(apiKey));
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

            if (result.equals("invalid key")) {
                parent_layout.removeView(findViewById(R.id.critic_reviews));

                error_view_container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 4f));

                error_text_view.setText(Html.fromHtml("<p>Oops! Invalid API key. Please try again later.</p>"));
                error_text_view.setMovementMethod(LinkMovementMethod.getInstance());
            }

            String title        = "";
            String author       = "";
            String genre        = "";
            String rating       = "";
            String detail_link  = "";

            try {
                json = new JSONObject(result);
                book_object = json.getJSONObject("book");

                if (book_object != null) {
                    if (book_object.has("title")) {
                        title = book_object.getString("title");
                    }

                    if (book_object.has("author")) {
                        author = book_object.getString("author");
                    }

                    if (book_object.has("genre")) {
                        genre = book_object.getString("genre");
                    }

                    if (book_object.has("rating")) {
                        rating = book_object.getString("rating") + '%';
                    }

                    if (book_object.has("detail_link") && book_object.has("title")) {
                        detail_link = book_object.getString("detail_link");
                    }

                    if (book_object.has("review_count")) {
                        review_count = book_object.getInt("review_count");
                    }

                    book_title.setText(title);
                    book_author.setText(author);
                    book_genre.setText(genre);
                    book_rating.setText(rating);

                    if (title.length() > 0) {
                        getSupportActionBar().setTitle(title);
                    } else {
                        getSupportActionBar().setTitle(ISBN.replaceAll("\\+", " "));
                    }

                    if (book_object.has("detail_link")) {
                        idreambooks_link.setText(Html.fromHtml("<a href='" +
                                detail_link + "'>'" + title +
                                "' Reviews from iDreamBooks.com</a>"));

                        idreambooks_link.setMovementMethod(LinkMovementMethod.getInstance());
                    }

                    if (review_count > 0) {
                        review_array = book_object.getJSONArray("critic_reviews");

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

                            review_array_list.add(new Review(source, snippet, review_link));
                        }
                    } else if (detail_link.length() > 0) {
                        new Scrape().execute(detail_link, title, author, ISBN);
                    } else {
                        parent_layout.removeView(findViewById(R.id.critic_reviews));

                        error_view_container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 4f));

                        error_text_view.setText(Html.fromHtml("<p>No information for " +
                                this.ISBN.replaceAll("\\+", " ") + " found in database." + "<br>" +
                                "Would you like to " + "<a href=" +
                                "https://www.google.com/?gws_rd=ssl#q=book+reviews+for+" +
                                this.ISBN + ">search online?</a></p>"));
                        error_text_view.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                    review_adapter.notifyDataSetChanged();

                }
            } catch (JSONException je) {
                je.printStackTrace();
            }

            loading_layout.setVisibility(View.GONE);
            info_linear.setVisibility(View.VISIBLE);
            critic_reviews.setVisibility(View.VISIBLE);
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
                TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
                if( view != null) view.setGravity(Gravity.CENTER);
                toast.show();
            }

            return url;
        }

        /**
         * Non-blocking ASyncTask with http request to iDreamBooks for review content
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

            private String title;
            private String author;
            private String ISBN;

            @Override
            protected JSONObject doInBackground(String... params) {
                String url      = params[0];
                title           = params[1];
                author          = params[2];
                ISBN            = params[3];
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

                ArrayList<Element> a = new ArrayList<>();

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

                        if (review_array.length() > 0) {
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

                                review_array_list.add(new Review(source, snippet, review_link));
                            }

                            review_adapter.notifyDataSetChanged();
                        } else {
                            parent_layout.removeView(findViewById(R.id.critic_reviews));
                            error_view_container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 4f));

                            error_text_view.setText(Html.fromHtml("<p>No Reviews for \"" +
                                    title + "\"" + " found." + "<br>" +
                                    "Would you like to " + "<a href=" +
                                    "https://www.google.com/?gws_rd=ssl#q=book+reviews+for+" +
                                    ISBN + ">search online?</a></p>"));
                            error_text_view.setMovementMethod(LinkMovementMethod.getInstance());
                        }


                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            }
        }
    }
}