package teamcode.Competition.Autos.MecanumAutos.DEAutos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import teamcode.Competition.Pipeline.MecanumPipeline.MecanumBarcodePipeline;
import teamcode.Competition.Subsystems.ArmSystem;
import teamcode.Competition.Subsystems.EndgameSystems;
import teamcode.common.AbstractOpMode;
import teamcode.common.Constants;
import teamcode.common.Debug;
import teamcode.common.MecanumDriveTrain;
import teamcode.common.Utils;


@Autonomous(name="Red DE Warehouse")
public class RedDEWarehouseAuto extends AbstractOpMode {

    private static final int FREIGHT = 2;
    MecanumDriveTrain drive;
    ArmSystem arm;
    EndgameSystems system;
    volatile boolean[] flags;
    Thread armCommands;
    OpenCvWebcam webcam;
    MecanumBarcodePipeline.BarcodePosition position;


    @Override
    protected void onInitialize() {
        system = new EndgameSystems(hardwareMap, false);
        arm = new ArmSystem(hardwareMap, false);
        drive = new MecanumDriveTrain(hardwareMap, true, system, arm);
        Debug.log("here");
        flags = new boolean[]{false, false, false, false, false};
        armCommands = new Thread(){
            public void run() {
                Utils.sleep(500);
                if(position == MecanumBarcodePipeline.BarcodePosition.LEFT){
                    arm.raise(Constants.BOTTOM_POSITION);
                }else if(position == MecanumBarcodePipeline.BarcodePosition.CENTER){
                    arm.raise(Constants.MEDIUM_POSITION + 1000);
                }else{
                    arm.raise(Constants.TOP_POSITION + 1000);
                }
                while (!flags[0]);
                if(position == MecanumBarcodePipeline.BarcodePosition.LEFT){
                    arm.runConveyorPos(0.8, 1000);
                }else{
                    Debug.log("her");
                    arm.score();
                }
                Utils.sleep(250);
                arm.retract();
                for(int i = 0; i < FREIGHT; i++) {
                    while (!flags[1]) ;
                    arm.raise(Constants.TOP_POSITION + 1000);
                    flags[1] = false;
                    while (!flags[2]) ;
                    arm.score();
                    Utils.sleep(250);
                    arm.retract();
                    flags[2] = false;
                }


                while(opModeIsActive()); //this was a mistake I made in my inital one too,
                // you want this here as a buffer so that the thread doesnt prematurely end vs what
                //I had which looped the threads actions and because the flags were all true there was no buffer time
            }
        };

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        WebcamName wc = hardwareMap.get(WebcamName.class, "Webcam");

        // W/ or W/ out live preview
        webcam = OpenCvCameraFactory.getInstance().createWebcam(wc, cameraMonitorViewId);
        MecanumBarcodePipeline pipeline = new MecanumBarcodePipeline();
        pipeline.setSide(MecanumBarcodePipeline.Side.RED );
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
        while(!opModeIsActive()){
            position = pipeline.getPos();
            telemetry.addData("", position);
            telemetry.update();
        }
    }

    private final double  VELOCITY = 10; //10
    @Override
    protected void onStart() {
        webcam.stopStreaming();
        armCommands.start();
       // 1300, -45
        drive.moveDistanceDEVelocity(1000, -45, VELOCITY);
        Utils.sleep(100);
        drive.rotateDistanceDE(160, 6);
        flags[0] = true;
        Utils.sleep(200);
        for(int i = 0; i < FREIGHT; i++) {
            drive.rotateDistanceDE(-90, 6);
            drive.strafeDistanceSensor(VELOCITY, 0);
            drive.driveColorSensor(2);
            drive.moveDistanceDEVelocity(200, 0, VELOCITY);
            drive.strafeDistanceSensor(VELOCITY,0);
            Utils.sleep(100);

            drive.rotateDistanceDE(-135, 6);
            flags[1] = true;
            drive.moveDistanceDEVelocity(550, 180, VELOCITY);
            flags[2] = true;
            Utils.sleep(200);
        }

        while(opModeIsActive());


    }

    /*
    drive.moveDistanceDE(500, 0, 0.3, 0);
        flags[0] = true;
        drive.rotateDistanceDE(-135, 0.3);
        sleep(250);
        drive.moveDistanceDE(490, -180, 0.3, 0);
        flags[1] = true;
        sleep(1000);
        drive.rotateDistanceDE(-90, 0.3);
        sleep(1000);
        drive.moveDistanceDENoErrorCorrection(1500, 90, 0.3);
        drive.moveDistanceDE(1800, 0, 0.3, 0);

     */

    /*
    if (position == MecanumBarcodePipeline.BarcodePosition.LEFT) {
                    arm.raise(Constants.BOTTOM_POSITION);

                } else if (position == MecanumBarcodePipeline.BarcodePosition.CENTER) {
                    arm.raise(Constants.MEDIUM_POSITION);
                } else {
                    arm.raise(Constants.TOP_POSITION);
                }
                flags[0] = false;
                while (!flags[1]) ;
                if (position == MecanumBarcodePipeline.BarcodePosition.LEFT) {
                    arm.runConveyorPos(0.5, 2000);
                } else {
                    arm.score();
                }
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                arm.retract();

     */

    /*drive.moveDistanceDE(400, 0, 0.3, 0);
        drive.rotateDistanceDE(-90, 0.3);
        drive.moveDistanceDE(1400, -90, 0.3, 0.2);
        drive.moveDistanceDE(200, -90, 0.3, 0.2);
        flags[0] = true;
        drive.moveDistanceDE(600, -180, 0.3, 0);
        flags[1] =true;
        drive.moveDistanceDE(600, 0, 0.3, 0);


        drive.goToAllianceHub;
        drive.score;
        drive.park;

         */

    @Override
    protected void onStop() {

    }
}
