package gltf.jfx.learn;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * 解释：
 * 圆心 (0,0) 正好被放在了窗口内容区域的左上角顶点。
 * 由于 X 轴向右，Y 轴向下，只有 x>0 且 y>0 的部分（即圆的右下部分）在可视区域内。
 * 圆的左半部分（x < 0）和上半部分（y < 0）都在屏幕可视范围之外（被切掉了）。
 */
public class VerifyOrigin extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. 创建一个圆
        // centerX = 0, centerY = 0, radius = 50
        // 为了视觉清晰，这里用了50半径。如果你改成10，效果一样，只是更小。
        Circle circle = new Circle(0, 0, 100);

        // 设置颜色以便观察
        circle.setFill(Color.ORANGE);
        circle.setStroke(Color.BLACK);

        // 2. 使用 Pane 作为容器
        // 重要：必须使用 Pane 或 Group。
        // 如果使用 StackPane 或 VBox，它们会默认把圆强制移动到中心，就看不出原点在左上角的效果了。
        Pane root = new Pane();
        root.getChildren().add(circle);

        // 3. 创建场景
        Scene scene = new Scene(root, 300, 200);

        primaryStage.setTitle("JavaFX 坐标原点验证");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}