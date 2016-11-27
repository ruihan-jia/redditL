package rick.redditl.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import rick.redditl.model.PostData;
import rick.redditl.R;
import rick.redditl.activity.CommentPage;
import rick.redditl.activity.WebActivity;
import rick.redditl.helper.TimeHelper;

/**
 * Created by Rick on 2016-09-13.
 *
 * references:
 * https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
 *
 */
public class PostListAdapter extends ArrayAdapter<PostData> {

    private Context context;

    //public LayoutInflater inflater;

    String TAG = "PostListAdapter";

    //screen size
    int screenWidth;
    int screenHeight;



    public PostListAdapter(Context contextIn, ArrayList<PostData> PostData) {
        super(contextIn, 0, PostData);

        context = contextIn;


        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        //inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Log.w(TAG,"getview called, position " + position);

        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final PostData postItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.postview, parent, false);
        }


        //getting all the elements
        TextView score = (TextView) convertView.findViewById(R.id.score);
        TextView titleText = (TextView) convertView.findViewById(R.id.title);
        TextView commentsNum = (TextView) convertView.findViewById(R.id.num_comments);
        TextView authorNsubreddit = (TextView) convertView.findViewById(R.id.authorNsubreddit);
        ImageView previewImageView = (ImageView) convertView.findViewById(R.id.previewImage);
        final ImageView expandedImageView = (ImageView) convertView.findViewById(R.id.expandedImage);


        //====================setting all the elements=======================

        //title and domain
        String titleNdomain = postItem.getTitle() + " (" + postItem.getDomain() + ")";
        SpannableString spanString =  new SpannableString(titleNdomain);
        spanString.setSpan(new RelativeSizeSpan(0.75f), titleNdomain.length() - (postItem.getDomain().length() + 2),titleNdomain.length(), 0); // set size
        spanString.setSpan(new ForegroundColorSpan(Color.GRAY), titleNdomain.length() - (postItem.getDomain().length() + 2), titleNdomain.length(), 0);// set color
        titleText.setText(spanString);
        //number of comments
        commentsNum.setText(Integer.toString(postItem.getNum_comments()) + " comments");
        //score
        score.setText(Integer.toString(postItem.getScore()));
        //time since post
        //this time is given in seconds, not milliseconds.
        String ago = TimeHelper.timeSincePost(postItem.getTimeCreated());

        //author and subreddit
        String authorNsubredditText = ago + " ago by " + postItem.getAuthor() + " to /r/" + postItem.getSubreddit();
        spanString =  new SpannableString(authorNsubredditText);
        spanString.setSpan(new ForegroundColorSpan(Color.BLACK), ago.length() + 8, ago.length() + 8 + postItem.getAuthor().length(), 0);// set color
        spanString.setSpan(new ForegroundColorSpan(Color.BLACK), authorNsubredditText.length() - (postItem.getSubreddit().length() + 3), authorNsubredditText.length(), 0);// set color
        authorNsubreddit.setText(spanString);

        //preview image
        if(postItem.getPreviewSource() != null){
            if (postItem.getPreviewThumbnail() != null){
                previewImageView.setImageBitmap(postItem.getPreviewThumbnail());
                Log.w(TAG, "set existing image position " + position);
            }else{
                new previewImageTask(previewImageView,postItem)
                        .execute(postItem.getPreviewImagesLowReso().url);
                Log.w(TAG,"get new image position " + position);
            }

            //Log.w(TAG,"preview image position " + position);
        }
        else{
            previewImageView.setImageResource(R.mipmap.reddit_logo_img);
        }

        //expanded image
        if(postItem.getImageExpanded() == false) {
            //make the image disappear
            expandedImageView.setVisibility(View.GONE);

        } else {
            //if expanded, set it to expanded image.
            Log.w(TAG,"image is expanded position " + position);
            expandedImageView.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 0);
            expandedImageView.setLayoutParams(lp);

            double heightD = postItem.getExpandedImage().getHeight()*(double)screenWidth/(double)postItem.getExpandedImage().getWidth();
            expandedImageView.setImageBitmap(Bitmap.createScaledBitmap(postItem.getExpandedImage(), screenWidth, (int)heightD, false));

        }


        //=======================set on click listeners==========================

        titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "test position " + position);

                //open webview activity
                openWeb(postItem.getUrl());


            }
        });
        previewImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "test position " + position);

                //if image is not expanded, expand
                if (postItem.getImageExpanded() == false) {
                    new checkUrl(expandedImageView,postItem)
                            .execute(postItem.getUrl());
                } else {
                    //else collpase iamge
                    expandedImageView.setVisibility(View.GONE);
                    postItem.setImageExpanded(false);
                }

            }
        });
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG,"test position " + position);

                //colapse the expanded image
                expandedImageView.setVisibility(View.GONE);
                postItem.setImageExpanded(false);

            }
        });
        commentsNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "test position " + position);

                //open comments activity
                openComments(postItem.getPermalink());


            }
        });


        return convertView;
    }

    /**
     * Takes an url and checks if it is an image file.
     * If it is, set the expanded image to the url image
     * Else opens up an webview and load the url
     */
    public class checkUrl extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        Bitmap mIcon11 = null;
        PostData post;

        public checkUrl(ImageView bmImage, PostData postIn) {
            this.bmImage = bmImage;
            post = postIn;
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
            Log.w(TAG,"img is " + img);

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
                Log.w(TAG, "setting image");

                //set the view to image
                bmImage.setVisibility(View.VISIBLE);
                //Log.w(TAG, "getwidth is " + result.getWidth() + " get height is " + result.getHeight() + " screenwidth is " + screenWidth);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 0);
                bmImage.setLayoutParams(lp);
                bmImage.setBackgroundColor(Color.WHITE);
                double heightD = result.getHeight()*(double)screenWidth/(double)result.getWidth();
                bmImage.setImageBitmap(Bitmap.createScaledBitmap(result, screenWidth, (int)heightD, false));

                //set the image to data
                post.setExpandedImage(result);
                post.setImageExpanded(true);


            } else
                openWeb(post.getUrl());

        }
    }

    /**
     * Open a new webview activity from the url
     *
     * @param urlIn
     */
    public void openWeb (String urlIn) {
        Intent intent = new Intent(context,WebActivity.class);

        intent.putExtra("URL", urlIn);
        context.startActivity(intent);
    }

    /**
     * Open the comment activity from the url
     *
     * @param urlIn
     */
    public void openComments (String urlIn) {
        Intent intent = new Intent(context,CommentPage.class);

        intent.putExtra("URL", urlIn);
        context.startActivity(intent);
    }

    /**
     * Get the preview image from server
     * From
     * http://stackoverflow.com/questions/2471935/how-to-load-an-imageview-by-url-in-android
     */
    public class previewImageTask extends AsyncTask<String, Void, Bitmap> {
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
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            post.setPreviewThumbnail(result);
        }
    }






}
