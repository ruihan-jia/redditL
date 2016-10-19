package rick.redditl.data;

import android.graphics.Bitmap;

/**
 * Created by Rick on 2016-07-19.
 */
public class PostData {

    //base data
    private String title;
    private String subreddit;
    private String author;
    private int score;
    private int num_comments;
    private String permalink;
    private String url;
    private long timeCreated;
    private Boolean isSelf;
    private String selfText;
    private String domain;

    //preview images
    private previewImages previewSource;
    private previewImages[] previewResolutions;
    private String previewID;
    private Bitmap previewThumbnail;

    //expanded images
    private Bitmap expandedImage;
    private Boolean imageExpanded;


    public PostData() {

    }

    public PostData(String titleIn, String subredditIn, String authorIn, int scoreIn,
                      int num_commentsIn, String permalinkIn, String urlIn, long timeCreatedIn,
                      Boolean isSelfIn, String selfTextIn, String domainIn) {

        title = titleIn;
        subreddit = subredditIn;
        author = authorIn;
        score = scoreIn;
        num_comments = num_commentsIn;
        permalink = permalinkIn;
        url = urlIn;
        timeCreated = timeCreatedIn;
        isSelf = isSelfIn;
        selfText = selfTextIn;
        domain = domainIn;


        imageExpanded = false;

    }


    public String getTitle() {
        return title;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getAuthor() {
        return author;
    }

    public int getScore() {
        return score;
    }

    public int getNum_comments() {
        return num_comments;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getUrl() {
        return url;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public boolean getIsSelf() {
        return isSelf;
    }

    public String getSelfText() {
        return selfText;
    }

    public String getDomain() {
        return domain;
    }

    public previewImages getPreviewSource() {
        return previewSource;
    }

    public previewImages[] getPreviewImages() {
        return previewResolutions;
    }

    public previewImages getPreviewLowReso () {
        return previewResolutions[0];
    }

    public Bitmap getPreviewThumbnail () {
        return previewThumbnail;
    }

    public Bitmap getExpandedImage () {
        return expandedImage;
    }

    public boolean getImageExpanded () {
        return imageExpanded;
    }

    public void setPreviewSource(previewImages sourceIn) {
        previewSource = sourceIn;
    }

    public void setResolution (previewImages[] resolutionsIn) {
        previewResolutions = resolutionsIn;
    }

    public void setPreviewThumbnail (Bitmap previewThumbnailIn) {
        previewThumbnail = previewThumbnailIn;
    }


    public void setExpandedImage (Bitmap expandedImageIn) {
        expandedImage = expandedImageIn;
    }

    public void setImageExpanded(Boolean expand) {
        imageExpanded = expand;
    }


}
