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
    int nodeNum = 0;
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
                        }
                        oPostData.setPreviewImagesRes(tempImages);
                    }

                    //=============END parsing data for post========================


                    //=============BEGIN parsing data for comments========================

                    Log.d(TAG,"parsing comment data");

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

            findNumNonGone();
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
     * @param nodeNum
     */
    public void showComment(int nodeNum)
    {

        Log.d(TAG, "show comment with num " + nodeNum);

        CommentData temp = findComment(oComments, nodeNum);
        if(temp!=null) {
            //temp.showComment();
        } else {
            Log.w(TAG, "no comment with nodeNum " + nodeNum + " found");
        }

        oCommentAdapter.notifyDataSetChanged();
    }


    /**
     * Given a nodeNum, find and return the object it belongs to recursively
     *
     * @param mComments
     * @param nodeNum
     * @return
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
     * @param marker
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


