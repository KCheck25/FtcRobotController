package org.firstinspires.ftc.teamcode.Kids;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.Common.Constants;
import org.firstinspires.ftc.teamcode.Common.MecDriveV2;
import org.firstinspires.ftc.teamcode.Common.ScoringSystem;
import org.firstinspires.ftc.teamcode.Common.Vector2D;


@TeleOp
public class KidsTeleOp extends LinearOpMode {

    MecDriveV2 drive;
    ElapsedTime time;
    ScoringSystem score;
    public void runOpMode() {

        time = new ElapsedTime();
        drive = new MecDriveV2(hardwareMap, false, telemetry, time);
        score = new ScoringSystem(hardwareMap, telemetry, time);


        waitForStart();

        while(opModeIsActive()) {

            //N S E W Drive
            double leftStickX = gamepad1.left_stick_x;
            double leftStickY = -gamepad1.left_stick_y;

            if (Math.abs(leftStickX) > Math.abs(leftStickY)) {
                leftStickY = 0;

            } else if (Math.abs(leftStickY) > Math.abs(leftStickX)) {
                leftStickX = 0;

            } else {
                leftStickY = 0;
                leftStickX = 0;
            }


            if (gamepad1.right_bumper) {
                drive.setPower(new Vector2D(-leftStickX * Constants.SPRINT_LINEAR_MODIFIER, -leftStickY * Constants.SPRINT_LINEAR_MODIFIER), gamepad1.right_stick_x * Constants.SPRINT_ROTATIONAL_MODIFIER, false);
            } else {
                drive.setPower(new Vector2D(-leftStickX * Constants.NORMAL_LINEAR_MODIFIER, -leftStickY * Constants.NORMAL_LINEAR_MODIFIER), gamepad1.right_stick_x * Constants.NORMAL_ROTATIONAL_MODIFIER, false);

            }

            if(gamepad1.right_trigger > 0.1){
                score.setIntakeLiftPosition(Constants.INTAKE_LINKAGE_UP, 0.2);//000

                score.setIntakePower(0.33);
            }


            if(gamepad1.left_trigger > 0.1){
                score.weDoLoveTheseBugsHashTagANnoyingServos();
                score.setIntakeLiftPosition(1.3, -0.9);//1.3, -0.9
                score.setIntakePower(0);

            }


        }

    }

}
