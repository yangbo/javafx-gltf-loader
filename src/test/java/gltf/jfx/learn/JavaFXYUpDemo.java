package gltf.jfx.learn;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * 演示 JavaFX 3D 坐标系从 Y-down (默认) 转换为 Y-up 的三种方式
 */
public class JavaFXYUpDemo extends Application {

    private static final double SCENE_WIDTH = 800;
    private static final double SCENE_HEIGHT = 600;

    private Group rootContent;      // 包含所有 3D 模型的根节点
    private Group cameraPivot;      // 相机的枢轴节点
    private PerspectiveCamera camera;

    private Label cameraInfoLabel;  // HUD 显示相机信息
    
    private Rotate rootRotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate cameraPivotRotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate cameraDirectRotateX = new Rotate(0, Rotate.X_AXIS);

    // 鼠标旋转变换
    private Rotate mouseRotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate mouseRotateY = new Rotate(0, Rotate.Y_AXIS);

    @Override
    public void start(Stage primaryStage) {
        // 1. 创建 3D 内容
        rootContent = new Group();
        setupAxes(rootContent);
        setupModel(rootContent);

        // 2. 设置相机及其容器
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-500);

        cameraPivot = new Group(camera);
        
        // 将变换添加到对应的节点
        rootContent.getTransforms().add(rootRotateX);
        cameraPivot.getTransforms().add(cameraPivotRotateX);
        camera.getTransforms().add(cameraDirectRotateX);

        // 3. 布局
        SubScene subScene = new SubScene(new Group(rootContent, cameraPivot), SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.ALICEBLUE);
        subScene.setCamera(camera);

        // 鼠标控制旋转 (简单的视角观察)
        setupMouseControl(subScene, rootContent);

        // 4. UI 控制面板
        VBox uiPanel = setupUI();

        // 5. 设置每帧更新 HUD
        setupHUDUpdater();

        // 使用 Pane 作为容器以支持自动调整大小
        Pane rootPane = new Pane(subScene, uiPanel);
        
        // 绑定 SubScene 的宽高到容器宽高
        subScene.widthProperty().bind(rootPane.widthProperty());
        subScene.heightProperty().bind(rootPane.heightProperty());

        Scene scene = new Scene(rootPane, SCENE_WIDTH, SCENE_HEIGHT);

        primaryStage.setTitle("JavaFX 3D Y-down to Y-up Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupAxes(Group parent) {
        double length = 300;
        double width = 2;

        // X 轴 - 红色
        Box xAxis = new Box(length, width, width);
        xAxis.setMaterial(new PhongMaterial(Color.RED));
        xAxis.setTranslateX(length / 2);

        // Y 轴 - 绿色
        Box yAxis = new Box(width, length, width);
        yAxis.setMaterial(new PhongMaterial(Color.GREEN));
        yAxis.setTranslateY(length / 2);

        // Z 轴 - 蓝色
        Box zAxis = new Box(width, width, length);
        zAxis.setMaterial(new PhongMaterial(Color.BLUE));
        zAxis.setTranslateZ(length / 2);

        parent.getChildren().addAll(xAxis, yAxis, zAxis);
        
        // 添加一些文字标识（可选，这里用小球代表正向）
        Box yPositiveIndicator = new Box(10, 10, 10);
        yPositiveIndicator.setMaterial(new PhongMaterial(Color.GREEN));
        yPositiveIndicator.setTranslateY(length);
        parent.getChildren().add(yPositiveIndicator);
    }

    private void setupModel(Group parent) {
        // 创建一个由 6 个不同颜色面组成的彩色立方体
        double size = 100;
        Group cube = createMultiColorCube(size);
        
        // 默认坐标系下，Y 轴向下。
        // 我们将立方体放在中心稍微靠下的位置
        cube.setTranslateY(size / 2); 
        
        parent.getChildren().add(cube);
    }

    private Group createMultiColorCube(double size) {
        Group cube = new Group();
        double h = size / 2;

        // 前面 (Z+) - 红色
        cube.getChildren().add(createFace(size, size, Color.RED, 0, 0, h, 0));
        // 后面 (Z-) - 橙色
        cube.getChildren().add(createFace(size, size, Color.ORANGE, 0, 0, -h, 180));
        // 上面 (Y-) - 绿色 (JavaFX 默认 Y 向下，所以 Y- 是物理意义上的上方)
        cube.getChildren().add(createFace(size, size, Color.GREEN, 0, -h, 0, 90, Rotate.X_AXIS));
        // 下面 (Y+) - 黄色
        cube.getChildren().add(createFace(size, size, Color.YELLOW, 0, h, 0, -90, Rotate.X_AXIS));
        // 左面 (X-) - 蓝色
        cube.getChildren().add(createFace(size, size, Color.BLUE, -h, 0, 0, -90, Rotate.Y_AXIS));
        // 右面 (X+) - 紫色
        cube.getChildren().add(createFace(size, size, Color.PURPLE, h, 0, 0, 90, Rotate.Y_AXIS));

        return cube;
    }

    private Box createFace(double w, double h, Color color, double tx, double ty, double tz, double angle) {
        return createFace(w, h, color, tx, ty, tz, angle, Rotate.Y_AXIS);
    }

    private Box createFace(double w, double h, Color color, double tx, double ty, double tz, double angle, Point3D axis) {
        Box face = new Box(w, h, 1); // 很薄的盒子作为面
        face.setMaterial(new PhongMaterial(color));
        face.setTranslateX(tx);
        face.setTranslateY(ty);
        face.setTranslateZ(tz);
        face.setRotationAxis(axis);
        face.setRotate(angle);
        return face;
    }

    private VBox setupUI() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-padding: 10;");

        cameraInfoLabel = new Label("相机信息: ");
        cameraInfoLabel.setStyle("-fx-font-family: 'Monospaced'; -fx-text-fill: #333;");

        Label label = new Label("选择转换方式:");
        ToggleGroup group = new ToggleGroup();

        RadioButton rbDefault = new RadioButton("默认 (Y-down)");
        rbDefault.setToggleGroup(group);
        rbDefault.setSelected(true);

        RadioButton rbRotateRoot = new RadioButton("旋转根 Group (Rotate Root)");
        rbRotateRoot.setToggleGroup(group);

        RadioButton rbRotatePivot = new RadioButton("旋转相机枢轴 (Rotate Camera Pivot)");
        rbRotatePivot.setToggleGroup(group);

        RadioButton rbRotateCamera = new RadioButton("直接旋转相机 (Rotate Camera Directly)");
        rbRotateCamera.setToggleGroup(group);

        Button btnResetMouse = new Button("重置鼠标旋转");
        btnResetMouse.setOnAction(e -> {
            mouseRotateX.setAngle(0);
            mouseRotateY.setAngle(0);
        });

        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            resetTransforms();
            if (newVal == rbRotateRoot) {
                rootRotateX.setAngle(180);
            } else if (newVal == rbRotatePivot) {
                cameraPivotRotateX.setAngle(180);
            } else if (newVal == rbRotateCamera) {
                cameraDirectRotateX.setAngle(180);
            }
        });

        panel.getChildren().addAll(cameraInfoLabel, new javafx.scene.control.Separator(), btnResetMouse, new javafx.scene.control.Separator(), label, rbDefault, rbRotateRoot, rbRotatePivot, rbRotateCamera);
        return panel;
    }

    private void setupHUDUpdater() {
        // 使用 AnimationTimer 实时更新相机信息
        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                updateCameraHUD();
            }
        };
        timer.start();
    }

    private void updateCameraHUD() {
        // 1. 相机本地属性 (相对于其直接父节点)
        double tx = camera.getTranslateX();
        double ty = camera.getTranslateY();
        double tz = camera.getTranslateZ();
        double localRotate = cameraDirectRotateX.getAngle();

        // 2. 世界坐标系中的状态
        // 世界位置 (把相机本地原点转到世界)
        Point3D worldPos = camera.localToScene(0, 0, 0);
        
        // 世界视线向量 (把相机本地 Z轴 1.0 的点转到世界，减去世界位置)
        Point3D worldLookVec = camera.localToScene(0, 0, 1).subtract(worldPos);

        String info = String.format(
                "--- 相机本地属性 ---\n" +
                "本地坐标: [X: %.2f, Y: %.2f, Z: %.2f]\n" +
                "本地 X轴旋转: %.1f°\n\n" +
                "--- 世界空间状态 ---\n" +
                "世界位置: [X: %.2f, Y: %.2f, Z: %.2f]\n" +
                "世界视线: [X: %.2f, Y: %.2f, Z: %.2f]",
                tx, ty, tz, localRotate,
                worldPos.getX(), worldPos.getY(), worldPos.getZ(),
                worldLookVec.getX(), worldLookVec.getY(), worldLookVec.getZ()
        );
        cameraInfoLabel.setText(info);
    }

    private void resetTransforms() {
        rootRotateX.setAngle(0);
        cameraPivotRotateX.setAngle(0);
        cameraDirectRotateX.setAngle(0);
    }

    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;

    private void setupMouseControl(SubScene scene, Group group) {
        group.getTransforms().addAll(mouseRotateX, mouseRotateY);

        scene.setOnMousePressed(event -> {
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        scene.setOnMouseDragged(event -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            double mouseDeltaX = (mousePosX - mouseOldX);
            double mouseDeltaY = (mousePosY - mouseOldY);

            mouseRotateY.setAngle(mouseRotateY.getAngle() + mouseDeltaX * 0.2);
            mouseRotateX.setAngle(mouseRotateX.getAngle() - mouseDeltaY * 0.2);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
