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

        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final CommentData oComment = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listadapter_comment_view, parent, false);
        }


        //getting all the elements
        ImageView depthDummy = (ImageView) convertView.findViewById(R.id.depthDummy);
        LinearLayout layoutDepthLL = (LinearLayout) convertView.findViewById(R.id.layoutDepth);
        Button upvoteBTN = (Button) convertView.findViewById(R.id.upvote);
        Button downvoteBTN = (Button) convertView.findViewById(R.id.downvote);
        Button hideBTN = (Button) convertView.findViewById(R.id.hide);
        TextView authorTV = (TextView) convertView.findViewById(R.id.author);
        TextView scoreNtimeTV = (TextView) convertView.findViewById(R.id.scoreNtime);
        TextView bodyTV = (TextView) convertView.findViewById(R.id.body);
        LinearLayout layoutUtilLL = (LinearLayout) convertView.findViewById(R.id.layoutUtility);
        Button permalinkBTN = (Button) convertView.findViewById(R.id.permalink);
        Button parentBTN = (Button) convertView.findViewById(R.id.parent);




        //====================setting all the elements=======================

        layoutDepthLL.setVisibility(LinearLayout.VISIBLE);
        layoutUtilLL.setVisibility(LinearLayout.VISIBLE);
        bodyTV.setVisibility(View.VISIBLE);

        //check if hidden or gone
        if(!oComment.getGone()) {
            if(oComment.getHidden() == false) {

                //check type
                if(oComment.getKind().equals("t1")){


                    android.view.ViewGroup.LayoutParams layoutParams = depthDummy.getLayoutParams();
                    layoutParams.width = oComment.getDepth()*15;
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
                layoutUtilLL.setVisibility(LinearLayout.GONE);
                bodyTV.setVisibility(View.GONE);

            }
        } else {
            layoutDepthLL.setVisibility(LinearLayout.GONE);
        }



        hideBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "test position " + position);
                int curDepth = oComment.getDepth();
                ArrayList<String> marker = new ArrayList<String>();
                marker.add(oComment.getCid());
                int i = position - 1;

                while(curDepth != 0) {
                    CommentData temp = getItem(i);

                    if(temp.getDepth() < curDepth) {
                        curDepth = temp.getDepth();
                        marker.add(temp.getCid());
                    }

                    i--;
                }

                ((CommentPage)mContext).hideComment(marker, oComment.getDepth());

            }
        });



        return convertView;
    }



}
