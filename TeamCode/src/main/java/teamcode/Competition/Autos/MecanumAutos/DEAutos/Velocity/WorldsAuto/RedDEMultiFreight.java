package teamcode.Competition.Autos.MecanumAutos.DEAutos.Velocity.WorldsAuto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.ArrayList;

import teamcode.Competition.Pipeline.MecanumPipeline.TapePipeline;
import teamcode.Competition.Subsystems.ArmSystem;
import teamcode.Competition.Subsystems.EndgameSystems;
import teamcode.common.AbstractOpMode;
import teamcode.common.Constants;
import teamcode.common.MecanumDriveTrain;
import teamcode.common.Movements.ArcMovement;
import teamcode.common.Movements.CoastFunction;
import teamcode.common.Movements.ModifyFlag;
import teamcode.common.Movements.ModifyZeroPower;
import teamcode.common.Movements.ModulateIntake;
import teamcode.common.Movements.Movement;
import teamcode.common.Movements.RotationalMovement;
import teamcode.common.Movements.StrafeTP;
import teamcode.common.Movements.TranslationalMovement;
import teamcode.common.Movements.Wait;
import teamcode.common.Movements.WallNormalization;
import teamcode.common.Movements.WarehouseNormalization;
import teamcode.common.Utils;

import static teamcode.Competition.Pipeline.MecanumPipeline.TapePipeline.BarcodePosition.CENTER;
import static teamcode.Competition.Pipeline.MecanumPipeline.TapePipeline.BarcodePosition.LEFT;


@Autonomous(name="\uD83D\uDFE5 RED FREIGHT")
public class RedDEMultiFreight extends AbstractOpMode {
    MecanumDriveTrain drive;
    EndgameSystems system;
    ArmSystem arm;
    Thread armThread;
    private final int FREIGHT = 3;
    private ArrayList<Movement> warehouseSplice;
    private final double VELOCITY = 50;
    private boolean earlyTerminate = false;
    PIDFCoefficients coefficients = new PIDFCoefficients(10, 0.5, 1.0, 1.0); //2.5
    int globalIterator;
    TapePipeline.BarcodePosition position = LEFT;
    OpenCvWebcam webcam;
    private ElapsedTime curTime = new ElapsedTime();

    @Override
    protected void onInitialize() {
        system = new EndgameSystems(hardwareMap, false);
        arm = new ArmSystem(hardwareMap, false);
        drive = new MecanumDriveTrain(hardwareMap, true, system, arm, coefficients);
        warehouseSplice = new ArrayList<>();
        armThread = new Thread(){
            public void run(){
                Utils.sleep(200);
                if(position == LEFT){
                    arm.raise(Constants.BOTTOM_POSITION);
                }else if(position == CENTER){
                    arm.raise(Constants.MEDIUM_POSITION + 3000);
                }else{
                    arm.raise(Constants.TOP_POSITION );
                }
                arm.setLinkageScored();
                Utils.sleep(200);
                while(!drive.getFlagIndex(4));
                if(position == LEFT) {
//                    arm.runConveyorPos(1.0,3000);
//                    Utils.sleep(100);
                }else {
                    Utils.sleep(100);
                    if (position == CENTER) {
                        arm.scoreFar();
                    } else {
                        arm.score();
                    }
                }
                arm.runConveyor(1.0);

                Utils.sleep(250);
                arm.retract();
                for(int i = 0; i < FREIGHT && opModeIsActive() && !isStopRequested(); i++) {
                    /*while(!drive.getFlagIndex(0));
                    arm.preScoreMultiFreight();

                    drive.setFlagIndex(0, false);
//                    while(!drive.getFlagIndex(5));
//                    arm.preScoreMultiFreight(drive.getCurrenElement());
//                    drive.setFlagIndex(5, false);
                    while (!drive.getFlagIndex(1));
                    arm.intakeDumb(-1.0);
                    if(i > 1) {
                        arm.raise(Constants.TOP_POSITION);
                    }else {
                        arm.raise(Constants.TOP_POSITION);
                    }
                    drive.setFlagIndex(1, false);
                    while (!drive.getFlagIndex(2));
                    arm.scoreAuto();
                    Utils.sleep(250);
                    arm.retract();
                    drive.setFlagIndex(2, false);*/

                    // test
                    while (!drive.getFlagIndex(0));
                    arm.preScoreMultiFreight();

                    drive.setFlagIndex(0, false);
                    while (!drive.getFlagIndex(1));
                    arm.intakeDumb(-1.0);

                    drive.setFlagIndex(1, false);
                    while (!drive.getFlagIndex(2));
                    if (i > 1) {
                        arm.raise(Constants.TOP_POSITION);
                    } else {
                        arm.raise(Constants.TOP_POSITION);
                    }

                    drive.setFlagIndex(2, false);
                    while (!drive.getFlagIndex(3));
                    arm.scoreAuto();
                    Utils.sleep(250);
                    arm.retract();
                    drive.setFlagIndex(3, false);
                }
            }
        };

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

        while(!opModeIsActive() && !isStopRequested()){
            position = pipeline.getPos();
            telemetry.addData("pos", pipeline.getPos());
            telemetry.addData("", pipeline.getVal());
            telemetry.update();
        }
    }

    @Override
    protected void onStart() {
        curTime.reset();
        curTime.startTime();
        armThread.start();
        arm.actuateWinchStop(1.0);
        drive.moveDistanceDEVelocity(750 + (position == CENTER ? 25 : (position == LEFT ? 20 : 50)), -45, 2 * VELOCITY); // 900 -45

        Utils.sleep(75);
        drive.rotateDistanceDEUnramped(150, 24);
        Utils.sleep(75);

        if(position == LEFT) {
            arm.runConveyorPos(1.0, 2000);
            drive.setFlagIndex(4,true);
        }else {
            drive.setFlagIndex(4, true);
            Utils.sleep(100);
        }

        Utils.sleep(750);
        drive.moveDistanceDEVelocity(100, 0, 2 * VELOCITY);
        Utils.sleep(100);
        drive.rotateDistanceDEUnramped(-120, 30);
        Utils.sleep(100);
        drive.strafeDistanceSensor(40, Math.PI / 5.0);
        boolean first = true;

        for(int i = 0; i < FREIGHT && !earlyTerminate; i++) {

            warehouseSplice.add(new TranslationalMovement( 1000 + (10 * i), 2 * VELOCITY, 0.0, true));
            warehouseSplice.add(new ModulateIntake(0.8  ));


            warehouseSplice.add(new CoastFunction(1.5, 300.0, 0.0));
            warehouseSplice.add(new ModifyZeroPower(DcMotor.ZeroPowerBehavior.BRAKE));
            warehouseSplice.add(new ModifyFlag(true, 0));


            warehouseSplice.add(new TranslationalMovement(300 /*+ 10 * i*/, 2 * VELOCITY, 180.0)); // 180.0
            warehouseSplice.add(new StrafeTP( (long)100,1.0));// change this to 100 and the arc to 1550

            warehouseSplice.add(new ModulateIntake(0.0));

            if (curTime.time() < 24) {
                warehouseSplice.add(new TranslationalMovement(100, 2 * -VELOCITY, 0, true));
                warehouseSplice.add(new Wait(50));
                warehouseSplice.add(new WallNormalization(45, Math.PI / 5));

                warehouseSplice.add(new Wait(50));
                warehouseSplice.add(new ModifyFlag(true, 1));
                warehouseSplice.add(new WarehouseNormalization(-4, 500, false));
                warehouseSplice.add(new Wait(50));

                warehouseSplice.add(new ModifyFlag(true, 2));
                warehouseSplice.add(new TranslationalMovement(130, 2 * -VELOCITY, 0, true));
                warehouseSplice.add(new ArcMovement(1600, -40.0, 131.8)); // -6, 1500
                warehouseSplice.add(new ModifyFlag(true, 3));
                warehouseSplice.add(new Wait(200));

                warehouseSplice.add(new RotationalMovement(-105, 30.0));
                warehouseSplice.add(new Wait(50));
                if (i < 2) {
                    warehouseSplice.add(new WallNormalization(45, Math.PI / 5.0));
                } else {
                    warehouseSplice.add(new WallNormalization(VELOCITY * 10, Math.PI / 5));
                }
            } else {

                earlyTerminate = true;
            }

            drive.splicedMovement(warehouseSplice);
            warehouseSplice.clear();
        }
        if (!earlyTerminate) {
            drive.moveDistanceDEVelocity(1400, 0, VELOCITY * 10);
        }

    }

    @Override
    protected void onStop() {
      //  drive.writeLoggerToFile();
        webcam.stopStreaming();
    }
}
