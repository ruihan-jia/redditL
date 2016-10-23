package rick.redditl.model;
import android.util.Log;

import java.util.ArrayList;

import rick.redditl.adapter.CommentListAdapter;

/**
 * Created by Rick on 2016-10-02.
 */
public class CommentData {

    public String TAG = "CommentData";

    private String kind; //could be t1, more or listing. listing does nothing
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

    public CommentData(String kindIn, String cidIn, String parentIdIn, String contentIn, String authorIn,
                       int scoreIn,long timeCreatedIn,int depthIn) {

        kind = kindIn;
        cid = cidIn;
        parentId = parentIdIn;
        content = contentIn;
        author = authorIn;
        score = scoreIn;
        timeCreated = timeCreatedIn;
        depth = depthIn;

        hidden = false;
    }

    public CommentData(String kindIn, String cidIn, String parentIdIn) {
        kind = kindIn;
        cid = cidIn;
        parentId = parentIdIn;

        hidden = false;
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

    public void hideComment() {
        hidden = true;
        Log.w(TAG, "comment hidden");
        if(replies != null) {
            for(CommentData object: replies) {
                hideCommentHelper(object);
            }
        }
    }

    public void hideCommentHelper (CommentData input) {
        input.gone = true;
        Log.w(TAG, "comment gone");
        if(input.replies != null) {
            for(CommentData object: input.replies) {
                hideCommentHelper(object);
            }
        }
    }



}
