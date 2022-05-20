package org.firstinspires.ftc.teamcode.NewTankDriveCode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.MecanumCode.Auto.Vision.BarcodePipelineRed;
import org.firstinspires.ftc.teamcode.MecanumCode.Common.CapstoneArm;
import org.firstinspires.ftc.teamcode.MecanumCode.Common.Carousel;
import org.firstinspires.ftc.teamcode.MecanumCode.Common.Constants;
import org.firstinspires.ftc.teamcode.MecanumCode.Common.MagneticArm;
import org.firstinspires.ftc.teamcode.MecanumCode.Common.MecanumDriveTrain;
import org.firstinspires.ftc.teamcode.MecanumCode.Common.OpModeWrapper;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.io.FileNotFoundException;


@Autonomous(name="Carousel Freight Blue Tank")
public class TankCarouselFreightBlue extends OpModeWrapper {

    MecanumDriveTrain drive;
    Carousel carousel;
    CapstoneArm capArm;
    MagneticArm magArm;
    // The IMU sensor object
    BNO055IMU imu;

    // State used for updating telemetry
    Orientation angles;
    Acceleration gravity;

    WebcamName wc;
    OpenCvCamera camera;

    //static final BarcodePipeline.AutoSide side = BarcodePipeline.AutoSide.RED;

    Thread armMovementThread;
    Thread telemetryThread;
    Thread carouselThread;
    private volatile boolean moveArm;


    // global obj
    BarcodePipelineRed.BarcodePosition capstonePos;
    static final BarcodePipelineRed bPipeline = new BarcodePipelineRed();

    @Override
    protected void onInitialize() throws FileNotFoundException {
        drive = new MecanumDriveTrain(hardwareMap);
        carousel = new Carousel(hardwareMap);
        capArm = new CapstoneArm(hardwareMap);
        magArm = new MagneticArm(hardwareMap);

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        wc = hardwareMap.get(WebcamName.class, "Webcam");

        // W/ or W/ out live preview
        camera = OpenCvCameraFactory.getInstance().createWebcam(wc, cameraMonitorViewId);
        // camera = OpenCvCameraFactory.getInstance().createWebcam(wc);

        camera.setPipeline(bPipeline);

        // Open an asynchronous connection to the device
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {

            // Start opening the camera and stream it
            @Override
            public void onOpened() {

                /*
                // create a rgb2gray mat pipeline
                class GrayPipeline extends OpenCvPipeline {
                    Mat gray = new Mat();
                    @Override
                    public Mat processFrame(Mat input) {
                        // mat src, mat dst, int code, convert rgb img to gray
                        Imgproc.cvtColor(input, gray, Imgproc.COLOR_RGB2GRAY);
                        return gray;
                    }
                } */

                camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            }

            // Method will be called if the camera cannot be opened
            @Override
            public void onError(int errorCode) {
                telemetry.addData("Camera Init Error", errorCode);
            }
        });


        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.RADIANS;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        moveArm = false;
        armMovementThread = new Thread(){
            @Override
            public void run(){
                while(!moveArm);
                capArm.goToPosition(0);
            }
        };

        telemetryThread = new Thread(){
            @Override
            public void run() {
                while (true) {
                    telemetry.addData("angle",drive.getAngle());
                    telemetry.addData("calculation: ", drive.getAngle() - (5*Math.PI/8));
                    telemetry.addData("pi/32: ", Math.PI/32);
                    telemetry.update();
                }
            }
        };

        carouselThread = new Thread(){
            @Override
            public void run() {
                carousel.spinCarousel(5000, currentOpMode(), Carousel.CarouselMode.AUTO);


            }
        };

        while(!opModeIsActive()){
            telemetry.addData("pos", bPipeline.getPos());
            telemetry.update();
        }

        capArm.setGrabberPosition(0.8);


    }

    @Override
    protected void onStart() {
        armMovementThread.start();
        capstonePos = bPipeline.getPos();


        //telemetryThread.start();
        //sleep(15000);
        /*drive.driveAuto(120, 240, MecanumDriveTrain.MovementType.STRAIGHT);
        drive.driveAuto(120, 240, MecanumDriveTrain.MovementType.STRAFE);
        drive.driveAuto(120, 240, MecanumDriveTrain.MovementType.ROTATE);

         */
        // Forward: 1 ft 540.3 tics (5403 for 10 ft)
        // Rotation: 360 degrees 3665 tics
        // Strafe: 590 tics/ft - = Left, + = Right

        drive.tankRotate(-Math.PI / 2, 0.3);


        drive.driveAuto(0.3, -600, MecanumDriveTrain.MovementType.STRAIGHT);
        //sleep(5000);

        //drive.tankRotate(34*Math.PI / 48, 0.3);
        //sleep(5000);

        carouselThread.start();


        while(drive.getAngle() > -Math.PI + (Math.PI/32)) {
            drive.tinyRotate(MecanumDriveTrain.rotatePower.Negative);
            telemetry.addData("angle", drive.getAngle());
            telemetry.addData("negative pi", -Math.PI);
            telemetry.update();
        }

        //sleep(5000);




        //drive.driveAuto(0.2, -50, MecanumDriveTrain.MovementType.STRAIGHT);
        //drive.setPower(-0.1, -0.1, -0.1, -0.1);

        //sleep(1000);
        drive.driveAuto(0.4, 500, MecanumDriveTrain.MovementType.STRAIGHT);

        //sleep(5000);

        drive.tankRotate(2*Math.PI/24, 0.3);

        drive.driveAuto(0.3, -1400, MecanumDriveTrain.MovementType.STRAIGHT);
        drive.driveAuto(0.3, 200, MecanumDriveTrain.MovementType.STRAIGHT);

        drive.tankRotate(Math.PI/2, 0.3);



        if (capstonePos == BarcodePipelineRed.BarcodePosition.RIGHT) {
            //drive.driveAuto(0.3, -520, MecanumDriveTrain.MovementType.STRAIGHT);
            //capArm.goToPosition(300);
            drive.driveAuto(0.3, -950, MecanumDriveTrain.MovementType.STRAIGHT);
            capArm.goToPosition(Constants.TOP_GOAL_POS);
            drive.driveAuto(0.3, -40, MecanumDriveTrain.MovementType.STRAIGHT);

            capArm.toggleGrab();

            sleep(500);
            capArm.toggleGrab();
            sleep(1000);
            moveArm = true;


            drive.driveAuto(0.3, 990, MecanumDriveTrain.MovementType.STRAIGHT);


        } else if (capstonePos == BarcodePipelineRed.BarcodePosition.CENTER) {
            drive.driveAuto(0.3, -1000, MecanumDriveTrain.MovementType.STRAIGHT);
            capArm.goToPosition(Constants.MID_GOAL_POS);
            drive.driveAuto(0.3, -40, MecanumDriveTrain.MovementType.STRAIGHT);

            capArm.toggleGrab();
            sleep(1000);
            capArm.toggleGrab();
            sleep(1000);
            moveArm = true;



            drive.driveAuto(0.3, 1040, MecanumDriveTrain.MovementType.STRAIGHT);


        } else {
            drive.driveAuto(0.3, -1165, MecanumDriveTrain.MovementType.STRAIGHT);
            capArm.goToPosition(Constants.BOTTOM_GOAL_POS);
            capArm.toggleGrab();
            sleep(1000);
            capArm.toggleGrab();
            sleep(1000);
            moveArm = true;


            drive.driveAuto(0.3, 1165, MecanumDriveTrain.MovementType.STRAIGHT);
        }



        drive.tankRotate(Math.PI/4, 0.3);


        drive.driveAuto(0.3, 500, MecanumDriveTrain.MovementType.STRAIGHT);

        //drive.tankRotate(-Math.PI/2, 0.25  );



    }


    @Override
    protected void onStop() {

    }

}