package gltf.jfx.learn;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.PickResult;
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
 * 交互：
 * - 鼠标右键：自由旋转
 * - 滚轮/上下方向键：缩放
 * - 鼠标左键点击：在物体表面标红，并慢慢旋转该点至视野中心
 */
public class JavaFXPhongMaterialDemo extends Application {

    private PhongMaterial material;
    private Sphere targetSphere;
    private Group sphereGroup;      // 包装球体和标记点，以便同步旋转
    private Sphere selectionMarker; // 点击位置的高红点
    private AmbientLight ambientLight;
    private PerspectiveCamera camera;

    // 动态生成的贴图
    private Image generatedDiffuseMap; 
    private Image fileDiffuseMap;      
    private Image specularMap;
    private Image generatedNormalMap;  
    private Image fileNormalMapEarth;  
    private Image selfIllumMap;

    private javafx.scene.image.ImageView diffusePreview; 
    private javafx.scene.image.ImageView normalPreview;  

    // 旋转变换
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private double lastMouseX, lastMouseY;

    @Override
    public void start(Stage primaryStage) {
        // 初始化贴图资源
        createTextures();
        loadTextureFiles();

        // 创建演示材质
        material = new PhongMaterial(Color.WHITE);
        material.setSpecularPower(5.0);

        // 创建 3D 物体
        targetSphere = new Sphere(150);
        targetSphere.setMaterial(material);

        // 创建点击标记（初始不可见）
        selectionMarker = new Sphere(3);
        PhongMaterial markerMat = new PhongMaterial(Color.RED);
        // 让标记点自发光，不受场景光照影响
        WritableImage whitePixel = new WritableImage(1, 1);
        whitePixel.getPixelWriter().setColor(0, 0, Color.WHITE);
        markerMat.setSelfIlluminationMap(whitePixel);
        selectionMarker.setMaterial(markerMat);
        selectionMarker.setMouseTransparent(true); // 不干扰拾取
        selectionMarker.setVisible(false);

        // 使用组包装，旋转应用到组上
        sphereGroup = new Group(targetSphere, selectionMarker);
        sphereGroup.getTransforms().addAll(rotateY, rotateX);

        // 创建背景物体以便观察透明度
        Box backgroundBox = new Box(100, 100, 100);
        backgroundBox.setMaterial(new PhongMaterial(Color.RED));
        backgroundBox.setTranslateZ(50); 

        Group worldRoot = new Group(backgroundBox, sphereGroup);

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

        // 3D 子场景
        SubScene subScene = new SubScene(new Group(worldRoot, pointLight, ambientLight), 700, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#222222"));
        subScene.setCamera(camera);

        // UI 布局
        VBox controls = createControlPanel();
        ScrollPane scrollPane = new ScrollPane(controls);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinWidth(320);
        scrollPane.setPrefWidth(320);
        scrollPane.setMaxWidth(320);
        scrollPane.setStyle("-fx-background-color: #333333; -fx-background: #333333;");

        javafx.scene.layout.StackPane subSceneContainer = new javafx.scene.layout.StackPane(subScene);
        subSceneContainer.setMinWidth(0); 
        subScene.widthProperty().bind(subSceneContainer.widthProperty());
        subScene.heightProperty().bind(subSceneContainer.heightProperty());

        HBox texturePreviews = createTexturePreviewPanel();
        VBox leftLayout = new VBox(subSceneContainer, texturePreviews);
        VBox.setVgrow(subSceneContainer, Priority.ALWAYS);

        HBox mainRoot = new HBox(leftLayout, scrollPane);
        HBox.setHgrow(leftLayout, Priority.ALWAYS);

        Scene scene = new Scene(mainRoot, 1024, 768);
        setupInteractions(scene, subScene);
        setupClickFocus();

        primaryStage.setTitle("JavaFX PhongMaterial 属性演示 - 小塔");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadTextureFiles() {
        fileNormalMapEarth = loadImage("/materials/1k_earth_bump.png");
        fileDiffuseMap = loadImage("/materials/1k_earth_daymap.png");
    }

    private Image loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) return new Image(url.toExternalForm());
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private void createTextures() {
        int size = 256;
        // 棋盘格
        WritableImage diff = new WritableImage(size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean isWhite = ((x / 32) + (y / 32)) % 2 == 0;
                diff.getPixelWriter().setColor(x, y, isWhite ? Color.WHITE : Color.LIGHTGRAY);
            }
        }
        generatedDiffuseMap = diff;

        // 镜面
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

        // 法线 (条纹)
        WritableImage normal = new WritableImage(size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double slopeX = 0.2 * Math.cos(x * 0.2);
                double len = Math.sqrt(slopeX * slopeX + 1.0);
                normal.getPixelWriter().setColor(x, y, Color.color(-slopeX / len * 0.5 + 0.5, 0.5, 1.0 / len * 0.5 + 0.5));
            }
        }
        generatedNormalMap = normal;

        // 自发光
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

        // --- 漫射 ---
        diffuseSourceBox = new ComboBox<>();
        diffuseSourceBox.getItems().addAll("Generated (Checker)", "File (Earth)");
        diffuseSourceBox.setValue("Generated (Checker)");
        diffuseSourceBox.setMaxWidth(Double.MAX_VALUE);
        diffuseSourceBox.setDisable(true);
        diffuseEnableBtn = new CheckBox("启用漫反射贴图");
        diffuseEnableBtn.setStyle("-fx-text-fill: white;");
        diffuseEnableBtn.setOnAction(e -> updateDiffuseMapState(diffuseEnableBtn.isSelected(), diffuseSourceBox.getValue()));
        diffuseSourceBox.setOnAction(e -> updateDiffuseMapState(diffuseEnableBtn.isSelected(), diffuseSourceBox.getValue()));

        TitledPane p1 = new TitledPane("漫射 (Diffuse)", new VBox(5,
            new Label("漫射颜色 (含Alpha):"),
            createColorPicker(Color.WHITE, c -> material.setDiffuseColor(c)),
            diffuseEnableBtn, new Label("贴图来源:"), diffuseSourceBox
        ));

        // --- 镜面 ---
        TitledPane p2 = new TitledPane("镜面 (Specular)", new VBox(5,
            new Label("镜面颜色:"),
            createColorPicker(Color.WHITE, c -> material.setSpecularColor(c)),
            new Label("粗糙度 (Specular Power):"),
            createNumericInput(5.0, v -> material.setSpecularPower(v)),
            createCheckBox("启用镜面贴图", b -> material.setSpecularMap(b ? specularMap : null))
        ));

        // --- 法线/自发光 ---
        normalSourceBox = new ComboBox<>();
        normalSourceBox.getItems().addAll("Generated (Code)", "File (Earth)");
        normalSourceBox.setValue("Generated (Code)");
        normalSourceBox.setMaxWidth(Double.MAX_VALUE);
        normalSourceBox.setDisable(true);
        normalEnableBtn = new CheckBox("启用凹凸贴图 (Normal Map)");
        normalEnableBtn.setStyle("-fx-text-fill: white;");
        normalEnableBtn.setOnAction(e -> updateNormalMapState(normalEnableBtn.isSelected(), normalSourceBox.getValue()));
        normalSourceBox.setOnAction(e -> updateNormalMapState(normalEnableBtn.isSelected(), normalSourceBox.getValue()));

        TitledPane p3 = new TitledPane("贴图 (Maps)", new VBox(5,
            normalEnableBtn, new Label("贴图来源:"), normalSourceBox,
            createCheckBox("启用自发光贴图", b -> material.setSelfIlluminationMap(b ? selfIllumMap : null))
        ));

        // --- 灯光 ---
        TitledPane p4 = new TitledPane("灯光 (Lighting)", new VBox(5,
            new Label("环境光颜色:"),
            createColorPicker(Color.rgb(60, 60, 60), c -> ambientLight.setColor(c))
        ));

        Button earthBtn = new Button("地球仪预设");
        earthBtn.setMaxWidth(Double.MAX_VALUE);
        earthBtn.setOnAction(e -> applyEarthPreset());

        Button waterBtn = new Button("模拟水面预设");
        waterBtn.setMaxWidth(Double.MAX_VALUE);
        waterBtn.setOnAction(e -> applyWaterPreset());

        Button resetBtn = new Button("重置材质");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setOnAction(e -> resetAll());

        panel.getChildren().addAll(header, p1, p2, p3, p4, new Separator(), earthBtn, waterBtn, resetBtn);
        panel.lookupAll(".label").forEach(n -> n.setStyle("-fx-text-fill: white;"));
        return panel;
    }

    private void resetAll() {
        material.setDiffuseColor(Color.WHITE);
        material.setDiffuseMap(null);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(5.0);
        material.setSpecularMap(null);
        material.setBumpMap(null);
        material.setSelfIlluminationMap(null);
        ambientLight.setColor(Color.rgb(60, 60, 60));
        diffuseEnableBtn.setSelected(false);
        diffuseSourceBox.setDisable(true);
        normalEnableBtn.setSelected(false);
        normalSourceBox.setDisable(true);
        camera.setTranslateZ(-800);
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        selectionMarker.setVisible(false);
        diffusePreview.setImage(generatedDiffuseMap);
        normalPreview.setImage(generatedNormalMap);
    }

    private void updateDiffuseMapState(boolean enabled, String source) {
        diffuseSourceBox.setDisable(!enabled);
        Image img = enabled ? (source.equals("File (Earth)") ? fileDiffuseMap : generatedDiffuseMap) : null;
        material.setDiffuseMap(img);
        diffusePreview.setImage(enabled ? (img != null ? img : generatedDiffuseMap) : generatedDiffuseMap);
    }

    private void updateNormalMapState(boolean enabled, String source) {
        normalSourceBox.setDisable(!enabled);
        Image img = enabled ? (source.equals("File (Earth)") ? fileNormalMapEarth : generatedNormalMap) : null;
        material.setBumpMap(img);
        normalPreview.setImage(enabled ? (img != null ? img : generatedNormalMap) : generatedNormalMap);
    }

    private HBox createTexturePreviewPanel() {
        HBox container = new HBox(15);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #444444;");
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPrefHeight(160);

        diffusePreview = new javafx.scene.image.ImageView(generatedDiffuseMap);
        normalPreview = new javafx.scene.image.ImageView(generatedNormalMap);

        container.getChildren().addAll(
            createPreviewItem("Diffuse Map", diffusePreview),
            createPreviewItem("Specular Map", new javafx.scene.image.ImageView(specularMap)),
            createPreviewItem("Normal Map (Bump)", normalPreview),
            createPreviewItem("Emissive Map", new javafx.scene.image.ImageView(selfIllumMap))
        );
        return container;
    }

    private VBox createPreviewItem(String title, javafx.scene.image.ImageView iv) {
        iv.setFitWidth(100); iv.setFitHeight(100);
        iv.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);");
        javafx.scene.layout.StackPane frame = new javafx.scene.layout.StackPane(iv);
        frame.setStyle("-fx-border-color: #666666; -fx-border-width: 1;");
        VBox box = new VBox(5, frame, new Label(title));
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void applyEarthPreset() {
        material.setDiffuseColor(Color.WHITE);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(20.0);
        diffuseEnableBtn.setSelected(true);
        diffuseSourceBox.setValue("File (Earth)");
        updateDiffuseMapState(true, "File (Earth)");
        normalEnableBtn.setSelected(true);
        normalSourceBox.setValue("File (Earth)");
        updateNormalMapState(true, "File (Earth)");
        material.setSelfIlluminationMap(null);
    }

    private void applyWaterPreset() {
        material.setDiffuseColor(Color.web("#0077be", 0.5));
        material.setSpecularPower(64.0);
        updateDiffuseMapState(false, "Generated (Checker)");
        diffuseEnableBtn.setSelected(false);
        normalEnableBtn.setSelected(true);
        normalSourceBox.setValue("Generated (Code)");
        
        int size = 256;
        WritableImage waterNormal = new WritableImage(size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double slopeX = 0.3 * Math.cos(x * 0.15 + y * 0.15);
                double slopeY = 0.3 * Math.cos(x * 0.15 + y * 0.15);
                double len = Math.sqrt(slopeX * slopeX + slopeY * slopeY + 1.0);
                waterNormal.getPixelWriter().setColor(x, y, Color.color(-slopeX / len * 0.5 + 0.5, -slopeY / len * 0.5 + 0.5, 1.0 / len * 0.5 + 0.5));
            }
        }
        material.setBumpMap(waterNormal);
        normalPreview.setImage(waterNormal);
    }

    private void setupInteractions(Scene scene, SubScene subScene) {
        subScene.setOnMousePressed(e -> { lastMouseX = e.getSceneX(); lastMouseY = e.getSceneY(); });
        subScene.setOnMouseDragged(e -> {
            double deltaX = e.getSceneX() - lastMouseX;
            double deltaY = e.getSceneY() - lastMouseY;
            lastMouseX = e.getSceneX(); lastMouseY = e.getSceneY();
            rotateY.setAngle(rotateY.getAngle() + deltaX * 0.3);
            rotateX.setAngle(rotateX.getAngle() - deltaY * 0.3);
        });
        subScene.setOnScroll(e -> {
            double factor = e.getDeltaY() < 0 ? 0.95 : 1.05;
            camera.setTranslateZ(camera.getTranslateZ() * factor);
        });
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.UP) camera.setTranslateZ(camera.getTranslateZ() + 20);
            if (e.getCode() == javafx.scene.input.KeyCode.DOWN) camera.setTranslateZ(camera.getTranslateZ() - 20);
        });
    }

    private void setupClickFocus() {
        targetSphere.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY && e.isStillSincePress()) {
                PickResult res = e.getPickResult();
                Point3D p = res.getIntersectedPoint();
                if (p != null) {
                    selectionMarker.setTranslateX(p.getX());
                    selectionMarker.setTranslateY(p.getY());
                    selectionMarker.setTranslateZ(p.getZ());
                    selectionMarker.setVisible(true);
                    
                    double deltaY = Math.toDegrees(Math.atan2(p.getX(), p.getZ()));
                    double radius = targetSphere.getRadius();
                    double deltaX = Math.toDegrees(Math.asin(p.getY() / radius));

                    Timeline timeline = new Timeline(new KeyFrame(javafx.util.Duration.millis(1000),
                        new KeyValue(rotateY.angleProperty(), rotateY.getAngle() - deltaY, Interpolator.EASE_BOTH),
                        new KeyValue(rotateX.angleProperty(), rotateX.getAngle() - deltaX, Interpolator.EASE_BOTH)
                    ));
                    timeline.play();
                }
            }
        });
    }

    private ColorPicker createColorPicker(Color initial, java.util.function.Consumer<Color> callback) {
        ColorPicker cp = new ColorPicker(initial); cp.setMaxWidth(Double.MAX_VALUE);
        cp.setOnAction(e -> callback.accept(cp.getValue())); return cp;
    }

    private CheckBox createCheckBox(String text, java.util.function.Consumer<Boolean> callback) {
        CheckBox cb = new CheckBox(text); cb.setStyle("-fx-text-fill: white;");
        cb.setOnAction(e -> callback.accept(cb.isSelected())); return cb;
    }

    private Spinner<Double> createNumericInput(double initial, java.util.function.Consumer<Double> callback) {
        Spinner<Double> s = new Spinner<>(0.0, 128.0, initial, 1.0); s.setEditable(true);
        s.setMaxWidth(Double.MAX_VALUE); s.valueProperty().addListener((o, oldV, newV) -> callback.accept(newV));
        return s;
    }

    public static void main(String[] args) { launch(args); }
}
