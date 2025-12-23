package gltf.jfx.learn;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * 平滑组作用的终极演示：
 * 1. 同一个 20 面棱锥模型。
 * 2. 动态切换 Smoothing Groups (0 vs 1)。
 * 3. 纯色高光材质，排除贴图干扰。
 */
public class JavaFXTriangleMeshExample extends Application {

    private TriangleMesh mesh;
    private MeshView meshView;
    private Label statusLabel;
    private boolean isSmoothEnabled = true;
    private final int divisions = 10; // 增加到 40 面，平滑感会更细腻

    @Override
    public void start(Stage primaryStage) {
        // 1. 初始化几何体 (20面棱锥)
        mesh = createPyramidMesh(divisions, 80, 150);
        meshView = new MeshView(mesh);
        applySmoothing(); // 初始开启平滑

        // 2. 使用纯色高光材质（这是观察法线平滑的最佳方式）
        PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(20);
        meshView.setMaterial(material);
        
        meshView.setRotationAxis(Rotate.Y_AXIS);
        meshView.setRotate(30);

        // 3. 场景与光照
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(200);
        light.setTranslateY(-200);
        light.setTranslateZ(-300);

        AmbientLight ambient = new AmbientLight(Color.rgb(40, 40, 40));

        Group world = new Group(meshView, buildAxes(200), light, ambient);
        
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-500);
        camera.setNearClip(0.1);
        camera.setFarClip(2000.0);

        SubScene subScene = new SubScene(world, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#111111"));
        subScene.setCamera(camera);

        // HUD
        statusLabel = new Label();
        statusLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-text-fill: white; -fx-padding: 20; -fx-font-family: 'Consolas'; -fx-font-size: 14;");
        updateStatus();

        VBox hud = new VBox(statusLabel);
        hud.setPickOnBounds(false);

        StackPane root = new StackPane(subScene, hud);
        Scene scene = new Scene(root, 800, 600);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case S:
                    isSmoothEnabled = !isSmoothEnabled;
                    applySmoothing();
                    updateStatus();
                    break;
                case W:
                    meshView.setDrawMode(meshView.getDrawMode() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
                    break;
                case C:
                    meshView.setCullFace(meshView.getCullFace() == CullFace.BACK ? CullFace.NONE : CullFace.BACK);
                    break;
                case LEFT: meshView.setRotate(meshView.getRotate() - 5); break;
                case RIGHT: meshView.setRotate(meshView.getRotate() + 5); break;
                case UP: camera.setTranslateZ(camera.getTranslateZ() + 20); break;
                case DOWN: camera.setTranslateZ(camera.getTranslateZ() - 20); break;
                default: break;
            }
        });

        primaryStage.setTitle("TriangleMesh Smoothing Groups 对比演示");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TriangleMesh createPyramidMesh(int div, float r, float h) {
        TriangleMesh m = new TriangleMesh();
        
        // 1. 顶点 (Points)
        m.getPoints().addAll(0, -h/2, 0); // 顶 (0)
        m.getPoints().addAll(0, h/2, 0);  // 底心 (1)
        for (int i = 0; i < div; i++) {
            double a = 2.0 * Math.PI * i / div;
            m.getPoints().addAll((float)(Math.sin(a)*r), h/2, (float)(Math.cos(a)*r));
        }

        // 2. 纹理坐标 (TexCoords) - 为每个顶点提供一个占位 UV
        // 这一点非常重要：有些显卡驱动在所有面共用同一个 UV 索引时，法线计算会出现奇点
        m.getTexCoords().addAll(0.5f, 0.5f); // 顶点的 UV (0)
        m.getTexCoords().addAll(0.5f, 0.5f); // 底心的 UV (1)
        for (int i = 0; i < div; i++) {
            m.getTexCoords().addAll((float)i/div, 1.0f); // 周围点的 UV (2+i)
        }

        // 3. 面 (Faces)
        // 侧面
        for (int i = 0; i < div; i++) {
            int p1 = 2 + i;
            int p2 = 2 + (i + 1) % div;
            // 确保相邻面在交界处共享完全相同的 [P, T] 索引对
            // 面 i   使用顶点 [p1, 2+i] 和 [p2, 2+(i+1)%div]
            // 面 i+1 使用顶点 [p2, 2+(i+1)%div] ...
            // 这样 100% 保证了顶点不会被拆分
            m.getFaces().addAll(0, 0, p1, p1, p2, p2);
        }
        // 底面
        for (int i = 0; i < div; i++) {
            int p1 = 2 + (i + 1) % div;
            int p2 = 2 + i;
            m.getFaces().addAll(1, 1, p1, p1, p2, p2);
        }
        return m;
    }

    private void applySmoothing() {
        // 创建一个包含所有面平滑组 ID 的数组
        // 侧面有 divisions 个，底面有 divisions 个，总共 2 * divisions 个三角形
        int[] groups = new int[divisions * 2];
        
        // 填充侧面组 (索引 0 到 divisions-1)
        int sideGroup = isSmoothEnabled ? 1 : 0;
        for (int i = 0; i < divisions; i++) {
            groups[i] = sideGroup;
        }
        
        // 填充底面组 (索引 divisions 到 2*divisions-1)
        // 底面通常设为不同的组（组 ID 为 2），以保持与侧面的硬边缘
        for (int i = divisions; i < divisions * 2; i++) {
            groups[i] = 2;
        }
        
        // 使用 setAll 一次性更新所有数据
        mesh.getFaceSmoothingGroups().setAll(groups);
    }

    private void updateStatus() {
        String state = isSmoothEnabled ? "【开启 (Value: 1)】" : "【关闭 (Value: 0)】";
        String desc = isSmoothEnabled ? 
            "效果：相邻面共享法线插值。即使是20面体，光影也会像圆锥一样顺滑。" :
            "效果：每个面独立计算法线。你可以清晰地看到20个平面的转折棱角。";
        
        statusLabel.setText(
            "平滑组状态：" + state + "\n" +
            "--------------------------------------------------\n" +
            desc + "\n" +
            "--------------------------------------------------\n" +
            "观察重点：\n" +
            " 1. 请在‘填充模式’下按 [S] 键瞬间切换对比。\n" +
            " 2. 注意侧面边缘的“棱角感”是否消失。\n" +
            " 3. 注意高光在表面是连成一片还是被切断。\n\n" +
            "操作：[S] 切换平滑  [W] 线框模式  [左右] 旋转"
        );
    }

    private Group buildAxes(double len) {
        double t = 1.0;
        Box x = new Box(len, t, t); x.setMaterial(new PhongMaterial(Color.RED)); x.setTranslateX(len/2);
        Box y = new Box(t, len, t); y.setMaterial(new PhongMaterial(Color.GREEN)); y.setTranslateY(len/2);
        Box z = new Box(t, t, len); z.setMaterial(new PhongMaterial(Color.BLUE)); z.setTranslateZ(len/2);
        return new Group(x, y, z);
    }

    public static void main(String[] args) { launch(args); }
}
