package rick.redditl.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
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
 * the oComments can be used to search for a comment
 * it is needed to perserve the information when comments are hidden
 */

public class CommentPage extends AppCompatActivity {

    public PostData oPostData;
    //this list stores actual data that cannot be deleted
    public ArrayList<CommentData> oComments;
    //this list is used in conjecture with the commentlistadapter
    public ArrayList<CommentData> commentAdapterList;
    CommentListAdapter oCommentAdapter;
    ListView commentListView;
    //int nodeNum = 0;
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

    int loadMoreCommentPosHelper = 0;
    int moreCommentsCounter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_page);

        context = this;

        oComments = new ArrayList<CommentData>();
        commentAdapterList = new ArrayList<CommentData>();
        oCommentAdapter = new CommentListAdapter(this, commentAdapterList);

        commentListView = (ListView) findViewById(R.id.commentListView);

        header = getLayoutInflater().inflate(R.layout.listadapter_comment_post_header, commentListView, false);

        //getting all the elements for post data
        score = (TextView) header.findViewById(R.id.score);
        titleText = (TextView) header.findViewById(R.id.title);
        commentsNum = (Button) header.findViewById(R.id.num_comments);
        commentsNum1 = (TextView) header.findViewById(R.id.num_comments1);
        authorNsubreddit = (TextView) header.findViewById(R.id.authorNsubreddit);
        previewImageView = (ImageView) header.findViewById(R.id.previewImage);
        expandedImageView = (ImageView) header.findViewById(R.id.expandedImage);


        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");

        url = "https://www.reddit.com" + url + ".json?raw_json=1";

        new asyncGetComments("general", 0, null).execute(url);


        //clicking the title text
        titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    class asyncGetComments extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        String URL = "";

        //general, loadMore
        String mode = "";
        //for loadMore
        int position;
        CommentData loadMoreComment;

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        public asyncGetComments(String modeIn, int positionIn, CommentData loadMoreCommentIn) {
            mode = modeIn;
            position = positionIn;
            loadMoreComment = loadMoreCommentIn;
            Log.d(TAG,"start asyncGet for " + mode);
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
                    if(mode == "general") {
                        //first time loading all the comments
                        JSONArray postNcomment = json.getJSONArray("CommentInfo");

                        JSONObject jsonPostData = postNcomment.getJSONObject(0).getJSONObject("data").getJSONArray("children").getJSONObject(0).getJSONObject("data");
                        JSONArray jsonComments = postNcomment.getJSONObject(1).getJSONObject("data").getJSONArray("children");

                        //parse the JSON object for post into PostData object
                        oPostData = PostDataParseHelper.parsePostData(jsonPostData);

                        commentListView.addHeaderView(header, null, false);
                        commentListView.setAdapter(oCommentAdapter);

                        //setting all the elements
                        PostHelper.setPostDataToView(oPostData, score, titleText, commentsNum, authorNsubreddit, previewImageView, expandedImageView);
                        //set textview comment. unique to comment page
                        commentsNum1.setText(Integer.toString(oPostData.getNum_comments()) + " comments");

                        //parse data for comments
                        //nodeNum = 0;
                        ArrayList<Integer> nodeID = new ArrayList<Integer>();
                        parseComments(jsonComments, 0, nodeID);

                    } else if(mode == "loadMore") {
                        Log.d(TAG,"received load more info");
                        //morechildren api returns a json with flattened structure.
                        //need to find structure by examining parent ids


                        //called to load specific comments
                        JSONObject data = json.getJSONObject("json").getJSONObject("data");
                        JSONArray jsonComments = data.getJSONArray("things");

                        CommentData parentComment = findCommentParent(oComments,loadMoreComment.getNodeID());
                        loadMoreCommentPosHelper = position;
                        ArrayList<Integer> tempNodeID = (ArrayList<Integer>)loadMoreComment.getNodeID().clone();
                        //need to remove the load more comment
                        if(loadMoreComment.getChildren()!= null) {
                            if(loadMoreComment.getChildren().size() < 20){
                                //this should delete the loadmoreComment variable
                                oCommentAdapter.remove(loadMoreComment);
                                parentComment.getReplies().remove(parentComment.getReplies().size() - 1);
                            }
                        } else {
                            parentComment.getReplies().remove(parentComment.getReplies().size()-1);
                        }

                        //parseMoreComments(jsonComments, loadMoreComment.getDepth(), loadMoreComment.getNodeID(), loadMoreComment, parentComment);

                        parseMoreComments(jsonComments,parentComment,tempNodeID);

                        oCommentAdapter.notifyDataSetChanged();

                    }


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
    public ArrayList<CommentData> parseComments(JSONArray jsonComments, int depth, ArrayList<Integer> nodeID) {
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
                    String name = commentData.getString("name");
                    String parentId = commentData.getString("parent_id");
                    String content = commentData.getString("body");
                    String contentHtml = commentData.getString("body_html");
                    String author = commentData.getString("author");
                    int score = commentData.getInt("score");
                    long timeCreated = commentData.getInt("created_utc");

                    //nodeNum++;
                    if(nodeID.size() > depth + 1) {
                        for(int k = 0; k < nodeID.size() - depth + 1; k++) {
                            //Log.d(TAG, "remove last: " + (nodeID.size() - 1) + ". nodeID size is " + nodeID.size());
                            nodeID.remove(nodeID.size() - 1);
                        }
                    }
                    if(nodeID.size() <= depth) {
                        //Log.d(TAG, "add at position " + (nodeID.size()) + " to i: " + i + ". nodeID size is " + nodeID.size());
                        nodeID.add(i);
                    } else if(nodeID.size() == depth + 1) {
                        //Log.d(TAG, "update position " + depth + " to i: " + i + ". nodeID size is " + nodeID.size());
                        nodeID.set(depth, i);
                    } else {
                        //if for some reason the size is still bigger than the depth then remove again
                        Log.d(TAG, "rare case remove last: " + (nodeID.size() - 1) + ". nodeID size is " + nodeID.size());
                        nodeID.remove(nodeID.size() - 1);
                        nodeID.set(depth, i);
                    }
                    //creat the new comment object
                    CommentData newReply = new CommentData(nodeID, kind, cid, name, parentId, content, contentHtml, author, score, timeCreated, depth);


                    //if depth 0, add the comment to the overall list of comments
                    if(depth == 0) {
                        oComments.add(newReply);
                        Log.d(TAG,"Depth 0 reply added with " + newReply.getAuthor());
                    }
                    else {
                        replies.add(newReply);
                    }

                    oCommentAdapter.add(newReply);

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
                        ArrayList<CommentData> commentReplies = parseComments(jsonReplies, depth + 1, nodeID);
                        //set the replies. else there is no replies for this object.
                        newReply.setReplies(commentReplies);
                    }




                }else if(kind.equals("more")) {
                    //get information
                    String cid = commentData.getString("id");
                    String parentId = commentData.getString("parent_id");
                    int count = commentData.getInt("count");

                    ArrayList<String> children = new ArrayList<String>();

                    //Log.d(TAG, "Kind more comment, id is " + cid + ", count is " + count + ", depth is " + depth);

                    if(count > 0) {
                        JSONArray childComments = commentData.getJSONArray("children");
                        int childrenNum = childComments.length();
                        for (int j = 0; j < childrenNum; j++) {
                            children.add(childComments.getString(j));
                            //Log.d(TAG, "child comment is " + childComments.getString(j));
                        }
                    } else
                        children = null;

                    //nodeNum++;
                    if(nodeID.size() > depth + 1) {
                        for(int k = 0; k < nodeID.size() - depth + 1; k++) {
                            //Log.d(TAG, "remove last: " + (nodeID.size() - 1) + ". nodeID size is " + nodeID.size());
                            nodeID.remove(nodeID.size() - 1);
                        }
                    }
                    if(nodeID.size() <= depth) {
                        //Log.d(TAG, "add at position " + (nodeID.size()) + " to i: " + i + ". nodeID size is " + nodeID.size());
                        nodeID.add(i);
                    } else if(nodeID.size() == depth + 1) {
                        //Log.d(TAG, "update position " + depth + " to i: " + i + ". nodeID size is " + nodeID.size());
                        nodeID.set(depth, i);
                    } else {
                        //if for some reason the size is still bigger than the depth then remove again
                        Log.d(TAG, "rare case remove last: " + (nodeID.size() - 1) + ". nodeID size is " + nodeID.size());
                        nodeID.remove(nodeID.size() - 1);
                        nodeID.set(depth, i);
                    }
                    //creat the new comment object
                    CommentData newReply = new CommentData(nodeID, kind, cid, parentId, count, children, depth);

                    //if depth 0, add the comment to the overall list of comments
                    if(depth == 0) {
                        oComments.add(newReply);
                        Log.d(TAG, "Depth 0 reply added with " + newReply.getAuthor());
                    }
                    else {
                        replies.add(newReply);
                    }

                    oCommentAdapter.add(newReply);

                }

            }

            oCommentAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return replies;


    }


    public ArrayList<CommentData> parseMoreComments(JSONArray jsonComments, CommentData parentComment, ArrayList<Integer> baseNodeID) {
        ArrayList<CommentData> replies = new ArrayList<CommentData>();;

        try{

            //go through all the replies
            int commentNum = jsonComments.length();
            for(int i = 0; i < commentNum; i++) {
                JSONObject jsonComment = jsonComments.getJSONObject(i);
                String kind = jsonComment.getString("kind");
                JSONObject commentData = jsonComment.getJSONObject("data");


                if(kind.equals("t1")) {
                    //get information
                    String cid = commentData.getString("id");
                    String name = commentData.getString("name");
                    String parentId = commentData.getString("parent_id");
                    String content = commentData.getString("body");
                    String contentHtml = commentData.getString("body_html");
                    String author = commentData.getString("author");
                    int score = commentData.getInt("score");
                    long timeCreated = commentData.getInt("created_utc");

                    Log.d(TAG,"parsing comment author is " + author + ", body is " + content + ", parentId is " + parentId + ", baseParentID is " + parentComment.getName());

                    CommentData tempParent;

                    CommentData newReply;

                    //if this comment is a comment on the root depth
                    if(parentId.equals(parentComment.getName())) {
                        tempParent = parentComment;

                        //clone parent comment's id
                        ArrayList<Integer> nodeID = (ArrayList<Integer>)parentComment.getNodeID().clone();
                        //add the size of current reply to the end to form the new node id
                        if(parentComment.getReplies()!=null) {
                            nodeID.add(parentComment.getReplies().size());
                        } else {
                            nodeID.add(0);
                        }

                        newReply = new CommentData(nodeID, kind, cid, name, parentId, content, contentHtml, author, score, timeCreated, parentComment.getDepth()+1);
                        parentComment.getReplies().add(newReply);

                    } else {
                        //tempParent = parseMoreCommentsHelper(replies, parentId);
                        CommentData tempParentComment = parseMoreCommentsHelper(replies, parentId);


                        //clone parent comment's id
                        ArrayList<Integer> nodeID = (ArrayList<Integer>)tempParentComment.getNodeID().clone();
                        //add the size of current reply to the end to form the new node id
                        if(tempParentComment.getReplies()!=null) {
                            nodeID.add(tempParentComment.getReplies().size());
                        } else {
                            nodeID.add(0);
                        }

                        newReply = new CommentData(nodeID, kind, cid, name, parentId, content, contentHtml, author, score, timeCreated, tempParentComment.getDepth()+1);
                        tempParentComment.getReplies().add(newReply);

                    }

                    /*
                    //clone parent comment's id
                    ArrayList<Integer> nodeID = (ArrayList<Integer>)tempParent.getNodeID().clone();
                    //add the size of current reply to the end to form the new node id
                    if(tempParent.getReplies()!=null) {
                        nodeID.add(tempParent.getReplies().size());
                    } else {
                        nodeID.add(0);
                    }

                    CommentData newReply = new CommentData(nodeID, kind, cid, parentId, content, contentHtml, author, score, timeCreated, tempParent.getDepth()+1);
                    tempParent.getReplies().add(newReply);
*/

                    oCommentAdapter.insert(newReply, loadMoreCommentPosHelper);
                    loadMoreCommentPosHelper++;



                } else if(kind.equals("more")) {
                    //get information
                    String cid = commentData.getString("id");
                    String parentId = commentData.getString("parent_id");
                    int count = commentData.getInt("count");

                    ArrayList<String> children = new ArrayList<String>();

                    if(count > 0) {
                        JSONArray childComments = commentData.getJSONArray("children");
                        int childrenNum = childComments.length();
                        for (int j = 0; j < childrenNum; j++) {
                            children.add(childComments.getString(j));
                            //Log.d(TAG, "child comment is " + childComments.getString(j));
                        }
                    } else
                        children = null;

                    CommentData tempParent;

                    //if this comment is a comment on the root depth
                    if(parentId == parentComment.getName()) {
                        tempParent = parentComment;
                    } else {
                        tempParent = parseMoreCommentsHelper(replies, parentId);
                    }

                    //clone parent comment's id
                    ArrayList<Integer> nodeID = (ArrayList<Integer>)tempParent.getNodeID().clone();
                    //add the size of current reply to the end to form the new node id
                    if(tempParent.getReplies()!=null) {
                        nodeID.add(tempParent.getReplies().size());
                    } else {
                        nodeID.add(0);
                    }

                    //creat the new comment object
                    CommentData newReply = new CommentData(nodeID, kind, cid, parentId, count, children, tempParent.getDepth()+1);
                    tempParent.getReplies().add(newReply);

                    oCommentAdapter.insert(newReply, loadMoreCommentPosHelper);
                    loadMoreCommentPosHelper++;

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return replies;

    }

    //given list of replies and a target id, find the comment
    public CommentData parseMoreCommentsHelper(ArrayList<CommentData> replies, String targetID) {
        CommentData temp;
        for(CommentData object : replies) {
            if(object.getCid() == targetID)
                return object;
            if(object.getReplies()!=null) {
                temp = parseMoreCommentsHelper(object.getReplies(), targetID);
                if(temp != null)
                    return temp;
            }
        }
        return null;
    }




//==============MANIPULATION OF OCOMMENTS DATA AND ADAPTER===========================



    /**
     * Given a nodeNum, find the CommentData object and then hide it.
     *
     * @param nodeID
     */
    public void hideComment(ArrayList<Integer> nodeID)
    {
        //testMethod(oComments);

        //Log.d(TAG, "hide comment with num " + nodeNum);
        Log.d(TAG, "hide comment with nodeID " + nodeID);
        CommentData temp = findComment(oComments, nodeID);
        if(temp!=null) {
            temp.hideComment();
        } else {
            Log.w(TAG, "no comment with nodeID " + nodeID + " found");
        }

        oCommentAdapter.notifyDataSetChanged();
    }

    /**
     * Given a nodeNum, find the CommentData object
     * Then change the comment to not hidden and show all the child comments
     * Add all the child comments to the list adapter object
     *
     * @param nodeID the nodeID of the comment being shown
     * @param position the position of the comment being shown
     */
    public void showComment(ArrayList<Integer> nodeID, int position)
    {

        //Log.d(TAG, "show comment with num " + nodeNum);
        Log.d(TAG, "show comment with nodeID " + nodeID);

        //find and change the info in the oComments array
        CommentData temp = findComment(oComments, nodeID);
        if(temp!=null) {
            temp.showComment();
            insertListAdapterElements(temp, position);
        } else {
            Log.w(TAG, "no comment with nodeID " + nodeID + " found");
        }

        //find and change the info in list adapter object

        oCommentAdapter.notifyDataSetChanged();
    }



    public void loadMoreComments(ArrayList<Integer> nodeID, ArrayList<String> comments, int position) {
        //construct the url
        //morechild api, api type is json, raw json is requested for html format, link id provided, comments sorted from top, then add the children.
        String url = "https://www.reddit.com/api/morechildren.json?api_type=json&raw_json=1&link_id=" + oPostData.getName() + "&sort=top&children=";

        if(comments.size() < 20) {
            for(int i = 0; i < comments.size(); i++) {
                url += comments.get(i) + ",";

            }
            url = url.substring(0, url.length()-1);
            Log.d(TAG, url);
        } else {
            for(int i = 0; i < 20; i++) {
                url += comments.get(i) + ",";
            }
            url = url.substring(0, url.length()-1);
            Log.d(TAG, url);
        }

        CommentData temp = findComment(oComments, nodeID);
        new asyncGetComments("loadMore", position, temp).execute(url);

    }




    /**
     * Given a nodeNum, find and return the object it belongs to recursively
     *
     * @param mComments arraylist of all the comments to find
     * @param nodeID the nodeID of the comment that need to be found
     * @return the CommentData object found. if not found, return null
     */
    public CommentData findComment(ArrayList<CommentData> mComments, ArrayList<Integer> nodeID) {
        int cLen = mComments.size();
        int j = 1;


        for(int i = 0; i < cLen; i++){
            int depth = mComments.get(i).getDepth();

            Log.d(TAG,"mComment nodeID is " + mComments.get(i).getNodeID() + ", target is " + nodeID + ", depth is " + depth);
            Log.d(TAG,"mComment nodeID at " + depth + " is " + mComments.get(i).getNodeID().get(depth) + ". nodeID at " + depth + " is " + nodeID.get(depth));

            if(mComments.get(i).getNodeID().get(depth) == nodeID.get(depth)) {
                Log.d(TAG, "Should go down this path");
                //if this comment is the one, return
                if(mComments.get(i).getNodeID() == nodeID || mComments.get(i).getDepth() == nodeID.size() - 1) {
                    Log.d(TAG, "This is the one");
                    return mComments.get(i);
                } else {
                    //go to next level to check for num
                    Log.d(TAG, "Check for children");
                    return findComment(mComments.get(i).getReplies(), nodeID);
                }
            }

            /*
            //reached last comment in the arraylist of comments or nodeNum is before the next comment
            if(j >= cLen || mComments.get(j).getNodeNum() > nodeNum) {
                //if this comment is the one, return
                if(mComments.get(i).getNodeNum() == nodeNum) {
                    return mComments.get(i);
                } else {
                    //go to next level to check for num
                    return findComment(mComments.get(i).getReplies(), nodeNum, nodeID);
                }
            }

            j++;
            */
        }

        return null;

    }


    //if base comment, create a dummy parent that includes all the base comments and pass it back
    public CommentData findCommentParent(ArrayList<CommentData> mComments, ArrayList<Integer> nodeID) {
        int cLen = mComments.size();
        //root case
        if(nodeID.size() <= 1) {
            CommentData dummyParent = new CommentData();
            dummyParent.setReplies(mComments);
            return dummyParent;
        }

        for(int i = 0; i < cLen; i++){
            int depth = mComments.get(i).getDepth();

            Log.d(TAG,"mComment nodeID is " + mComments.get(i).getNodeID() + ", target is " + nodeID + ", depth is " + depth);
            Log.d(TAG,"mComment nodeID at " + depth + " is " + mComments.get(i).getNodeID().get(depth) + ". nodeID at " + depth + " is " + nodeID.get(depth));

            if(mComments.get(i).getNodeID().get(depth) == nodeID.get(depth)) {
                Log.d(TAG, "Should go down this path");

                if(depth == nodeID.size()-2){
                    Log.d(TAG, "This is the one");
                    return mComments.get(i);
                } else {
                    //go to next level to check for num
                    Log.d(TAG, "Check for children");
                    return findCommentParent(mComments.get(i).getReplies(), nodeID);
                }

            }
        }

        return null;

    }




//====================LIST ADAPTER METHODS==========================


    public void insertListAdapterElements(CommentData oComment, int position) {

        oComment.setHidden(false);
        Log.d(TAG, "comment shown, nodeID: " + oComment.getNodeID() + ", cid: " + oComment.getCid());
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



    public void testMethod(ArrayList<CommentData> mComments) {
        int cLen = mComments.size();
        for(int i = 0; i < cLen; i++) {
            Log.d(TAG, "mComment nodeID(" + i + ") is " + mComments.get(i).getNodeID() + ", author is " + mComments.get(i).getAuthor() + ", depth is " + mComments.get(i).getDepth() + ", replies are " + mComments.get(i).getReplies());
            if(mComments.get(i).getReplies()!=null)
                testMethod(mComments.get(i).getReplies());
        }
        return;
    }



}


