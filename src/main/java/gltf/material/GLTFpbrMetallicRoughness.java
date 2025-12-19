package gltf.material;

import gltf.exception.GLTFException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class GLTFpbrMetallicRoughness {
    public final GLTFTextureInfo baseColorTexture;
    public final float[] baseColorFactor;
    public final float metallicFactor;
    public final float roughnessFactor;
    public final GLTFTextureInfo metallicRoughnessTexture;
    public final JSONObject extras;
    public GLTFpbrMetallicRoughness(GLTFTextureInfo baseColorTexture,
                                    float[] baseColorFactor,
                                    float metallicFactor,
                                    float roughnessFactor,
                                    GLTFTextureInfo metallicRoughnessTexture,
                                    JSONObject extras) {
        this.baseColorTexture = baseColorTexture;
        this.baseColorFactor = baseColorFactor;
        this.metallicFactor = metallicFactor;
        this.roughnessFactor = roughnessFactor;
        this.metallicRoughnessTexture = metallicRoughnessTexture;
        this.extras = extras;
    }

    public static GLTFpbrMetallicRoughness fromJSONObject(JSONObject jObj, GLTFTexture[] textures) throws GLTFException {
        try{
            float[] baseColorFactor = new float[4];
            if(jObj.containsKey("baseColorFactor")){
                JSONArray jarr = jObj.getJSONArray("baseColorFactor");
                for (int i = 0; i < jarr.size(); i++) {
                    baseColorFactor[i] = jarr.getFloatValue(i);
                }
            }
            return new GLTFpbrMetallicRoughness(
                jObj.containsKey("baseColorTexture") ?
                    GLTFTextureInfo.fromJSONObject(jObj.getJSONObject("baseColorTexture"), textures)
                    : null,
                jObj.containsKey("baseColorFactor") ?
                    baseColorFactor
                    : null,
                jObj.containsKey("metallicFactor") ?
                    jObj.getFloatValue("metallicFactor")
                    : 1.0f,
                jObj.containsKey("roughnessFactor") ?
                    jObj.getFloatValue("roughnessFactor")
                    : 1.0f,
                jObj.containsKey("metallicRoughnessTexture") ?
                    GLTFTextureInfo.fromJSONObject(jObj.getJSONObject("metallicRoughnessTexture"), textures)
                    : null,
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
    //TODO add this maybe
}
