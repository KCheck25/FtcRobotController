package org.firstinspires.ftc.teamcode.League1.Testing.AutonomousTesting;

////import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.League1.Autonomous.Vision.KevinGodPipeline;
import org.firstinspires.ftc.teamcode.League1.Common.Constants;
import org.firstinspires.ftc.teamcode.League1.Subsystems.MecDrive;
import org.firstinspires.ftc.teamcode.League1.Subsystems.ScoringSystem2;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.concurrent.atomic.AtomicBoolean;

@Disabled
@Autonomous
public class DuplicateAuto extends LinearOpMode {
    MecDrive drive;
    ScoringSystem2 score;
    //Constants constants;
    Thread armThread, feedForward, idController;
    ElapsedTime time = new ElapsedTime();
    AtomicBoolean hold, armUp, armDown, finalMove, linkageUp;

    ColorRangeSensor distance, color;
    Servo cameraServo;

    OpenCvWebcam camera;
    KevinGodPipeline pipeline;
    KevinGodPipeline.ParkPos parkPos;

    int normalizeDistance;
    boolean failed;



    @Override
    public void runOpMode() throws InterruptedException {
        distance = hardwareMap.get(ColorRangeSensor.class, "distance");
        //color = hardwareMap.get(ColorRangeSensor.class, "color");

        drive = new MecDrive(hardwareMap, false, telemetry, true);
        //constants = newConstants();
        score = new ScoringSystem2(hardwareMap);
        hold = new AtomicBoolean(false);
        armUp = new AtomicBoolean(false);
        finalMove = new AtomicBoolean(false);
        linkageUp = new AtomicBoolean(false);
        armDown = new AtomicBoolean(false);

        score.setLinkagePosition(Constants.linkageDown);
        score.setGrabberPosition(Constants.grabbing);


        cameraServo = hardwareMap.get(Servo.class, "camera");
        failed = false;


        distance.setGain(300);


        /*
        idController = new Thread(){
            @Override
            public void run() {



                while(opModeIsActive()){
                    if(hold.get()){
                        ElapsedTime time = new ElapsedTime();
                        double startTime = time.milliseconds();

                        int leftIntegralSum = 0;
                        int rightIntegralSum = 0;


                        int rLiftPos = score.getRightEncoderPos();
                        int lLiftPos = -1 * score.getLeftEncoderPos();

                        int tics = score.getHeight();

                        int leftPreviousError = Math.abs(tics - lLiftPos);
                        int rightPreviousError = Math.abs(tics - rLiftPos);

                        while(hold.get()){

                            rLiftPos = score.getRightEncoderPos();
                            lLiftPos = -1 * score.getLeftEncoderPos();

                            double currentTime = time.milliseconds();

                            int leftError = tics - lLiftPos;
                            int rightError = tics - rLiftPos;

                            leftIntegralSum += (0.5 * (leftError + leftPreviousError) * (currentTime - startTime));
                            rightIntegralSum += (0.5 * (rightError + rightPreviousError) * (currentTime - startTime));

                            double leftDerivative = (leftError - leftPreviousError)/(currentTime - startTime);
                            double rightDerivative = (rightError - rightPreviousError)/(currentTime - startTime);

                            double leftPower = (pid.i * leftIntegralSum) + (pid.d * leftDerivative);
                            double rightPower = (pid.i * rightIntegralSum) + (pid.d * rightDerivative);

                            if(tics < ((rLiftPos + lLiftPos) / 2)){
                                leftPower *= -1;
                                rightPower *= -1;
                            }

                            score.setPower(rightPower, leftPower);


                            startTime = currentTime;
                            leftPreviousError = leftError;
                            rightPreviousError = rightError;



                        }




                    }
                }
            }
        };

         */

        armThread = new Thread(){
            @Override
            public void run() {
                //score.setLinkagePosition(0.7);
                while(opModeIsActive()) {
                    if(armUp.get()) {
                        hold.set(false);

                        /*
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                         */

                        score.moveToPosition(830, 1);
                        hold.set(true);
                        score.setLinkagePositionLogistic(Constants.linkageScore, 300, 50);


                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        score.setGrabberPosition(0.3);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        armUp.set(false);
                    }else if(armDown.get()){
                        hold.set(false);
                        score.setLinkagePosition(Constants.linkageUp);
                        score.moveToPosition(0, 0.5);
                        //score.setLinkagePositionLogistic(Constants.linkageDown, 250, 30);
                        armDown.set(false);
                    }else if(finalMove.get()){

                        score.setLinkagePositionLogistic(Constants.linkageUp, 250);
                        finalMove.set(false);

                    }else if(linkageUp.get()){
                        try {
                            sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        score.setLinkagePosition(Constants.linkageUp);
                        linkageUp.set(false);
                    }




                }

                //Might need this
                //hold.set(true);
            }
        };



        feedForward = new Thread(){
            @Override
            public void run() {
                while(opModeIsActive()){
                    if(hold.get()){
                        score.setPower(0.2);
                    }
                }
            }
        };



        WebcamName webcamName = hardwareMap.get(WebcamName.class, "Webcam 1");
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId);
        pipeline = new KevinGodPipeline(telemetry, drive, KevinGodPipeline.AutoSide.BLUE_RIGHT);

        camera.setPipeline(pipeline);


        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(320, 176, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addData("Camera Init Error", errorCode);
                telemetry.update();

            }
        });

        ////FtcDashboard.getInstance().startCameraStream(camera, 0);





        cameraServo.setPosition(Constants.sleeve);




        waitForStart();
        double startTime = time.seconds();

        parkPos = pipeline.getPosition();
        pipeline.changeMode(KevinGodPipeline.Mode.POLE);




        armThread.start();
        feedForward.start();




        cameraServo.setPosition(Constants.pole);


        linkageUp.set(true);
        drive.goTOPIDPos(-2100, 0.5,MecDrive.MovementType.STRAIGHT);

        armUp.set(true);
        drive.tankRotatePID(Math.PI / 4.5, 0.7, true);
        sleep(200);
        //drive.simpleMoveToPosition(-250, MecDrive.MovementType.ROTATE, 0.4);
        normalizeDistance = pipeline.normalize(0.2, 172, 2);
        //pipeline.Ynormalize(0.2, 95, 5);

        if(pipeline.getNormalizationBroke()){
            drive.tankRotatePID(Math.PI/2, 0.6, false);
            armDown.set(true);

            if (parkPos == KevinGodPipeline.ParkPos.LEFT) {
                drive.simpleMoveToPosition(-500, MecDrive.MovementType.STRAIGHT, 0.5);

            } else if (parkPos == KevinGodPipeline.ParkPos.RIGHT) {
                drive.simpleMoveToPosition(700, MecDrive.MovementType.STRAIGHT, 0.5 );

            }
        }else {


            //drive.simpleMoveToPosition(-40, MecDrive.MovementType.STRAIGHT, 0.3);

            while(armUp.get()){

            }

            cameraServo.setPosition(Constants.cone);
            sleep(350);
            score.setGrabberPosition(Constants.grabbing);
            //sleep(500);
            armDown.set(true);
            pipeline.changeMode(KevinGodPipeline.Mode.BLUECONE);


            //drive.simpleMoveToPosition(140, MecDrive.MovementType.STRAIGHT, 0.3);

            drive.tankRotatePID(Math.PI / 2, 0.85, false);

            //drive.simpleMoveToPosition(-370 - normalizeDistance, MecDrive.MovementType.ROTATE, 0.4);
            //pipeline.normalizeToPole(0.3, 82, 10);

            score.setGrabberPosition(0.65);


            //drive.tankRotatePID(Math.PI/2, 1);            //pipeline.normalizeToPole(0.3, 42, 5);

            //drive.simpleMoveToPosition(120, MecDrive.MovementType.STRAFE, 0.4);
            //drive.goTOPIDPos(120, 1, MecDrive.MovementType.STRAIGHT);


            //Dont know if need to check multiple time


            for (int i = 0; i < 4; i++) {


                score.setLinkagePosition(Constants.linkageUp);


            /*if(i == 0){

                //drive.autoDiagonals(false, true, MecDrive.DiagonalPath.BLUERIGHT);
            }else{

                //FIX THIS CODE
                //drive.autoDiagonals(false, false, MecDrive.DiagonalPath.BLUERIGHT);

            }
            if(i % 2 == 0){
                drive.simpleMoveToPosition(-55, MecDrive.MovementType.STRAFE, 0.3);

            }else{
                drive.simpleMoveToPosition(-40, MecDrive.MovementType.STRAFE, 0.3);

            }
*/
                pipeline.normalizeStrafe(-0.35, 170, 5);

                if(pipeline.getNormalizationBroke()){
                    drive.tankRotatePID(Math.PI/2, 0.6, false);
                    break;
                }


                score.setLinkagePosition(0.75 + (i * 0.04));

                double startDistanceTime = time.seconds();
                while (distance.getDistance(DistanceUnit.CM) > 3.8) {
                    drive.setPowerAuto(0.45, MecDrive.MovementType.STRAIGHT);

                    telemetry.addData("distance", distance.getDistance(DistanceUnit.CM));
                    telemetry.update();

                    if (time.seconds() - startDistanceTime > 3) {
                        drive.simpleBrake();
                        drive.tankRotatePID(Math.PI / 2, 0.6, false);
                        failed = true;
                        break;
                    }

                }

                if (failed) {
                    break;
                }

                drive.simpleBrake();


                score.setGrabberPosition(Constants.grabbing);
                sleep(400);

                score.moveToPosition(200, 1);
                hold.set(true);
                sleep(200);
                finalMove.set(true);


                pipeline.changeMode(KevinGodPipeline.Mode.POLE);
                cameraServo.setPosition(Constants.pole);

                drive.goTOPIDPos(-1035, 1, MecDrive.MovementType.STRAIGHT);
                if (time.seconds() - startTime > 26) {
                    break;
                }

                while(finalMove.get()){

                }

                //score.moveToPosition(0, 1);

                armUp.set(true);

                //TODO: see if want to change to Math.PI/3.7
                drive.tankRotatePID(Math.PI / 5, 0.8, true);
                //drive.simpleMoveToPosition(290, MecDrive.MovementType.ROTATE, 0.4);
                normalizeDistance = pipeline.normalize(0.2, 172, 2);

                if(pipeline.getNormalizationBroke()){
                    drive.tankRotatePID(Math.PI/2, 0.6, false);
                    break;
                }

                while(armUp.get()){

                }


                //drive.simpleMoveToPosition(-30, MecDrive.MovementType.STRAIGHT, 0.3);




                cameraServo.setPosition(Constants.cone);
                sleep(200);

                armDown.set(true);
                pipeline.changeMode(KevinGodPipeline.Mode.BLUECONE);


                //drive.simpleMoveToPosition(70, MecDrive.MovementType.STRAIGHT, 0.4);

                drive.tankRotatePID(Math.PI / 2, 0.85, false);
                //drive.simpleMoveToPosition(-320 + normalizeDistance, MecDrive.MovementType.ROTATE, 0.4);
                //drive.tankRotatePID(Math.PI/2, 1);            //pipeline.normalizeToPole(0.3, 42, 5);


                //drive.simpleMoveToPosition(150, MecDrive.MovementType.STRAFE, 0.4);
                //drive.goTOPIDPos(150, 1, MecDrive.MovementType.STRAIGHT);

                score.setGrabberPosition(0.65);


            }

            score.setGrabberPosition(Constants.grabbing);

            camera.closeCameraDevice();


            finalMove.set(true);


            //drive.coast();

            if (failed) {
                if (parkPos == KevinGodPipeline.ParkPos.CENTER) {
                    drive.simpleMoveToPosition(-700, MecDrive.MovementType.STRAIGHT, 0.5);

                } else if (parkPos == KevinGodPipeline.ParkPos.RIGHT) {
                    drive.simpleMoveToPosition(-1500, MecDrive.MovementType.STRAIGHT, 0.5);

                }

            } else {
                if (parkPos == KevinGodPipeline.ParkPos.LEFT) {
                    drive.simpleMoveToPosition(-500, MecDrive.MovementType.STRAIGHT, 0.8);

                } else if (parkPos == KevinGodPipeline.ParkPos.RIGHT) {
                    drive.simpleMoveToPosition(700, MecDrive.MovementType.STRAIGHT, 1);

                }
            }
        }

        //Will have to check if this aligns straight already (need color sensor or not) ->
        // may need to turn into slight diagonal instead of straight to check color
        //drive.simpleMoveToPosition(675, MecDrive.MovementType.STRAIGHT, 0.3);


    }



}