package rick.redditl.model;
import android.util.Log;

import java.util.ArrayList;

import rick.redditl.adapter.CommentListAdapter;

/**
 * Created by Rick on 2016-10-02.
 *
 * Comment data will be saved in a tree structure
 * Each node can have a number of sub-nodes stored in the replies variable
 * Each node has a number to help with ease of access
 */
public class CommentData {

    public String TAG = "CommentData";

    private int nodeNum;    //gives each comment a number to keep track in the tree
    private String kind;     //could be t1, more or listing. listing does nothing
    //common
    private String cid;
    private String parentId;
    //utilities
    private int depth;
    private boolean hidden;
    private boolean gone;

    //t1
    private String content;
    private String author;
    private int score;
    private long timeCreated;
    private ArrayList<CommentData> replies;

    //more
    private int count;




    public CommentData() {

    }

    public CommentData(int nodeNumIn, String kindIn, String cidIn, String parentIdIn, String contentIn, String authorIn,
                       int scoreIn,long timeCreatedIn,int depthIn) {

        nodeNum = nodeNumIn;
        kind = kindIn;
        cid = cidIn;
        parentId = parentIdIn;
        content = contentIn;
        author = authorIn;
        score = scoreIn;
        timeCreated = timeCreatedIn;
        depth = depthIn;

        hidden = false;

        Log.d(TAG, "comment created, nodeNum is " + nodeNumIn + ", cid is " + cid +
                ", author is " + author + ", depth is " + depth);
    }

    public CommentData(int nodeNumIn, String kindIn, String cidIn, String parentIdIn) {
        nodeNum = nodeNumIn;
        kind = kindIn;
        cid = cidIn;
        parentId = parentIdIn;

        hidden = false;
    }

    public int getNodeNum() {
        return nodeNum;
    }

    public String getKind() {
        return kind;
    }

    public String getCid() {
        return cid;
    }

    public String getParentId() {
        return parentId;
    }

    public int getDepth() {
        return depth;
    }

    public boolean getHidden() {
        return hidden;
    }

    public boolean getGone() {
        return gone;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public int getScore() {
        return score;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public int getCount() {
        return count;
    }

    public ArrayList<CommentData> getReplies() {
        return replies;
    }

    public void setReplies(ArrayList<CommentData> repliesIn) {
        replies = repliesIn;
    }

    /**
     * Set the comment to hidden, and set all the comments below it to gone
     */
    public void hideComment() {
        hidden = true;
        Log.d(TAG, "comment hidden, nodeNum: " + nodeNum + ", cid: " + cid);
        if(replies != null) {
            for(CommentData object: replies) {
                hideCommentHelper(object);
            }
        }
    }

    /**
     * Helper function, set the comment and all its sub comments to gone
     *
     * @param input
     */
    public void hideCommentHelper (CommentData input) {
        input.gone = true;
        Log.d(TAG, "comment gone, nodeNum: " + nodeNum + ", cid: " + cid);
        if(input.replies != null) {
            for(CommentData object: input.replies) {
                hideCommentHelper(object);
            }
        }
    }

    public void showComment() {
        hidden = false;
        Log.d(TAG, "comment shown, nodeNum: " + nodeNum + ", cid: " + cid);
        if(replies != null) {
            for(CommentData object: replies) {
                if(object.getGone())
                showCommentHelper(object);
            }
        }
    }

    public void showCommentHelper (CommentData input) {
        if(input.gone == true)
        input.gone = true;
        //Log.d(TAG, "comment gone, nodeNum: " + nodeNum + ", cid: " + cid);
        if(input.replies != null) {
            for(CommentData object: input.replies) {
                showCommentHelper(object);
            }
        }
    }


}
