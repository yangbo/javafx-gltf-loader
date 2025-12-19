package gltf.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

public class JSONUtils {
    public static float[] JSONToFloatArray(JSONArray array) throws JSONException{
        float[] returnVal = new float[array.size()];
        for (int i = 0; i < array.size(); i++) {
            try{
                returnVal[i] = array.getFloatValue(i);
            }catch(RuntimeException re){
                re.printStackTrace();
                throw new JSONException("invalid type");
            }
        }
        return returnVal;
    }
    public static int[] JSONToIntArray(JSONArray array) throws JSONException{
        int[] returnVal = new int[array.size()];
        for (int i = 0; i < array.size(); i++) {
            try{
                returnVal[i] = array.getIntValue(i);
            }catch(RuntimeException re){
                re.printStackTrace();
                throw new JSONException("invalid type");
            }
        }
        return returnVal;
    }
}
