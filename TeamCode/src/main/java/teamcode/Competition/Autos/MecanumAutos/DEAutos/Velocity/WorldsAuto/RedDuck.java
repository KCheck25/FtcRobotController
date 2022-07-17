package teamcode.Competition.Autos.MecanumAutos.DEAutos.Velocity.WorldsAuto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.checkerframework.checker.units.qual.A;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import teamcode.Competition.Pipeline.MecanumPipeline.TapePipeline;
import teamcode.Competition.Pipeline.MecanumPipeline.TapePipeline.BarcodePosition;
import teamcode.Competition.Subsystems.ArmSystem;
import teamcode.Competition.Subsystems.EndgameSystems;
import teamcode.common.AbstractOpMode;
import teamcode.common.Constants;
import teamcode.common.Debug;
import teamcode.common.MecanumDriveTrain;
import teamcode.common.Utils;
import teamcode.common.Vector2D;

import static teamcode.Competition.Pipeline.MecanumPipeline.TapePipeline.BarcodePosition.*;

@Autonomous(name="\uD83D\uDFE5 RED DUCK")
public class RedDuck extends AbstractOpMode {
    MecanumDriveTrain drive;
    ArmSystem system;
    EndgameSystems systems;
    private OpenCvWebcam webcam;
    private BarcodePosition position;
    Thread armThread;
    volatile boolean[] flags = new boolean[]{false, false, false, false};
    private PIDFCoefficients coefficients = new PIDFCoefficients(5,0.5,1.0,0);


    @Override
    protected void onInitialize() {
        system = new ArmSystem(hardwareMap, false);
        systems = new EndgameSystems(hardwareMap, false);
        drive = new MecanumDriveTrain(hardwareMap, true, systems, system, coefficients);
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        WebcamName wc = hardwareMap.get(WebcamName.class, "Webcam");

        // W/ or W/ out live preview
        webcam = OpenCvCameraFactory.getInstance().createWebcam(wc, cameraMonitorViewId);
        TapePipeline pipeline = new TapePipeline();
        pipeline.setSide(TapePipeline.Side.RED);
        webcam.setPipeline(pipeline);

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT); //specify cam orientation and calibrate the resolution
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addData("Camera Init Error", errorCode);
                telemetry.update();
            }
        });
        armThread = new Thread(){
            @Override
            public void run(){
                while(!flags[0]);
                if(position == LEFT) {
                    system.raise(Constants.BOTTOM_POSITION + 1000);
                }else if(position == CENTER){
                    system.raise(Constants.MEDIUM_POSITION + 3000);
                }else{
                    system.raise(Constants.TOP_POSITION + 500);
                }
                while(!flags[1]);


                system.retract();
                while(!flags[2]);
                system.raise(Constants.TOP_POSITION + 3000);
                while(!flags[3]);

                Utils.sleep(200);
                system.retract();
                Utils.sleep(300);

            }
        };

        while(!opModeIsActive() && !isStopRequested()){
            position = pipeline.getPos();
            telemetry.addData("pos", pipeline.getPos());
            telemetry.addData("", pipeline.getVal());
            telemetry.update();
        }

    }

    private final double VELOCITY = 10;
    @Override
    protected void onStart() {
        system.actuateWinchStop(1.0);
        webcam.stopStreaming();
        armThread.start();
        drive.moveDistanceDEVelocity(1200, -45, 2 * VELOCITY);
        //Utils.sleep(200);

        Utils.sleep(200);
        //drive.driveColorSensorWarehouse(6);
        drive.moveDistanceDEVelocity(540, 0, VELOCITY);
        Utils.sleep(200);
        drive.rotateDistanceDE(75, 6);

        //extend
        flags[0] = true;
        Utils.sleep(200);

        //score
        Utils.sleep(300);
        drive.moveDistanceDEVelocity(720 + (position == CENTER ? 40 : 20), 180, VELOCITY);
        if(position == LEFT){
            system.runConveyorPos(0.5, 2000);
        }else {
            system.score();
        }
        Utils.sleep(200);
        //drive.moveDistanceDEVelocity(200, 90, VELOCITY ); //400


        drive.rotateDistanceDE(160, 12);
        flags[1] = true;
        //Utils.sleep(200);
        drive.strafeDistanceSensor(6, 0);
        system.setIsDuck(true);
        //drive.moveDistanceDEVelocity(300, 180, VELOCITY);

        Utils.sleep(200);
        drive.moveDistanceDEVelocity(100, -90, 2 * VELOCITY);
        Utils.sleep(200);
        drive.moveDistanceDEVelocity(400, 0, 2 * VELOCITY);//
        //drive.driveColorSensorWarehouse(4);
        Utils.sleep(200);
        drive.moveDistanceDEVelocity(400, 0, VELOCITY);
        Utils.sleep(200);

        drive.duck();
        system.lowerLinkage();
        system.intakeDumb(1.0);
        drive.setPower(0.02,0.02,0.02,0.02);

        systems.scoreDuckAuto();
        Utils.sleep(250);

        //drive.moveDistanceDEVelocity(150, 60, VELOCITY / 2.0);
        drive.moveDistanceDEVelocity(450, -90, VELOCITY / 2.0);
        Utils.sleep(150);
        drive.moveDistanceDEVelocity(75, 0, VELOCITY / 2.0);
        Utils.sleep(150);
        drive.rotateDistanceDE(190, 6);
        Utils.sleep(100);
        drive.moveDistanceDEVelocity(400, -90, VELOCITY / 2.0); //400


        Utils.sleep(200);
        //drive.moveDistanceDEVelocity(100, -90, VELOCITY / 2.0);

        //drive.moveDistanceDEVelocity(420, 0, VELOCITY / 2.0);
        //Utils.sleep(500);
        system.duckPreScore();
        Utils.sleep(200);
        drive.moveDistanceDEVelocity(550, -57, -2 * VELOCITY);
        //Utils.sleep(200);
        /*Utils.sleep(200);
        drive.moveDistanceDEVelocity(100, -90, 2 * VELOCITY);*/

        Utils.sleep(150);
        //drive.driveColorSensorWarehouse(6);
        drive.moveDistanceDEVelocity(900, 180.0, VELOCITY);

        Utils.sleep(150);
        drive.rotateDistanceDE(75, 6);


//        drive.moveDistanceDEVelocity(300, 180, VELOCITY);
//        Utils.sleep(200);
//        system.intakeDumb(0);
//        drive.strafeDistanceSensor(20, 0);
//        drive.moveDistanceDEVelocity(300, 90, 2 * VELOCITY); //400
//        Utils.sleep(200);
//        drive.moveDistanceDEVelocity(750, 180,  VELOCITY);
//        Utils.sleep(200);
//        drive.rotateDistanceDE(-75, 12);
        Utils.sleep(200);


        //extend
        flags[2] = true;
        drive.moveDistanceDEVelocity(700, 180, VELOCITY);
        Utils.sleep(250);
        //score
        system.score();
        Utils.sleep(200);
        system.jitterHouse();
        Utils.sleep(200);
        flags[3] = true;
        Utils.sleep(200);

        drive.moveDistanceDEVelocity(1000, 0, 3 * VELOCITY);
        Utils.sleep(100);
        drive.moveDistanceDEVelocity(500, -90, 3 * VELOCITY ); //400
        Utils.sleep(100);
        drive.moveDistanceDEVelocity(150, 0, 2 * VELOCITY);
        Utils.sleep(100);
        /*Utils.sleep(100);
        drive.moveDistanceDEVelocity(100, -45, VELOCITY);*/
        //drive.setPower(0.2,0.2,0.2,0.2);
        while(opModeIsActive() && !isStopRequested());
    }

    //drive.strafeDistanceSensor(6,0);

    @Override
    protected void onStop() { }
}
