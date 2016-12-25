package rick.redditl.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import rick.redditl.adapters.CommentListAdapter;
import rick.redditl.helper.PostDataParseHelper;
import rick.redditl.helper.PostHelper;
import rick.redditl.models.CommentData;
import rick.redditl.network.JSONParser;
import rick.redditl.models.PostData;
import rick.redditl.R;

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
    Button commentsNum;
    TextView commentsNum1;
    TextView authorNsubreddit;
    ImageView previewImageView;
    ImageView expandedImageView;

    View header;

    Context context;


    public String TAG = "CommentPage";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_page);

        context = this;


        oComments = new ArrayList<CommentData>();
        oCommentAdapter = new CommentListAdapter(this, oComments);

        commentListView = (ListView) findViewById(R.id.commentListView);

        header = getLayoutInflater().inflate(R.layout.listadapter_comment_post_header, commentListView, false);

        //commentListView.addHeaderView(header, null, false);

        //commentListView.setAdapter(oCommentAdapter);


        //getting all the elements
        score = (TextView) header.findViewById(R.id.score);
        titleText = (TextView) header.findViewById(R.id.title);
        commentsNum = (Button) header.findViewById(R.id.num_comments);
        commentsNum1 = (TextView) header.findViewById(R.id.num_comments1);
        authorNsubreddit = (TextView) header.findViewById(R.id.authorNsubreddit);
        previewImageView = (ImageView) header.findViewById(R.id.previewImage);
        //final ImageView expandedImageView = (ImageView) header.findViewById(R.id.expandedImage);
        expandedImageView = (ImageView) header.findViewById(R.id.expandedImage);


        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");


        url = "https://www.reddit.com" + url + ".json?raw_json=1";



        new asyncGET().execute(url);




        //clicking the title text
        titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open webview activity
                //openWeb(oPostData.getUrl());

                PostHelper.openWeb(oPostData.getUrl(), context);



            }
        });
        //clicking the image thumbnail
        previewImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if image is not expanded, expand
                if (oPostData.getImageExpanded() == false) {
                    new PostHelper.checkUrl(expandedImageView,oPostData, context)
                            .execute(oPostData.getUrl());
                } else {
                    //else collpase iamge
                    expandedImageView.setVisibility(View.GONE);
                    oPostData.setImageExpanded(false);
                }

            }
        });
        //clicking expanded image
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //colapse the expanded image
                expandedImageView.setVisibility(View.GONE);
                oPostData.setImageExpanded(false);

            }
        });



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
                    oPostData = PostDataParseHelper.parsePostData(jsonPostData);

                    commentListView.addHeaderView(header, null, false);
                    commentListView.setAdapter(oCommentAdapter);

                    //setting all the elements
                    PostHelper.setPostDataToView(oPostData,score, titleText, commentsNum, authorNsubreddit, previewImageView, expandedImageView);
                    //set textview comment
                    commentsNum1.setText(Integer.toString(oPostData.getNum_comments()) + " comments");


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
                    String contentHtml = commentData.getString("body_html");
                    String author = commentData.getString("author");
                    int score = commentData.getInt("score");
                    long timeCreated = commentData.getInt("created_utc");

                    nodeNum++;
                    //creat the new comment object
                    CommentData newReply = new CommentData(nodeNum, kind, cid, parentId, content, contentHtml, author, score, timeCreated, depth);

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


