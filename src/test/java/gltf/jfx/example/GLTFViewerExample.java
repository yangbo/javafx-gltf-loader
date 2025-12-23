package gltf.jfx.example;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * GLTF 模型查看器示例程序
 * 包含居中逻辑、右上角坐标轴指示器以及可见的相机模型
 */
public class GLTFViewerExample extends Application {

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    
    private final Rotate rotateX = new Rotate(-20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-20, Rotate.Y_AXIS);

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. 加载模型
            //java.net.URL resource = getClass().getResource("/robot/scene.gltf");
            //String gltfPath = (resource != null) ? java.nio.file.Paths.get(resource.toURI()).toString() : "src/main/resources/asset/robot/scene.gltf";
             String gltfPath = "c:\\projects\\仿真系统\\models\\railway_tracks\\scene.gltf";
            // String gltfPath = "c:\\projects\\仿真系统\\TTAP\\05-3D模型\\train-wheels.gltf";
            JFXGLTFAsset asset = new JFXGLTFAsset(gltfPath);
            Group modelGroup = asset.build3DScene(asset.scenes[0]);

            // 2. 居中计算
            double[] bounds = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                               Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
            calculateWorldBounds(asset.scenes[0].nodes[0], new float[]{1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1}, bounds);

            double centerX = (bounds[0] + bounds[3]) / 2.0;
            double centerY = (bounds[1] + bounds[4]) / 2.0;
            double centerZ = (bounds[2] + bounds[5]) / 2.0;
            double modelRadius = Math.sqrt(Math.pow(bounds[3]-bounds[0], 2) + Math.pow(bounds[4]-bounds[1], 2) + Math.pow(bounds[5]-bounds[2], 2)) / 2.0;
            if (modelRadius < 1) modelRadius = 100;

            double sizeX = bounds[3] - bounds[0];
            double sizeY = bounds[4] - bounds[1];
            double sizeZ = bounds[5] - bounds[2];

            modelGroup.setTranslateX(-centerX);
            modelGroup.setTranslateY(-centerY);
            modelGroup.setTranslateZ(-centerZ);

            // 3. 构建 3D 世界
            Group world = new Group();
            Rotate gltfFix = new Rotate(180, Rotate.X_AXIS);
            modelGroup.getTransforms().add(gltfFix);
            world.getChildren().add(modelGroup);

            // 相机辅助模型 (对着模型)
            double cameraModelDist = modelRadius * 2.0;
            Group cameraGizmo = createCameraGizmo(modelRadius * 0.15);
            cameraGizmo.setTranslateZ(-cameraModelDist);
            world.getChildren().add(cameraGizmo);

            world.getTransforms().addAll(rotateY, rotateX);

            // 4. 主 SubScene
            Group mainRoot = new Group(world, new AmbientLight(Color.WHITE));
            PointLight pl = new PointLight(Color.WHITE);
            pl.setTranslateZ(-modelRadius * 5);
            mainRoot.getChildren().add(pl);

            SubScene mainSubScene = new SubScene(mainRoot, 1024, 768, true, SceneAntialiasing.BALANCED);
            mainSubScene.setFill(Color.web("#222222"));
            
            PerspectiveCamera godCamera = new PerspectiveCamera(true);
            godCamera.setNearClip(0.1);
            godCamera.setFarClip(1000000.0);
            godCamera.setTranslateZ(-modelRadius * 5.0);
            mainSubScene.setCamera(godCamera);

            // 5. 坐标轴 SubScene
            Group axisContent = createAxisGizmo(60);
            axisContent.getTransforms().addAll(rotateY, rotateX);
            
            // 将坐标轴内容平移到 SubScene 的中心 (100, 100)，避免原点在左上角
            Group axisContainer = new Group(axisContent);
            axisContainer.setTranslateX(100);
            axisContainer.setTranslateY(100);
            
            SubScene axisSubScene = new SubScene(new Group(axisContainer, new AmbientLight(Color.WHITE)), 200, 200, true, SceneAntialiasing.BALANCED);
            axisSubScene.setCamera(new PerspectiveCamera(false));

            // 6. 容器与响应式布局
            Text infoText = new Text(String.format("Model Size:\nX: %.2f\nY: %.2f\nZ: %.2f", sizeX, sizeY, sizeZ));
            infoText.setFill(Color.WHITE);
            infoText.setFont(Font.font("Consolas", 14));
            StackPane.setAlignment(infoText, Pos.TOP_LEFT);
            StackPane.setMargin(infoText, new javafx.geometry.Insets(10));

            StackPane rootPane = new StackPane();
            rootPane.getChildren().addAll(mainSubScene, axisSubScene, infoText);
            StackPane.setAlignment(axisSubScene, Pos.TOP_RIGHT);

            // 绑定尺寸，防止模型消失
            mainSubScene.widthProperty().bind(rootPane.widthProperty());
            mainSubScene.heightProperty().bind(rootPane.heightProperty());

            Scene scene = new Scene(rootPane, 1024, 768);
            
            scene.setOnMousePressed(me -> { mousePosX = me.getSceneX(); mousePosY = me.getSceneY(); });
            scene.setOnMouseDragged(me -> {
                double mouseDeltaX = (me.getSceneX() - mousePosX);
                double mouseDeltaY = (me.getSceneY() - mousePosY);
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                rotateY.setAngle(rotateY.getAngle() + mouseDeltaX * 0.2);
                rotateX.setAngle(rotateX.getAngle() - mouseDeltaY * 0.2);
            });
            scene.setOnScroll(se -> {
                if (se.isControlDown()) {
                    double scaleFactor = se.getDeltaY() > 0 ? 1.1 : 0.9;
                    double newScale = modelGroup.getScaleX() * scaleFactor;
                    modelGroup.setScaleX(newScale);
                    modelGroup.setScaleY(newScale);
                    modelGroup.setScaleZ(newScale);
                    infoText.setText(String.format("Model Size:\nX: %.2f\nY: %.2f\nZ: %.2f", 
                        sizeX * newScale, sizeY * newScale, sizeZ * newScale));
                } else {
                    double zoomFactor = se.getDeltaY() > 0 ? 0.9 : 1.1;
                    godCamera.setTranslateZ(godCamera.getTranslateZ() * zoomFactor);
                }
            });

            primaryStage.setTitle("GLTF Viewer - Visible Camera and Axis Gizmo");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Group createAxisGizmo(double size) {
        Group g = new Group();
        
        // X 轴 (红色)
        Box xAxis = new Box(size, 2, 2);
        xAxis.setTranslateX(size / 2);
        xAxis.setMaterial(new PhongMaterial(Color.RED));
        Text xLabel = new Text("X");
        xLabel.setFill(Color.RED);
        xLabel.setFont(Font.font(14));
        xLabel.setTranslateX(size + 5);
        
        // Y 轴 (绿色)
        Box yAxis = new Box(2, size, 2);
        yAxis.setTranslateY(size / 2);
        yAxis.setMaterial(new PhongMaterial(Color.GREEN));
        Text yLabel = new Text("Y");
        yLabel.setFill(Color.GREEN);
        yLabel.setFont(Font.font(14));
        yLabel.setTranslateY(size + 15);
        
        // Z 轴 (蓝色)
        Box zAxis = new Box(2, 2, size);
        zAxis.setTranslateZ(size / 2);
        zAxis.setMaterial(new PhongMaterial(Color.BLUE));
        Text zLabel = new Text("Z");
        zLabel.setFill(Color.BLUE);
        zLabel.setFont(Font.font(14));
        zLabel.setTranslateZ(size + 5);
        // 注意：在 2D Overlay 中 Text 不支持 Z 轴位移感官，但在 SubScene 中会随 Group 旋转
        
        g.getChildren().addAll(xAxis, yAxis, zAxis, xLabel, yLabel, zLabel);
        return g;
    }

    private Group createCameraGizmo(double size) {
        Group g = new Group();
        Box body = new Box(size, size * 0.6, size * 0.8);
        body.setMaterial(new PhongMaterial(Color.SILVER));
        Cylinder lens = new Cylinder(size * 0.3, size * 0.4);
        lens.setMaterial(new PhongMaterial(Color.BLACK));
        lens.setRotationAxis(Rotate.X_AXIS); lens.setRotate(90); lens.setTranslateZ(size * 0.5);
        Cylinder arrow = new Cylinder(size * 0.05, size * 1.5);
        arrow.setMaterial(new PhongMaterial(Color.LIME));
        arrow.setRotationAxis(Rotate.X_AXIS); arrow.setRotate(90); arrow.setTranslateZ(size * 1.2);
        g.getChildren().addAll(body, lens, arrow);
        return g;
    }

    private void calculateWorldBounds(gltf.GLTFNode node, float[] parentTransform, double[] bounds) {
        float[] localTransform = node.transformationMatrix;
        if (isDefaultMatrix(localTransform)) {
            localTransform = computeTRSMatrix(node.translation, node.rotation, node.scale);
        }
        float[] worldTransform = multiplyMatrices(parentTransform, localTransform);
        if (node.mesh != null) {
            for (gltf.mesh.GLTFMeshPrimitive primitive : node.mesh.primitives) {
                if (primitive.attributes.positionsAccessor != null) {
                    float[] min = primitive.attributes.positionsAccessor.min, max = primitive.attributes.positionsAccessor.max;
                    if (min != null && max != null) {
                        float[][] corners = {{min[0],min[1],min[2]}, {max[0],max[1],max[2]}};
                        for (float[] c : corners) {
                            float[] p = transformPoint(worldTransform, c);
                            bounds[0]=Math.min(bounds[0],p[0]); bounds[1]=Math.min(bounds[1],p[1]); bounds[2]=Math.min(bounds[2],p[2]);
                            bounds[3]=Math.max(bounds[3],p[0]); bounds[4]=Math.max(bounds[4],p[1]); bounds[5]=Math.max(bounds[5],p[2]);
                        }
                    }
                }
            }
        }
        for (gltf.GLTFNode child : node.getChildren()) calculateWorldBounds(child, worldTransform, bounds);
    }

    private float[] multiplyMatrices(float[] a, float[] b) {
        float[] res = new float[16];
        for (int i=0; i<4; i++) for (int j=0; j<4; j++) for (int k=0; k<4; k++) res[i+j*4] += a[i+k*4]*b[k+j*4];
        return res;
    }

    private float[] transformPoint(float[] m, float[] p) {
        float w = m[3]*p[0] + m[7]*p[1] + m[11]*p[2] + m[15];
        if (Math.abs(w) < 0.000001) w = 1.0f;
        return new float[]{(m[0]*p[0]+m[4]*p[1]+m[8]*p[2]+m[12])/w, (m[1]*p[0]+m[5]*p[1]+m[9]*p[2]+m[13])/w, (m[2]*p[0]+m[6]*p[1]+m[10]*p[2]+m[14])/w};
    }

    private boolean isDefaultMatrix(float[] m) {
        for (int i=0; i<16; i++) if (Math.abs(m[i]-((i%5==0)?1.0f:0.0f))>0.000001) return false;
        return true;
    }

    private float[] computeTRSMatrix(float[] t, float[] r, float[] s) {
        float x=r[0], y=r[1], z=r[2], w=r[3], x2=x+x, y2=y+y, z2=z+z, xx=x*x2, xy=x*y2, xz=x*z2, yy=y*y2, yz=y*z2, zz=z*z2, wx=w*x2, wy=w*y2, wz=w*z2;
        float[] m = new float[16];
        m[0]=(1-(yy+zz))*s[0]; m[1]=(xy+wz)*s[0]; m[2]=(xz-wy)*s[0]; m[4]=(xy-wz)*s[1]; m[5]=(1-(xx+zz))*s[1]; m[6]=(yz+wx)*s[1];
        m[8]=(xz+wy)*s[2]; m[9]=(yz-wx)*s[2]; m[10]=(1-(xx+yy))*s[2]; m[12]=t[0]; m[13]=t[1]; m[14]=t[2]; m[15]=1;
        return m;
    }

    public static void main(String[] args) { launch(args); }
}
