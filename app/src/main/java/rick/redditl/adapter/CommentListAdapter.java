package rick.redditl.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import rick.redditl.activity.CommentPage;
import rick.redditl.model.CommentData;
import rick.redditl.R;
import rick.redditl.helper.TimeHelper;

/**
 * Created by Rick on 2016-10-02.
 * references:http://www.vogella.com/tutorials/AndroidListView/article.html#listview_defaultadapter
 * creating each element in the list
 */
public class CommentListAdapter extends ArrayAdapter<CommentData> {
    private Context mContext;

    //public LayoutInflater inflater;
    String TAG = "CommentListAdapter";

    public CommentListAdapter(Context contextIn, ArrayList<CommentData> CommentDataIn) {
        super(contextIn, 0, CommentDataIn);
        mContext = contextIn;
    }

    /*
     * when each element is initialized, populate it with the correct information
     * also set on click listeners for the buttons
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Log.w(TAG, "getview called, position " + position);

/*
        int i = 0;
        int j = 0;
        while(i<=position && j < getCount()-2) {
            if(!getItem(j).getGone())
                i++;
            if(i<=position)
                j++;
        }
        position = j;
*/
        final CommentData oComment = getItem(position);

        //Log.w(TAG, "after change, position " + position + " author is " + oComment.getAuthor());

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listadapter_comment_view, parent, false);
        }


        //getting all the elements
        LinearLayout layoutMainLL = (LinearLayout) convertView.findViewById(R.id.layoutMain);
        ImageView depthDummy = (ImageView) convertView.findViewById(R.id.depthDummy);
        LinearLayout layoutDepthLL = (LinearLayout) convertView.findViewById(R.id.layoutDepth);
        Button upvoteBTN = (Button) convertView.findViewById(R.id.upvote);
        Button downvoteBTN = (Button) convertView.findViewById(R.id.downvote);
        Button hideBTN = (Button) convertView.findViewById(R.id.hide);
        Button showBTN = (Button) convertView.findViewById(R.id.show);
        TextView authorTV = (TextView) convertView.findViewById(R.id.author);
        TextView scoreNtimeTV = (TextView) convertView.findViewById(R.id.scoreNtime);
        TextView bodyTV = (TextView) convertView.findViewById(R.id.body);
        LinearLayout layoutUtilLL = (LinearLayout) convertView.findViewById(R.id.layoutUtility);
        Button permalinkBTN = (Button) convertView.findViewById(R.id.permalink);
        Button parentBTN = (Button) convertView.findViewById(R.id.parent);




        //====================setting all the elements=======================

        if(layoutMainLL.getVisibility() == LinearLayout.GONE){layoutMainLL.setVisibility(LinearLayout.VISIBLE);}
        layoutDepthLL.setVisibility(LinearLayout.VISIBLE);
        layoutUtilLL.setVisibility(LinearLayout.VISIBLE);
        bodyTV.setVisibility(View.VISIBLE);
        hideBTN.setVisibility(View.VISIBLE);

        //check if hidden or gone
        if(!oComment.getGone()) {
            if(oComment.getHidden() == false) {

                //check type
                if(oComment.getKind().equals("t1")){

                    showBTN.setVisibility(View.GONE);

                    android.view.ViewGroup.LayoutParams layoutParams = depthDummy.getLayoutParams();
                    layoutParams.width = oComment.getDepth()*20;
                    depthDummy.setLayoutParams(layoutParams);

                    Log.w(TAG, "depth is " + oComment.getDepth());

                    authorTV.setText(oComment.getAuthor());

                    SpannableString spanString =  new SpannableString(oComment.getScore() + " points " + TimeHelper.timeSincePost(oComment.getTimeCreated()) + " ago");
                    spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, Integer.toString(oComment.getScore()).length() + 7, 0); // set bold
                    scoreNtimeTV.setText(spanString);

                    bodyTV.setText(oComment.getContent());

                }
            } else {
                //comment is hidden
                Log.w(TAG, "comment is hidden, depth " + oComment.getDepth());
                hideBTN.setVisibility(View.GONE);
                showBTN.setVisibility(View.VISIBLE);
                layoutUtilLL.setVisibility(LinearLayout.GONE);
                bodyTV.setVisibility(View.GONE);

            }
        } else {
            layoutMainLL.setVisibility(LinearLayout.GONE);
            Log.w(TAG, "comment is gone, depth " + oComment.getDepth());
            //convertView = LayoutInflater.from(getContext()).inflate(R.layout.null_item, null);
        }



        final int position1 = position;
        /*
         * When user clicks on the hide button, traverse the list from current position back to top
         * record every parent comment's cid into an array, until it reaches the 0 depth
         * the result is an arraylist of cids from the 0 depth all the way to selected comment
         * with one comment per level.
         * This arraylist is then passed to the hidecomment method along with the comment's depth
         * in the CommentPage method.
         */
        hideBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "test position " + position1);

                /*
                int curDepth = oComment.getDepth();
                ArrayList<String> marker = new ArrayList<String>();
                marker.add(oComment.getCid());
                int i = position1 - 1;

                while(curDepth != 0) {
                    CommentData temp = getItem(i);

                    if(temp.getDepth() < curDepth) {
                        curDepth = temp.getDepth();
                        marker.add(temp.getCid());
                    }

                    i--;
                }

                ((CommentPage)mContext).hideComment(marker, oComment.getDepth());
                */
                ((CommentPage)mContext).hideComment(oComment.getCid());

                ((CommentPage)mContext).removeListAdapterElements(findChildCommentsPos(position));


            }
        });



        return convertView;
    }

    public ArrayList<CommentData> findChildCommentsPos(int position) {
        int posDepth = getItem(position).getDepth();
        ArrayList<CommentData> marker = new ArrayList<CommentData>();
        //marker.add(getItem(position));

        Boolean cont = true;
        int i = position + 1;
        while(i < getCount() - 1 && cont) {
            if(getItem(i).getDepth() > posDepth) {
                marker.add(getItem(i));
                i++;
            }
            else
                cont = false;

        }

        return marker;

    }

/*
    @Override
    public int getCount() {
        return ((CommentPage)mContext).getNumNonGone();
    }
*/



}
