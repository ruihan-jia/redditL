package rick.redditl.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import rick.redditl.adapter.CommentListAdapter;
import rick.redditl.model.CommentData;
import rick.redditl.helper.JSONParser;
import rick.redditl.model.PostData;
import rick.redditl.R;
import rick.redditl.model.PreviewImageData;

public class CommentPage extends AppCompatActivity {

    public PostData oPostData;
    public ArrayList<CommentData> oComments;
    CommentListAdapter oCommentAdapter;
    ListView commentListView;
    int numNonGone = 0;

    public String TAG = "CommentPage";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_page);


        oComments = new ArrayList<CommentData>();
        oCommentAdapter = new CommentListAdapter(this, oComments);

        commentListView = (ListView) findViewById(R.id.commentListView);
        commentListView.setAdapter(oCommentAdapter);


        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");


        url = "https://www.reddit.com" + url + ".json";



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


                    //=============BEGIN parsing data for post========================
                    //getting all the post data
                    String title = jsonPostData.getString("title");
                    String subreddit = jsonPostData.getString("subreddit");
                    String author = jsonPostData.getString("author");
                    int score = jsonPostData.getInt("score");
                    int num_comments = jsonPostData.getInt("num_comments");
                    String permalink = jsonPostData.getString("permalink");
                    String url = jsonPostData.getString("url");
                    long timeCreated = jsonPostData.getInt("created_utc");
                    Boolean isSelf = jsonPostData.getBoolean("is_self");
                    String selfText = jsonPostData.getString("selftext");
                    String domain = jsonPostData.getString("domain");
                    //creating the actual item in the list
                    oPostData = new PostData(title, subreddit, author, score,
                            num_comments, permalink, url, timeCreated, isSelf, selfText, domain);

                    //if has preview
                    if (jsonPostData.has("preview")) {
                        //getting preview images for the post
                        JSONObject previewData = jsonPostData.getJSONObject("preview").getJSONArray("images").getJSONObject(0);
                        JSONObject previewSource = previewData.getJSONObject("source");
                        PreviewImageData tempImage = new PreviewImageData((String) previewSource.getString("url"),
                                (int) previewSource.getInt("width"), (int) previewSource.getInt("height"));
                        //setting preview images for the item in the list
                        oPostData.setPreviewSource(tempImage);
                        //getting preview image resolutions
                        JSONArray previewResolutions = previewData.getJSONArray("resolutions");
                        int resolutionNum = previewResolutions.length();
                        PreviewImageData tempImages[] = new PreviewImageData[resolutionNum];
                        for (int j = 0; j < resolutionNum; j++) {
                            JSONObject imageResolutionData = previewResolutions.getJSONObject(j);
                            tempImages[j] = new PreviewImageData((String) imageResolutionData.getString("url"),
                                    (int) imageResolutionData.getInt("width"), (int) imageResolutionData.getInt("height"));
                            //Log.w(TAG,"loop is " + j + " with resolution url " + imageResolutionData.getString("url"));
                        }
                        oPostData.setPreviewImagesRes(tempImages);
                    }

                    //=============END parsing data for post========================


                    //=============BEGIN parsing data for comments========================

                    Log.w(TAG,"parsing comment data");

                    parseComments(jsonComments, 0);

                    //=============END parsing data for comments========================


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }


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

                    //creat the new comment object
                    CommentData newReply = new CommentData(kind, cid, parentId, content, author, score, timeCreated, depth);

                    //if depth 0, add the comment to the overall list of comments
                    if(depth == 0) {

                        oComments.add(newReply);

                    }
                    else {
                        replies.add(newReply);
                        oCommentAdapter.add(newReply);
                    }


                    //oCommentAdapter.add(newReply);

                    //Log.w(TAG,"comment id is: " + cid);

                    JSONArray jsonReplies = null;
                    //if json has replies and the reply is not empty,
                    if((commentData.has("replies") == true)) {
                        if(!commentData.get("replies").equals("")) {
                            //set json reply info
                            jsonReplies = commentData.getJSONObject("replies").getJSONObject("data").getJSONArray("children");
                            //Log.w(TAG,"json comment replied");

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

            findNumNonGone();
            oCommentAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return replies;


    }

    /*
     * Takes a chain of cids and the depth of comment
     * calls findComment method to find the object
     * then call the object's hideComment method to set its parameters to hidden
     * Afterwards, notify data has changed so that the list is updated
     *
     * @param cidChain  an arraylist of cids
     */
    public void hideComment(ArrayList<String> cidChain, int depthIn)
    {

        findComment(oComments, cidChain, depthIn + 1).hideComment();

        findNumNonGone();
        oCommentAdapter.notifyDataSetChanged();
    }

    public void hideComment(String cidIn)
    {

        Log.w(TAG, "hide comment with cid " + cidIn);
        CommentData temp = findComment(oComments, cidIn);
        if(temp!=null) {
            temp.hideComment();
        } else {
            Log.w(TAG, "no comment with cid " + cidIn + " found");
        }


        oCommentAdapter.notifyDataSetChanged();
    }


    //find the specified comment recursively with the helper of cid chain
    public CommentData findComment(ArrayList<CommentData> mComments, ArrayList<String> cidChain, int depthIn)
    {
        Log.w(TAG, "depth is " + depthIn + ". chain length is " + cidChain.size());

        for(CommentData object: mComments) {
            Log.w(TAG,"cid comp " + object.getCid() + " == " + cidChain.get(depthIn - 1));
            if(object.getCid() == cidChain.get(depthIn - 1)) {

                if(depthIn == 1)
                    return object;
                else
                    return findComment(object.getReplies(), cidChain, depthIn - 1);
            }

        }
        return null;

    }

    public CommentData findComment(ArrayList<CommentData> mComments, String cidIn) {
        for(CommentData object: mComments) {
            Log.w(TAG,"comp cid " + object.getCid() + " with author " + object.getAuthor());
            if(object.getCid() == cidIn) {
                return object;
            }
            else {
                if(object.getReplies()!=null) {
                    CommentData temp = findComment(object.getReplies(), cidIn);
                    if(temp != null) {
                        return temp;
                    }
                }
            }

        }
        return null;
    }

    public void removeListAdapterElements(ArrayList<CommentData> marker) {
        if(marker!= null) {
            for(CommentData object: marker) {
                Log.w(TAG,"removed comment author " + object.getAuthor() + " with cid " + object.getCid());
                oCommentAdapter.remove(object);
            }
            oCommentAdapter.notifyDataSetChanged();
        }
    }


    public void findNumNonGone() {
        numNonGone = 0;
        findNumNonGoneHelper(oComments);
    }

    public int getNumNonGone() {
        return numNonGone;
    }

    public void findNumNonGoneHelper(ArrayList<CommentData> mComments) {
        for(CommentData object: mComments) {
            if(!object.getGone())
                numNonGone++;
            if(object.getReplies()!=null) {
                findNumNonGoneHelper(object.getReplies());
            }
        }
    }


}


