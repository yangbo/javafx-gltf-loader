package gltf.material;

import gltf.exception.GLTFException;
import com.alibaba.fastjson.JSONObject;

public class GLTFOcclusionTextureInfo extends GLTFTextureInfo{

    public final float strength;

    public GLTFOcclusionTextureInfo(GLTFTexture texture, int texCoordIdx, float strength, JSONObject extras) {
        super(texture, texCoordIdx, extras);
        this.strength = strength;
    }
    public GLTFOcclusionTextureInfo(int textureIdx,
                                    int texCoordIdx,
                                    Float strength,
                                    JSONObject extras,
                                    GLTFTexture[] textures){
        this(textures[textureIdx],
            texCoordIdx == -1 ?
                0
                : texCoordIdx,
            strength,
            extras);
    }
    public static GLTFOcclusionTextureInfo fromJSONObject(JSONObject jObj, GLTFTexture[] textures) throws GLTFException {
        try{
            return new GLTFOcclusionTextureInfo(
                jObj.getIntValue("index"),
                jObj.containsKey("texCoord") ?
                    jObj.getIntValue("texCoord")
                    : 0,
                jObj.containsKey("Strength") ?
                    jObj.getFloatValue("strength")
                    : 1,
                jObj.containsKey("extras") ?
                    jObj.getJSONObject("extras")
                    : null,
                textures
            );
        }catch(Exception e){
            e.printStackTrace();
            GLTFException.throwGLTFExceptionWithCause(jObj);
        }
        return null;
    }
}
