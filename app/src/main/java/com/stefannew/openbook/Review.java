package com.stefannew.openbook;

public class Review {

    public String source;
    public String snippet;
    public String review_link;

    /**
     * Constructor for Review object.
     *
     * @param source            Source of the review (publication)
     * @param snippet           Contents of the review
     * @param review_link       URI of the full review
     */
    public Review(String source, String snippet, String review_link) {
        this.source = source;
        this.review_link = review_link;
        this.snippet = snippet;
    }
}
