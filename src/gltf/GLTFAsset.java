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
import org.json.JSONArray;
import org.json.JSONObject;

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
        this.obj = new JSONObject(content);
        
        // 解析 buffers
        this.buffersJSON = this.obj.optJSONArray("buffers");
        if (buffersJSON != null) {
            this.buffers = new GLTFBuffer[buffersJSON.length()];
            for (int i = 0; i < this.buffersJSON.length(); i++) {
                buffers[i] = getBufferFromIndex(i);
            }
        } else {
            this.buffers = new GLTFBuffer[0];
        }

        // 解析 bufferViews
        this.bufferViewsJSON = this.obj.optJSONArray("bufferViews");
        if (bufferViewsJSON != null) {
            this.bufferViews = new GLTFBufferView[this.bufferViewsJSON.length()];
            for (int i = 0; i < bufferViewsJSON.length(); i++) {
                this.bufferViews[i] = new GLTFBufferView(
                        (JSONObject) this.bufferViewsJSON.get(i),
                        this.buffers);
            }
        } else {
            this.bufferViews = new GLTFBufferView[0];
        }

        // 解析 accessors
        this.accessorsJSON = obj.optJSONArray("accessors");
        if (accessorsJSON != null) {
            this.accessors = new GLTFAccessor[this.accessorsJSON.length()];
            for (int i = 0; i < accessorsJSON.length(); i++) {
                this.accessors[i] = GLTFAccessor.fromJSONObject(
                        this.accessorsJSON.getJSONObject(i),
                        this.bufferViews);
            }
        } else {
            this.accessors = new GLTFAccessor[0];
        }

        // 解析 images
        this.imagesJSON = obj.optJSONArray("images");
        if (imagesJSON != null) {
            this.images = new GLTFImage[this.imagesJSON.length()];
            for (int i = 0; i < imagesJSON.length(); i++) {
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
        this.samplersJSON = obj.optJSONArray("samplers");
        if (samplersJSON != null) {
            this.samplers = new GLTFTextureSampler[this.samplersJSON.length()];
            for (int i = 0; i < this.samplersJSON.length(); i++) {
                this.samplers[i] = GLTFTextureSampler.fromJSONObject(
                        this.samplersJSON.getJSONObject(i)
                );
            }
        } else {
            this.samplers = new GLTFTextureSampler[0];
        }

        // 解析 textures
        this.texturesJSON = obj.optJSONArray("textures");
        if (texturesJSON != null) {
            this.textures = new GLTFTexture[this.texturesJSON.length()];
            for (int i = 0; i < this.texturesJSON.length(); i++) {
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
        this.materialsJSON = obj.optJSONArray("materials");
        if (materialsJSON != null) {
            this.materials = new GLTFMaterial[this.materialsJSON.length()];
            for (int i = 0; i < this.materialsJSON.length(); i++) {
                this.materials[i] = GLTFMaterial.fromJSONObject(
                        this.materialsJSON.getJSONObject(i),
                        this.textures
                );
            }
        } else {
            this.materials = new GLTFMaterial[0];
        }

        // 解析 meshes
        this.meshesJSON = obj.optJSONArray("meshes");
        if (meshesJSON != null) {
            this.meshes = new GLTFMesh[this.meshesJSON.length()];
            for (int i = 0; i < this.meshesJSON.length(); i++) {
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
        this.skinsJSON = obj.optJSONArray("skins");
        if (skinsJSON != null) {
            this.skins = new GLTFSkin[this.skinsJSON.length()];
            for (int i = 0; i < this.skinsJSON.length(); i++) {
                this.skins[i] = GLTFSkin.fromJSONObject(
                        this.skinsJSON.getJSONObject(i)
                );
            }
        } else {
            this.skins = new GLTFSkin[0];
        }

        // 解析 cameras
        this.camerasJSON = obj.optJSONArray("cameras");
        if (camerasJSON != null) {
            this.cameras = new GLTFCamera[this.camerasJSON.length()];
            for (int i = 0; i < this.camerasJSON.length(); i++) {
                this.cameras[i] = GLTFCamera.fromJSONObject(
                        this.camerasJSON.getJSONObject(i)
                );
            }
        } else {
            this.cameras = new GLTFCamera[0];
        }

        // 解析 nodes
        this.nodesJSON = obj.optJSONArray("nodes");
        if (nodesJSON != null) {
            this.nodes = new GLTFNode[this.nodesJSON.length()];
            for (int i = 0; i < this.nodesJSON.length(); i++) {
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
        this.scenesJSON = obj.optJSONArray("scenes");
        if (scenesJSON != null) {
            this.scenes = new GLTFScene[this.scenesJSON.length()];
            for (int i = 0; i < this.scenesJSON.length(); i++) {
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
        GLTFAsset asset = new GLTFAsset("asset/scene.gltf");

        System.out.println(/*asset.buffers[0].bytes.length*/);
        //System.out.println(asset.bufferViews.length);
    }
}
