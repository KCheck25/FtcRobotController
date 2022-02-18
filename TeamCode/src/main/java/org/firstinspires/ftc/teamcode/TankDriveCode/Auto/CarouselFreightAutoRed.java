 package org.firstinspires.ftc.teamcode.TankDriveCode.Auto;

 import com.qualcomm.hardware.bosch.BNO055IMU;
 import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
 import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
 import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
 import com.qualcomm.robotcore.hardware.DcMotor;
 import com.qualcomm.robotcore.hardware.Servo;

 import org.firstinspires.ftc.robotcore.external.Func;
 import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
 import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
 import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
 import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
 import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
 import org.firstinspires.ftc.teamcode.TankDriveCode.Auto.DriveTrain;

 import java.util.Locale;

 @Autonomous(name = "Carousel + Freight Auto (red)", group = "Linear Opmode")
 //@Disabled

 public class CarouselFreightAutoRed extends LinearOpMode {
     // The IMU sensor object
     BNO055IMU imu;
     DcMotor extender;
     //DcMotor carousel;
     Servo grabber;


     double servoPosition = 0.5;


     boolean isExtended = false;
     boolean isGrabbing = true;
     boolean servoMoving = false;
     boolean previousYState;
     Thread armThread;



     // State used for updating telemetry
     Orientation angles;
     Acceleration gravity;

     //----------------------------------------------------------------------------------------------
     // Main logic
     //----------------------------------------------------------------------------------------------

     @Override
     public void runOpMode() {
         org.firstinspires.ftc.teamcode.TankDriveCode.Auto.DriveTrain drive = new org.firstinspires.ftc.teamcode.TankDriveCode.Auto.DriveTrain(hardwareMap);
         extender = hardwareMap.get(DcMotor.class, "ExtensionArm");
         extender.setDirection(DcMotor.Direction.FORWARD);
         extender.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
         extender.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
         grabber = hardwareMap.get(Servo.class, "Grabber");

         //carousel = hardwareMap.get(DcMotor.class, "Carousel");
         //carousel.setDirection(DcMotor.Direction.REVERSE);

         armThread = new Thread(){
             @Override
             public void run(){
                 extendArm(0);
             }
         };






         // Set up the parameters with which we will use our IMU. Note that integration
         // algorithm here just reports accelerations to the logcat log; it doesn't actually
         // provide positional information.
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

         // Set up our telemetry dashboard
         //composeTelemetry();
         grabber.setPosition(0);

         // Wait until we're told to go
         waitForStart();
         /*
         imu.startAccelerationIntegration(new Position(), new Velocity(), 1000);


         telemetry.addData("status", new Func<String>() {
             @Override public String value() {
                 return imu.getSystemStatus().toShortString();
             }
         });
         telemetry.addData("calib", new Func<String>() {
             @Override public String value() {
                         return imu.getCalibrationStatus().toString();
             }
         });

         telemetry.addData("heading", new Func<String>() {
             @Override public String value() {
                 return formatAngle(angles.angleUnit, angles.firstAngle);
             }
         });
         telemetry.addData("roll", new Func<String>() {
             @Override public String value() {
                 return formatAngle(angles.angleUnit, angles.secondAngle);
             }
         });
         telemetry.addData("pitch", new Func<String>() {
             @Override public String value() {
                 return formatAngle(angles.angleUnit, angles.thirdAngle);
             }
         });
         telemetry.addData("grvty", new Func<String>() {
             @Override public String value() {
                 return gravity.toString();
             }
         });
         telemetry.addData("mag", new Func<String>() {
             @Override public String value() {
                 return String.format(Locale.getDefault(), "%.3f",
                         Math.sqrt(gravity.xAccel*gravity.xAccel
                                 + gravity.yAccel*gravity.yAccel
                                 + gravity.zAccel*gravity.zAccel));
             }
         });
         telemetry.addData("pause", "this is just a pause value");

          */

         /*Top Goal
         drive.goToPosition(-450, false, 0.3);
         grabber.setPosition(0.3);





         sleep(1000);

         drive.goToPosition(550, false, 0.3);

         sleep(1000);

          */

         //rotateToPosition(drive, -50);

         drive.withoutEncoders();
         while(imu.getAngularOrientation().firstAngle > -(15 * Math.PI) / 72){
             telemetry.addData("Rads: ", imu.getAngularOrientation().firstAngle);
             drive.lf.setPower(-0.3);
             telemetry.update();
         }

         drive.brake();
         //sleep(2000);

         //spinCarousel(4000);

         extendArm(6800);

         //Middle Goal
         //drive.goToPosition(-490, false, 0.3);

         //Top Goal
         drive.goToPosition(-520, false, 0.3);
         drive.brake();
         grabber.setPosition(0.3);
         sleep(1000);
         armThread.start();
         drive.goToPosition(380, false, 0.2);
         sleep(1000);

         drive.withoutEncoders();

         while(imu.getAngularOrientation().firstAngle > - (13 * Math.PI) / 32){
             telemetry.addData("Rads: ", imu.getAngularOrientation().firstAngle);
             telemetry.addData("TargetPosition: ", (15 * Math.PI) / 32);
             drive.rf.setPower(0.3);
             drive.lf.setPower(-0.3);
             telemetry.update();
         }

         drive.goToPosition(590, false, 0.2);



         /*
         rotateToPosition(drive, -200);
         drive.goToPosition(-500, false, 0.3);
         extendArm(7100);
         drive.goToPosition(-300, false, 0.3);
         grabber.setPosition(0.5);
         sleep(4000);

          */








         //Middle Goal
         /*
         extendArm(3450);
         drive.goToPosition(-616, false);
         grabber.setPosition(0.3);

          */





         /*
         //Bottom Goal
         extendArm(1370);
         drive.goToPosition(-740, false);
         extendArm(1300);
         grabber.setPosition(0.3);
         drive.goToPosition(-40, false);
         sleep(1000);
         drive.goToPosition(200, false);

          */




         /*drive.lf.setPower(0.4);
         drive.rf.setPower(-0.4);
         sleep(1000);
         drive.lf.setPower(0);
         drive.rf.setPower(0);

          */








         // Start the logging of measured acceleration


         // Loop and update the dashboard
         /*
         while (opModeIsActive()) {
             telemetry.addData("status", new Func<String>() {
                 @Override public String value(){
                     return " " + rotateDegrees();
                 }
             });
             telemetry.update();
         }

          */

     }

     /*
     private Orientation rotateDegrees(){
         return imu.getAngularOrientation();
     }

      */

     //----------------------------------------------------------------------------------------------
     // Telemetry Configuration
     //----------------------------------------------------------------------------------------------

     void composeTelemetry() {

         // At the beginning of each telemetry update, grab a bunch of data
         // from the IMU that we will then display in separate lines.
         telemetry.addAction(new Runnable() { @Override public void run()
         {
             // Acquiring the angles is relatively expensive; we don't want
             // to do that in each of the three items that need that info, as that's
             // three times the necessary expense.
             angles   = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
             gravity  = imu.getGravity();
         }
         });

         telemetry.addLine()
                 .addData("status", new Func<String>() {
                     @Override public String value() {
                         return imu.getSystemStatus().toShortString();
                     }
                 })
                 .addData("calib", new Func<String>() {
                     @Override public String value() {
                         return imu.getCalibrationStatus().toString();
                     }
                 });

         telemetry.addLine()
                 .addData("heading", new Func<String>() {
                     @Override public String value() {
                         return formatAngle(angles.angleUnit, angles.firstAngle);
                     }
                 })
                 .addData("roll", new Func<String>() {
                     @Override public String value() {
                         return formatAngle(angles.angleUnit, angles.secondAngle);
                     }
                 })
                 .addData("pitch", new Func<String>() {
                     @Override public String value() {
                         return formatAngle(angles.angleUnit, angles.thirdAngle);
                     }
                 });

         telemetry.addLine()
                 .addData("grvty", new Func<String>() {
                     @Override public String value() {
                         return gravity.toString();
                     }
                 })
                 .addData("mag", new Func<String>() {
                     @Override public String value() {
                         return String.format(Locale.getDefault(), "%.3f",
                                 Math.sqrt(gravity.xAccel*gravity.xAccel
                                         + gravity.yAccel*gravity.yAccel
                                         + gravity.zAccel*gravity.zAccel));
                     }
                 });
     }

     //----------------------------------------------------------------------------------------------
     // Formatting
     //----------------------------------------------------------------------------------------------

     String formatAngle(AngleUnit angleUnit, double angle) {
         return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
     }

     String formatDegrees(double degrees){
         return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
     }


     public void extendArm(int armPosition) {

         extender.setTargetPosition(armPosition);

         extender.setMode(DcMotor.RunMode.RUN_TO_POSITION);

         extender.setPower(0.5);

         while (extender.isBusy()) {

         }

         extender.setPower(0);

         extender.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
     }


     public synchronized void grab(double position){
         double targetPosition;
         servoMoving = true;

         previousYState = gamepad1.y;

         if(isGrabbing) {
             targetPosition = 0;
         }else{
             targetPosition = position;
         }

         servoPosition = targetPosition;
         grabber.setPosition(servoPosition);

         while(previousYState == gamepad1.y){

         }
         isGrabbing = !isGrabbing;

     }

     public void rotateToPosition(DriveTrain drive, int tics) {
         drive.lf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
         drive.lf.setTargetPosition(tics);
         drive.lf.setMode(DcMotor.RunMode.RUN_TO_POSITION);

         drive.rf.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
         drive.rf.setTargetPosition(-tics);
         drive.rf.setMode(DcMotor.RunMode.RUN_TO_POSITION);

         drive.lf.setPower(0.2);
         drive.rf.setPower(0.2);

         while(drive.lf.isBusy() && drive.rf.isBusy()){

         }

         drive.brake();
     }

     /*
     public void spinCarousel(int tics) {


         carousel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
         carousel.setTargetPosition(tics);


         carousel.setMode(DcMotor.RunMode.RUN_TO_POSITION);


         double power;

         carousel.setPower(0.2);

         while (carousel.isBusy()) {

         }

     }

      */


 }