package gltf.material;

import gltf.exception.GLTFException;
import com.alibaba.fastjson.JSONObject;

public class GLTFEmissiveTextureInfo extends GLTFTextureInfo{
    public GLTFEmissiveTextureInfo(GLTFTexture texture, int texCoord, JSONObject extras) {
        super(texture, texCoord, extras);
    }
    public static GLTFEmissiveTextureInfo fromJSONObject(JSONObject jObj, GLTFTexture[] textures) throws GLTFException {
        try{
            return new GLTFEmissiveTextureInfo(
                textures[jObj.getIntValue("index")],
                jObj.containsKey("texCoord") ?
                    jObj.getIntValue("texCoord")
                    : 0,
                jObj.containsKey("extras") ?
                    jObj.getJSONObject("extras")
                    : null
            );
        }catch(Exception e){
            GLTFException.throwGLTFExceptionWithCause(jObj);
            //TODO catch all exception types separately
        }
        return null;
    }
}
