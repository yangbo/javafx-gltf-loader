package gltf.accessor;

import gltf.buffer.GLTFBufferView;
import gltf.exception.InvalidGLTFTypeException;
import gltf.type.GLTFAccessorType;
import gltf.type.GLTFComponentType;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class GLTFAccessor {
    protected final GLTFBufferView bufferView;
    protected final GLTFComponentType componentType;
    protected final int nElem;
    protected final GLTFAccessorType type;
    protected final int byteOffset;

    protected GLTFAccessor(GLTFBufferView bufferView,
                           GLTFComponentType componentType,
                           int byteOffset,
                           int nElem,
                           GLTFAccessorType type){
        this.bufferView = bufferView;
        this.componentType = componentType;
        this.byteOffset = byteOffset;
        this.type = type;
        this.nElem = nElem;
    }

    /**
     * creates a GLTF accessor from a JSONObject of the
     * "accessors" property of the GLTF's root.
     * @param jObj the JSONObject to create the GLTFAccessor object from
     * @param bufferViews the bufferViews array
     * @return the created GLTFAcccessor object
     */
    public static GLTFAccessor fromJSONObject(JSONObject jObj,
                                              GLTFBufferView[] bufferViews)
            throws InvalidGLTFTypeException {
        GLTFComponentType componentType = GLTFComponentType.fromTypeId(
                jObj.getIntValue("componentType")
        );
        GLTFAccessorType dataType = GLTFAccessorType.valueOf(
                String.valueOf(jObj.get("type"))
        );

        int bufferViewIdx = jObj.containsKey("bufferView") ? jObj.getIntValue("bufferView") : -1;
        GLTFBufferView bufferView = bufferViewIdx != -1 ? bufferViews[bufferViewIdx] : null;

        int byteOffset = jObj.containsKey("byteOffset") ? jObj.getIntValue("byteOffset") : 0;
        int nElem = jObj.getIntValue("count");

        JSONArray minArray = jObj.getJSONArray("min");
        JSONArray maxArray = jObj.getJSONArray("max");

        switch (componentType) {
            case BYTE:
            case UNSIGNED_BYTE:
                //edit if a case is seen where
                //an accessor uses the BYTE componentType
                return null;
            case SHORT:
            case UNSIGNED_SHORT:
                short[] minShort = null;
                short[] maxShort = null;
                if (minArray != null && maxArray != null) {
                    minShort = new short[dataType.size];
                    maxShort = new short[dataType.size];
                    for (int i = 0; i < dataType.size; i++) {
                        minShort[i] = i < minArray.size() ? minArray.getShortValue(i) : (short) 0;
                        maxShort[i] = i < maxArray.size() ? maxArray.getShortValue(i) : (short) 0;
                    }
                }
                return new GLTFShortAccessor(bufferView,
                        componentType,
                        byteOffset,
                        nElem,
                        minShort,
                        maxShort,
                        dataType);
            case UNSIGNED_INT:
                int[] minInt = null;
                int[] maxInt = null;
                if (minArray != null && maxArray != null) {
                    minInt = new int[dataType.size];
                    maxInt = new int[dataType.size];
                    for (int i = 0; i < dataType.size; i++) {
                        minInt[i] = i < minArray.size() ? minArray.getIntValue(i) : 0;
                        maxInt[i] = i < maxArray.size() ? maxArray.getIntValue(i) : 0;
                    }
                }
                return new GLTFIntAccessor(bufferView,
                        componentType,
                        byteOffset,
                        nElem,
                        minInt,
                        maxInt,
                        dataType);
            case FLOAT:
                float[] minFloat = null;
                float[] maxFloat = null;
                if (minArray != null && maxArray != null) {
                    minFloat = new float[dataType.size];
                    maxFloat = new float[dataType.size];
                    for (int i = 0; i < dataType.size; i++) {
                        minFloat[i] = i < minArray.size() ? minArray.getFloatValue(i) : 0f;
                        maxFloat[i] = i < maxArray.size() ? maxArray.getFloatValue(i) : 0f;
                    }
                }
                return new GLTFFloatAccessor(bufferView,
                        componentType,
                        byteOffset,
                        nElem,
                        minFloat,
                        maxFloat,
                        dataType);
        }
        return null;
    }

    /**
     * ensures that the accessor is of the expected types.
     * @param accessorTypes the expected types of the accessor:
     *                     SCALAR, VECT2, VECT3, etc
     * @param componentTypes the expected possible component types
     *                       of the accessor: FLOAT, USINGED_INT, SHORT, etc
     * @return the accessor if the types are as expected
     * @throws InvalidGLTFTypeException if the types are not as expected
     */
    public GLTFAccessor assertType(GLTFAccessorType[] accessorTypes,
                                   GLTFComponentType[] componentTypes) throws InvalidGLTFTypeException{
        boolean rightComponentType = false;
        for(GLTFComponentType componentType: componentTypes){
            if(this.componentType == componentType){
                rightComponentType = true; break;
            }
        }
        boolean rightAccessorType = false;
        for(GLTFAccessorType accessorType: accessorTypes){
            if(this.type == accessorType){
                rightAccessorType = true; break;
            }
        }
        if(!rightAccessorType || !rightComponentType)
            throw new InvalidGLTFTypeException("type check for accessor "+this+" failed");
        return this;
    }
}
