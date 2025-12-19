package gltf.material;

import gltf.buffer.GLTFBufferView;
import gltf.exception.GLTFException;
import com.alibaba.fastjson.JSONObject;

import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class GLTFImage {
    public final BufferedImage image;
    public GLTFImage(BufferedImage image){
        this.image = image;
    }

    /**
     * This function creates a GLTFImage object from the
     * JSONObject passed in parameter
     * @param jObj
     * @param bufferViews
     * @return
     * @throws IOException if the image cannot be created. from the source specified in the
     */
    public static GLTFImage fromJSONObject(JSONObject jObj,
                                           GLTFBufferView[] bufferViews,
                                           String gltfDir)
        throws GLTFException
    {
        if(jObj.containsKey("uri")){
            try{
                return new GLTFExternalImage(
                    jObj.getString("uri"),
                    gltfDir
                );
            }catch(IOException ioe){
                GLTFException.throwGLTFExceptionWithCause(jObj);
            }
        }
        else if(jObj.containsKey("bufferView")){
            try {
                return new GLTFBufferViewImage(
                    jObj.getIntValue("bufferView"),
                    bufferViews,
                    jObj.getString("mimeType")
                );
            }catch(IOException ioe){
                ioe.printStackTrace();
                GLTFException.throwGLTFExceptionWithCause(jObj);
            }
        }
        return null;
    }
}
