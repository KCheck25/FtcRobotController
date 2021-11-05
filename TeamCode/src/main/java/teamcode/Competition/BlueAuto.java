package teamcode.Competition;

import com.intel.realsense.librealsense.Pipeline;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import teamcode.Competition.BarcodePipeline;
import teamcode.common.AbstractOpMode;
import teamcode.common.Constants;
import teamcode.common.Debug;
import teamcode.common.Localizer;
import teamcode.common.Utils;
import teamcode.common.Vector2D;
import teamcode.common.WestCoastDriveTrain;

@Autonomous(name="BlueAuto")
public class BlueAuto extends AbstractOpMode {

    WestCoastDriveTrain driveTrain;
    ArmSystem arm;
    EndgameSystems system; //carousel
    Localizer localizer;

    OpenCvWebcam webcam;
    BarcodePipeline.BarcodePosition position;

    @Override
    protected void onInitialize() {
        localizer = new Localizer(hardwareMap, new Vector2D(0,0), 0,10);
        driveTrain = new WestCoastDriveTrain(hardwareMap, localizer);
        arm = new ArmSystem(hardwareMap, false);
        system = new EndgameSystems(hardwareMap, true);

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        WebcamName wc = hardwareMap.get(WebcamName.class, "Webcam");

        // W/ or W/ out live preview
        webcam = OpenCvCameraFactory.getInstance().createWebcam(wc, cameraMonitorViewId);
        // camera = OpenCvCameraFactory.getInstance().createWebcam(wc);

        BarcodePipeline pipeline = new BarcodePipeline();
        webcam.setPipeline(pipeline);
        pipeline.setSide(BarcodePipeline.Side.BLUE);
        localizer.liftOdo();

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT); //specify cam orientation and calibrate the resolution
            }

            @Override
            public void onError(int errorCode) {
                //telemetry.addData("Camera Init Error", errorCode);
                //telemetry.update();
            }
        });
        while(!opModeIsActive()){
            position = pipeline.getPos();
            telemetry.addData("", position);
            telemetry.update();
        }
    }

    @Override
    protected void onStart() {
        webcam.stopStreaming();
        telemetry.clear();
        localizer.start();
        driveTrain.moveToPosition(new Vector2D(0, 6), 12, 0.5, true);
        // Utils.sleep(2000);
        driveTrain.rotateDistance(-0.5, Math.toRadians(-120));
        if (position == BarcodePipeline.BarcodePosition.RIGHT) {
            arm.raise(Constants.TOP_POSITION);
        } else if(position == BarcodePipeline.BarcodePosition.CENTER){
            arm.raise(Constants.MEDIUM_POSITION);

        }else
        if (position == BarcodePipeline.BarcodePosition.LEFT) {

            driveTrain.moveToPosition(new Vector2D(8, 16), -12, 0.5, false);
            driveTrain.rotateDistance(-0.4, Math.toRadians(-170));
            arm.raise(Constants.BOTTOM_POSITION);
        } else{
            Debug.log("here");
            driveTrain.moveToPosition(new Vector2D(7.53, 17.25), -12, 0.5, false);
            driveTrain.rotateDistance(-0.4, Math.toRadians(-170));

        }
        arm.score();
        Utils.sleep(500);
        Vector2D position = localizer.getCurrentState().getPosition();
        double rotation = localizer.getCurrentState().getRotation();
        Vector2D constructedVector = new Vector2D(position.getX() + 7* Math.cos(rotation), position.getY() - 7* Math.sin(rotation));
        telemetry.addData("vec", constructedVector);
        telemetry.update();
        driveTrain.moveToPosition(constructedVector, 12, 0.5, false);
        arm.retract();
        driveTrain.rotateDistance(0.4, Math.toRadians(-90));
        driveTrain.moveToPosition(new Vector2D(-29, 19), 24, 0.5, false);


        //driveTrain.moveToPosition(new Vector2D(-14,6), 12, 0.1);


        while(opModeIsActive());


    }

    @Override
    protected void onStop() {
        localizer.stopThread();
    }
}
