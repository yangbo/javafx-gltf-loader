package gltf.mesh;

import gltf.accessor.GLTFAccessor;
import gltf.exception.GLTFException;
import gltf.material.GLTFMaterial;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class GLTFMesh {
    public final GLTFMeshPrimitive[] primitives;

    public GLTFMesh(GLTFMeshPrimitive[] primitives) {
        this.primitives = primitives;
    }
    public static GLTFMesh fromJSONObject(JSONObject jObj,
                                          GLTFAccessor[] accessors,
                                          GLTFMaterial[] materials)
        throws GLTFException
    {
        try{
            JSONArray jarr = jObj.getJSONArray("primitives");
            GLTFMeshPrimitive[] primitives = new GLTFMeshPrimitive[jarr.size()];
            for (int i = 0; i < jarr.size(); i++) {
                primitives[i] = GLTFMeshPrimitive.fromJSONObject(
                    jarr.getJSONObject(i),
                    accessors,
                    materials
                );
            }
            return new GLTFMesh(
                primitives
            );
        }catch(Exception e){
            e.printStackTrace();
            GLTFException.throwGLTFExceptionWithCause(jObj);
        }
        return null;
    }
}
