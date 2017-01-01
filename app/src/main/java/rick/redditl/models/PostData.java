package rick.redditl.models;

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
    //id for internal use
    private String pid;
    private String name;

    //preview images
    private PreviewImageData previewSource;
    private PreviewImageData[] previewImagesRes;
    private String previewID;
    private String thumbnailURL;
    private Bitmap previewThumbnail;

    //expanded images
    private Bitmap expandedImage;
    private Boolean imageExpanded;


    public PostData() {

    }

    public PostData(String titleIn, String subredditIn, String authorIn, int scoreIn,
                      int num_commentsIn, String permalinkIn, String urlIn, long timeCreatedIn,
                      Boolean isSelfIn, String selfTextIn, String domainIn, String thumbnailURLIn,
                    String pidIn, String nameIn) {

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
        thumbnailURL = thumbnailURLIn;
        pid = pidIn;
        name = nameIn;

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

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public String getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public PreviewImageData getPreviewSource() {
        return previewSource;
    }

    public void setPreviewSource(PreviewImageData sourceIn) {
        previewSource = sourceIn;
    }

    //get all the preview images in different resolutions
    public PreviewImageData[] getPreviewImagesRes() {
        return previewImagesRes;
    }

    public void setPreviewImagesRes (PreviewImageData[] resolutionsIn) {
        previewImagesRes = resolutionsIn;
    }

    //get the first lowest resolution image
    public PreviewImageData getPreviewImagesLowReso () {
        return previewImagesRes[0];
    }

    public Bitmap getPreviewThumbnail () {
        return previewThumbnail;
    }

    public void setPreviewThumbnail (Bitmap previewThumbnailIn) {
        previewThumbnail = previewThumbnailIn;
    }

    public Bitmap getExpandedImage () {
        return expandedImage;
    }

    public void setExpandedImage (Bitmap expandedImageIn) {
        expandedImage = expandedImageIn;
    }

    public boolean getImageExpanded () {
        return imageExpanded;
    }

    public void setImageExpanded(Boolean expand) {
        imageExpanded = expand;
    }



}
