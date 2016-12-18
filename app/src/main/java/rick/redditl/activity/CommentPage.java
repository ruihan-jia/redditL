package rick.redditl.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import rick.redditl.adapter.CommentListAdapter;
import rick.redditl.adapter.PostListAdapter;
import rick.redditl.helper.GeneralHelper;
import rick.redditl.helper.ParserHelper;
import rick.redditl.helper.TimeHelper;
import rick.redditl.model.CommentData;
import rick.redditl.helper.JSONParser;
import rick.redditl.model.PostData;
import rick.redditl.R;
import rick.redditl.model.PreviewImageData;

/**
 * There is one object that stores the post data to be displayed at top
 * Using oPostData
 *
 * There are two objects that stores the comments data
 * One is the arraylist of CommentData, oComments
 * The other is the list adapter object, accessible through oCommentAdapter
 *
 * The list adapter object is what is showing on the screen
 * While the arraylist is the actual data to fall back on
 */

public class CommentPage extends AppCompatActivity {

    public PostData oPostData;
    public ArrayList<CommentData> oComments;
    CommentListAdapter oCommentAdapter;
    ListView commentListView;
    int nodeNum = 0;
    int numNonGone = 0;

    //elements for header of listview
    TextView score;
    TextView titleText;
    TextView commentsNum;
    TextView authorNsubreddit;
    ImageView previewImageView;
    ImageView expandedImageView;

    View header;


    public String TAG = "CommentPage";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_page);


        oComments = new ArrayList<CommentData>();
        oCommentAdapter = new CommentListAdapter(this, oComments);

        commentListView = (ListView) findViewById(R.id.commentListView);

        header = getLayoutInflater().inflate(R.layout.listadapter_comment_post_header, commentListView, false);

        //commentListView.addHeaderView(header, null, false);

        //commentListView.setAdapter(oCommentAdapter);


        //getting all the elements
        score = (TextView) header.findViewById(R.id.score);
        titleText = (TextView) header.findViewById(R.id.title);
        commentsNum = (TextView) header.findViewById(R.id.num_comments);
        authorNsubreddit = (TextView) header.findViewById(R.id.authorNsubreddit);
        previewImageView = (ImageView) header.findViewById(R.id.previewImage);
        //final ImageView expandedImageView = (ImageView) header.findViewById(R.id.expandedImage);
        expandedImageView = (ImageView) header.findViewById(R.id.expandedImage);


        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");


        url = "https://www.reddit.com" + url + ".json?raw_json=1";



        new asyncGET().execute(url);

    }



    //get json data from server
    class asyncGET extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        String URL = "";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();
                //params.put("name", args[1]);
                //params.put("password", args[2]);
                //input url
                URL = args[0];

                Log.d("request", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        URL, "GET", params);

                if (json != null) {
                    Log.d("JSON result", json.toString());

                    return json;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(JSONObject json) {
            if (json != null) {
                //Toast.makeText(MainPage.this, json.toString(), Toast.LENGTH_LONG).show();

                try {
                    JSONArray postNcomment = json.getJSONArray("CommentInfo");

                    JSONObject jsonPostData = postNcomment.getJSONObject(0).getJSONObject("data").getJSONArray("children").getJSONObject(0).getJSONObject("data");
                    JSONArray jsonComments = postNcomment.getJSONObject(1).getJSONObject("data").getJSONArray("children");

                    //parse the JSON object for post into PostData object
                    oPostData = ParserHelper.parsePostData(jsonPostData);

                    commentListView.addHeaderView(header, null, false);
                    commentListView.setAdapter(oCommentAdapter);


                    GeneralHelper.setPostDataToView(oPostData,score, titleText, commentsNum, authorNsubreddit, previewImageView, expandedImageView);

                    /*
                    //====================setting all the elements=======================

                    //title and domain
                    String titleNdomain = oPostData.getTitle() + " (" + oPostData.getDomain() + ")";
                    SpannableString spanString =  new SpannableString(titleNdomain);
                    spanString.setSpan(new RelativeSizeSpan(0.75f), titleNdomain.length() - (oPostData.getDomain().length() + 2),titleNdomain.length(), 0); // set size
                    spanString.setSpan(new ForegroundColorSpan(Color.GRAY), titleNdomain.length() - (oPostData.getDomain().length() + 2), titleNdomain.length(), 0);// set color
                    titleText.setText(spanString);
                    //number of comments
                    //commentsNum.setText(Integer.toString(oPostData.getNum_comments()) + " comments");
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
                            //new PostListAdapter.previewImageTask(previewImageView,oPostData).execute(oPostData.getPreviewImagesLowReso().url);
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

                        double heightD = postItem.getExpandedImage().getHeight()*(double)screenWidth/(double)postItem.getExpandedImage().getWidth();
                        expandedImageView.setImageBitmap(Bitmap.createScaledBitmap(postItem.getExpandedImage(), screenWidth, (int) heightD, false));

                    }
                    */



                    //=============BEGIN parsing data for comments========================

                    nodeNum = 0;
                    parseComments(jsonComments, 0);

                    //=============END parsing data for comments========================


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }


    /**
     *
     * Takes a json array and parse the current level of arrays into the CommentData data structure
     * Then calls itself recursively to move on to the next level of json array
     *
     * @param jsonComments
     * @param depth
     * @return an arraylist of commentData
     */
    public ArrayList<CommentData> parseComments(JSONArray jsonComments, int depth) {
        ArrayList<CommentData> replies = new ArrayList<CommentData>();;

        try{
            //go through all the replies
            //or in 0 depth case, go through all the top level comments
            int commentNum = jsonComments.length();
            for(int i = 0; i < commentNum; i++)
            {
                JSONObject jsonComment = jsonComments.getJSONObject(i);
                String kind = jsonComment.getString("kind");
                JSONObject commentData = jsonComment.getJSONObject("data");

                if(kind.equals("t1")) {
                    //get information
                    String cid = commentData.getString("id");
                    String parentId = commentData.getString("parent_id");
                    String content = commentData.getString("body");
                    String author = commentData.getString("author");
                    int score = commentData.getInt("score");
                    long timeCreated = commentData.getInt("created_utc");

                    nodeNum++;
                    //creat the new comment object
                    CommentData newReply = new CommentData(nodeNum, kind, cid, parentId, content, author, score, timeCreated, depth);

                    //if depth 0, add the comment to the overall list of comments
                    if(depth == 0) {
                        oComments.add(newReply);
                    }
                    else {
                        replies.add(newReply);
                        oCommentAdapter.add(newReply);
                    }
                    //oCommentAdapter.add(newReply);

                    JSONArray jsonReplies = null;
                    //if json has replies and the reply is not empty,
                    if((commentData.has("replies") == true)) {
                        if(!commentData.get("replies").equals("")) {
                            //set json reply info
                            jsonReplies = commentData.getJSONObject("replies").getJSONObject("data").getJSONArray("children");

                        }
                    }

                    //if there are more replies.
                    if(jsonReplies != null) {
                        //recursively call with newly created comment and its replies info
                        ArrayList<CommentData> commentReplies = parseComments(jsonReplies, depth + 1);
                        //set the replies. else there is no replies for this object.
                        newReply.setReplies(commentReplies);
                    }

                }

            }

            oCommentAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return replies;


    }


    /**
     * Given a nodeNum, find the CommentData object and then hide it.
     *
     * @param nodeNum
     */
    public void hideComment(int nodeNum)
    {

        Log.d(TAG, "hide comment with num " + nodeNum);
        CommentData temp = findComment(oComments, nodeNum);
        if(temp!=null) {
            temp.hideComment();
        } else {
            Log.w(TAG, "no comment with nodeNum " + nodeNum + " found");
        }

        oCommentAdapter.notifyDataSetChanged();
    }

    /**
     * Given a nodeNum, find the CommentData object
     * Then change the comment to not hidden and show all the child comments
     * Add all the child comments to the list adapter object
     *
     * @param nodeNum the nodeNum of the comment being shown
     * @param position the position of the comment being shown
     */
    public void showComment(int nodeNum, int position)
    {

        Log.d(TAG, "show comment with num " + nodeNum);

        //find and change the info in the oComments array
        CommentData temp = findComment(oComments, nodeNum);
        if(temp!=null) {
            //temp.showComment();
            insertListAdapterElements(temp, position);
        } else {
            Log.w(TAG, "no comment with nodeNum " + nodeNum + " found");
        }

        //find and change the info in list adapter object

        oCommentAdapter.notifyDataSetChanged();
    }

    public void insertListAdapterElements(CommentData oComment, int position) {

        oComment.setHidden(false);
        Log.d(TAG, "comment shown, nodeNum: " + oComment.getNodeNum() + ", cid: " + oComment.getCid());
        if(oComment.getReplies() != null) {
            for(CommentData object: oComment.getReplies()) {
                position++;
                //for all the child comments, set to show
                object.setGone(false);
                oCommentAdapter.insert(object, position);
                //do the same for all the child comments
                position = insertListAdapterElementsHelper(object, position);
            }
        }

    }

    public int insertListAdapterElementsHelper(CommentData oComment, int position) {

        //if the comment is hidden, no need to show the child comments
        if(!oComment.getHidden()){
            if(oComment.getReplies() != null) {
                for(CommentData object: oComment.getReplies()) {
                    //if the current comment being manipulated (and not hidden) has child comments, show them
                    position++;
                    object.setGone(false);
                    oCommentAdapter.insert(object,position);
                    position = insertListAdapterElementsHelper(object, position);

                }
            }
        }

        return position;
    }


    /**
     * Given a nodeNum, find and return the object it belongs to recursively
     *
     * @param mComments arraylist of all the comments to find
     * @param nodeNum the nodeNum of the comment that need to be found
     * @return the CommentData object found. if not found, return null
     */
    public CommentData findComment(ArrayList<CommentData> mComments, int nodeNum) {
        int cLen = mComments.size();
        int j = 1;

        for(int i = 0; i < cLen; i++){

            //reached last comment in the arraylist of comments or nodeNum is before the next comment
            if(j >= cLen || mComments.get(j).getNodeNum() > nodeNum) {
                //if this comment is the one, return
                if(mComments.get(i).getNodeNum() == nodeNum) {
                    return mComments.get(i);
                } else {
                    //go to next level to check for num
                    return findComment(mComments.get(i).getReplies(), nodeNum);
                }
            }

            j++;
        }

        return null;

    }

    /**
     * Given a list of CommentData objects, remove them from the list adapter object.
     *
     * @param marker A list a comments that needs to be removed from the list adapter object
     */
    public void removeListAdapterElements(ArrayList<CommentData> marker) {
        if(marker!= null) {
            for(CommentData object: marker) {
                Log.d(TAG,"removed comment author " + object.getAuthor() + " with cid " + object.getCid());
                oCommentAdapter.remove(object);
            }
            oCommentAdapter.notifyDataSetChanged();
        }
    }



}


