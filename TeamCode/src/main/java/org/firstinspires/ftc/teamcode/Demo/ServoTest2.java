package org.firstinspires.ftc.teamcode.Demo;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.CRServo;

@TeleOp
public class ServoTest2 extends LinearOpMode {
    private Servo servo;
    private CRServo servo2;
    public void runOpMode() {
        servo = hardwareMap.get(Servo.class, "servo");
        servo2 = hardwareMap.get(CRServo.class, "servo2");
        // turnSpacer();
        //pullString(); // use pullStringWithCR() if continuous rotation servo is being used.
        pullStringWithCR();
        /*servo.setPosition(0);
        waitForStart();
        while (opModeIsActive()) {
            telemetry.update();
            while (gamepad1.b == true) {
                servo.setPosition(0.15);
                sleep(750);
            }
            servo.setPosition(0);
        }*/
    }

    // turns the servo 45 degrees to rotate the spacer out of the hook
    public void turnSpacer() {
        //servo = hardwareMap.get(Servo.class, "servo");
        servo.setPosition(0.5);
        waitForStart();
        while (opModeIsActive()) {
            telemetry.update();
            while (gamepad1.b == true) {
                servo.setPosition(0.20);
                sleep(850);
            }
            servo.setPosition(0.5);
        }
    }

    //pulls the pin out of the airplane shooter. Servo runs for 3 seconds.
    public void pullString() {
        //servo = hardwareMap.get(Servo.class, "servo");
        servo.setPosition(1);
        waitForStart();
        while (opModeIsActive()) {
            telemetry.update();
            while (gamepad1.a == true) {
                servo.setPosition(0);
                sleep(4000);
            }
            servo.setPosition(1);
        }
    }
    public void pullStringWithCR(){
        // if CR Servo is used
        servo2.setPower(0);
        waitForStart();
        while (opModeIsActive()) {
            telemetry.update();
            while (gamepad1.x == true) {
                servo2.setPower(-1);
                sleep(2000);
            }
            //sleep(4000);
            servo2.setPower(0);
        }
    }
}
