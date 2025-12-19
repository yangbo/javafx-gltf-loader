package gltf.material;

import gltf.exception.GLTFException;
import com.alibaba.fastjson.JSONObject;

public class GLTFTextureInfo {
    public final GLTFTexture texture;
    public final int texCoordIdx;
    public final JSONObject extras;

    public GLTFTextureInfo(GLTFTexture texture,
                           int texCoordIdx,
                           JSONObject extras)
    {
        this.texture = texture;
        this.texCoordIdx = texCoordIdx;
        this.extras = extras;
    }

    public static GLTFTextureInfo fromJSONObject(JSONObject jObj, GLTFTexture[] textures) throws GLTFException {
        try{
            return new GLTFTextureInfo(
                textures[jObj.getIntValue("index")],
                jObj.containsKey("texCoord") ?
                    jObj.getIntValue("texCoord")
                    : 0,
                jObj.containsKey("extras") ?
                    jObj.getJSONObject("extras")
                    : null
            );
        }catch(Exception e){
            e.printStackTrace();
            GLTFException.throwGLTFExceptionWithCause(jObj);
        }
        return null;
    }
}
