package rick.redditl.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import rick.redditl.Constants;
import rick.redditl.R;
import rick.redditl.activity.WebActivity;
import rick.redditl.adapter.PostListAdapter;
import rick.redditl.model.PostData;

/**
 * Created by Rick on 2016-12-17.
 */
public class GeneralHelper {

    static String TAG = "GeneralHelper";




    /**
     * Takes an input integer and convert it to a string.
     * If the integer is greater than 9999, divide it by 1000 and add k at the end
     * Example: 12345 to 12.3k
     * @param input a positive integer
     * @return
     */
    public static String convertIntToStringK(int input) {
        if(input > 9999) {
            input = input/100;
            double temp = input;
            temp = temp/10;

            return Double.toString(temp) + 'k';

        } else {
            return Integer.toString(input);
        }

    }





}
