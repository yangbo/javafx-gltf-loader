package gltf.jfx.learn;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * 演示 JavaFX 透视相机 (PerspectiveCamera) 的 fieldOfView (FOV) 属性的作用。
 * 包含长焦、标准和鱼眼三种镜头的快速切换。
 */
public class JavaFXCameraFOVExample extends Application {

    private PerspectiveCamera camera;
    private Slider fovSlider;
    private Slider rotateYSlider;
    private Slider rotateXSlider;
    private Slider nearClipSlider;
    private Slider farClipSlider;
    private Label fovValueLabel;
    private Label lensTypeLabel;
    private Label cameraInfoLabel; // HUD 标签

    @Override
    public void start(Stage primaryStage) {
        // 1. 创建 3D 内容
        Group sceneRoot = new Group();
        setup3DScene(sceneRoot);

        // 2. 设置相机
        camera = new PerspectiveCamera(true); // true 表示固定在原点，观察 Z 正方向
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-500); // 将相机后退一点，以便看清场景

        // 3. 创建 SubScene 用于 3D 渲染
        SubScene subScene = new SubScene(sceneRoot, 1000, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.ALICEBLUE);
        subScene.setCamera(camera);

        // 创建 HUD (Heads-Up Display)
        cameraInfoLabel = new Label();
        cameraInfoLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-text-fill: white; -fx-padding: 10px; -fx-font-family: 'Consolas', 'Monospaced';");
        cameraInfoLabel.setTranslateX(10);
        cameraInfoLabel.setTranslateY(10);
        updateCameraHUD();

        // 将 HUD 叠加在 subScene 之上
        Group uiOverlay = new Group(subScene, cameraInfoLabel);

        // 4. 创建 UI 控制面板
        VBox controls = createControls();

        // 5. 布局
        BorderPane root = new BorderPane();
        root.setCenter(uiOverlay);
        root.setBottom(controls);

        Scene scene = new Scene(root, 1024, 850);
        primaryStage.setTitle("JavaFX PerspectiveCamera 参数演示 (FOV, Clipping, Rotation, Zoom)");

        // 添加键盘控制：上下按键修改相机 Z 轴位置（前进/后退）
        scene.setOnKeyPressed(event -> {
            double step = 20;
            switch (event.getCode()) {
                case UP -> camera.setTranslateZ(camera.getTranslateZ() + step);
                case DOWN -> camera.setTranslateZ(camera.getTranslateZ() - step);
            }
            updateCameraHUD();
        });

        primaryStage.setScene(scene);
        primaryStage.show();
        
        // 确保场景获取焦点以接收按键事件
        subScene.requestFocus();
    }

    private void setup3DScene(Group root) {
        // 添加一些灯光
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateZ(-1000);
        root.getChildren().add(light);
        root.getChildren().add(new AmbientLight(Color.rgb(100, 100, 100)));

        // 创建一个由大量立方体组成的环绕场景，更好地演示 180° 视角
        // 在相机周围放置一个阵列，包括侧面和后方
        for (int z = -2; z < 15; z++) { // 从相机后方一点开始到远方
            for (int x = -5; x <= 5; x++) { // 左右铺开
                if (x == 0 && z < 2) continue; // 给相机留出位置

                Box box = new Box(40, 40, 40);
                box.setTranslateX(x * 300);
                box.setTranslateY(Math.sin(x * 0.5 + z * 0.5) * 100); // 波动的高度
                box.setTranslateZ(z * 300);
                
                // 颜色根据位置变化
                double hue = (x + 5) * 18 + (z + 2) * 10;
                box.setMaterial(new PhongMaterial(Color.hsb(hue % 360, 0.8, 0.9)));
                
                root.getChildren().add(box);
            }
        }

        // 添加大量的球体悬浮在空中，增加空间感
        for (int i = 0; i < 50; i++) {
            Sphere sphere = new Sphere(15);
            // 随机分布在相机周围，包括极宽的角度
            double angle = Math.random() * Math.PI * 2;
            double dist = 400 + Math.random() * 1000;
            sphere.setTranslateX(Math.cos(angle) * dist);
            sphere.setTranslateY(-200 - Math.random() * 400);
            sphere.setTranslateZ(Math.sin(angle) * dist);
            sphere.setMaterial(new PhongMaterial(Color.WHITE));
            root.getChildren().add(sphere);
        }

        // 添加一个更大的地面参考，方便观察边缘拉伸
        Box ground = new Box(4000, 5, 6000);
        ground.setTranslateY(200);
        ground.setTranslateZ(2000);
        PhongMaterial groundMat = new PhongMaterial(Color.DARKSLATEGRAY);
        ground.setMaterial(groundMat);
        root.getChildren().add(ground);
        
        // 添加墙壁（参考物）以便观察鱼眼的极广视角
        Box leftWall = new Box(5, 1000, 6000);
        leftWall.setTranslateX(-2000);
        leftWall.setTranslateZ(2000);
        leftWall.setMaterial(new PhongMaterial(Color.DARKRED));
        
        Box rightWall = new Box(5, 1000, 6000);
        rightWall.setTranslateX(2000);
        rightWall.setTranslateZ(2000);
        rightWall.setMaterial(new PhongMaterial(Color.DARKBLUE));
        
        root.getChildren().addAll(leftWall, rightWall);

        // 在相机非常近的地方添加物体，以便演示近裁剪面效果
        // 相机初始 TranslateZ 为 -500，相对于原点
        // 我们在相机正前方稍微偏左下的位置放两个物体
        
        // 近处的小立方体
        Box nearBox = new Box(30, 30, 30);
        nearBox.setTranslateX(-80);
        nearBox.setTranslateY(80);
        nearBox.setTranslateZ(-400); // 相机在 -500，这个物体在 -400，距离相机仅 100 单位
        nearBox.setMaterial(new PhongMaterial(Color.YELLOW));
        
        // 近处的小球
        Sphere nearSphere = new Sphere(20);
        nearSphere.setTranslateX(-120);
        nearSphere.setTranslateY(120);
        nearSphere.setTranslateZ(-350); // 距离相机 150 单位
        nearSphere.setMaterial(new PhongMaterial(Color.CYAN));
        
        root.getChildren().addAll(nearBox, nearSphere);
    }

    private VBox createControls() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: #f0f0f0;");

        // FOV 滑动条
        HBox sliderBox = new HBox(10);
        sliderBox.setAlignment(Pos.CENTER);
        Label label = new Label("Field of View (FOV):");
        fovSlider = new Slider(1, 170, 45);
        fovSlider.setPrefWidth(400);
        fovSlider.setShowTickLabels(true);
        fovSlider.setShowTickMarks(true);
        
        fovValueLabel = new Label("45.0°");
        sliderBox.getChildren().addAll(label, fovSlider, fovValueLabel);

        // 镜头类型描述
        lensTypeLabel = new Label("当前镜头: 标准镜头 (Normal)");
        lensTypeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        // 快速切换按钮
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button teleBtn = new Button("长焦镜头 (Telephoto)");
        teleBtn.setOnAction(e -> fovSlider.setValue(15));

        Button normBtn = new Button("标准镜头 (Normal)");
        normBtn.setOnAction(e -> fovSlider.setValue(45));

        Button fishBtn = new Button("鱼眼镜头 (Fisheye)");
        fishBtn.setOnAction(e -> fovSlider.setValue(140));

        buttonBox.getChildren().addAll(teleBtn, normBtn, fishBtn);

        // 相机旋转控制 (Y轴旋转 - 左右)
        HBox rotateYBox = new HBox(10);
        rotateYBox.setAlignment(Pos.CENTER);
        Label rotateYLabel = new Label("水平旋转 (Yaw):");
        rotateYSlider = new Slider(-180, 180, 0);
        rotateYSlider.setPrefWidth(400);
        Label rotateYValue = new Label("0.0°");
        rotateYBox.getChildren().addAll(rotateYLabel, rotateYSlider, rotateYValue);

        // 相机旋转控制 (X轴旋转 - 上下)
        HBox rotateXBox = new HBox(10);
        rotateXBox.setAlignment(Pos.CENTER);
        Label rotateXLabel = new Label("垂直旋转 (Pitch):");
        rotateXSlider = new Slider(-45, 45, 0);
        rotateXSlider.setPrefWidth(400);
        Label rotateXValue = new Label("0.0°");
        rotateXBox.getChildren().addAll(rotateXLabel, rotateXSlider, rotateXValue);

        // 使用变换组件
        Rotate rx = new Rotate(0, Rotate.X_AXIS);
        Rotate ry = new Rotate(0, Rotate.Y_AXIS);
        camera.getTransforms().addAll(ry, rx);

        // 绑定逻辑
        fovSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double fov = newVal.doubleValue();
            camera.setFieldOfView(fov);
            fovValueLabel.setText(String.format("%.1f°", fov));
            updateLensDescription(fov);
            updateCameraHUD();
        });

        rotateYSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ry.setAngle(newVal.doubleValue());
            rotateYValue.setText(String.format("%.1f°", newVal.doubleValue()));
            updateCameraHUD();
        });

        rotateXSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rx.setAngle(newVal.doubleValue());
            rotateXValue.setText(String.format("%.1f°", newVal.doubleValue()));
            updateCameraHUD();
        });

        // 近裁剪面控制 (Near Clip)
        HBox nearClipBox = new HBox(10);
        nearClipBox.setAlignment(Pos.CENTER);
        Label nearClipLabel = new Label("近裁剪面 (Near Clip):");
        nearClipSlider = new Slider(0.1, 500, 0.1);
        nearClipSlider.setPrefWidth(400);
        Label nearClipValue = new Label("0.1");
        nearClipBox.getChildren().addAll(nearClipLabel, nearClipSlider, nearClipValue);

        // 远裁剪面控制 (Far Clip)
        HBox farClipBox = new HBox(10);
        farClipBox.setAlignment(Pos.CENTER);
        Label farClipLabel = new Label("远裁剪面 (Far Clip):");
        farClipSlider = new Slider(500, 10000, 10000);
        farClipSlider.setPrefWidth(400);
        Label farClipValue = new Label("10000.0");
        farClipBox.getChildren().addAll(farClipLabel, farClipSlider, farClipValue);

        nearClipSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            camera.setNearClip(newVal.doubleValue());
            nearClipValue.setText(String.format("%.1f", newVal.doubleValue()));
            updateCameraHUD();
        });

        farClipSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            camera.setFarClip(newVal.doubleValue());
            farClipValue.setText(String.format("%.1f", newVal.doubleValue()));
            updateCameraHUD();
        });

        Label hintLabel = new Label("提示：使用键盘 [↑/↓] 按键可前后移动相机 (改变 Z 轴)");
        hintLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");

        vbox.getChildren().addAll(sliderBox, rotateYBox, rotateXBox, nearClipBox, farClipBox, lensTypeLabel, buttonBox, hintLabel);
        return vbox;
    }

    private void updateCameraHUD() {
        if (camera != null && cameraInfoLabel != null) {
            double rx = 0, ry = 0;
            for (javafx.scene.transform.Transform t : camera.getTransforms()) {
                if (t instanceof Rotate) {
                    Rotate r = (Rotate) t;
                    if (r.getAxis().equals(Rotate.X_AXIS)) rx = r.getAngle();
                    if (r.getAxis().equals(Rotate.Y_AXIS)) ry = r.getAngle();
                }
            }
            String info = String.format(
                    """
                    相机位置: [X: %.2f, Y: %.2f, Z: %.2f]
                    旋转: [Pitch: %.2f°, Yaw: %.2f°]
                    视角 (FOV): %.2f°
                    近剪裁面: %.1f
                    远剪裁面: %.1f""",
                    camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ(),
                    rx, ry,
                    camera.getFieldOfView(),
                    camera.getNearClip(),
                    camera.getFarClip()
            );
            cameraInfoLabel.setText(info);
        }
    }

    private void updateLensDescription(double fov) {
        if (fov >= 120) {
            lensTypeLabel.setText("当前镜头: 鱼眼镜头 (Fisheye) - 视野极广，边缘畸变大");
        } else if (fov >= 62) {
            lensTypeLabel.setText("当前镜头: 广角镜头 (Wide Angle) - 适合大场景");
        } else if (fov >= 40) {
            lensTypeLabel.setText("当前镜头: 标准镜头 (Normal) - 接近人眼自然视角");
        } else if (fov >= 30) {
            lensTypeLabel.setText("当前镜头: 中焦镜头 (Medium) - 稍微的压缩感");
        } else {
            lensTypeLabel.setText("当前镜头: 长焦镜头 (Telephoto) - 视野窄，空间压缩感强");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
