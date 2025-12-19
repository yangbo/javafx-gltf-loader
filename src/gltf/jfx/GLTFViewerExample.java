package gltf.jfx;

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

            // 加载 glTF 资产 (使用项目自带的测试文件)
            String gltfPath = "C:\\Users\\yangbo\\Downloads\\railway_tracks\\scene.gltf";
            JFXGLTFAsset asset = new JFXGLTFAsset(gltfPath);

            // 构建 3D 场景
            Group modelGroup = asset.build3DScene(asset.scenes[0]);

            // --- 居中逻辑开始 ---
            double[] bounds = {
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, // min
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY  // max
            };

            // 使用单位矩阵开始递归计算世界空间包围盒
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

            // 如果找到了有效的边界，则应用居中变换
            if (minX != Double.POSITIVE_INFINITY) {
                double centerX = (minX + maxX) / 2.0;
                double centerY = (minY + maxY) / 2.0;
                double centerZ = (minZ + maxZ) / 2.0;

                // 应用居中位移
                modelGroup.setTranslateX(-centerX);
                modelGroup.setTranslateY(-centerY);
                modelGroup.setTranslateZ(-centerZ);
                
                System.out.println(String.format("模型世界空间中心点: [%.2f, %.2f, %.2f]", centerX, centerY, centerZ));
                System.out.println(String.format("模型世界空间尺寸: [%.2f, %.2f, %.2f]", maxX-minX, maxY-minY, maxZ-minZ));
            }
            // --- 居中逻辑结束 ---

            // 为了更好的观察，我们将模型放在一个可以旋转的组中
            Group world = new Group();
            world.getChildren().add(modelGroup);
            world.getTransforms().addAll(rotateX, rotateY);

            root.getChildren().add(world);

            // 设置环境光，否则模型可能太暗
            AmbientLight ambientLight = new AmbientLight(Color.WHITE);
            root.getChildren().add(ambientLight);
            
            // 设置点光源
            PointLight pointLight = new PointLight(Color.WHITE);
            pointLight.setTranslateZ(-100);
            pointLight.setTranslateY(-100);
            root.getChildren().add(pointLight);

            // 创建场景
            Scene scene = new Scene(root, 1024, 768, true, SceneAntialiasing.BALANCED);
            scene.setFill(Color.SILVER);

            // 设置摄像头
            PerspectiveCamera camera = new PerspectiveCamera(true);
            camera.setNearClip(0.1);
            camera.setFarClip(10000.0);
            camera.setTranslateZ(-200); // 将摄像头后退一点
            scene.setCamera(camera);

            // 添加鼠标交互，用于旋转模型
            scene.setOnMousePressed(me -> {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
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

            // 添加滚轮交互，用于缩放模型
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

            System.out.println("成功加载模型: " + gltfPath);

        } catch (Exception e) {
            System.err.println("加载模型时发生错误:");
            e.printStackTrace();
        }
    }

    /**
     * 递归计算节点及其子节点的世界空间包围盒
     */
    private void calculateWorldBounds(gltf.GLTFNode node, float[] parentTransform, double[] bounds) {
        // 1. 计算当前节点的世界变换矩阵
        float[] localTransform = node.transformationMatrix;
        // 如果有 translation/rotation/scale，应该叠加（这里简化处理，优先使用 matrix）
        // glTF 规范要求：如果定义了 matrix，则忽略 T/R/S
        float[] worldTransform = multiplyMatrices(parentTransform, localTransform);

        // 2. 如果节点有 Mesh，计算其在世界空间下的边界
        if (node.mesh != null) {
            for (gltf.mesh.GLTFMeshPrimitive primitive : node.mesh.primitives) {
                if (primitive.attributes.positionsAccessor != null) {
                    float[] min = primitive.attributes.positionsAccessor.min;
                    float[] max = primitive.attributes.positionsAccessor.max;
                    if (min != null && max != null) {
                        // 局部空间的 8 个顶点
                        float[][] corners = {
                            {min[0], min[1], min[2]},
                            {min[0], min[1], max[2]},
                            {min[0], max[1], min[2]},
                            {min[0], max[1], max[2]},
                            {max[0], min[1], min[2]},
                            {max[0], min[1], max[2]},
                            {max[0], max[1], min[2]},
                            {max[0], max[1], max[2]}
                        };

                        // 转换到世界空间并更新全局边界
                        for (float[] corner : corners) {
                            float[] worldPos = transformPoint(worldTransform, corner);
                            bounds[0] = Math.min(bounds[0], worldPos[0]);
                            bounds[1] = Math.min(bounds[1], worldPos[1]);
                            bounds[2] = Math.min(bounds[2], worldPos[2]);
                            bounds[3] = Math.max(bounds[3], worldPos[3]);
                            bounds[4] = Math.max(bounds[4], worldPos[4]);
                            bounds[5] = Math.max(bounds[5], worldPos[5]);
                        }
                    }
                }
            }
        }

        // 3. 递归处理子节点
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
        float[] res = new float[3];
        float w = m[3] * p[0] + m[7] * p[1] + m[11] * p[2] + m[15];
        res[0] = (m[0] * p[0] + m[4] * p[1] + m[8] * p[2] + m[12]) / w;
        res[1] = (m[1] * p[0] + m[5] * p[1] + m[9] * p[2] + m[13]) / w;
        res[2] = (m[2] * p[0] + m[6] * p[1] + m[10] * p[2] + m[14]) / w;
        return new float[]{res[0], res[1], res[2], 0, 0, 0}; // 扩充以匹配 index 使用，实际只用前 3 个
    }

    public static void main(String[] args) {
        launch(args);
    }
}
