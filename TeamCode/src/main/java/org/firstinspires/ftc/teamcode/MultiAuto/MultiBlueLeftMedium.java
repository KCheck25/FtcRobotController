package org.firstinspires.ftc.teamcode.MultiAuto;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.Auto.ContourMultiScore;
import org.firstinspires.ftc.teamcode.Common.Constants;
import org.firstinspires.ftc.teamcode.Common.MecanumDrive;
import org.firstinspires.ftc.teamcode.Common.ScoringSystem;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.concurrent.atomic.AtomicBoolean;


public class MultiBlueLeftMedium extends LinearOpMode {
    Thread liftThread;
    MecanumDrive drive;
    ContourMultiScore detect1;
    ScoringSystem score;
    Constants constants;
    OpenCvWebcam webcam;
    AtomicBoolean cont;

    private double properCX = 141; //67
    public static int positive_negative = 1;
    public static int turnDenom = 4;

    private boolean left, right = true;

    @Override
    public void runOpMode() throws InterruptedException {
        detect1 = new ContourMultiScore(telemetry);
        drive = new MecanumDrive(hardwareMap, telemetry);
        score = new ScoringSystem(hardwareMap, telemetry);
        constants = new Constants();
        cont = new AtomicBoolean();
        cont.set(false);

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        webcam.setPipeline(detect1);

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {

            @Override
            public void onOpened() {
                webcam.startStreaming(320, 176, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addData("Camera Init Error", errorCode);
                telemetry.update();
            }
        });

        liftThread = new Thread() {
            @Override
            public void run(){
                while(opModeIsActive()){
                    if((score.getEncoderPosition() > 200 && cont.get())){
                        score.setPower(constants.getSteadyPow());
                    }

//
//                    telemetry.addData("liftPow", score.getPower());
//                    telemetry.addData("liftPos", score.getEncoderPosition());
//                    telemetry.update();
                }

            }
        };

        // code to turn servo of cam
        score.setCamPosition(constants.getSleeveCamPos());
        detect1.park = true;
//        // ftc dashboard
//        FtcDashboard.getInstance().startCameraStream(webcam, 0);
//        telemetry.addData("Status", "Initialized");

        telemetry.update();

        //close claw
        score.setClawPosition(constants.getClawClosePos());
        waitForStart();
        detect1.park = false;
        // turn servo of cam forward for poles
        score.setCamPosition(constants.getStrafeCamPos());

        liftThread.start();
        //lift claw a little bit
        score.goToPosition(50, 0.7);
        sleep(200);
        // go forward next to pole
        drive.goToPositionPID(1060, "go forward next to pole");
        // turn to left 45 degrees to medium pole
        drive.turn(-Math.PI / 4);
        // go to pole a bit
        drive.goToPosition(0.3, 0.3, 0.3, 0.3, 90, "go forward some to pole");
        sleep(100);


        // camera position correction
        while (detect1.getcX() < properCX - 5 || detect1.getcX() > properCX + 5) {
            if (detect1.getcX() < properCX - 5 && left) {
                // strafe to the right
//                drive.goToPosition(0.15, -0.15, -0.15, 0.15);
                drive.goToPosition(-0.15, 0.15, 0.15, -0.15);
                left = false;
            }
            if(detect1.getcX() > properCX + 5 && right) {
                // strafe to the left (change fr and bl)
                drive.goToPosition(0.15, -0.15, -0.15, 0.15);

//                drive.goToPosition(-0.15, 0.15, 0.15, -0.15);
                right = false;
            }
            if (detect1.getcX() >= properCX - 5 && detect1.getcX() <= properCX + 5) {
                drive.goToPosition(0, 0, 0, 0);
            }
        }

        sleep(200);

        scoreCone(438, 416, 437, 426);

        // turn back straight
        drive.turn(-Math.PI / 5);
        //moves robot to correct parking position
//        if (detect1.getParkPosition() == ContourMultiScore.ParkingPosition.LEFT) {
//            // move to left park (strafe right)
//            drive.goToPosition(-0.3, -0.3, -0.3, -0.3, 1372, "move backwards");
//            drive.goToPosition(-0.3, 0.3, 0.3, -0.3 , 200, "strafe right");
//
//
//
//        } else if (detect1.getParkPosition() == ContourMultiScore.ParkingPosition.CENTER) {
//            // move to center park (don't move at all)
//            drive.goToPosition(-0.3, 0.3, 0.3, -0.3 , 200, "strafe right");
//
//        } else {
//            // move to right park (strafe more left)
//            drive.goToPosition(0.3, 0.3, 0.3, 0.3 , 1000, "move forward");
//            drive.goToPosition(-0.3, 0.3, 0.3, -0.3 , 200, "strafe right");
//
//
//        }


        score.setClawPosition(constants.getClawOpenPos());



        /*
        // turn back straight
        drive.turn(Math.PI / 4.3);
        //go forward to blue cone tape adjacent mat
        drive.goToPositionPID(drive.avgPosition(1028, 1056, 1041, 1026), "go forward to next mat");
        // turn to tape/cones
        drive.turn(-Math.PI / 2);
        // find tape, get cone
        useColorSensor();
        // back up
        drive.goToPosition(-0.4, -0.4, -0.4, -0.4, -284, "back up");
        //put lift down

        // turn 90 to the right
        drive.turn(Math.PI / 2);
        // strafe right
        drive.goToPosition(-0.4, 0.4, 0.4, -0.4, drive.avgPosition(-1727, 1651, 1650, -891), "strafe right");
        // camera position correction
        if (detect1.getcX() < properCX - 5 || detect1.getcX() > properCX + 5) {
            while (detect1.getcX() < properCX - 5) {
                // strafe to the right
                drive.goToPosition(0.2, -0.2, -0.2, 0.2);

            }
            while (detect1.getcX() > properCX + 5) {
                // strafe to the left (change fr and bl)
                drive.goToPosition(-0.2, 0.2, 0.2, -0.2);

            }
        }
        // scoring cone
        scoreCone(184, 165, 163, 147);


         */
    }

    public void scoreCone(int fl, int fr, int bl, int br) {
        cont.set(true);
        // move arm medium
        score.goToPosition(constants.getHeightMed(), 0.85);
        //begin thread for maintaining height of slides


        //3700 - 3800
        drive.goToPosition(0.15, 0.15, 0.15, 0.15);
        sleep(100);
//        while (detect1.getBoundArea() <= 9550.0 || detect1.getBoundArea() >= 10000) {
//            if (detect1.getBoundArea() >= 9600.0 && detect1.getBoundArea() <= 10000 && detect1.getDistance() <= 4/*|| detect1.getcX() <= 18*/) {
//                drive.goToPosition(0, 0, 0, 0);
//            }
//
//        }



        drive.goToPosition(0, 0, 0, 0);


        sleep(500);

        //lower cone onto pole
        score.goToPosition(score.getEncoderPosition()-300, 0.4);
        score.setClawPosition(constants.getClawOpenPos());
        sleep(300);

        //move back from pole
        drive.goToPosition(-0.3, -0.3, -0.3, -0.3, drive.avgPosition(fl, fr, bl, br), "move back from pole");
        cont.set(false);
        //moves slides down
        score.goToPosition(0, 0.3);
        sleep(300);
    }

    public void useColorSensor() {
        drive.findTape();
        score.goToPosition(174, 0.7);
        score.grabConeAuto();


    }
}