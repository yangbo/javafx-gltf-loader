package gltf.jfx.learn;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

/**
 * 演示 JavaFX 3D 基础知识：
 * 1. 创建球体
 * 2. 默认 3D 坐标系统原点及坐标轴（X-红, Y-绿, Z-蓝）
 * 3. 默认相机的位置、类型及交互
 * 4. 实时显示相机和物体状态的 HUD
 */
public class JavaFX3DBasics extends Application {

    private PerspectiveCamera camera;
    private Sphere sphere;
    private Label statusLabel;
    private final Group world = new Group();

    @Override
    public void start(Stage primaryStage) {
        // 1. 创建球体并放在原点
        sphere = new Sphere(50);
        PhongMaterial sphereMaterial = new PhongMaterial(); // 冯材质，是一种材质算法名称，以发明人命名
        sphereMaterial.setDiffuseColor(Color.ORANGE);   // 漫反射光颜色
        sphereMaterial.setSpecularColor(Color.WHITE);   // 高光颜色
        sphere.setMaterial(sphereMaterial);
        // 默认位置就是 (0,0,0)
        sphere.setTranslateX(0);
        sphere.setTranslateY(0);
        sphere.setTranslateZ(0);

        // 2. 创建坐标轴辅助线
        Group axisGroup = buildAxes(200);

        // 将物体和坐标轴加入世界模型
        world.getChildren().addAll(sphere, axisGroup);

        // 3. 设置相机
        // fixedEyeAtCameraZero 参数解释：
        // true:  相机位置固定在自身的 (0,0,0)。当你移动相机（setTranslate）时，你是在移动整个视点，这更符合真实的 3D 场景逻辑。
        // false: 默认值。相机的视点会根据画布尺寸进行偏移，使 (0,0) 对应左上角，且在 Z=0 处的单位与像素对应，主要用于 2D/3D 混合布局。
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        
        // 默认情况下，相机在 (0,0,0) 且看向 Z 轴正方向。
        // 为了看到原点的球体，我们需要将相机沿 Z 轴后退（负方向移动）
        camera.setTranslateZ(-500);

        // 4. 构建场景层级
        // 使用 SubScene 来容纳 3D 内容
        SubScene subScene = new SubScene(world, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.ALICEBLUE);
        subScene.setCamera(camera);

        // 5. 构建 2D HUD (信息显示层)
        statusLabel = new Label();
        statusLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-text-fill: white; -fx-padding: 10; -fx-font-family: 'Consolas';");
        updateStatus();

        VBox hud = new VBox(statusLabel);
        hud.setAlignment(Pos.TOP_RIGHT);
        hud.setPickOnBounds(false); // 允许点击穿透 HUD 层

        StackPane root = new StackPane(subScene, hud);

        // 6. 键盘交互
        Scene scene = new Scene(root, 800, 600);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            double moveAmount = 10;
            switch (event.getCode()) {
                case UP:
                    // 前进：沿 Z 轴正向移动（靠近原点/物体）
                    camera.setTranslateZ(camera.getTranslateZ() + moveAmount);
                    break;
                case DOWN:
                    // 后退：沿 Z 轴负向移动（远离原点/物体）
                    camera.setTranslateZ(camera.getTranslateZ() - moveAmount);
                    break;
                default:
                    break;
            }
            updateStatus();
        });

        primaryStage.setTitle("JavaFX 3D 基础演示");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 构建坐标轴辅助线
     * X 轴：红色
     * Y 轴：绿色
     * Z 轴：蓝色
     */
    private Group buildAxes(double length) {
        double thickness = 2.0;
        
        // X 轴
        Box xAxis = new Box(length, thickness, thickness);
        xAxis.setMaterial(new PhongMaterial(Color.RED));
        xAxis.setTranslateX(length / 2);    // 因为 Box 立方体的模型中心在立方体中心

        // Y 轴
        Box yAxis = new Box(thickness, length, thickness);
        yAxis.setMaterial(new PhongMaterial(Color.GREEN));
        yAxis.setTranslateY(length / 2);

        // Z 轴
        Box zAxis = new Box(thickness, thickness, length);
        zAxis.setMaterial(new PhongMaterial(Color.BLUE));
        zAxis.setTranslateZ(length / 2);

        return new Group(xAxis, yAxis, zAxis);
    }

    /**
     * 更新 HUD 信息
     */
    private void updateStatus() {
        String info = String.format(
                "--- 状态信息 ---\n" +
                "相机类型: PerspectiveCamera\n" +
                "相机位置: [X: %.1f, Y: %.1f, Z: %.1f]\n" +
                "相机方向: 看向 Z 轴正方向\n" +
                "球体位置: [X: %.1f, Y: %.1f, Z: %.1f]\n" +
                "操作说明: 使用键盘 [上下箭头] 控制相机前进/后退",
                camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ(),
                sphere.getTranslateX(), sphere.getTranslateY(), sphere.getTranslateZ()
        );
        statusLabel.setText(info);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
