//Auto for if we are just scoring preset

package org.firstinspires.ftc.teamcode.Autonomous.TestMeAutos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Common.Constants;
import org.firstinspires.ftc.teamcode.Common.MecDriveV2;
import org.firstinspires.ftc.teamcode.Common.ScoringSystem;
//@Disabled
@Autonomous
public class BlueBackAutoTest extends LinearOpMode {

    MecDriveV2 drive;
    ScoringSystem score;
    ElapsedTime time;

    public void runOpMode() {

        time = new ElapsedTime();
        drive = new MecDriveV2(hardwareMap, false, telemetry, time);
        score = new ScoringSystem(hardwareMap, telemetry, time);
        score.setGrabberPosition(Constants.OPEN);
        score.setGrabberPosition(Constants.GRABBING);
        int rando = 2;
        //vision stuff to assign 1, 2, or 3 to rando

        waitForStart();
        if (rando == 1) {
            random1();
        }
        else if (rando == 2) {
            random2();
        }
        else if (rando == 3) {
            random3();
        }

        drive.simpleBrake();
        sleep(500);

    }

    public void random1() {
        drive.simpleMoveToPosition(-300, MecDriveV2.MovementType.STRAFE, Constants.AUTO_LINEAR_SPEED);
        score.setIntakePower(-0.2);
        sleep(500);
        score.setIntakePower(0);
        drive.simpleMoveToPosition(-300, MecDriveV2.MovementType.STRAFE, Constants.AUTO_LINEAR_SPEED);
        drive.simpleMoveToPosition(600, MecDriveV2.MovementType.STRAIGHT, Constants.AUTO_LINEAR_SPEED);
        drive.tankRotate(Math.PI/(-2), Constants.AUTO_ROTATIONAL_SPEED);
        creep();
        autoScore();
        drive.simpleMoveToPosition(-600, MecDriveV2.MovementType.STRAFE, Constants.AUTO_LINEAR_SPEED);

    }

    public void random2 () {
        drive.simpleMoveToPosition(635, Constants.AUTO_LINEAR_SPEED);
        score.setIntakePower(-0.2);
        sleep(500);
        score.setIntakePower(0);
        drive.simpleMoveToPosition(-50, Constants.AUTO_SAFE_MO);

        drive.tankRotate((Math.PI/(-2)), Constants.AUTO_ROTATIONAL_SPEED);

        drive.simpleMoveToPosition(-650, Constants.AUTO_LINEAR_SPEED);
        drive.simpleMoveToPosition(-145, Constants.AUTO_SAFE_MO);

        creep();
        autoScore();

        drive.simpleMoveToPosition(100, MecDriveV2.MovementType.STRAIGHT, Constants.AUTO_LINEAR_SPEED);

        drive.simpleMoveToPosition(-650, MecDriveV2.MovementType.STRAFE, Constants.AUTO_LINEAR_SPEED);
    }

    public void random3() {
        drive.simpleMoveToPosition(600, Constants.AUTO_LINEAR_SPEED);
        drive.tankRotate(Math.PI/(-2), Constants.AUTO_ROTATIONAL_SPEED);

        score.setIntakePower(-0.2);
        sleep(500);
        score.setIntakePower(0);

        drive.simpleMoveToPosition(-650, Constants.AUTO_LINEAR_SPEED);

        creep();
        autoScore();

        drive.simpleMoveToPosition(100, Constants.AUTO_SAFE_MO);
        drive.simpleMoveToPosition(-600, MecDriveV2.MovementType.STRAFE, Constants.AUTO_LINEAR_SPEED);

    }

    public void autoScore() {
        score.setLinkagePositionLogistic(0.5, 1000, 100);
        //  score.goToLiftTarget(Constants.LIFT_LOW, 0.8);
        score.setLinkagePositionLogistic(Constants.LINKAGE_UP, 1500, 100);
        sleep(300);
        score.setGrabberPosition(Constants.OPEN);
        sleep(300);
        score.setLinkagePositionLogistic(0.5, 1000, 100);
        //   score.goToLiftTarget(0, 0.3);
        score.setLinkagePositionLogistic(Constants.LINKAGE_DOWN, 1000, 100);

    }

    public void creep() {
        drive.setPowerAuto(Constants.AUTO_SLOWED_SPEED, MecDriveV2.MovementType.STRAIGHT);
        sleep(300);
        drive.setPowerAuto(0, MecDriveV2.MovementType.STRAIGHT);
    }


}
