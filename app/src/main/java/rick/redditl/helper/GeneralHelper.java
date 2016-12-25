package rick.redditl.helper;

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
