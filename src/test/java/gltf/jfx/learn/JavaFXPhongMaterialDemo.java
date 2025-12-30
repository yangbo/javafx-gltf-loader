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
 * 支持键盘上下箭头和鼠标滚轮进行缩放。
 */
public class JavaFXPhongMaterialDemo extends Application {

    private PhongMaterial material;
    private Sphere targetSphere;
    private AmbientLight ambientLight;
    private PerspectiveCamera camera;
    
    // 动态生成的贴图
    private Image generatedDiffuseMap; // 代码生成的漫反射贴图
    private Image fileDiffuseMap;      // 从文件加载的漫反射贴图 (地球)
    private Image specularMap;
    private Image generatedNormalMap;  // 代码生成的法线贴图
    private Image fileNormalMapStone;  // 从文件加载的法线贴图 (石头)
    private Image fileNormalMapEarth;  // 从文件加载的法线贴图 (地球配套)
    private Image selfIllumMap;

    private javafx.scene.image.ImageView diffusePreview; // 用于动态更新底部预览
    private javafx.scene.image.ImageView normalPreview;  // 用于动态更新底部预览

    // 旋转控制
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private double lastMouseX, lastMouseY;

    @Override
    public void start(Stage primaryStage) {
        // 初始化贴图
        createTextures();
        loadTextureFiles();

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
        camera = new PerspectiveCamera(true);
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
        setupInteractions(scene, subScene);

        primaryStage.setTitle("JavaFX PhongMaterial 属性演示 - 小塔");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadTextureFiles() {
        // 加载法线贴图 (石头)
        fileNormalMapStone = loadImage("/materials/bump-map-stone.png");
        // 加载法线贴图 (地球)
        fileNormalMapEarth = loadImage("/materials/earthNormalMap_8k-tig.png");
        // 加载漫反射贴图 (地球)
        fileDiffuseMap = loadImage("/materials/1k_earth_daymap.png");
        if (fileDiffuseMap == null) {
            fileDiffuseMap = loadImage("/materials/2k_earth_daymap.jpg");
        }
    }

    private Image loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                Image img = new Image(url.toExternalForm());
                System.out.println("成功加载贴图文件: " + path);
                return img;
            } else {
                System.err.println("无法找到资源文件: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        generatedDiffuseMap = diff;

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
        WritableImage normal = new WritableImage(size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double slopeX = 0.2 * Math.cos(x * 0.2);
                double slopeY = 0;
                double len = Math.sqrt(slopeX * slopeX + slopeY * slopeY + 1.0);
                normal.getPixelWriter().setColor(x, y, Color.color(
                    -slopeX / len * 0.5 + 0.5,
                    -slopeY / len * 0.5 + 0.5,
                    1.0 / len * 0.5 + 0.5
                ));
            }
        }
        generatedNormalMap = normal;

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

    private ComboBox<String> diffuseSourceBox;
    private ComboBox<String> normalSourceBox;
    private CheckBox diffuseEnableBtn;
    private CheckBox normalEnableBtn;

    private VBox createControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");

        Label header = new Label("PhongMaterial 属性");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        panel.getChildren().add(header);

        // --- 漫射属性 ---
        diffuseSourceBox = new ComboBox<>();
        diffuseSourceBox.getItems().addAll("Generated (Checker)", "File (Earth)");
        diffuseSourceBox.setValue("Generated (Checker)");
        diffuseSourceBox.setMaxWidth(Double.MAX_VALUE);
        diffuseSourceBox.setDisable(true);

        diffuseEnableBtn = new CheckBox("启用漫反射贴图");
        diffuseEnableBtn.setStyle("-fx-text-fill: white;");

        diffuseEnableBtn.setOnAction(e -> {
            boolean enabled = diffuseEnableBtn.isSelected();
            diffuseSourceBox.setDisable(!enabled);
            updateDiffuseMapState(enabled, diffuseSourceBox.getValue());
        });

        diffuseSourceBox.setOnAction(e -> {
            updateDiffuseMapState(diffuseEnableBtn.isSelected(), diffuseSourceBox.getValue());
        });

        TitledPane p1 = new TitledPane("漫射 (Diffuse)", new VBox(5,
            new Label("漫射颜色 (包含透明度):"),
            createColorPicker(Color.WHITE, c -> material.setDiffuseColor(c)),
            diffuseEnableBtn,
            new Label("贴图来源:"),
            diffuseSourceBox
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
        normalSourceBox = new ComboBox<>();
        normalSourceBox.getItems().addAll("Generated (Code)", "File (Stone)", "File (Earth Normal)");
        normalSourceBox.setValue("Generated (Code)");
        normalSourceBox.setMaxWidth(Double.MAX_VALUE);
        normalSourceBox.setDisable(true);

        normalEnableBtn = new CheckBox("启用凹凸贴图 (Normal Map)");
        normalEnableBtn.setStyle("-fx-text-fill: white;");

        normalEnableBtn.setOnAction(e -> {
            boolean enabled = normalEnableBtn.isSelected();
            normalSourceBox.setDisable(!enabled);
            updateNormalMapState(enabled, normalSourceBox.getValue());
        });

        normalSourceBox.setOnAction(e -> {
            updateNormalMapState(normalEnableBtn.isSelected(), normalSourceBox.getValue());
        });

        TitledPane p3 = new TitledPane("贴图 (Maps)", new VBox(5,
            normalEnableBtn,
            new Label("贴图来源:"),
            normalSourceBox,
            createCheckBox("启用自发光贴图 (Emissive)", b -> material.setSelfIlluminationMap(b ? selfIllumMap : null))
        ));

        // --- 灯光设置 ---
        TitledPane p4 = new TitledPane("灯光 (Lighting)", new VBox(5,
            new Label("环境光颜色:"),
            createColorPicker(Color.rgb(60, 60, 60), c -> ambientLight.setColor(c))
        ));

        // --- 预设 ---
        Button earthBtn = new Button("地球仪预设");
        earthBtn.setMaxWidth(Double.MAX_VALUE);
        earthBtn.setOnAction(e -> applyEarthPreset());

        Button waterBtn = new Button("模拟水面预设");
        waterBtn.setMaxWidth(Double.MAX_VALUE);
        waterBtn.setOnAction(e -> applyWaterPreset());

        Button resetBtn = new Button("重置材质");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setOnAction(e -> resetMaterial());

        panel.getChildren().addAll(p1, p2, p3, p4, new Separator(), earthBtn, waterBtn, resetBtn);
        panel.lookupAll(".label").forEach(n -> n.setStyle("-fx-text-fill: white;"));

        return panel;
    }

    private void resetMaterial() {
        material.setDiffuseColor(Color.WHITE);
        material.setDiffuseMap(null);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(5.0);
        material.setSpecularMap(null);
        material.setBumpMap(null);
        material.setSelfIlluminationMap(null);
        ambientLight.setColor(Color.rgb(60, 60, 60));
        
        diffuseEnableBtn.setSelected(false);
        diffuseSourceBox.setValue("Generated (Checker)");
        diffuseSourceBox.setDisable(true);
        diffusePreview.setImage(generatedDiffuseMap);
        
        normalEnableBtn.setSelected(false);
        normalSourceBox.setValue("Generated (Code)");
        normalSourceBox.setDisable(true);
        normalPreview.setImage(generatedNormalMap);
        
        camera.setTranslateZ(-800);
    }

    private void updateDiffuseMapState(boolean enabled, String source) {
        Image img = null;
        if (enabled) {
            img = source.equals("File (Earth)") ? fileDiffuseMap : generatedDiffuseMap;
        }
        material.setDiffuseMap(img != null ? img : null);
        diffusePreview.setImage(enabled ? (img != null ? img : generatedDiffuseMap) : generatedDiffuseMap);
    }

    private void updateNormalMapState(boolean enabled, String source) {
        Image img = null;
        if (enabled) {
            if (source.equals("File (Stone)")) img = fileNormalMapStone;
            else if (source.equals("File (Earth Normal)")) img = fileNormalMapEarth;
            else img = generatedNormalMap;
        }
        material.setBumpMap(img != null ? img : null);
        normalPreview.setImage(enabled ? (img != null ? img : generatedNormalMap) : generatedNormalMap);
    }

    private HBox createTexturePreviewPanel() {
        HBox container = new HBox(15);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #444444;");
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefHeight(160);

        diffusePreview = new javafx.scene.image.ImageView(generatedDiffuseMap);
        diffusePreview.setFitWidth(100);
        diffusePreview.setFitHeight(100);

        normalPreview = new javafx.scene.image.ImageView(generatedNormalMap);
        normalPreview.setFitWidth(100);
        normalPreview.setFitHeight(100);

        container.getChildren().addAll(
            createTextureViewWithExistingIV("Diffuse Map", diffusePreview),
            createTextureView("Specular Map", specularMap),
            createTextureViewWithExistingIV("Normal Map (Bump)", normalPreview),
            createTextureView("Emissive Map", selfIllumMap)
        );

        return container;
    }

    private VBox createTextureViewWithExistingIV(String title, javafx.scene.image.ImageView iv) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        Label label = new Label(title);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
        iv.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);");
        javafx.scene.layout.StackPane frame = new javafx.scene.layout.StackPane(iv);
        frame.setStyle("-fx-border-color: #666666; -fx-border-width: 1;");
        box.getChildren().addAll(frame, label);
        return box;
    }

    private VBox createTextureView(String title, Image img) {
        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
        iv.setFitWidth(100);
        iv.setFitHeight(100);
        return createTextureViewWithExistingIV(title, iv);
    }

    private void applyEarthPreset() {
        material.setDiffuseColor(Color.WHITE);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(20.0);
        
        diffuseEnableBtn.setSelected(true);
        diffuseSourceBox.setValue("File (Earth)");
        diffuseSourceBox.setDisable(false);
        updateDiffuseMapState(true, "File (Earth)");
        
        normalEnableBtn.setSelected(true);
        normalSourceBox.setValue("File (Earth Normal)");
        normalSourceBox.setDisable(false);
        updateNormalMapState(true, "File (Earth Normal)");
        
        material.setSelfIlluminationMap(null);
    }

    private void applyWaterPreset() {
        material.setDiffuseColor(Color.web("#0077be", 0.5));
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(64.0);
        
        diffuseEnableBtn.setSelected(false);
        diffuseSourceBox.setDisable(true);
        updateDiffuseMapState(false, "Generated (Checker)");
        
        normalEnableBtn.setSelected(true);
        normalSourceBox.setValue("Generated (Code)");
        normalSourceBox.setDisable(false);
        
        int size = 256;
        WritableImage waterNormal = new WritableImage(size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double freq = 0.15;
                double amp = 2.0;
                double slopeX = amp * freq * Math.cos(x * freq + y * freq);
                double slopeY = amp * freq * Math.cos(x * freq + y * freq);
                double len = Math.sqrt(slopeX * slopeX + slopeY * slopeY + 1.0);
                waterNormal.getPixelWriter().setColor(x, y, Color.color(
                    -slopeX / len * 0.5 + 0.5,
                    -slopeY / len * 0.5 + 0.5,
                    1.0 / len * 0.5 + 0.5
                ));
            }
        }
        material.setBumpMap(waterNormal);
        normalPreview.setImage(waterNormal);
        
        material.setSelfIlluminationMap(null);
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

    private void setupInteractions(Scene scene, SubScene subScene) {
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

        // 鼠标滚轮缩放
        subScene.setOnScroll(e -> {
            double zoomFactor = 1.05;
            double deltaY = e.getDeltaY();
            if (deltaY < 0) {
                camera.setTranslateZ(camera.getTranslateZ() / zoomFactor);
            } else {
                camera.setTranslateZ(camera.getTranslateZ() * zoomFactor);
            }
        });

        // 键盘上下箭头缩放
        scene.setOnKeyPressed(e -> {
            double step = 20.0;
            switch (e.getCode()) {
                case UP:
                    camera.setTranslateZ(camera.getTranslateZ() + step);
                    break;
                case DOWN:
                    camera.setTranslateZ(camera.getTranslateZ() - step);
                    break;
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
