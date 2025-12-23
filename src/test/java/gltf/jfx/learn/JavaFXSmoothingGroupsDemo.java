package gltf.jfx.learn;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * 平滑组 (Smoothing Groups) 对比演示
 * 左侧：关闭平滑 (棱角分明)
 * 右侧：开启平滑 (圆润光滑)
 */
public class JavaFXSmoothingGroupsDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. 创建两个完全相同的圆柱体 Mesh，唯一的区别是平滑组设置
        // 10 边形：这是一个“甜点”数字，既能看出棱角，又能展示平滑后的圆润
        TriangleMesh meshHard = createCylinderMesh(10, 50, 200, false);
        TriangleMesh meshSmooth = createCylinderMesh(10, 50, 200, true);

        // 2. 创建材质 (强高光，无贴图)
        PhongMaterial material = new PhongMaterial(Color.LIGHTGRAY);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(32); // 高光越集中，平滑效果越明显

        // 3. 构建左侧物体 (硬边)
        MeshView viewHard = new MeshView(meshHard);
        viewHard.setCullFace(CullFace.BACK);
        viewHard.setMaterial(material);
        viewHard.setTranslateX(-100);

        // 4. 构建右侧物体 (平滑)
        MeshView viewSmooth = new MeshView(meshSmooth);
        viewSmooth.setCullFace(CullFace.BACK);
        viewSmooth.setMaterial(material);
        viewSmooth.setTranslateX(100);

        // 5. 场景与光照
        Group root3D = new Group(viewHard, viewSmooth);
        
        // 添加点光源，让高光在表面流动
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateZ(-300);
        light.setTranslateY(-50);
        
        root3D.getChildren().add(light);
        root3D.getChildren().add(new AmbientLight(Color.rgb(50, 50, 50)));

        // 6. 摄像机
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-600);
        camera.setNearClip(0.1);
        camera.setFarClip(2000.0);

        SubScene subScene = new SubScene(root3D, 800, 500, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(20, 20, 20));
        subScene.setCamera(camera);

        // 7. HUD 说明
        Label label = new Label(
            "左侧: Smoothing Group = 0 (硬边/Flat)\n" +
            "右侧: Smoothing Group = 1 (平滑/Smooth)\n\n" +
            "观察重点：注意高光(Highlight)在表面的表现。\n" +
            "左边：高光在每个面上是断裂的。\n" +
            "右边：高光在曲面上是连续流动的。"
        );
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10; -fx-background-color: rgba(0,0,0,0.3); -fx-font-family: 'Consolas';");
        
        StackPane root = new StackPane(subScene, label);
        StackPane.setAlignment(label, Pos.TOP_RIGHT); // 将说明文字置于右上角
        
        Scene scene = new Scene(root, 800, 800);

        // 8. 动画：让物体旋转以便观察光影
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                viewHard.setRotate(viewHard.getRotate() + 0.5);
                viewHard.setRotationAxis(Rotate.Y_AXIS);
                
                viewSmooth.setRotate(viewSmooth.getRotate() + 0.5);
                viewSmooth.setRotationAxis(Rotate.Y_AXIS);
            }
        }.start();

        primaryStage.setTitle("JavaFX Smoothing Groups Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 创建圆柱体 Mesh
     * @param div 分段数 (例如 10)
     * @param r 半径
     * @param h 高度
     * @param smooth 是否开启平滑
     */
    private TriangleMesh createCylinderMesh(int div, float r, float h, boolean smooth) {
        TriangleMesh m = new TriangleMesh();

        // 1. 顶点 (Points)
        // 顶部圆周点 (0 ~ div-1)
        for (int i = 0; i < div; i++) {
            double a = 2.0 * Math.PI * i / div;
            m.getPoints().addAll((float)(Math.sin(a)*r), -h/2, (float)(Math.cos(a)*r));
        }
        // 底部圆周点 (div ~ 2*div-1)
        for (int i = 0; i < div; i++) {
            double a = 2.0 * Math.PI * i / div;
            m.getPoints().addAll((float)(Math.sin(a)*r), h/2, (float)(Math.cos(a)*r));
        }

        // 2. 纹理坐标 (TexCoords)
        // 关键：为了平滑，侧面的点必须共用 UV 索引。我们给所有点分配一个 dummy UV。
        // 为了避免驱动层面的顶点拆分，我们只给一个 UV 点，所有顶点都用它。
        m.getTexCoords().addAll(0.5f, 0.5f);

        // 3. 面 (Faces)
        // 侧面由 quad 组成，每个 quad 拆分为 2 个三角形
        for (int i = 0; i < div; i++) {
            int top1 = i;
            int top2 = (i + 1) % div;
            int bot1 = i + div;
            int bot2 = (i + 1) % div + div;

            // 三角形 1: top1 - top2 - bot1 (逆时针 CCW)
            // 索引格式: p,t, p,t, p,t (t 永远是 0)
            m.getFaces().addAll(top1,0, top2,0, bot1,0);
            
            // 三角形 2: top2 - bot2 - bot1 (逆时针 CCW)
            m.getFaces().addAll(top2,0, bot2,0, bot1,0);
            
            // 设置平滑组
            // 如果 smooth=true，设为 1；否则设为 0
            int group = smooth ? 1 : 0;
            m.getFaceSmoothingGroups().addAll(group, group);
        }
        
        // (注：为保持代码简洁，本示例省略了上下底面的盖子，专注于侧面平滑演示)

        return m;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
