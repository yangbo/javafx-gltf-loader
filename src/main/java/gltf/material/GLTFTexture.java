package gltf.material;

import com.alibaba.fastjson.JSONObject;

public class GLTFTexture {
    public final GLTFTextureSampler sampler;
    public final GLTFImage source;
    public final String name;
    public final JSONObject extras;

    public GLTFTexture(GLTFTextureSampler sampler, GLTFImage source, String name, JSONObject extras) {
        this.sampler = sampler;
        this.source = source;
        this.name = name;
        this.extras = extras;
    }
    public static GLTFTexture fromJSONObject(JSONObject jObj,
                                             GLTFImage[] images,
                                             GLTFTextureSampler[] samplers){
        return new GLTFTexture(
            jObj.containsKey("sampler") ?
                samplers[jObj.getIntValue("sampler")]
                : null,
            jObj.containsKey("source") ?
                images[jObj.getIntValue("source")]
                : null,
            jObj.containsKey("name") ?
                jObj.getString("name")
                : "",
            jObj.containsKey("extras") ?
                jObj.getJSONObject("extras")
                : null
        );
    }
}
