package com.stefannew.bookreview;

/**
 * Basic Review object
 */
public class Review {

    public String source;
    public String image_source;
    public String snippet;
    public String review_link;

    public Review(String source, String image_source, String snippet, String review_link) {
        this.source = source;
        this.image_source = image_source;
        this.review_link = review_link;
        this.snippet = snippet;
    }

}
