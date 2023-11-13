package org.firstinspires.ftc.teamcode.Testing.Demo;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@Disabled
@TeleOp(name = "BasicOpMode")
public class BasicOpMode extends LinearOpMode {

    DcMotor motor;
    Servo servo;

    //object keeping track of enum
    Thing keepingTrack;

    //Initialize Thread
    public Thread testingThread;

    //enum syntax
    public enum Thing{
        A,
        B,
        C
    }




    public void runOpMode() throws InterruptedException {

        //setting object to a specific enum
        keepingTrack = Thing.A;

        //Declare Thread
        testingThread = new Thread(){

            //Overriden method that is called when thread is started
            @Override
            public void run(){

                //While match is going
                while(opModeIsActive()){
                    //Running in parallel
                }
            }
        };


        // deviceName is what its called in config

        // initializing motors
        motor = hardwareMap.get(DcMotor.class, "motor1");
        ColorSensor sensor = hardwareMap.get(ColorSensor.class, "sensor");



        // servos
        servo = hardwareMap.get(Servo.class, "servo");

        // everything before this line happens on innit. everything after is on start.
        waitForStart();


        //Start the thread and run the run() method
        //Make sure to have this after the waitForStart();
        testingThread.start();

        goToPositon(100);

        while (opModeIsActive()) {
            // powering a motor and accessing gamepads
            // motors accept a power between -1 and 1, where 1 is max power, -1, is max power in the opposite direction, and 0 is stopped
            motor.setPower(gamepad1.left_stick_y);

            motor.getCurrentPosition();

            // telemetry - sends data to driver hub
            telemetry.addData("Motor pos", motor.getCurrentPosition());
            telemetry.update();

            // servos are set to a position between 0 and 1
            // they automatically go to this position - no need to set power
            //servo.setPosition(gamepad1.left_trigger);


        }


    }

    public void goToPositon(int pos) {
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        while (motor.getCurrentPosition() < pos) {
            motor.setPower(0.5);
        }
        motor.setPower(0);
    }

}