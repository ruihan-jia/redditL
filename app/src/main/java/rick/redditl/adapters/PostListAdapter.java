package rick.redditl.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import rick.redditl.helper.PostHelper;
import rick.redditl.models.PostData;
import rick.redditl.R;
import rick.redditl.activities.CommentPage;
import rick.redditl.activities.WebActivity;

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

        Log.d(TAG, "getview called, position " + position);

        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final PostData postItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listadapter_post_view, parent, false);
        }


        //getting all the elements
        TextView score = (TextView) convertView.findViewById(R.id.score);
        TextView titleText = (TextView) convertView.findViewById(R.id.title);
        Button commentsNum = (Button) convertView.findViewById(R.id.num_comments);
        TextView authorNsubreddit = (TextView) convertView.findViewById(R.id.authorNsubreddit);
        ImageView previewImageView = (ImageView) convertView.findViewById(R.id.previewImage);
        final ImageView expandedImageView = (ImageView) convertView.findViewById(R.id.expandedImage);

        //set the post data to elements
        PostHelper.setPostDataToView(postItem, score, titleText, commentsNum, authorNsubreddit, previewImageView, expandedImageView);

        //=======================set on click listeners==========================

        //clicking the title text
        titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Onclick title text, position " + position);

                //open webview activity
                //openWeb(postItem.getUrl());
                PostHelper.openWeb(postItem.getUrl(), context);


            }
        });
        //clicking the image thumbnail
        previewImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Onclick thumbnail, position " + position);

                //if image is not expanded, expand
                if (postItem.getImageExpanded() == false) {
                    /*new checkUrl(expandedImageView,postItem)
                            .execute(postItem.getUrl());*/
                    new PostHelper.checkUrl(expandedImageView,postItem, context)
                            .execute(postItem.getUrl());
                } else {
                    //else collpase iamge
                    expandedImageView.setVisibility(View.GONE);
                    postItem.setImageExpanded(false);
                }

            }
        });
        //clicking expanded image
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Onclick expanded image, position " + position);

                //colapse the expanded image
                expandedImageView.setVisibility(View.GONE);
                postItem.setImageExpanded(false);

            }
        });
        //clicking the comments
        commentsNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Onclick comments, position " + position);

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
            Log.d(TAG,"img is " + img);

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





}
