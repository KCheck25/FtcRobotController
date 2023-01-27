package org.firstinspires.ftc.teamcode.Testing.MotionProfile;

////import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Competition.Interleagues.Subsystems.Used.ScoringSystem2;

@Disabled

@Autonomous
public class LogisticServoTest extends LinearOpMode {

    Servo servo;
    ScoringSystem2 score;
    //Constants constants;

    @Override
    public void runOpMode() throws InterruptedException {

        //constants = newConstants();

        score = new ScoringSystem2(hardwareMap);

        score.setLinkagePosition(0.95);
        telemetry.addData("Servo pos", score.getLeftLinkage());
        telemetry.update();

        waitForStart();

        //score.setLinkagePositionLogistic(0.85, 250);
        score.setTimeServoPosLogistic(0.05, 500);
        telemetry.addData("Servo pos", score.getLeftLinkage());
        telemetry.update();
        sleep(1000);
        //score.setLinkagePositionLogistic(0.2, 500);
        score.setTimeServoPosLogistic(0.05, 500);
        telemetry.addData("Servo pos", score.getLeftLinkage());
        telemetry.update();


        while(opModeIsActive()){

        }

    }
    /*public void setServoPosLogistic(double target, int sleepTime) {
        int resolution = 100;
        double step = 4.0 / resolution;
        double start = servo.getPosition();
        double startX = -2.0;
        for(int i = 0; i < resolution; i++) {
            servo.setPosition(logistic(startX, start, target));
            startX += step;
            sleep(sleepTime);
        }
        servo.setPosition(target);
    }*//*

    public double logistic(double x, double lower, double upper) {
        double k = 2;
        double x0 = 0;
        upper -= lower;
        return (upper / (1 + Math.pow(Math.E, -k * ( x - x0 )))) + lower;
    }*/



}