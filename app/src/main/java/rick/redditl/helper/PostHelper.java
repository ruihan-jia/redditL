package rick.redditl.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import rick.redditl.Constants;
import rick.redditl.R;
import rick.redditl.activities.WebActivity;
import rick.redditl.models.PostData;

/**
 * Created by Rick on 2016-12-18.
 */
public class PostHelper {

    public static String TAG = "PostHelper";



    public static void setPostDataToView(PostData oPostData, TextView score,TextView titleText,
                                         Button commentsNum, TextView authorNsubreddit, ImageView previewImageView,
                                         ImageView expandedImageView) {

        //====================setting all the elements=======================

        //title and domain
        String titleNdomain = oPostData.getTitle() + " (" + oPostData.getDomain() + ")";
        SpannableString spanString =  new SpannableString(titleNdomain);
        spanString.setSpan(new RelativeSizeSpan(0.75f), titleNdomain.length() - (oPostData.getDomain().length() + 2),titleNdomain.length(), 0); // set size
        spanString.setSpan(new ForegroundColorSpan(Color.GRAY), titleNdomain.length() - (oPostData.getDomain().length() + 2), titleNdomain.length(), 0);// set color
        titleText.setText(spanString);
        //number of comments
        commentsNum.setText(Integer.toString(oPostData.getNum_comments()) + " comments");
        //score, if greater than 9999, change size
        score.setText(GeneralHelper.convertIntToStringK(oPostData.getScore()));
        //time since post
        //this time is given in seconds, not milliseconds.
        String ago = TimeHelper.timeSincePost(oPostData.getTimeCreated());

        //author and subreddit
        String authorNsubredditText = ago + " ago by " + oPostData.getAuthor() + " to /r/" + oPostData.getSubreddit();
        spanString =  new SpannableString(authorNsubredditText);
        spanString.setSpan(new ForegroundColorSpan(Color.BLACK), ago.length() + 8, ago.length() + 8 + oPostData.getAuthor().length(), 0);// set color
        spanString.setSpan(new ForegroundColorSpan(Color.BLACK), authorNsubredditText.length() - (oPostData.getSubreddit().length() + 3), authorNsubredditText.length(), 0);// set color
        authorNsubreddit.setText(spanString);


        //preview image
        if(oPostData.getPreviewSource() != null){
            if (oPostData.getPreviewThumbnail() != null){
                previewImageView.setImageBitmap(oPostData.getPreviewThumbnail());
            }else{
                //make request to server for preview image, then fill postdata with image.
                new previewImageTask(previewImageView,oPostData).execute(oPostData.getPreviewImagesLowReso().url);
            }
        }
        else{
            previewImageView.setImageResource(R.mipmap.reddit_logo_img);
        }

        //expanded image
        if(oPostData.getImageExpanded() == false) {
            //make the image disappear
            expandedImageView.setVisibility(View.GONE);

        } else {
            //if expanded, set it to expanded image.
            expandedImageView.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 0);
            expandedImageView.setLayoutParams(lp);

            double heightD = oPostData.getExpandedImage().getHeight()*(double)Constants.screenWidth/(double)oPostData.getExpandedImage().getWidth();
            expandedImageView.setImageBitmap(Bitmap.createScaledBitmap(oPostData.getExpandedImage(), Constants.screenWidth, (int) heightD, false));

        }



    }


    /**
     * Get the preview image from server
     * From
     * http://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
     */
    public static class previewImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        PostData post;

        public previewImageTask(ImageView bmImage, PostData postIn) {
            this.bmImage = bmImage;
            post = postIn;
            bmImage.setImageResource(R.mipmap.reddit_logo_img);
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                Log.d(TAG, "doInBackground: url is " + urldisplay);
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            //this is the view that was passed in and will be populated
            bmImage.setImageBitmap(result);
            //update the post data object with the image as well
            post.setPreviewThumbnail(result);
        }
    }






    /**
     * Takes an url and checks if it is an image file.
     * If it is, set the expanded image to the url image
     * Else opens up an webview and load the url
     */
    public static class checkUrl extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        Bitmap mIcon11 = null;
        PostData post;
        Context context;

        public checkUrl(ImageView bmImage, PostData postIn, Context contextIn) {
            this.bmImage = bmImage;
            post = postIn;
            context = contextIn;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];


            URLConnection connection = null;
            try {
                connection = new URL(urldisplay).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String contentType = connection.getHeaderField("Content-Type");
            Boolean img = contentType.startsWith("image/");
            Log.d(TAG, "img is " + img);

            if(img) {
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }

            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                Log.d(TAG, "setting image");

                //set the view to image
                bmImage.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 0);
                bmImage.setLayoutParams(lp);
                bmImage.setBackgroundColor(Color.WHITE);
                double heightD = result.getHeight()*(double) Constants.screenWidth/(double)result.getWidth();
                bmImage.setImageBitmap(Bitmap.createScaledBitmap(result, Constants.screenWidth, (int)heightD, false));

                //set the image to data
                post.setExpandedImage(result);
                post.setImageExpanded(true);


            } else
                openWeb(post.getUrl(), context);

        }
    }


    /**
     * Open a new webview activity from the url
     *
     * @param urlIn
     */
    public static void openWeb (String urlIn, Context context) {


        Intent intent = new Intent(context,WebActivity.class);

        intent.putExtra("URL", urlIn);
        context.startActivity(intent);
    }


}
