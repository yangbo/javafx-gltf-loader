package gltf;

import gltf.exception.GLTFException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class GLTFScene {
    public final GLTFNode[] nodes;

    public GLTFScene(GLTFNode[] nodes) {
        this.nodes = nodes;
    }

    public static GLTFScene fromJSONObject(JSONObject jObj, GLTFNode[] nodes) throws GLTFException {
        try{
            GLTFNode[] sceneNodes = new GLTFNode[]{};
            if(jObj.containsKey("nodes")){
                JSONArray jarr = jObj.getJSONArray("nodes");
                sceneNodes = new GLTFNode[jarr.size()];
                for (int i = 0; i < jarr.size(); i++) {
                    sceneNodes[i] = nodes[jarr.getIntValue(i)];
                }
            }
            return new GLTFScene(
                sceneNodes
            );
        }catch(Exception e){
            e.printStackTrace();
            GLTFException.throwGLTFExceptionWithCause(jObj);
        }
        return null;
    }
}
