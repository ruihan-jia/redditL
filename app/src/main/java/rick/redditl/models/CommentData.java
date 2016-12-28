package rick.redditl.models;
import android.util.Log;

import java.util.ArrayList;

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
    private String contentHtml;
    private String author;
    private int score;
    private long timeCreated;
    private ArrayList<CommentData> replies;

    //more
    private int count;




    public CommentData() {

    }

    public CommentData(int nodeNumIn, String kindIn, String cidIn, String parentIdIn, String contentIn, String contentHtmlIn, String authorIn,
                       int scoreIn,long timeCreatedIn,int depthIn) {

        nodeNum = nodeNumIn;
        kind = kindIn;
        cid = cidIn;
        parentId = parentIdIn;
        content = contentIn;
        contentHtml = contentHtmlIn;
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

    public String getContentHtml() {
        return contentHtml;
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

    public void setHidden(Boolean hiddenIn) {
        hidden = hiddenIn;
    }

    public void setGone(Boolean goneIn) {
        gone = goneIn;
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

    /**
     * Set the current comment to non-hidden, and show all the child comments
     */
    public void showComment() {
        hidden = false;
        Log.d(TAG, "comment shown, nodeNum: " + nodeNum + ", cid: " + cid);
        if(replies != null) {
            for(CommentData object: replies) {
                //for all the child comments, set to show
                object.setGone(false);
                //do the same for all the child comments
                showCommentHelper(object);
            }
        }
    }

    /**
     * Takes a comment, if it is not hidden, then show all of its child comments
     * Then calls itself to do the same for its child comments
     *
     * @param input the comment that needs to be manipulated
     */
    public void showCommentHelper (CommentData input) {
        //Log.d(TAG, "comment gone, nodeNum: " + nodeNum + ", cid: " + cid);
        //if the comment is hidden, no need to show the child comments
        if(!input.getHidden()){
            if(input.replies != null) {
                for(CommentData object: input.replies) {
                    //if the current comment being manipulated (and not hidden) has child comments, show them
                    object.setGone(false);
                    showCommentHelper(object);
                }
            }
        }
    }


}