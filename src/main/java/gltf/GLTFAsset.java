package gltf;

import gltf.accessor.GLTFAccessor;
import gltf.buffer.GLTFBuffer;
import gltf.buffer.GLTFBufferView;
import gltf.exception.GLTFException;
import gltf.material.GLTFImage;
import gltf.material.GLTFMaterial;
import gltf.material.GLTFTexture;
import gltf.material.GLTFTextureSampler;
import gltf.mesh.GLTFMesh;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class GLTFAsset {
    private final File gltfFile;
    private final String gltfDir;
    private final JSONObject obj;
    private final JSONArray buffersJSON;
    private final JSONArray bufferViewsJSON;
    private final JSONArray accessorsJSON;
    private final JSONArray imagesJSON;
    private final JSONArray samplersJSON;
    private final JSONArray texturesJSON;
    private final JSONArray materialsJSON;
    private final JSONArray meshesJSON;
    private final JSONArray skinsJSON;
    private final JSONArray camerasJSON;
    private final JSONArray nodesJSON;
    private final JSONArray scenesJSON;

    public final GLTFBuffer[] buffers;
    public final GLTFBufferView[] bufferViews;
    public final GLTFAccessor[] accessors;
    public final GLTFImage[] images;
    public final GLTFTextureSampler[] samplers;
    public final GLTFTexture[] textures;
    public final GLTFMaterial[] materials;
    public final GLTFMesh[] meshes;
    public final GLTFSkin[] skins;
    public final GLTFCamera[] cameras;
    public final GLTFNode[] nodes;
    public final GLTFScene[] scenes;



    public GLTFAsset(String filePath) throws IOException, GLTFException {
        this.gltfFile = new File(filePath);
        this.gltfDir = this.gltfFile.getParent();
        String content = new Scanner(this.gltfFile).useDelimiter("\\Z").next();

        // 获取根对象
        this.obj = JSON.parseObject(content);
        
        // 解析 buffers
        this.buffersJSON = this.obj.getJSONArray("buffers");
        if (buffersJSON != null) {
            this.buffers = new GLTFBuffer[buffersJSON.size()];
            for (int i = 0; i < this.buffersJSON.size(); i++) {
                buffers[i] = getBufferFromIndex(i);
            }
        } else {
            this.buffers = new GLTFBuffer[0];
        }

        // 解析 bufferViews
        this.bufferViewsJSON = this.obj.getJSONArray("bufferViews");
        if (bufferViewsJSON != null) {
            this.bufferViews = new GLTFBufferView[this.bufferViewsJSON.size()];
            for (int i = 0; i < bufferViewsJSON.size(); i++) {
                this.bufferViews[i] = new GLTFBufferView(
                        (JSONObject) this.bufferViewsJSON.get(i),
                        this.buffers);
            }
        } else {
            this.bufferViews = new GLTFBufferView[0];
        }

        // 解析 accessors
        this.accessorsJSON = obj.getJSONArray("accessors");
        if (accessorsJSON != null) {
            this.accessors = new GLTFAccessor[this.accessorsJSON.size()];
            for (int i = 0; i < accessorsJSON.size(); i++) {
                this.accessors[i] = GLTFAccessor.fromJSONObject(
                        this.accessorsJSON.getJSONObject(i),
                        this.bufferViews);
            }
        } else {
            this.accessors = new GLTFAccessor[0];
        }

        // 解析 images
        this.imagesJSON = obj.getJSONArray("images");
        if (imagesJSON != null) {
            this.images = new GLTFImage[this.imagesJSON.size()];
            for (int i = 0; i < imagesJSON.size(); i++) {
                this.images[i] = GLTFImage.fromJSONObject(
                        this.imagesJSON.getJSONObject(i),
                        bufferViews,
                        gltfDir
                );
            }
        } else {
            this.images = new GLTFImage[0];
        }

        // 解析 samplers
        this.samplersJSON = obj.getJSONArray("samplers");
        if (samplersJSON != null) {
            this.samplers = new GLTFTextureSampler[this.samplersJSON.size()];
            for (int i = 0; i < this.samplersJSON.size(); i++) {
                this.samplers[i] = GLTFTextureSampler.fromJSONObject(
                        this.samplersJSON.getJSONObject(i)
                );
            }
        } else {
            this.samplers = new GLTFTextureSampler[0];
        }

        // 解析 textures
        this.texturesJSON = obj.getJSONArray("textures");
        if (texturesJSON != null) {
            this.textures = new GLTFTexture[this.texturesJSON.size()];
            for (int i = 0; i < this.texturesJSON.size(); i++) {
                this.textures[i] = GLTFTexture.fromJSONObject(
                        this.texturesJSON.getJSONObject(i),
                        this.images,
                        this.samplers
                );
            }
        } else {
            this.textures = new GLTFTexture[0];
        }

        // 解析 materials
        this.materialsJSON = obj.getJSONArray("materials");
        if (materialsJSON != null) {
            this.materials = new GLTFMaterial[this.materialsJSON.size()];
            for (int i = 0; i < this.materialsJSON.size(); i++) {
                this.materials[i] = GLTFMaterial.fromJSONObject(
                        this.materialsJSON.getJSONObject(i),
                        this.textures
                );
            }
        } else {
            this.materials = new GLTFMaterial[0];
        }

        // 解析 meshes
        this.meshesJSON = obj.getJSONArray("meshes");
        if (meshesJSON != null) {
            this.meshes = new GLTFMesh[this.meshesJSON.size()];
            for (int i = 0; i < this.meshesJSON.size(); i++) {
                this.meshes[i] = GLTFMesh.fromJSONObject(
                        this.meshesJSON.getJSONObject(i),
                        this.accessors,
                        this.materials
                );
            }
        } else {
            this.meshes = new GLTFMesh[0];
        }
        // 解析 skins
        this.skinsJSON = obj.getJSONArray("skins");
        if (skinsJSON != null) {
            this.skins = new GLTFSkin[this.skinsJSON.size()];
            for (int i = 0; i < this.skinsJSON.size(); i++) {
                this.skins[i] = GLTFSkin.fromJSONObject(
                        this.skinsJSON.getJSONObject(i)
                );
            }
        } else {
            this.skins = new GLTFSkin[0];
        }

        // 解析 cameras
        this.camerasJSON = obj.getJSONArray("cameras");
        if (camerasJSON != null) {
            this.cameras = new GLTFCamera[this.camerasJSON.size()];
            for (int i = 0; i < this.camerasJSON.size(); i++) {
                this.cameras[i] = GLTFCamera.fromJSONObject(
                        this.camerasJSON.getJSONObject(i)
                );
            }
        } else {
            this.cameras = new GLTFCamera[0];
        }

        // 解析 nodes
        this.nodesJSON = obj.getJSONArray("nodes");
        if (nodesJSON != null) {
            this.nodes = new GLTFNode[this.nodesJSON.size()];
            for (int i = 0; i < this.nodesJSON.size(); i++) {
                this.nodes[i] = GLTFNode.fromJSONObject(
                        this.nodesJSON.getJSONObject(i),
                        this.cameras,
                        this.nodes,
                        this.meshes,
                        this.skins
                );
            }
        } else {
            this.nodes = new GLTFNode[0];
        }

        // 解析 scenes
        this.scenesJSON = obj.getJSONArray("scenes");
        if (scenesJSON != null) {
            this.scenes = new GLTFScene[this.scenesJSON.size()];
            for (int i = 0; i < this.scenesJSON.size(); i++) {
                this.scenes[i] = GLTFScene.fromJSONObject(
                        this.scenesJSON.getJSONObject(i),
                        this.nodes
                );
            }
        } else {
            this.scenes = new GLTFScene[0];
        }
    }

    private GLTFBuffer getBufferFromIndex(int i) throws IOException {
        JSONObject bObj = (JSONObject)this.buffersJSON.get(i);
        return GLTFBuffer.fromBuffer(bObj, this.gltfDir);
    }
    public static void main(String[] args) throws IOException, GLTFException {
        GLTFAsset asset = new GLTFAsset("robot/scene.gltf");

        System.out.println(/*asset.buffers[0].bytes.length*/);
        //System.out.println(asset.bufferViews.length);
    }
}
