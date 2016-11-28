package rick.redditl.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import rick.redditl.adapter.PostListAdapter;
import rick.redditl.helper.JSONParser;
import rick.redditl.helper.ParserHelper;
import rick.redditl.model.PostData;
import rick.redditl.R;
import rick.redditl.model.PreviewImageData;


public class MainPage extends AppCompatActivity {

    private TextView displayData;

    String TAG = "MainPage";

    PostData[] postsData;

    public ListView mainListView;

    public ArrayList<PostData> postsList;

    PostListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        //displayData = (TextView) findViewById(R.id.displayData);
        mainListView = (ListView) findViewById(R.id.mainListView);

        postsData = new PostData[26];

        postsList = new ArrayList<PostData>();

        adapter = new PostListAdapter(this, postsList);
        mainListView.setAdapter(adapter);

        String url = "https://www.reddit.com/.json";
        url = url + "?raw_json=1";



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

                        //parse the JSON object and pass back a PostData object
                        postsData[i] = ParserHelper.parsePostData(postJSON);

                        //add to adapter
                        adapter.add(postsData[i]);


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
