package rick.redditl.helper;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rick.redditl.models.PostData;
import rick.redditl.models.PreviewImageData;

/**
 * Created by Rick on 2016-11-27.
 */
public class PostDataParseHelper {
    static String TAG = "PostDataParseHelper";

    /**
     * Takes JSON object of a post and parse all the information and images
     * Then put them into a PostData object and return it
     *
     * @param postJSON JSON object of a post
     * @return a PostData object filled with the information
     */
    public static PostData parsePostData(JSONObject postJSON) {
        Log.d(TAG, "Parsing Post data");
        PostData result = null;

        try {
            //getting all the post data
            String title = postJSON.getString("title");
            String subreddit = postJSON.getString("subreddit");
            String author = postJSON.getString("author");
            int score = postJSON.getInt("score");
            int num_comments = postJSON.getInt("num_comments");
            String permalink = postJSON.getString("permalink");
            String url = postJSON.getString("url");
            long timeCreated = postJSON.getInt("created_utc");
            Boolean isSelf = postJSON.getBoolean("is_self");
            String selfText = postJSON.getString("selftext");
            String domain = postJSON.getString("domain");
            String thumbnailURL = postJSON.getString("thumbnail");
            String pid = postJSON.getString("id");
            String name = postJSON.getString("name");

            //creating the actual item in the list
            result = new PostData(title, subreddit, author, score,
                    num_comments, permalink, url, timeCreated, isSelf, selfText, domain, thumbnailURL, pid, name);


            //if has preview
            if (postJSON.has("preview")) {

                //getting preview images for the post
                JSONObject previewData = postJSON.getJSONObject("preview").getJSONArray("images").getJSONObject(0);
                JSONObject previewSource = previewData.getJSONObject("source");
                PreviewImageData tempImage = new PreviewImageData((String) previewSource.getString("url"),
                        (int) previewSource.getInt("width"), (int) previewSource.getInt("height"));

                //setting preview images for the item in the list
                result.setPreviewSource(tempImage);

                //getting preview image resolutions
                JSONArray previewResolutions = previewData.getJSONArray("resolutions");
                int resolutionNum = previewResolutions.length();
                PreviewImageData tempImages[] = new PreviewImageData[resolutionNum];
                for (int j = 0; j < resolutionNum; j++) {
                    JSONObject imageResolutionData = previewResolutions.getJSONObject(j);

                    tempImages[j] = new PreviewImageData((String) imageResolutionData.getString("url"),
                            (int) imageResolutionData.getInt("width"), (int) imageResolutionData.getInt("height"));

                    //Log.d(TAG, "loop is " + j + " with resolution url " + imageResolutionData.getString("url"));


                }
                result.setPreviewImagesRes(tempImages);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }
}
