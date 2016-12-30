package rick.redditl.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import rick.redditl.Constants;
import rick.redditl.adapters.PostListAdapter;
import rick.redditl.helper.PostDataParseHelper;
import rick.redditl.network.JSONParser;
import rick.redditl.models.PostData;
import rick.redditl.R;


public class MainPage extends AppCompatActivity {

    String TAG = "MainPage";

    //listview related variables
    public ListView mainListView;
    PostListAdapter adapter;
    public ArrayList<PostData> postsList;


    Button searchSubreddit;
    AlertDialog.Builder subredditDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);


        //set the screen height and width for later use
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Constants.screenWidth = size.x;
        Constants.screenHeight = size.y;

        //listview related variables
        mainListView = (ListView) findViewById(R.id.mainListView);
        postsList = new ArrayList<PostData>();
        //listview adapters
        adapter = new PostListAdapter(this, postsList);
        mainListView.setAdapter(adapter);


        //dialog elements
        searchSubreddit = (Button) findViewById(R.id.subReddit);


        loadUrl("");



        //clicking the title text
        searchSubreddit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "search subreddit clicked");
                //subredditDialog.show();
                showSubRedditDialog();
            }
        });

    }

    protected void loadUrl(String subreddit) {
        String url = "https://www.reddit.com/";
        Log.d(TAG,"input subreddit is " + subreddit);

        if(subreddit == "" || subreddit == null || subreddit.isEmpty()) {
            url = url + ".json?raw_json=1";
        } else {
            url = url + "r/" + subreddit + "/.json?raw_json=1";
        }

        adapter.clear();

        new asyncGET().execute(url);


    }

    //useless
    public void onClickTitle (View v) {
        Log.d(TAG,"clicked");

    }

    /**
     * Used to get json data from server
     */
    class asyncGET extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        String URL = "";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainPage.this);
            pDialog.setMessage("Attempting login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            //pDialog.show();
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

            int success = 0;
            String message = "";

            if (pDialog != null && pDialog.isShowing()) {
                //pDialog.dismiss();
            }

            if (json != null) {
                try {
                    JSONObject data = json.getJSONObject("data");
                    JSONArray posts = data.getJSONArray("children");

                    int postLength = posts.length();
                    for(int i = 0; i < postLength; i++)
                    {
                        JSONObject postNumber = posts.getJSONObject(i);
                        JSONObject postJSON = postNumber.getJSONObject("data");

                        //parse the JSON object and add the PostData object to the adapter
                        adapter.add(PostDataParseHelper.parsePostData(postJSON));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /*
                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                */
            }

            /*
            if (success == 1) {
                Log.d("Success!", message);
            }else{
                Log.d("Failure", message);
            }
            */
        }

    }




    public void showSubRedditDialog(){
        subredditDialog = new AlertDialog.Builder(this);
        subredditDialog.setTitle("Enter subreddit name:");

        //set up container to wrap around linearlayout
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(80, 20, 80, 0); //left top right bottom
        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setLayoutParams(lp);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.LEFT);
        //setup container and dialog
        container.addView(input, lp);
        subredditDialog.setView(container);

        // Set up the buttons
        subredditDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString();
                Log.d(TAG,"Subreddit searched for is " + inputText);
                //reload everything
                loadUrl(inputText);
            }
        });
        subredditDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        subredditDialog.show();
    }




    class WebAppInterface {
        @JavascriptInterface
        public String toString() { return "injectedObject"; }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
