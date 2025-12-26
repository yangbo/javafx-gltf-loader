package gltf.jfx.learn;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * 演示 JavaFX 环境光和点光源的示例程序。
 * 支持移动、旋转单个灯光，以及整体移动、旋转灯光组。
 */
public class JavaFXLightingDemo extends Application {

    private static final double SCENE_WIDTH = 1024;
    private static final double SCENE_HEIGHT = 768;

    // 3D 节点
    private Group root3D;
    private Group lightGroup;      // 包含点光源及其视觉表示的组
    private PointLight pointLight;
    private Sphere lightVisual;    // 点光源的视觉球体
    private AmbientLight ambientLight;

    // 变换
    private Rotate groupRotateY = new Rotate(0, Rotate.Y_AXIS);
    private Rotate groupRotateX = new Rotate(0, Rotate.X_AXIS);
    
    // 鼠标控制视角
    private Rotate cameraRotateX = new Rotate(-20, Rotate.X_AXIS);
    private Rotate cameraRotateY = new Rotate(30, Rotate.Y_AXIS);

    // HUD
    private Label hudLabel;

    @Override
    public void start(Stage primaryStage) {
        root3D = new Group();

        // 1. 设置场景内容
        setupShapes();
        setupAxes();

        // 2. 设置灯光
        setupLights();

        // 3. 设置相机
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-800);

        Group cameraGroup = new Group(camera);
        cameraGroup.getTransforms().addAll(cameraRotateY, cameraRotateX);

        // 4. 创建 SubScene
        SubScene subScene = new SubScene(new Group(root3D, cameraGroup, lightGroup), SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);

        // 5. 设置 UI 和 HUD
        VBox uiRoot = setupUI();
        
        // 布局
        Group root = new Group(subScene, uiRoot);
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);

        // 交互控制
        setupInteractions(scene, subScene);

        // HUD 更新
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateHUD();
            }
        }.start();

        primaryStage.setTitle("JavaFX 3D Lighting Demo - 小塔演示");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupShapes() {
        // 中心大球体
        Sphere sphere = new Sphere(100);
        PhongMaterial mat = new PhongMaterial(Color.WHITE);
        mat.setSpecularColor(Color.LIGHTBLUE);
        sphere.setMaterial(mat);
        sphere.setTranslateY(0);

        // 周围的一些方块
        for (int i = 0; i < 8; i++) {
            Box box = new Box(40, 40, 40);
            box.setMaterial(new PhongMaterial(Color.web("0x3498db")));
            double angle = i * Math.PI / 4;
            box.setTranslateX(Math.cos(angle) * 250);
            box.setTranslateZ(Math.sin(angle) * 250);
            box.setTranslateY(50);
            root3D.getChildren().add(box);
        }

        root3D.getChildren().add(sphere);
    }

    private void setupAxes() {
        double len = 400;
        double w = 2;
        
        Box xAxis = new Box(len, w, w);
        xAxis.setMaterial(new PhongMaterial(Color.RED));
        xAxis.setTranslateX(len / 2);

        Box yAxis = new Box(w, len, w);
        yAxis.setMaterial(new PhongMaterial(Color.GREEN));
        yAxis.setTranslateY(len / 2);

        Box zAxis = new Box(w, w, len);
        zAxis.setMaterial(new PhongMaterial(Color.BLUE));
        zAxis.setTranslateZ(len / 2);

        root3D.getChildren().addAll(xAxis, yAxis, zAxis);
    }

    private void setupLights() {
        // 1. 环境光
        ambientLight = new AmbientLight(Color.rgb(50, 50, 50));

        // 2. 点光源组
        pointLight = new PointLight(Color.WHITE);
        
        // 视觉表示
        lightVisual = new Sphere(10);
        PhongMaterial lightMat = new PhongMaterial();
        lightMat.setDiffuseColor(Color.BLACK);
        lightMat.setSpecularColor(Color.BLACK);
        // 使用自发光颜色（emissiveColor）使球体看起来在发光，且不受点光源自身影响
        lightMat.setSelfIlluminationMap(null); 
        lightMat.setDiffuseColor(Color.YELLOW); // 备选，简单起见直接设置颜色
        
        // 重新配置：JavaFX 的 Shape3D 并没有 setLightOn 方法
        // 我们通过设置一个不受光照影响的材质来模拟
        PhongMaterial emmisionMat = new PhongMaterial(Color.YELLOW);
        emmisionMat.setSelfIlluminationMap(null); 
        
        lightVisual.setMaterial(emmisionMat);

        lightGroup = new Group(pointLight, lightVisual);
        lightGroup.setTranslateX(200);
        lightGroup.setTranslateY(-200);
        lightGroup.setTranslateZ(-200);

        // 添加变换
        lightGroup.getTransforms().addAll(groupRotateY, groupRotateX);

        root3D.getChildren().add(ambientLight);
    }

    private VBox setupUI() {
        VBox container = new VBox(15);
        container.setPrefWidth(300);
        container.setStyle("-fx-background-color: rgba(40, 40, 40, 0.8); -fx-padding: 20; -fx-text-fill: white;");
        container.setAlignment(Pos.TOP_LEFT);

        hudLabel = new Label();
        hudLabel.setStyle("-fx-font-family: 'Consolas', 'Monospaced'; -fx-text-fill: #00ff00; -fx-font-size: 13px;");

        Label title = new Label("灯光控制面板");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");

        // 灯光颜色滑块 (示例)
        Label ambLabel = new Label("环境光强度:");
        ambLabel.setStyle("-fx-text-fill: white;");
        Slider ambSlider = new Slider(0, 1, 0.2);
        ambSlider.valueProperty().addListener((obs, oldV, newV) -> {
            double val = newV.doubleValue();
            ambientLight.setColor(Color.color(val, val, val));
        });

        Label instr = new Label(
                "操作说明:\n" +
                "- 鼠标右键拖拽: 旋转摄像机视角\n" +
                "- W/S: 灯光前后移动 (Z轴)\n" +
                "- A/D: 灯光左右移动 (X轴)\n" +
                "- Q/E: 灯光上下移动 (Y轴)\n" +
                "- 方向键: 旋转灯光组\n" +
                "- R键: 重置位置"
        );
        instr.setStyle("-fx-text-fill: #aaa; -fx-padding: 10 0 0 0;");

        container.getChildren().addAll(title, hudLabel, new javafx.scene.control.Separator(), ambLabel, ambSlider, instr);
        return container;
    }

    private void updateHUD() {
        // 获取点光源在场景中的位置
        Point3D pos = lightGroup.localToScene(pointLight.getTranslateX(), pointLight.getTranslateY(), pointLight.getTranslateZ());
        
        String info = String.format(
                "--- 灯光信息 ---\n" +
                "点光源位置:\n X: %.1f, Y: %.1f, Z: %.1f\n" +
                "灯光组旋转:\n X: %.1f°, Y: %.1f°\n" +
                "环境光颜色: %s",
                pos.getX(), pos.getY(), pos.getZ(),
                groupRotateX.getAngle(), groupRotateY.getAngle(),
                ambientLight.getColor().toString()
        );
        hudLabel.setText(info);
    }

    private double lastMouseX, lastMouseY;

    private void setupInteractions(Scene scene, SubScene subScene) {
        // 键盘控制灯光移动
        scene.setOnKeyPressed(e -> {
            double step = 10.0;
            switch (e.getCode()) {
                case W: lightGroup.setTranslateZ(lightGroup.getTranslateZ() + step); break;
                case S: lightGroup.setTranslateZ(lightGroup.getTranslateZ() - step); break;
                case A: lightGroup.setTranslateX(lightGroup.getTranslateX() - step); break;
                case D: lightGroup.setTranslateX(lightGroup.getTranslateX() + step); break;
                case Q: lightGroup.setTranslateY(lightGroup.getTranslateY() - step); break;
                case E: lightGroup.setTranslateY(lightGroup.getTranslateY() + step); break;
                
                case UP:    groupRotateX.setAngle(groupRotateX.getAngle() - 5); break;
                case DOWN:  groupRotateX.setAngle(groupRotateX.getAngle() + 5); break;
                case LEFT:  groupRotateY.setAngle(groupRotateY.getAngle() - 5); break;
                case RIGHT: groupRotateY.setAngle(groupRotateY.getAngle() + 5); break;
                
                case R:
                    lightGroup.setTranslateX(200);
                    lightGroup.setTranslateY(-200);
                    lightGroup.setTranslateZ(-200);
                    groupRotateX.setAngle(0);
                    groupRotateY.setAngle(0);
                    break;
            }
        });

        // 鼠标控制视角
        subScene.setOnMousePressed(e -> {
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
        });

        subScene.setOnMouseDragged(e -> {
            double deltaX = e.getSceneX() - lastMouseX;
            double deltaY = e.getSceneY() - lastMouseY;
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();

            if (e.isSecondaryButtonDown()) { // 右键拖拽旋转相机
                cameraRotateY.setAngle(cameraRotateY.getAngle() + deltaX * 0.3);
                cameraRotateX.setAngle(cameraRotateX.getAngle() - deltaY * 0.3);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
