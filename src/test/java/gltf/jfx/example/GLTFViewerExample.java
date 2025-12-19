package gltf.jfx.example;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * GLTF 模型查看器示例程序
 */
public class GLTFViewerExample extends Application {

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    @Override
    public void start(Stage primaryStage) {
        try {
            // 创建根节点
            Group root = new Group();

            // 加载 glTF 资产
            java.net.URL resource = getClass().getResource("/robot/scene.gltf");
            String gltfPath;
            if (resource != null) {
                gltfPath = java.nio.file.Paths.get(resource.toURI()).toString();
            } else {
                gltfPath = "src/main/resources/asset/robot/scene.gltf";
            }
            System.out.println("正在加载模型: " + gltfPath);
            JFXGLTFAsset asset = new JFXGLTFAsset(gltfPath);

            // 构建 3D 场景
            Group modelGroup = asset.build3DScene(asset.scenes[0]);

            // --- 居中逻辑开始 ---
            double[] bounds = {
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, // min
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY  // max
            };

            float[] identity = {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
            };

            for (gltf.GLTFNode node : asset.scenes[0].nodes) {
                calculateWorldBounds(node, identity, bounds);
            }

            double minX = bounds[0], minY = bounds[1], minZ = bounds[2];
            double maxX = bounds[3], maxY = bounds[4], maxZ = bounds[5];

            double modelRadius = 100;

            if (minX != Double.POSITIVE_INFINITY) {
                double centerX = (minX + maxX) / 2.0;
                double centerY = (minY + maxY) / 2.0;
                double centerZ = (minZ + maxZ) / 2.0;

                // 使模型几何中心位于局部原点
                modelGroup.setTranslateX(-centerX);
                modelGroup.setTranslateY(-centerY);
                modelGroup.setTranslateZ(-centerZ);
                
                double dx = maxX - minX;
                double dy = maxY - minY;
                double dz = maxZ - minZ;
                modelRadius = Math.sqrt(dx*dx + dy*dy + dz*dz) / 2.0;
                
                System.out.println(String.format("模型世界空间中心点: [%.2f, %.2f, %.2f]", centerX, centerY, centerZ));
                System.out.println(String.format("模型世界空间尺寸: [%.2f, %.2f, %.2f]", dx, dy, dz));
            }
            // --- 居中逻辑结束 ---

            // 为了旋转，将 modelGroup 放入 world 组
            Group world = new Group();
            world.getChildren().add(modelGroup);
            
            // 修正 JavaFX 坐标系
            Rotate initialRotate = new Rotate(180, Rotate.X_AXIS);
            world.getTransforms().addAll(initialRotate, rotateX, rotateY);

            // 将 world 放入 root
            root.getChildren().add(world);

            // 设置环境光
            AmbientLight ambientLight = new AmbientLight(Color.WHITE);
            root.getChildren().add(ambientLight);
            
            // 设置点光源
            PointLight pointLight = new PointLight(Color.WHITE);
            pointLight.setTranslateZ(-1000);
            pointLight.setTranslateY(-1000);
            root.getChildren().add(pointLight);

            // 创建场景，Scene 构造函数中的 true 表示启用深度缓冲
            double width = 1024;
            double height = 768;
            Scene scene = new Scene(root, width, height, true, SceneAntialiasing.BALANCED);
            scene.setFill(Color.SILVER);

            // 设置摄像头
            PerspectiveCamera camera = new PerspectiveCamera(true);
            camera.setNearClip(0.1);
            camera.setFarClip(100000.0);
            
            // 自动调整相机距离
            double distance = modelRadius * 3.0; 
            if (distance < 10) distance = 200;
            
            camera.setTranslateZ(-distance); 
            scene.setCamera(camera);
            
            // ！！！核心修复：将 root 移至窗口中心 ！！！
            // 在 PerspectiveCamera(true) 模式下，(0,0,0) 默认在视口中心
            // 但如果之前模型完全看不见，可能是因为 root 的坐标超出了视野。
            // 既然用户反馈“完全看不见”，我们将 root 归零，利用相机后退来观察。
            root.setTranslateX(0);
            root.setTranslateY(0);

            // 添加鼠标交互
            scene.setOnMousePressed(me -> {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
            });

            scene.setOnMouseDragged(me -> {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                double mouseDeltaX = (mousePosX - mouseOldX);
                double mouseDeltaY = (mousePosY - mouseOldY);

                rotateY.setAngle(rotateY.getAngle() + mouseDeltaX * 0.2);
                rotateX.setAngle(rotateX.getAngle() - mouseDeltaY * 0.2);
            });

            scene.setOnScroll(se -> {
                double delta = se.getDeltaY();
                double scale = modelGroup.getScaleX();
                double newScale = delta > 0 ? scale * 1.1 : scale / 1.1;
                modelGroup.setScaleX(newScale);
                modelGroup.setScaleY(newScale);
                modelGroup.setScaleZ(newScale);
            });

            primaryStage.setTitle("JavaFX GLTF Viewer Example");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateWorldBounds(gltf.GLTFNode node, float[] parentTransform, double[] bounds) {
        float[] localTransform = node.transformationMatrix;
        if (isDefaultMatrix(localTransform)) {
            localTransform = computeTRSMatrix(node.translation, node.rotation, node.scale);
        }
        float[] worldTransform = multiplyMatrices(parentTransform, localTransform);
        if (node.mesh != null) {
            for (gltf.mesh.GLTFMeshPrimitive primitive : node.mesh.primitives) {
                if (primitive.attributes.positionsAccessor != null) {
                    float[] min = primitive.attributes.positionsAccessor.min;
                    float[] max = primitive.attributes.positionsAccessor.max;
                    if (min != null && max != null) {
                        float[][] corners = {
                            {min[0], min[1], min[2]}, {min[0], min[1], max[2]},
                            {min[0], max[1], min[2]}, {min[0], max[1], max[2]},
                            {max[0], min[1], min[2]}, {max[0], min[1], max[2]},
                            {max[0], max[1], min[2]}, {max[0], max[1], max[2]}
                        };
                        for (float[] corner : corners) {
                            float[] worldPos = transformPoint(worldTransform, corner);
                            bounds[0] = Math.min(bounds[0], worldPos[0]);
                            bounds[1] = Math.min(bounds[1], worldPos[1]);
                            bounds[2] = Math.min(bounds[2], worldPos[2]);
                            bounds[3] = Math.max(bounds[3], worldPos[0]);
                            bounds[4] = Math.max(bounds[4], worldPos[1]);
                            bounds[5] = Math.max(bounds[5], worldPos[2]);
                        }
                    }
                }
            }
        }
        for (gltf.GLTFNode child : node.getChildren()) {
            calculateWorldBounds(child, worldTransform, bounds);
        }
    }

    private float[] multiplyMatrices(float[] a, float[] b) {
        float[] res = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                res[i + j * 4] = 0;
                for (int k = 0; k < 4; k++) {
                    res[i + j * 4] += a[i + k * 4] * b[k + j * 4];
                }
            }
        }
        return res;
    }

    private float[] transformPoint(float[] m, float[] p) {
        float w = m[3] * p[0] + m[7] * p[1] + m[11] * p[2] + m[15];
        if (Math.abs(w) < 0.000001) w = 1.0f;
        return new float[]{
            (m[0] * p[0] + m[4] * p[1] + m[8] * p[2] + m[12]) / w,
            (m[1] * p[0] + m[5] * p[1] + m[9] * p[2] + m[13]) / w,
            (m[2] * p[0] + m[6] * p[1] + m[10] * p[2] + m[14]) / w
        };
    }

    private boolean isDefaultMatrix(float[] m) {
        if (m == null || m.length != 16) return true;
        for (int i = 0; i < 16; i++) {
            float expected = (i % 5 == 0) ? 1.0f : 0.0f;
            if (Math.abs(m[i] - expected) > 0.000001) return false;
        }
        return true;
    }

    private float[] computeTRSMatrix(float[] t, float[] r, float[] s) {
        float x = r[0], y = r[1], z = r[2], w = r[3];
        float x2 = x + x, y2 = y + y, z2 = z + z;
        float xx = x * x2, xy = x * y2, xz = x * z2;
        float yy = y * y2, yz = y * z2, zz = z * z2;
        float wx = w * x2, wy = w * y2, wz = w * z2;
        float[] m = new float[16];
        m[0] = (1 - (yy + zz)) * s[0]; m[1] = (xy + wz) * s[0]; m[2] = (xz - wy) * s[0]; m[3] = 0;
        m[4] = (xy - wz) * s[1]; m[5] = (1 - (xx + zz)) * s[1]; m[6] = (yz + wx) * s[1]; m[7] = 0;
        m[8] = (xz + wy) * s[2]; m[9] = (yz - wx) * s[2]; m[10] = (1 - (xx + yy)) * s[2]; m[11] = 0;
        m[12] = t[0]; m[13] = t[1]; m[14] = t[2]; m[15] = 1;
        return m;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
