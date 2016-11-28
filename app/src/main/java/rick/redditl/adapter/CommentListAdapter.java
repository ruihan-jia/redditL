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

        final CommentData oComment = getItem(position);

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
        //gone deprecated. since gone comments are removed from listadapter object
        if(!oComment.getGone()) {
            if(oComment.getHidden() == false) {

                //check type
                if(oComment.getKind().equals("t1")){

                    showBTN.setVisibility(View.GONE);

                    //display information
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
                //set various visibilities
                hideBTN.setVisibility(View.GONE);
                showBTN.setVisibility(View.VISIBLE);
                layoutUtilLL.setVisibility(LinearLayout.GONE);
                bodyTV.setVisibility(View.GONE);

                //display information
                android.view.ViewGroup.LayoutParams layoutParams = depthDummy.getLayoutParams();
                layoutParams.width = oComment.getDepth()*20;
                depthDummy.setLayoutParams(layoutParams);
                authorTV.setText(oComment.getAuthor());
                SpannableString spanString =  new SpannableString(oComment.getScore() + " points " + TimeHelper.timeSincePost(oComment.getTimeCreated()) + " ago");
                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, Integer.toString(oComment.getScore()).length() + 7, 0); // set bold
                scoreNtimeTV.setText(spanString);


            }
        } else {
            //should no longer enter here
            layoutMainLL.setVisibility(LinearLayout.GONE);
            Log.w(TAG, "comment is gone, depth " + oComment.getDepth());
        }


        final int position1 = position;

        // When user clicks on the hide button, call the hideComment method from CommentPage.
        hideBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Onclick hide position " + position1 + ". nodenum is " + oComment.getNodeNum());

                //change the data in CommentData arraylist
                ((CommentPage)mContext).hideComment(oComment.getNodeNum());

                //remove the data from the list adapter object
                ((CommentPage)mContext).removeListAdapterElements(findChildCommentsPos(position));


            }
        });


        // When user clicks on the show button,
        showBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Onclick show position " + position1 + ". nodenum is " + oComment.getNodeNum());
                //show the comment
                ((CommentPage)mContext).showComment(oComment.getNodeNum(), position1);
            }
        });



        return convertView;
    }

    /**
     * Given a position in the list adapter, find all the child comment positions
     *
     * @param position the manipulated comment's position in the list adapter object
     * @return ArrayList<CommentData> A list of comments that needs to be removed
     */
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
