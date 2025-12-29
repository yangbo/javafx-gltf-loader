package gltf.jfx.learn;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * 演示 JavaFX PhongMaterial 的各项属性。
 * 包括：漫射色、漫反射贴图、镜面颜色、镜面贴图、镜面反射率、凹凸贴图、自发光贴图和透明度。
 */
public class JavaFXPhongMaterialDemo extends Application {

    private PhongMaterial material;
    private Sphere targetSphere;
    private AmbientLight ambientLight;
    
    // 动态生成的贴图
    private Image diffuseMap;
    private Image specularMap;
    private Image normalMap; // JavaFX bumpMap 实际上需要 Normal Map (蓝紫色法线贴图)
    private Image selfIllumMap;

    // 旋转控制
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private double lastMouseX, lastMouseY;

    @Override
    public void start(Stage primaryStage) {
        // 初始化贴图
        createTextures();

        // 创建材质
        material = new PhongMaterial(Color.WHITE);
        material.setSpecularPower(5.0); // 设置默认粗糙度（Specular Power）为 5

        // 创建演示球体
        targetSphere = new Sphere(150);
        targetSphere.setMaterial(material);
        targetSphere.getTransforms().addAll(rotateY, rotateX);

        // 创建背景物体（用于演示透明效果）
        Box backgroundBox = new Box(100, 100, 100);
        backgroundBox.setMaterial(new PhongMaterial(Color.RED));
        backgroundBox.setTranslateZ(50); // 放在球体中心稍微靠后的位置

        Group world = new Group(backgroundBox, targetSphere);

        // 设置相机
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-800);

        // 设置灯光
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(400);
        pointLight.setTranslateY(-400);
        pointLight.setTranslateZ(-400);

        ambientLight = new AmbientLight(Color.rgb(60, 60, 60));

        // 3D 场景
        Group sceneRoot = new Group(world, pointLight, ambientLight);
        SubScene subScene = new SubScene(sceneRoot, 700, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#222222"));
        subScene.setCamera(camera);

        // UI 控制面板
        VBox controls = createControlPanel();
        
        // 使用 ScrollPane 包装控制面板，并设置固定/最小宽度
        ScrollPane scrollPane = new ScrollPane(controls);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinWidth(320);
        scrollPane.setPrefWidth(320);
        scrollPane.setMaxWidth(320); // 固定宽度，不随窗口拉伸
        scrollPane.setStyle("-fx-background-color: #333333; -fx-background: #333333;");

        // 使用 StackPane 包装 SubScene 以实现自适应
        javafx.scene.layout.StackPane subSceneContainer = new javafx.scene.layout.StackPane(subScene);
        subSceneContainer.setStyle("-fx-background-color: #222222;");
        subSceneContainer.setMinWidth(0); // 允许容器缩小到 0
        subScene.widthProperty().bind(subSceneContainer.widthProperty());
        subScene.heightProperty().bind(subSceneContainer.heightProperty());
        
        // 底部贴图预览
        HBox texturePreviews = createTexturePreviewPanel();
        
        // 垂直排列：3D 视图在上，贴图预览在下
        VBox leftLayout = new VBox(subSceneContainer, texturePreviews);
        VBox.setVgrow(subSceneContainer, Priority.ALWAYS);
        
        HBox mainRoot = new HBox(leftLayout, scrollPane);
        HBox.setHgrow(leftLayout, Priority.ALWAYS);
        
        Scene scene = new Scene(mainRoot, 1024, 768);
        setupInteractions(subScene);

        primaryStage.setTitle("JavaFX PhongMaterial 属性演示 - 小塔");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createTextures() {
        int size = 256;
        // 1. 漫反射贴图：棋盘格
        WritableImage diff = new WritableImage(size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean isWhite = ((x / 32) + (y / 32)) % 2 == 0;
                diff.getPixelWriter().setColor(x, y, isWhite ? Color.WHITE : Color.LIGHTGRAY);
            }
        }
        diffuseMap = diff;

        // 2. 镜面贴图：中间亮圆，四周暗
        WritableImage spec = new WritableImage(size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = (x - size / 2.0) / (size / 2.0);
                double dy = (y - size / 2.0) / (size / 2.0);
                double dist = Math.min(1.0, Math.sqrt(dx * dx + dy * dy));
                spec.getPixelWriter().setColor(x, y, Color.gray(1.0 - dist));
            }
        }
        specularMap = spec;

        // 3. 凹凸贴图 (实为法线贴图)：生成蓝紫色的垂直条纹法线
        // 计算原理：假设高度 h = sin(x * 0.2)，则斜率 dh/dx = 0.2 * cos(x * 0.2)
        // 法线向量 N = normalize(-dh/dx, -dh/dy, 1)
        WritableImage normal = new WritableImage(size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double slopeX = 0.2 * Math.cos(x * 0.2);
                double slopeY = 0;
                
                // 归一化法线
                double len = Math.sqrt(slopeX * slopeX + slopeY * slopeY + 1.0);
                double nx = -slopeX / len;
                double ny = -slopeY / len;
                double nz = 1.0 / len;
                
                // 映射到颜色范围 [0, 1]
                normal.getPixelWriter().setColor(x, y, Color.color(
                    nx * 0.5 + 0.5,
                    ny * 0.5 + 0.5,
                    nz * 0.5 + 0.5
                ));
            }
        }
        normalMap = normal;

        // 4. 自发光贴图：网格线
        WritableImage emissive = new WritableImage(size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean isEdge = (x % 64 < 4) || (y % 64 < 4);
                emissive.getPixelWriter().setColor(x, y, isEdge ? Color.CYAN : Color.TRANSPARENT);
            }
        }
        selfIllumMap = emissive;
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");

        Label header = new Label("PhongMaterial 属性");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        panel.getChildren().add(header);

        // --- 漫射属性 ---
        TitledPane p1 = new TitledPane("漫射 (Diffuse)", new VBox(5,
            new Label("漫射颜色 (包含透明度):"),
            createColorPicker(Color.WHITE, c -> {
                // JavaFX 17+ PhongMaterial 如果颜色包含 alpha，则支持透明
                material.setDiffuseColor(c);
            }),
            createCheckBox("启用漫反射贴图", b -> material.setDiffuseMap(b ? diffuseMap : null))
        ));

        // --- 镜面属性 ---
        TitledPane p2 = new TitledPane("镜面 (Specular)", new VBox(5,
            new Label("镜面颜色:"),
            createColorPicker(Color.WHITE, c -> material.setSpecularColor(c)),
            new Label("粗糙度 (Specular Power):"),
            createNumericInput(5.0, v -> material.setSpecularPower(v)),
            createCheckBox("启用镜面贴图", b -> material.setSpecularMap(b ? specularMap : null))
        ));

        // --- 凹凸与自发光 ---
        TitledPane p3 = new TitledPane("贴图 (Maps)", new VBox(5,
            createCheckBox("启用凹凸贴图 (Normal Map)", b -> material.setBumpMap(b ? normalMap : null)),
            createCheckBox("启用自发光贴图 (Emissive)", b -> material.setSelfIlluminationMap(b ? selfIllumMap : null))
        ));

        // --- 灯光设置 ---
        TitledPane p4 = new TitledPane("灯光 (Lighting)", new VBox(5,
            new Label("环境光颜色:"),
            createColorPicker(Color.rgb(60, 60, 60), c -> ambientLight.setColor(c))
        ));

        // --- 预设 ---
        Button waterBtn = new Button("模拟水面预设");
        waterBtn.setMaxWidth(Double.MAX_VALUE);
        waterBtn.setOnAction(e -> applyWaterPreset());

        Button resetBtn = new Button("重置材质");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setOnAction(e -> {
            material.setDiffuseColor(Color.WHITE);
            material.setDiffuseMap(null);
            material.setSpecularColor(Color.WHITE);
            material.setSpecularPower(5.0);
            material.setSpecularMap(null);
            material.setBumpMap(null);
            material.setSelfIlluminationMap(null);
            ambientLight.setColor(Color.rgb(60, 60, 60));
        });

        panel.getChildren().addAll(p1, p2, p3, p4, new Separator(), waterBtn, resetBtn);
        
        // 确保所有 Label 在 TitledPane 中也是白色的
        panel.lookupAll(".label").forEach(n -> n.setStyle("-fx-text-fill: white;"));

        return panel;
    }

    private HBox createTexturePreviewPanel() {
        HBox container = new HBox(15);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #444444;");
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefHeight(160);

        container.getChildren().addAll(
            createTextureView("Diffuse Map", diffuseMap),
            createTextureView("Specular Map", specularMap),
            createTextureView("Normal Map (Bump)", normalMap),
            createTextureView("Emissive Map", selfIllumMap)
        );

        return container;
    }

    private VBox createTextureView(String title, Image img) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        
        Label label = new Label(title);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
        
        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
        iv.setFitWidth(100);
        iv.setFitHeight(100);
        iv.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);");
        
        // 给图片加个边框
        javafx.scene.layout.StackPane frame = new javafx.scene.layout.StackPane(iv);
        frame.setStyle("-fx-border-color: #666666; -fx-border-width: 1;");
        
        box.getChildren().addAll(frame, label);
        return box;
    }

    private void applyWaterPreset() {
        material.setDiffuseColor(Color.web("#0077be", 0.5)); // 半透明海蓝色
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(64.0);
        material.setDiffuseMap(null);
        material.setSpecularMap(null);
        material.setSelfIlluminationMap(null);
        
        // 生成波动的法线贴图来模拟水面
        int size = 256;
        WritableImage waterNormal = new WritableImage(size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // 假设高度函数为 h = 2.0 * sin(x * 0.15 + y * 0.15)
                double freq = 0.15;
                double amp = 2.0;
                double slopeX = amp * freq * Math.cos(x * freq + y * freq);
                double slopeY = amp * freq * Math.cos(x * freq + y * freq);
                
                double len = Math.sqrt(slopeX * slopeX + slopeY * slopeY + 1.0);
                double nx = -slopeX / len;
                double ny = -slopeY / len;
                double nz = 1.0 / len;
                
                waterNormal.getPixelWriter().setColor(x, y, Color.color(
                    nx * 0.5 + 0.5,
                    ny * 0.5 + 0.5,
                    nz * 0.5 + 0.5
                ));
            }
        }
        material.setBumpMap(waterNormal);
    }

    private ColorPicker createColorPicker(Color initial, java.util.function.Consumer<Color> callback) {
        ColorPicker cp = new ColorPicker(initial);
        cp.setMaxWidth(Double.MAX_VALUE);
        cp.setOnAction(e -> callback.accept(cp.getValue()));
        return cp;
    }

    private CheckBox createCheckBox(String text, java.util.function.Consumer<Boolean> callback) {
        CheckBox cb = new CheckBox(text);
        cb.setStyle("-fx-text-fill: white;");
        cb.setOnAction(e -> callback.accept(cb.isSelected()));
        return cb;
    }

    private Spinner<Double> createNumericInput(double initial, java.util.function.Consumer<Double> callback) {
        Spinner<Double> spinner = new Spinner<>(0.0, 128.0, initial, 1.0);
        spinner.setEditable(true);
        spinner.setMaxWidth(Double.MAX_VALUE);
        spinner.valueProperty().addListener((obs, oldV, newV) -> callback.accept(newV));
        return spinner;
    }

    private void setupInteractions(SubScene subScene) {
        subScene.setOnMousePressed(e -> {
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
        });

        subScene.setOnMouseDragged(e -> {
            double deltaX = e.getSceneX() - lastMouseX;
            double deltaY = e.getSceneY() - lastMouseY;
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();

            rotateY.setAngle(rotateY.getAngle() + deltaX * 0.3);
            rotateX.setAngle(rotateX.getAngle() - deltaY * 0.3);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
