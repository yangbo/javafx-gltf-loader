package gltf.material;

import gltf.exception.GLTFException;
import com.alibaba.fastjson.JSONObject;

public class GLTFNormalTextureInfo extends GLTFTextureInfo{

    protected GLTFNormalTextureInfo(GLTFTexture texture,
                                    int texCoord,
                                    float scale,
                                    JSONObject extras) {
        super(texture, texCoord, extras);
    }

    public static GLTFNormalTextureInfo fromJSONObject(JSONObject jObj,
                                                       GLTFTexture[] textures)
            throws GLTFException
    {
        try{
            return new GLTFNormalTextureInfo(
                textures[jObj.getIntValue("index")],
                jObj.containsKey("texCoord") ?
                    jObj.getIntValue("texCoord")
                    : 0,
                jObj.containsKey("scale") ?
                    jObj.getFloatValue("scale")
                    : 1,
                jObj.containsKey("extras") ?
                    jObj.getJSONObject("extras")
                    : null
                );
        }catch(NullPointerException npe){
            //catch the NullPointerException thrown by JSONObject.getInt() in case of a missing index property
            npe.printStackTrace();
            System.out.println("Index property missing from JSONObject:\n"+jObj.toJSONString());
            GLTFException.throwGLTFExceptionWithCause(jObj);
        }catch(Exception e){
            GLTFException.throwGLTFExceptionWithCause(jObj);
        }
        return null;
    }
}
