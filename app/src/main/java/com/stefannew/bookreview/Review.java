package com.stefannew.bookreview;

public class Review {

    public String source;
    public String image_source;
    public String snippet;
    public String review_link;

    /**
     * Constructor for Review object.
     *
     * @param source            Source of the review (publication)
     * @param image_source      URI of the source logo
     * @param snippet           Contents of the review
     * @param review_link       URI of the full review
     */
    public Review(String source, String image_source, String snippet, String review_link) {
        this.source = source;
        this.image_source = image_source;
        this.review_link = review_link;
        this.snippet = snippet;
    }
}
