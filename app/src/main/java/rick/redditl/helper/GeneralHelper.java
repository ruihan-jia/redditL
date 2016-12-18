package rick.redditl.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;

import rick.redditl.Constants;
import rick.redditl.R;
import rick.redditl.adapter.PostListAdapter;
import rick.redditl.model.PostData;

/**
 * Created by Rick on 2016-12-17.
 */
public class GeneralHelper {

    static String TAG = "GeneralHelper";


    public static void setPostDataToView(PostData oPostData, TextView score,TextView titleText,
                                  TextView commentsNum, TextView authorNsubreddit, ImageView previewImageView,
                                  ImageView expandedImageView) {

        //====================setting all the elements=======================

        //title and domain
        String titleNdomain = oPostData.getTitle() + " (" + oPostData.getDomain() + ")";
        SpannableString spanString =  new SpannableString(titleNdomain);
        spanString.setSpan(new RelativeSizeSpan(0.75f), titleNdomain.length() - (oPostData.getDomain().length() + 2),titleNdomain.length(), 0); // set size
        spanString.setSpan(new ForegroundColorSpan(Color.GRAY), titleNdomain.length() - (oPostData.getDomain().length() + 2), titleNdomain.length(), 0);// set color
        titleText.setText(spanString);
        //number of comments
        if(commentsNum!= null)
            commentsNum.setText(Integer.toString(oPostData.getNum_comments()) + " comments");
        //score, if greater than 9999, change size
        score.setText(convertIntToStringK(oPostData.getScore()));
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
     * Takes an input integer and convert it to a string.
     * If the integer is greater than 9999, divide it by 1000 and add k at the end
     * Example: 12345 to 12.3k
     * @param input a positive integer
     * @return
     */
    public static String convertIntToStringK(int input) {
        if(input > 9999) {
            input = input/100;
            double temp = input;
            temp = temp/10;

            return Double.toString(temp) + 'k';

        } else {
            return Integer.toString(input);
        }

    }
}
