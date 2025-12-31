package gltf.jfx.learn;

import gltf.jfx.example.JFXGLTFAsset;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.io.File;

/**
 * JavaFX 拾取 (Picking) API 演示程序
 * 展示如何对内置几何体和加载的自定义 glTF 模型进行交互拾取
 */
public class JavaFXPickingDemo extends Application {

    private double mousePosX;
    private double mousePosY;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    private Label infoLabel;
    private Group modelGroup;
    private PerspectiveCamera camera;
    private Shape3D lastPickedNode = null;
    private Material lastOriginalMaterial = null;
    // 给高亮材质添加透明效果
//    private final PhongMaterial highlightMaterial = new PhongMaterial(Color.color(0.7, 0.2, 0.2, 0.05));
    private final PhongMaterial highlightMaterial = new PhongMaterial(Color.YELLOW);

    @Override
    public void start(Stage primaryStage) {
        try {

            // 1. 加载模型 (方案 B)
            // 优先从项目资源路径寻找
            String gltfPath = "src/test/resources/robot/scene.gltf";
            File gltfFile = new File(gltfPath);
            if (!gltfFile.exists()) {
                System.out.println("警告: 未找到默认模型路径 " + gltfPath + "，请检查文件位置。");
            }

            JFXGLTFAsset asset = new JFXGLTFAsset(gltfPath);
            modelGroup = asset.build3DScene(asset.scenes[0]);
            modelGroup.setId("RobotModelRoot");
            
            // glTF 通常是 Y-up，JavaFX 是 Y-down，旋转 180 度修正
            modelGroup.getTransforms().add(new Rotate(180, Rotate.X_AXIS));

            // 2. 添加一个内置几何体用于对比
            Box referenceBox = new Box(50, 50, 50);
            referenceBox.setMaterial(new PhongMaterial(Color.SKYBLUE));
            referenceBox.setTranslateX(150);
            referenceBox.setTranslateY(0);

            // 3. 构建 3D 世界容器
            Group world = new Group();
            world.getChildren().addAll(modelGroup, referenceBox);
            world.getTransforms().addAll(rotateY, rotateX);

            // 4. 灯光
            AmbientLight ambientLight = new AmbientLight(Color.rgb(100, 100, 100));
            PointLight pointLight = new PointLight(Color.WHITE);
            pointLight.setTranslateZ(-500);
            pointLight.setTranslateY(-200);

            Group root3D = new Group(world, ambientLight, pointLight);

            // 5. 创建 SubScene
            SubScene subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
            subScene.setFill(Color.DARKSLATEGRAY);

            camera = new PerspectiveCamera(true);
            camera.setNearClip(0.1);
            camera.setFarClip(10000.0);
            camera.setTranslateZ(-800);
            subScene.setCamera(camera);

            // 6. UI 信息面板
            infoLabel = new Label("点击 3D 物体查看拾取信息");
            infoLabel.setTextFill(Color.LIGHTGREEN);
            infoLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));

            Label javaFXPickingApiDemo = new Label("JavaFX Picking API Demo");
            javaFXPickingApiDemo.setTextFill(Color.WHITE);
            VBox uiPanel = new VBox(10, javaFXPickingApiDemo, infoLabel);
            uiPanel.setPadding(new Insets(20));
            uiPanel.setPickOnBounds(false); 
            uiPanel.setMouseTransparent(true); // 让 UI 面板对鼠标完全透明，事件穿透到 3D 场景
            // vbox 可以设置圆角效果
            uiPanel.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.5), new CornerRadii(5), Insets.EMPTY)));
            // uiPanel.setMaxWidth(400);
            uiPanel.setMaxHeight(250);

            StackPane mainRoot = new StackPane(subScene, uiPanel);
            StackPane.setAlignment(uiPanel, Pos.TOP_LEFT);
            StackPane.setMargin(uiPanel, new Insets(10));

            // 绑定尺寸
            subScene.widthProperty().bind(mainRoot.widthProperty());
            subScene.heightProperty().bind(mainRoot.heightProperty());

            Scene scene = new Scene(mainRoot, 1024, 768);

            // 7. 交互逻辑
            
            // 鼠标旋转控制
            scene.setOnMousePressed(me -> {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                
                // 执行拾取检查
                handlePicking(me);
            });

            scene.setOnMouseDragged(me -> {
                double dx = me.getSceneX() - mousePosX;
                double dy = me.getSceneY() - mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                
                rotateY.setAngle(rotateY.getAngle() + dx * 0.2);
                rotateX.setAngle(rotateX.getAngle() - dy * 0.2);
            });

            scene.setOnScroll(se -> {
                double zoom = se.getDeltaY() > 0 ? 0.9 : 1.1;
                camera.setTranslateZ(camera.getTranslateZ() * zoom);
            });

            primaryStage.setTitle("JavaFX 3D Picking Demo - Custom Models & Primitives");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理拾取逻辑
     */
    private void handlePicking(MouseEvent event) {
        PickResult res = event.getPickResult();
        Node intersectedNode = res.getIntersectedNode();

        if (intersectedNode instanceof Shape3D shape) {

            // 视觉反馈：恢复旧的，设置新的
            if (lastPickedNode != null && lastOriginalMaterial != null) {
                lastPickedNode.setMaterial(lastOriginalMaterial);
            }
            
            lastPickedNode = shape;
            lastOriginalMaterial = shape.getMaterial();
            shape.setMaterial(highlightMaterial);

            // 提取详细信息
            StringBuilder sb = new StringBuilder();
            sb.append("命中节点: ").append(getNodeInfo(shape)).append("\n");
            
            Point3D p = res.getIntersectedPoint();
            sb.append(String.format("焦点坐标(模型本地): X:%.2f, Y:%.2f, Z:%.2f\n", p.getX(), p.getY(), p.getZ()));
            
            sb.append("面索引 (Face Index): ").append(res.getIntersectedFace()).append("\n");
            
            Point2D tex = res.getIntersectedTexCoord();
            if (tex != null) {
                sb.append(String.format("纹理坐标 (u,v): %.3f, %.3f\n", tex.getX(), tex.getY()));
            }
            
            sb.append(String.format("API 原始距离: %.2f\n", res.getIntersectedDistance()));

            // 手动计算物理距离
            // 1. 获取交点在世界坐标系中的位置
            Point3D intersectedPointInWorld = shape.localToScene(res.getIntersectedPoint());
            // 2. 获取相机在世界坐标系中的位置
            Point3D cameraPositionInWorld = camera.localToScene(0, 0, 0);
            // 3. 计算两点间的距离
            double physicalDistance = cameraPositionInWorld.distance(intersectedPointInWorld);
            sb.append(String.format("计算物理距离: %.2f\n", physicalDistance));

            // 获取边界信息
            Bounds localBounds = shape.getBoundsInLocal();
            sb.append(String.format("本地边界 (Local): %.1f x %.1f x %.1f\n", 
                localBounds.getWidth(), localBounds.getHeight(), localBounds.getDepth()));
            
            Bounds parentBounds = shape.getBoundsInParent();
            sb.append(String.format("父级边界 (Parent): %.1f x %.1f x %.1f\n", 
                parentBounds.getWidth(), parentBounds.getHeight(), parentBounds.getDepth()));

            // 获取相对于模型根节点的边界
            Bounds rootBounds = JFXGLTFAsset.getBoundsInParents(shape, modelGroup);
            sb.append(String.format("ModelGroup边界 (GLTF Group): %.1f x %.1f x %.1f\n",
                rootBounds.getWidth(), rootBounds.getHeight(), rootBounds.getDepth()));

            // 获取相对于场景的边界
            Bounds sceneBounds = JFXGLTFAsset.getBoundsInParents(shape, null);
            sb.append(String.format("场景边界 (Scene): %.1f x %.1f x %.1f\n", 
                sceneBounds.getWidth(), sceneBounds.getHeight(), sceneBounds.getDepth()));

            infoLabel.setText(sb.toString());
        } else {
            // 点击了背景
            if (lastPickedNode != null && lastOriginalMaterial != null) {
                lastPickedNode.setMaterial(lastOriginalMaterial);
                lastPickedNode = null;
            }
            infoLabel.setText("点击了背景\nPickResult: " + (intersectedNode != null ? intersectedNode.getClass().getSimpleName() : "null"));
        }
    }

    private String getNodeInfo(Node node) {
        String name = node.getId();
        if (name == null || name.isEmpty()) {
            // 尝试查找父级是否有 ID (glTF 加载器通常把名称设为 ID)
            Node parent = node.getParent();
            while (parent != null && (parent.getId() == null || parent.getId().isEmpty())) {
                parent = parent.getParent();
            }
            name = (parent != null) ? parent.getId() : "Unknown Node";
        }
        
        String type = node.getClass().getSimpleName();
        if (node instanceof MeshView) {
            type = "MeshView (Custom Model)";
        } else if (node instanceof Box) {
            type = "Box (Primitive)";
        }
        
        return String.format("[%s] %s", type, name);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
