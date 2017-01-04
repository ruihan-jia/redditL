package rick.redditl.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import rick.redditl.activities.CommentPage;
import rick.redditl.helper.CommentHelper;
import rick.redditl.helper.PostDataParseHelper;
import rick.redditl.helper.PostHelper;
import rick.redditl.models.CommentData;
import rick.redditl.R;
import rick.redditl.helper.TimeHelper;
import rick.redditl.network.JSONParser;

/**
 * Created by Rick on 2016-10-02.
 * references:http://www.vogella.com/tutorials/AndroidListView/article.html#listview_defaultadapter
 * creating each element in the list
 */
public class CommentListAdapter extends ArrayAdapter<CommentData> {
    private Context mContext;
    private CommentPage oCommentPage;

    //public LayoutInflater inflater;
    String TAG = "CommentListAdapter";

    public CommentListAdapter(Context contextIn, ArrayList<CommentData> CommentDataIn) {
        super(contextIn, 0, CommentDataIn);
        mContext = contextIn;
        oCommentPage = new CommentPage();

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
        LinearLayout kindT1LL = (LinearLayout) convertView.findViewById(R.id.kindK1);
        LinearLayout kindMoreLL = (LinearLayout) convertView.findViewById(R.id.kindMore);
        Button loadMoreBTN = (Button) convertView.findViewById(R.id.loadMore);
        Button continueThreadBTN = (Button) convertView.findViewById(R.id.continueThread);




        //====================setting all the elements to default=======================

        if(layoutMainLL.getVisibility() == LinearLayout.GONE){layoutMainLL.setVisibility(LinearLayout.VISIBLE);}
        layoutDepthLL.setVisibility(LinearLayout.VISIBLE);
        layoutUtilLL.setVisibility(LinearLayout.VISIBLE);
        kindT1LL.setVisibility(LinearLayout.VISIBLE);
        bodyTV.setVisibility(View.VISIBLE);
        hideBTN.setVisibility(View.VISIBLE);
        kindMoreLL.setVisibility(LinearLayout.GONE);

        //check if hidden or gone
        //gone deprecated. since gone comments are removed from listadapter object
        if(!oComment.getGone()) {
            if(oComment.getHidden() == false) {

                //check type
                if(oComment.getKind().equals("t1")){

                    showBTN.setVisibility(View.GONE);

                    //set depth to the right
                    android.view.ViewGroup.LayoutParams layoutParams = depthDummy.getLayoutParams();
                    layoutParams.width = oComment.getDepth()*20;
                    depthDummy.setLayoutParams(layoutParams);

                    //display information
                    authorTV.setText(oComment.getAuthor());

                    SpannableString spanString =  new SpannableString(oComment.getScore() + " points " + TimeHelper.timeSincePost(oComment.getTimeCreated()) + " ago");
                    spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, Integer.toString(oComment.getScore()).length() + 7, 0); // set bold
                    scoreNtimeTV.setText(spanString);

                    //bodyTV.setText(oComment.getContent());
                    setTextViewHTML(bodyTV,oComment.getContentHtml());

                } else if (oComment.getKind().equals("more")) {

                    //set various visibilities
                    kindT1LL.setVisibility(LinearLayout.GONE);
                    layoutUtilLL.setVisibility(LinearLayout.GONE);
                    bodyTV.setVisibility(View.GONE);

                    kindMoreLL.setVisibility(View.VISIBLE);

                    if(oComment.getCount() > 0) {
                        loadMoreBTN.setVisibility(View.VISIBLE);
                        continueThreadBTN.setVisibility(View.GONE);
                    } else {
                        loadMoreBTN.setVisibility(View.GONE);
                        continueThreadBTN.setVisibility(View.VISIBLE);
                    }

                    //display information
                    android.view.ViewGroup.LayoutParams layoutParams = depthDummy.getLayoutParams();
                    layoutParams.width = oComment.getDepth()*20;
                    depthDummy.setLayoutParams(layoutParams);

                    Log.w(TAG, "depth is " + oComment.getDepth());

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
                //Log.d(TAG, "Onclick hide position " + position1 + ". nodenum is " + oComment.getNodeNum());
                Log.d(TAG, "Onclick hide position " + position1 + ". nodeID is " + oComment.getNodeID());

                //change the data in CommentData arraylist
                ((CommentPage)mContext).hideComment(oComment.getNodeNum(), oComment.getNodeID());

                //remove the data from the list adapter object
                ((CommentPage)mContext).removeListAdapterElements(findChildCommentsPos(position));


            }
        });


        // When user clicks on the show button,
        showBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(TAG, "Onclick show position " + position1 + ". nodenum is " + oComment.getNodeNum());
                Log.d(TAG, "Onclick show position " + position1 + ". nodeID is " + oComment.getNodeID());
                //show the comment
                ((CommentPage)mContext).showComment(oComment.getNodeNum(), oComment.getNodeID(), position1);
            }
        });


        // When user clicks on the load more comments button,
        loadMoreBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Onclick load more comments position " + position1 + ". nodenum is " + oComment.getNodeNum());
                //show the comment
                ((CommentPage)mContext).loadMoreComments(oComment.getChildren(), position);
            }
        });

        return convertView;
    }






    protected void makeLinkClickable(final SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        final CharSequence s = strBuilder.subSequence(start, end);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                // Do something with span.getURL() to handle the link click...

                Log.d(TAG, "text is " + s + ", content is " + span.getURL());
                //oCommentPage.openTest(mContext, span.getURL(), s);
                openWebDialog(mContext, span.getURL(), s);
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    //http://stackoverflow.com/questions/12418279/android-textview-with-clickable-links-how-to-capture-clicks
    protected void setTextViewHTML(TextView text, String html)
    {
        //html = "<div class=\"md\"><p>[test](google.com)\n<a href=\"https://google.com\">https://google.com</a>\n<a href=\"http://i.imgur.com/AqLvXJh.gifv\">to test</a>[imgur](<a href=\"http://www.imgur.com\">www.imgur.com</a>)</p></div>";

        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            //Log.d(TAG,"set url " + span.getURL());
            makeLinkClickable(strBuilder, span);
        }
        //for some reason the strbuilder has 2 extra lines down the bottom. this is used to delete it.
        if(strBuilder.length() >= 2)
            strBuilder.delete(strBuilder.length()-2,strBuilder.length());
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());

    }


    public void openWebDialog(final Context contextIn, final String url, CharSequence title){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(contextIn);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(url);

        alertDialogBuilder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //Toast.makeText(this, "You clicked yes button",Toast.LENGTH_LONG).show();
                Log.d(TAG, "go");
                PostHelper.openWeb(url, contextIn);
            }
        });

        alertDialogBuilder.setNeutralButton("Copy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //Toast.makeText(this, "You clicked yes button",Toast.LENGTH_LONG).show();
                Log.d(TAG, "copy");
                android.content.ClipboardManager clipboard = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text",
                        url);
                clipboard.setPrimaryClip(clip);
            }
        });

        alertDialogBuilder.setNegativeButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "share");
                //finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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



}
