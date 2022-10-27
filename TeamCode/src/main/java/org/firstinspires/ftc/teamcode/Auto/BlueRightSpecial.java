package org.firstinspires.ftc.teamcode.Auto;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.Common.MecanumDrive;
import org.firstinspires.ftc.teamcode.Common.ScoringSystem;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.concurrent.atomic.AtomicBoolean;
@Autonomous
public class BlueRightSpecial extends LinearOpMode {
    MecanumDrive drive;
    ScoringSystem score;
    AtomicBoolean cont;
    Thread liftThread;
    DetectionAlgorithmTest detect;
    OpenCvWebcam webcam;
    BNO055IMU imu;
    @Override
    public void runOpMode() throws InterruptedException {
        double initialRadians;
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // seehe calibration sample opmode
        parameters.loggingEnabled = true;
        parameters.loggingTag = "IMU";

        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        detect = new DetectionAlgorithmTest(telemetry);
        drive = new MecanumDrive(hardwareMap, telemetry);
        score = new ScoringSystem(hardwareMap);
        cont = new AtomicBoolean();
        cont.set(false);

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        webcam.setPipeline(detect);

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

        // Camera checks sleeve...stores parking location??

        //TODO: Possibly change turns from encoder to IMU angles
        //TODO: Work on auto for all the side (make different methods for each side?)

        liftThread = new Thread(){
            @Override
            public void run(){
                while(opModeIsActive()){
                    if((score.getEncoderPosition() > 1200 && cont.get())){
                        score.setPower(0.1);
                    }


                    telemetry.addData("liftPow", score.getPower());
                    telemetry.addData("liftPos", score.getEncoderPosition());
                    telemetry.update();
                }

            }
        };
        //TRY SETTING THE COMMANDS INSIDE THE THREAD AND SEE IF IT WORKS THAT WAY

        initialRadians = imu.getAngularOrientation().firstAngle;
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        waitForStart();
        webcam.stopStreaming();
        liftThread.start();

        blueRightDefense();
    }
    public void blueRightDefense(){
        //close claw
        score.setClawPosition(0.24);
        sleep(1000);
        //lift claw a little bit
        score.goToPosition(100, 0.7);
        sleep(200);
        // move forward 3 squares
        drive.goToPosition(0.3, 0.3,  0.3, 0.3, avgPosition(2307, 2203, 2230, 2313), "forward");

        //strafe left
        drive.goToPosition(-0.3, 0.3, 0.3, -0.3, avgPosition(-558, 590, 600, -559), "strafe left");
        sleep(100);
        // turn
        //drive.goToPosition(-0.3, 0.3, -0.3, 0.3, avgPosition(-311, 325, -345, 333), "turn to pole");

        // move arm max
        score.goToPosition(2340, 0.85);
        cont.set(true);
        drive.goToPosition(0.3, 0.3, 0.3, 0.3, avgPosition(119, 110, 95, 50), "move to pole");
        sleep(1500);
        score.setClawPosition(0);
        sleep(200);
        score.setClawPosition(0.24);
        sleep(200);
        drive.goToPosition(-0.3, -0.3, -0.3, -0.3, avgPosition(-200, -197, -211, -298), "move back from pole");
        // lowers arm after scoring first cone
        cont.set(false);
        score.goToPosition(1000, 0.5);
        sleep(200);
        drive.goToPosition(0.3,0.3,0.3,0.3,avgPosition(386,398,367,371),"Drive forward");
        sleep(15000);
        drive.goToPosition(-0.3,-0.3,-0.3,-0.3, avgPosition(-400,-420,-450,-450),"drive backward");
        score.goToPosition(0, 0.3);

        if (detect.getPosition() == DetectionAlgorithmTest.ParkingPosition.LEFT) {
            // move to left
            drive.goToPosition(-0.3, 0.3, 0.3,-0.3, avgPosition(-633,594,675,-588),"strafe left");
        } else if (detect.getPosition() == DetectionAlgorithmTest.ParkingPosition.CENTER) {
            // move to center
            drive.goToPosition(0.3, -0.3, -0.3, 0.3, avgPosition(611,-684,-700,773), "strafe right (center)");
        } else {
            // move to right
            drive.goToPosition(0.3, -0.3, -0.3, 0.3, avgPosition(1722,-1894,-1924,2033), "strafe right (more right)");

        }
        //1 (far left) (general code)
        //drive.goToPosition(0.3, -0.3, -0.3, 0.3, avgPosition(750,-750,-750,750), "strafe right");
        //cont.set(false);





        //2 middle
        //3 far right
        //drive.goToPosition(0.3, -0.3, -0.3, 0.3, avgPosition(1152, -1177, -1164, 1196), "strafe right");




        score.setClawPosition(0);
    }
    public int avgPosition(int fl, int fr, int bl, int br){
        return (int)(Math.abs(fl) + Math.abs(fr) + Math.abs(bl) + Math.abs(br))/4;
    }
    /*
    //TODO: check if camera angle works
    private class DetectionAlgorithm extends OpenCvPipeline {
        Mat original;
        Mat changed;

        @Override
        public Mat processFrame(Mat input) {
            input.copyTo(original);
            changed = new Mat();
            if(original.empty()) {
                return input;
            }
            // cyan magenta yellow
            Imgproc.cvtColor(original, changed, Imgproc.COLOR_RGB2YCrCb);
            // magenta 255, 0, 255
            Core.inRange(changed, new Scalar(240, 0 ,240), new Scalar(255, 0, 255), changed);
            return null;
        }


    }
    */

}
