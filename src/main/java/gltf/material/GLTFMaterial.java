package gltf.material;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import static gltf.material.GLTFMaterialAlphaMode.OPAQUE;

public class GLTFMaterial {
    public final boolean doubleSided;
    public final GLTFpbrMetallicRoughness pbrMetallicRoughness;
    public final GLTFNormalTextureInfo normalMap;
    public final GLTFOcclusionTextureInfo occlusionMap;
    public final GLTFTextureInfo emissiveMap;
    public final float[] emissiveFactor;
    public final GLTFMaterialAlphaMode alphaMode;
    public final float alphaCutoff;
    public GLTFMaterial(String materialName,
                        boolean doubleSided,
                        GLTFpbrMetallicRoughness pbrMetallicRoughness,
                        GLTFNormalTextureInfo normalMap,
                        GLTFOcclusionTextureInfo occlusionMap,
                        GLTFTextureInfo emissiveMap,
                        float[] emissiveFactor,
                        GLTFMaterialAlphaMode alphaMode,
                        float alphaCutoff)
    {
        this.doubleSided = doubleSided;
        this.pbrMetallicRoughness = pbrMetallicRoughness;
        this.normalMap = normalMap;
        this.occlusionMap = occlusionMap;
        this.emissiveMap = emissiveMap;
        this.emissiveFactor = emissiveFactor;
        this.alphaMode = alphaMode;
        this.alphaCutoff = alphaCutoff;
    }

    public static GLTFMaterial fromJSONObject(JSONObject jObj, GLTFTexture[] textures) {
        //TODO code this method
        try{
            float[] emissiveFactor = new float[3];
            if(jObj.containsKey("emissiveFactor")){
                JSONArray jarr = jObj.getJSONArray("emissiveFactor");
                for(int i = 0; i < jarr.size(); i++){
                    emissiveFactor[i] = jarr.getFloatValue(i);
                }
            }
            return new GLTFMaterial(
                jObj.containsKey("name") ?
                    jObj.getString("name")
                    : "",
                jObj.containsKey("doubleSided") ?
                    jObj.getBooleanValue("doubleSided")
                    : false,
                jObj.containsKey("pbrMetallicRoughness") ?
                    GLTFpbrMetallicRoughness.fromJSONObject(jObj.getJSONObject("pbrMetallicRoughness"), textures)
                    : null,
                jObj.containsKey("normalTexture") ?
                    GLTFNormalTextureInfo.fromJSONObject(jObj.getJSONObject("normalTexture"), textures)
                    : null,
                jObj.containsKey("occlusionTexture") ?
                    GLTFOcclusionTextureInfo.fromJSONObject(jObj.getJSONObject("occlusionTexture"), textures)
                    : null,
                jObj.containsKey("emissiveTexture") ?
                    GLTFEmissiveTextureInfo.fromJSONObject(jObj.getJSONObject("emissiveTexture"), textures)
                    : null,
                jObj.containsKey("emissiveFactor") ?
                    emissiveFactor
                    : null,
                jObj.containsKey("alphaMode") ?
                    GLTFMaterialAlphaMode.valueOf(jObj.getString("alphaMode"))
                    : OPAQUE,
                jObj.containsKey("alphaCutoff") ?
                    jObj.getFloatValue("alphaCutoff")
                    : 0.5f
            );
        }catch(Exception e){

        }
        return null;
    }
}
