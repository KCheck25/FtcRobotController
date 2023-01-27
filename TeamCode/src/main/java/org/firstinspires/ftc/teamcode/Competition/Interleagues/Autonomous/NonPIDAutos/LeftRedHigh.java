package org.firstinspires.ftc.teamcode.Competition.Interleagues.Autonomous.NonPIDAutos;

////import com.acmerobotics.dashboard.FtcDashboard;
////import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.PIDCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Competition.Interleagues.Autonomous.Vision.OlderPipelines.KevinGodPipeline;
import org.firstinspires.ftc.teamcode.Competition.Interleagues.Common.Constants;
import org.firstinspires.ftc.teamcode.Competition.Interleagues.Subsystems.Used.MecDrive;
import org.firstinspires.ftc.teamcode.Competition.Interleagues.Subsystems.Used.ScoringSystem2;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.concurrent.atomic.AtomicBoolean;

@Disabled
@Autonomous
public class LeftRedHigh extends LinearOpMode {
    MecDrive drive;
    ScoringSystem2 score;
    Thread armThread, feedForward, idController;
    PIDCoefficients pid = new PIDCoefficients(0, 0, 0);
    AtomicBoolean hold, armUp, armDown;

    BNO055IMU imu;
    ColorRangeSensor distance, color;
    Servo cameraServo;

    OpenCvWebcam camera;
    KevinGodPipeline pipeline;
    KevinGodPipeline.ParkPos parkPos;

    //TelemetryPacket packet = new TelemetryPacket();

    int normalizeDistance;




    @Override
    public void runOpMode() throws InterruptedException {
        //FtcDashboard dashboard = FtcDashboard.getInstance();
        //dashboard.updateConfig();

        drive = new MecDrive(hardwareMap, false, telemetry);
        score = new ScoringSystem2(hardwareMap);
        hold = new AtomicBoolean(false);
        armUp = new AtomicBoolean(false);
        armDown = new AtomicBoolean(false);

        score.setLinkagePosition(Constants.linkageDown);
        score.setGrabberPosition(Constants.grabbing);

        distance = hardwareMap.get(ColorRangeSensor.class, "distance");
        //color = hardwareMap.get(ColorRangeSensor.class, "color");
        cameraServo = hardwareMap.get(Servo.class, "camera");


        //color.setGain(300);
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
                        score.moveToPosition(830, 1);
                        hold.set(true);
                        score.setLinkagePositionLogistic(Constants.linkageScore, 500, 50);
                        armUp.set(false);
                    }else if(armDown.get()){
                        hold.set(false);
                        score.setLinkagePosition(Constants.linkageUp);
                        score.moveToPosition(0, 0.5);
                        score.setLinkagePositionLogistic(Constants.linkageDown, 250, 30);
                        armDown.set(false);
                    }


                    telemetry.addData("red", color.red());
                    telemetry.addData("blue", color.blue());
                    telemetry.update();

                    //packet.put("red", color.red());
                    //packet.put("blue", color.blue());
                    //dashboard.sendTelemetryPacket(packet);



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








        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.RADIANS;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";

        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);



        WebcamName webcamName = hardwareMap.get(WebcamName.class, "Webcam 1");
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId);
        pipeline = new KevinGodPipeline(telemetry, drive, KevinGodPipeline.AutoSide.RED_LEFT);

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

        cameraServo.setPosition(0.5);





        waitForStart();
        color.resetDeviceConfigurationForOpMode();

        parkPos = pipeline.getPosition();

        //pipeline.setMode(false);

        armThread.start();
        feedForward.start();

        drive.addToLoggingString("originalColorRed: " + color.getNormalizedColors().red);
        drive.addToLoggingString("originalColorBlue: " + color.getNormalizedColors().blue);
        drive.addToLoggingString("");


        cameraServo.setPosition(0.73);



        drive.simpleMoveToPosition(-1600, MecDrive.MovementType.STRAIGHT, 0.4);
        //tankRotate(Math.PI / 4.25, 0.3);

        //TODO: figure out encoder val for this rotate
        drive.simpleMoveToPosition(230, MecDrive.MovementType.ROTATE, 0.4);
        normalizeDistance = pipeline.normalize(0.3, 172, 5);
        //pipeline.Ynormalize(0.2, 95, 5);


        armUp.set(true);

        drive.simpleMoveToPosition(30, MecDrive.MovementType.STRAIGHT, 0.3);


        while(armUp.get()){

        }
        sleep(500);
        score.setGrabberPosition(0.3);
        sleep(500);
        score.setGrabberPosition(Constants.grabbing);



        armDown.set(true);



        //drive.simpleMoveToPosition(140, MecDrive.MovementType.STRAIGHT, 0.3);

        //tankRotate(Math.PI / 2, 0.3);

        //TODO: figure out this rotate
        drive.simpleMoveToPosition(370 - normalizeDistance, MecDrive.MovementType.ROTATE, 0.4);
        //pipeline.normalizeToPole(0.3, 82, 10);

        score.setGrabberPosition(0.7);


        tankRotate(Math.PI/2, 0.2);

        drive.simpleMoveToPosition(-120, MecDrive.MovementType.STRAFE, 0.4);
        drive.simpleMoveToPosition(120, MecDrive.MovementType.STRAIGHT, 0.4);







        //Dont know if need to check multiple time
        while(color.red() < 85 && color.blue() < 200){

            drive.setPower(0.45, 0, 0, 0.45);

        }

        drive.simpleBrake();






        drive.simpleMoveToPosition(-40, MecDrive.MovementType.STRAFE, 0.3);



        //score.setGrabberPosition(0.7);

        for(int i = 0; i < 2; i++) {


            //TODO: Logic doesnt work
            if(i != 0){


                while(color.red() < 85  && color.blue() < 200){

                    drive.setPower(0.45, 0, 0, 0.45);
                    drive.addToLoggingString("ColorRed: " + color.getNormalizedColors().red);
                    drive.addToLoggingString("ColorBlue: " + color.getNormalizedColors().blue);
                    drive.addToLoggingString("");



                }


                drive.simpleBrake();



                drive.simpleMoveToPosition(-40, MecDrive.MovementType.STRAFE, 0.3);


            }



            score.setLinkagePosition(0.77 + (i * 0.03));


            while (distance.getDistance(DistanceUnit.CM) > 3) {
                drive.setPowerAuto(0.3, MecDrive.MovementType.STRAIGHT);

                //telemetry.addData("distance", distance.getDistance(DistanceUnit.CM));
                //telemetry.update();

            }

            drive.simpleBrake();


            score.setGrabberPosition(Constants.grabbing);
            sleep(300);


            score.moveToPosition(200, 0.8);
            hold.set(true);

            drive.simpleMoveToPosition(-650, MecDrive.MovementType.STRAIGHT, 0.5);
            score.setLinkagePosition(Constants.linkageUp);



            //tankRotate(Math.PI / 4.35, 0.3);
            drive.simpleMoveToPosition(-290, MecDrive.MovementType.ROTATE, 0.4);
            normalizeDistance = pipeline.normalize(0.2, 170, 3);

            //pipeline.normalizeToPole(0.3, 165, 10);
            //pipeline.Ynormalize(0.2, 92, 5);



            armUp.set(true);

            //drive.simpleMoveToPosition(-50, MecDrive.MovementType.STRAIGHT, 0.3);


            while(armUp.get()){

            }
            sleep(500);
            score.setGrabberPosition(0.3);
            sleep(500);

            armDown.set(true);



            //drive.simpleMoveToPosition(70, MecDrive.MovementType.STRAIGHT, 0.4);

            //tankRotate(Math.PI / 2, 0.3);
            drive.simpleMoveToPosition(320 + normalizeDistance, MecDrive.MovementType.ROTATE, 0.4);
            tankRotate(Math.PI/2, 0.2);

            if(i != 1) {
                drive.simpleMoveToPosition(-150, MecDrive.MovementType.STRAFE, 0.4);
                drive.simpleMoveToPosition(150, MecDrive.MovementType.STRAIGHT, 0.4);


            }
            score.setGrabberPosition(0.7);


            //pipeline.normalizeToPole(0.3, 42, 5);

        }

        score.setGrabberPosition(Constants.grabbing);

        camera.closeCameraDevice();

        drive.addToLoggingString("endColorRed: " + color.getNormalizedColors().red);
        drive.addToLoggingString("endColorBlue: " + color.getNormalizedColors().blue);
        drive.addToLoggingString("");



        if(parkPos == KevinGodPipeline.ParkPos.LEFT){
            score.setLinkagePosition(0.83);
            drive.simpleMoveToPosition(650, MecDrive.MovementType.STRAIGHT, 0.5);


        }else if(parkPos == KevinGodPipeline.ParkPos.RIGHT){
            drive.simpleMoveToPosition(-650, MecDrive.MovementType.STRAIGHT, 0.5);

        }

        drive.writeLoggerToFile();

        //Will have to check if this aligns straight already (need color sensor or not) ->
        // may need to turn into slight diagonal instead of straight to check color
        //drive.simpleMoveToPosition(675, MecDrive.MovementType.STRAIGHT, 0.3);


    }


    //TODO: Test if we want to use IMU again
    public void tankRotate(double radians, double power){

        if(radians > imu.getAngularOrientation().firstAngle){
            power *= -1;
        }

        if(power > 0) {
            while (Math.abs(imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle) < Math.abs(radians)) {
                telemetry.addData("target", radians);
                telemetry.addData("current", imu.getAngularOrientation().firstAngle);
                telemetry.update();
                drive.setPowerAuto(power, MecDrive.MovementType.ROTATE);
            }
        }else {
            while (Math.abs(imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle) > Math.abs(radians)) {
                telemetry.addData("target", radians);
                telemetry.addData("current", imu.getAngularOrientation().firstAngle);
                telemetry.update();
                drive.setPowerAuto(power, MecDrive.MovementType.ROTATE);
            }
        }

        drive.simpleBrake();




    }



    public void tankRotatePID(double radians, double power){

        /*if(radians > imu.getAngularOrientation().firstAngle){
            power *= -1;
        }*/

        ElapsedTime time = new ElapsedTime();
        double startTime = time.seconds();

        radians = wrapAngle(radians);
        double radError = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle - radians;
        double previousError = radError;
        double integralSum = 0;





        while(Math.abs(radError) > 0.0001){

            telemetry.addData("target", radians);

            double currentAngle = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS).firstAngle;

            double currentTime = time.seconds();

            radError = currentAngle - radians;

            telemetry.addData("Error", radError);

            integralSum += (radError + previousError)/(currentTime - startTime);
            double derivative = (radError - previousError)/(currentTime - startTime);

            drive.setPowerAuto(((pid.p * radError) + (pid.i * integralSum) + (pid.d * derivative)), MecDrive.MovementType.ROTATE);


        }

        drive.simpleBrake();




    }

    public double wrapAngle(double angle){
        while(angle > Math.PI){
            angle -= (2 * Math.PI);
        }

        while(angle < -Math.PI){
            angle += (2 * Math.PI);
        }

        return angle;
    }


}