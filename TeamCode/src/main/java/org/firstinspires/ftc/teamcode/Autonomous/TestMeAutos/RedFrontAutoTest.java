//red path two avoids team game element

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
public class RedFrontAutoTest extends LinearOpMode {

    MecDriveV2 drive;
    ScoringSystem score;
    ElapsedTime time;

    public void runOpMode() {

        time = new ElapsedTime();
        drive = new MecDriveV2(hardwareMap, false, telemetry, time);
        score = new ScoringSystem(hardwareMap, telemetry, time);
      //  score.setGrabberPosition(Constants.OPEN);
        score.setGrabberPosition(Constants.GRABBING);
        score.setBumperPixelRelease(Constants.AUTO_SCORING_CLAMP_OPEN);
        score.setIntakeLiftPos(Constants.INTAKE_LINKAGE_DOWN-0.1);

        int rando = 2;
        //vision stuff to assign 1, 2, or 3 to rando

        waitForStart();
        score.setIntakeLiftPos(Constants.INTAKE_LINKAGE_UP);
        score.setBumperPixelRelease(Constants.AUTO_SCORING_CLAMP_CLOSED);
        sleep(1000);

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
        drive.simpleMoveToPosition(-600, Constants.AUTO_LINEAR_SPEED);
        drive.tankRotate(Math.PI/2, Constants.AUTO_ROTATIONAL_SPEED);

        /*
        score.setIntakePower(-0.2);
        sleep(500);
        score.setIntakePower(0);
         */
        spikeScore();
        drive.simpleMoveToPosition(100, Constants.AUTO_LINEAR_SPEED);
        drive.simpleMoveToPosition(-600, MecDriveV2.MovementType.STRAFE, Constants.AUTO_LINEAR_SPEED);
        drive.tankRotate(Math.PI/(-2), Constants.AUTO_ROTATIONAL_SPEED);

        drive.simpleMoveToPosition(-1000, Constants.AUTO_LINEAR_SPEED);
    }

    public void random2() {
        drive.simpleMoveToPosition(-1200, Constants.AUTO_LINEAR_SPEED);
        drive.simpleMoveToPosition(100, Constants.AUTO_LINEAR_SPEED);
        drive.tankRotate(-3.12, Constants.AUTO_ROTATIONAL_SPEED);
        drive.simpleMoveToPosition(-100, MecDriveV2.MovementType.STRAFE, Constants.AUTO_LINEAR_SPEED);
        /*
        score.setIntakePower(-0.2);
        sleep(500);
        score.setIntakePower(0);
         */
        spikeScore();
        drive.tankRotate(-(3.12/2), Constants.AUTO_ROTATIONAL_SPEED);
        drive.simpleMoveToPosition(-1900, Constants.AUTO_LINEAR_SPEED);
        drive.simpleMoveToPosition(-800, MecDriveV2.MovementType.STRAFE, Constants.AUTO_LINEAR_SPEED);


    }

    public void random3() {
        drive.simpleMoveToPosition(-600, Constants.AUTO_LINEAR_SPEED);
        drive.tankRotate(Math.PI/(-2), Constants.AUTO_ROTATIONAL_SPEED);

        /*
        score.setIntakePower(-0.2);
        sleep(500);
        score.setIntakePower(0);
         */
        spikeScore();
        drive.simpleMoveToPosition(100, Constants.AUTO_LINEAR_SPEED);
        drive.simpleMoveToPosition(600, MecDriveV2.MovementType.STRAFE, Constants.AUTO_LINEAR_SPEED);

        drive.simpleMoveToPosition(-1000, Constants.AUTO_LINEAR_SPEED);

    }

    public void spikeScore() {
        drive.simpleMoveToPosition(-100, Constants.AUTO_SAFE_MO);
        score.setBumperPixelRelease(Constants.AUTO_SCORING_CLAMP_OPEN);
        drive.simpleMoveToPosition(100, Constants.AUTO_SLOWED_SPEED);
        score.setBumperPixelRelease(Constants.AUTO_SCORING_CLAMP_CLOSED);
        drive.simpleMoveToPosition(100, Constants.AUTO_SAFE_MO);

    }

}
