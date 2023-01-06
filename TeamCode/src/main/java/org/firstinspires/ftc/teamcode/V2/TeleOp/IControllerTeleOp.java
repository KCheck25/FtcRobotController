package org.firstinspires.ftc.teamcode.V2.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.League1.Common.Constants;
import org.firstinspires.ftc.teamcode.League1.Common.Vector2D;
import org.firstinspires.ftc.teamcode.League1.Subsystems.MecDrive;
import org.firstinspires.ftc.teamcode.V2.NewSubsystem.ScoringSystemV2;
import org.firstinspires.ftc.teamcode.V2.NewSubsystem.ScoringSystemV2EpicLift;


@TeleOp (name = "TryingToMakeTheIController")
public class IControllerTeleOp extends LinearOpMode {

    ////Constants //constants = newConstants();
    ScoringSystemV2EpicLift score;
    MecDrive drive;

    public double currentTime, startTime, rightIntegralSum, leftIntegralSum, rightPreviousError, leftPreviousError, i;
    //EndgameSystems systems;

    ColorRangeSensor distance, color;

    PassivePower passive;

    volatile boolean autoLinkageFlag, grabFlag, shiftLinkageFlag, manualFlag, changeStackFlag, linkageUp, linkageDown, setStartTime;

    Thread liftThread,/* capThread,*/ linkageThread;

    //Enums for feed forward
    public enum PassivePower{
        //Feed forward is on
        EXTENDED,

        //Nothing
        MOVEMENT,

        //Power is set to 0
        ZERO,
    }


    @Override
    public void runOpMode() throws InterruptedException {
        //Initializing flags
        autoLinkageFlag = true;
        grabFlag = true;
        shiftLinkageFlag = true;
        manualFlag = true;
        linkageDown = false;
        linkageUp = false;

        ElapsedTime time = new ElapsedTime();


        //Feed forward is going to be off
        passive = PassivePower.ZERO;

        score = new ScoringSystemV2EpicLift(hardwareMap);
        //robot = new Robot(hardwareMap);
        drive = new MecDrive(hardwareMap,false, telemetry);
        //systems = new EndgameSystems(hardwareMap);


        //score.setLinkagePositionLogistic(Constants.linkageDown, 500);
        score.setGrabberPosition(Constants.open - 0.15);

        distance = hardwareMap.get(ColorRangeSensor.class, "distance");
        //color = hardwareMap.get(ColorRangeSensor.class, "color");

        i = score.getI();

        //Color sensor gain values
        color.setGain(300);
        distance.setGain(300);


        //Lift Thread
        liftThread = new Thread(){
            @Override
            public void run() {
                while(opModeIsActive()){

                    //Lift up to scoring position
                    if(gamepad1.left_trigger > 0.1){
                        //score.setPower(0.2);
                        if(score.getScoringMode() != ScoringSystemV2EpicLift.ScoringMode.ULTRA) {
                            score.epicAutoGoToPosition();

                            score.setLinkagePosition(Constants.linkageScoreV2 - 0.05);
                            passive = PassivePower.EXTENDED;
                        }else{
                            score.setLinkagePosition(0.15);
                        }

                    }else {
                        if(passive == PassivePower.EXTENDED){

                            if(setStartTime){
                                startTime = time.seconds();
                                setStartTime = false;
                            }

                            currentTime = time.seconds();


                            //TODO: check if we need to negate any

                            int rightPos = -1 * score.getRightEncoderPos();
                            int leftPos = score.getLeftEncoderPos();

                            int rightError = score.getHeight() - rightPos;
                            int leftError = score.getHeight() - leftPos;






                            //TODO: check if we need to negate any


                            rightIntegralSum += (50 * (rightError + rightPreviousError) * (currentTime - startTime));
                            leftIntegralSum += (50 * (leftError + leftPreviousError) * (currentTime - startTime));




                            //TODO: look at telemetry and see if we can have new bound (change integral sum limit)
                            if (rightIntegralSum > 20000) {
                                rightIntegralSum = 20000;
                            } else if (rightIntegralSum < -20000) {
                                rightIntegralSum = -20000;
                            }

                            if (leftIntegralSum > 20000) {
                                leftIntegralSum = 20000;
                            } else if (leftIntegralSum < -20000) {
                                leftIntegralSum = -20000;
                            }


                            double rightDerivative = (rightError - rightPreviousError) / (currentTime - startTime);
                            double leftDerivative = (leftError - leftPreviousError) / (currentTime - startTime);


                            double rightPower = ((i * rightIntegralSum));
                            double leftPower = ((i * leftIntegralSum));


                            score.setPower(rightPower, leftPower);


                            startTime = currentTime;
                            rightPreviousError = rightError;
                            leftPreviousError = leftError;


                            telemetry.update();


                            //score.setPowerSingular(0.23);
                        }else if(passive == PassivePower.ZERO){
                            score.setPower(0);
                        }
                    }


                    //Scoring feature
                    if(gamepad1.right_trigger > 0.1){

                        if(score.getScoringMode() != ScoringSystemV2EpicLift.ScoringMode.ULTRA) {
                            score.setGrabberPosition(Constants.score);

                            try {
                                sleep(600);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                            linkageDown = true;


                            //Do nothing during movement phase
                            //Reset to zero and no passive power
                            score.moveToPosition(0, 0.75);
                            passive = PassivePower.ZERO;

                            //Open Grabber and reset linkage
                            score.setGrabberPosition(Constants.open - 0.15);
                            //score.setLinkagePositionLogistic(Constants.linkageDownV2, 300);
                            //score.setLinkagePositionLogistic(0.8, 500);
                        }else{

                            score.setGrabberPosition(Constants.open - 0.15);
                            try {
                                sleep(700);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

                        //TODO: fix this
                        /*score.lowerConeStack();
                        score.setLinkageConeStack(true);*/

                        //Resetting flags
                        autoLinkageFlag = true;
                        grabFlag = true;

                        //Not extended anymore
                        score.setExtended(false);
                        setStartTime = true;

                        //Automated Grab
                    }else if((distance.getNormalizedColors().red > 0.85 || distance.getNormalizedColors().blue > 0.85) && autoLinkageFlag){


                        score.setGrabberPosition(Constants.grabbing);

                        grabFlag = false;

                        try {
                            Thread.currentThread().sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(score.getScoringMode() == ScoringSystemV2EpicLift.ScoringMode.ULTRA){
                            try {
                                sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                        linkageUp = true;
                        autoLinkageFlag = false;


                    }


                    /*else if((distance.getDistance(DistanceUnit.CM) < 2) && grabFlag) {
                        score.setGrabberPosition(Constants.grabbing);

                        grabFlag = false;
                        try {
                            sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }



                    }
                    */


                    //TODO: see if need to fix this logic
                    //Auto linkage up logic after sensing a cone


                    //TODO: tune this (both raise and lower)
                    //Linkage stack cone heights with dpad up and down
                    if((gamepad1.left_bumper || gamepad1.dpad_up || gamepad1.dpad_down) && changeStackFlag){

                        //Raise linkage by height of a cone (max height of 5)
                        if(gamepad1.left_bumper || gamepad1.dpad_up) {
                            score.raiseConeStack();
                            score.setLinkageConeStack(false);
                            changeStackFlag = false;

                            //Lower linkage by height of a cone (min height of 1)
                        }else if(gamepad1.dpad_down){
                            score.lowerConeStack();
                            score.setLinkageConeStack(false);
                            changeStackFlag = false;

                        }

                        try {
                            sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    if(!gamepad1.dpad_down && !gamepad1.dpad_up && !gamepad1.left_bumper){
                        changeStackFlag = true;
                    }


                    //Linkage up position
                    if(gamepad1.left_stick_button){
                        score.setLinkagePosition(Constants.linkageScoreV2 - 0.05);

                    }



                    //Manual open and close grabber
                    if(gamepad1.start && manualFlag){
                        if(score.getGrabberPosition() != Constants.open - 0.15) {
                            score.setGrabberPosition(Constants.open - 0.15);
                            try {
                                sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            grabFlag = true;
                        }else{
                            score.setGrabberPosition(Constants.grabbing);
                            try {
                                sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            grabFlag = false;
                        }
                        manualFlag = false;
                    }else if(!gamepad1.start){
                        manualFlag = true;
                    }



                    //Changing scoring modes (toggle)
                    if(gamepad1.y){
                        score.setScoringMode(ScoringSystemV2EpicLift.ScoringMode.LOW);

                    }else if(gamepad1.x){
                        score.setScoringMode(ScoringSystemV2EpicLift.ScoringMode.MEDIUM);

                    }else if(gamepad1.b){
                        score.setScoringMode(ScoringSystemV2EpicLift.ScoringMode.HIGH);

                    }else if(gamepad1.a){
                        //Ultra
                        score.setScoringMode(ScoringSystemV2EpicLift.ScoringMode.ULTRA);
                    }


                    //Manual slides (dpad right and left)
                    if(gamepad1.dpad_right){
                        passive = PassivePower.MOVEMENT;
                        score.setPower(1);
                    }else if(gamepad1.dpad_left){
                        passive = PassivePower.MOVEMENT;
                        score.setPower(-0.55);
                    }else{

                        //Feedforward if slides are extended
                        if(score.isExtended() && (score.getScoringMode() == ScoringSystemV2EpicLift.ScoringMode.LOW || score.getScoringMode() == ScoringSystemV2EpicLift.ScoringMode.MEDIUM)){
                            passive = PassivePower.EXTENDED;
                        }else{
                            passive = PassivePower.ZERO;
                        }


                    }


                }

            }
        };

        //CapThread
        /*capThread = new Thread(){


            @Override
            public void run() {
                while(opModeIsActive()){

                    //Capstone extension and retraction
                    if(gamepad2.right_trigger > 0 || gamepad2.left_trigger > 0) {
                        double val = gamepad2.right_trigger - gamepad2.left_trigger;
                        systems.setCapstoneExtensionPower(val);

                        //Fine tune with left and right bumper
                    } else if (gamepad2.right_bumper) {
                        systems.setCapstoneExtensionPower(0.25);
                    } else if (gamepad2.left_bumper) {
                        systems.setCapstoneExtensionPower(-0.25);
                    } else{
                        systems.setCapstoneExtensionPower(0);
                    }



                    //Setting y potion of cap mech
                    double yPos = systems.getYCapPosition();
                    systems.setXCapstoneRotatePower(gamepad2.right_stick_x);
                    systems.setYCapPosition(yPos - systems.map(gamepad2.right_stick_y, -1, 1, -0.0010, 0.0010));

                    //Reset the y servo
                    if (gamepad2.x) {
                        systems.zeroCap();

                        //Change the speed of x servo rotation
                    } else if (gamepad2.dpad_right && previousRight != gamepad2.dpad_right) {
                        systems.setXCapSpeedDivisor(10);
                    } else if (gamepad2.dpad_left && previousLeft != gamepad2.dpad_left) {
                        systems.setXCapSpeedDivisor(7);
                    }


                    previousLeft = gamepad2.dpad_left;
                    previousRight = gamepad2.dpad_right;
                    previousUp = gamepad2.dpad_up;
                    previousDown = gamepad2.dpad_down;
                }

                //On stop
                systems.zeroCap();
                systems.setXCapstoneRotatePower(0);
                systems.setCapstoneExtensionPower(0);


            }
        };
*/
        //Logistic Linkage thread
        linkageThread = new Thread() {

            @Override
            public void run() {
                while(opModeIsActive()) {
                    if(linkageUp) {

                        try {
                            Thread.currentThread().sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        score.setLinkagePosition(Constants.linkageScoreV2 - 0.05);
                        linkageUp = false;
                    }else if(linkageDown) {

                        score.setLinkagePosition(Constants.linkageUpV2);
                        try {
                            Thread.currentThread().sleep(70);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        score.setLinkagePositionLogistic(Constants.linkageDownV2, 220);
                        linkageDown = false;
                    }
                }
            }
        };


        waitForStart();

        //score.setLinkagePosition(Constants.linkageUpV2);


        //Starting Threads
        liftThread.start();
        //capThread.start();
        linkageThread.start();

        while(opModeIsActive()){

            //N S E W Drive
            double leftStickX = gamepad1.left_stick_x;
            double leftStickY = gamepad1.left_stick_y;

            if(Math.abs(leftStickX) > Math.abs(leftStickY)){
                leftStickY = 0;

            }else if(Math.abs(leftStickY) > Math.abs(leftStickX)){
                leftStickX = 0;

            }else{
                leftStickY = 0;
                leftStickX = 0;
            }

            if (gamepad1.right_bumper) {
                drive.setPower(new Vector2D(leftStickX * Constants.SPRINT_LINEAR_MODIFIER, leftStickY * Constants.SPRINT_LINEAR_MODIFIER), gamepad1.right_stick_x * Constants.SPRINT_ROTATIONAL_MODIFIER, false);
            } else if(score.isExtended()){
                //Slow down when slides are extended
                drive.setPower(new Vector2D(leftStickX * Constants.EXTENDED_LINEAR_MODIFIER, leftStickY * Constants.EXTENDED_LINEAR_MODIFIER), gamepad1.right_stick_x * Constants.EXTENDED_ROTATIONAL_MODIFIER, false);
            } else{
                drive.setPower(new Vector2D(leftStickX * Constants.NORMAL_LINEAR_MODIFIER, leftStickY * Constants.NORMAL_LINEAR_MODIFIER), gamepad1.right_stick_x * Constants.NORMAL_ROTATIONAL_MODIFIER, false);
            }


            //Telemetry
            telemetry.addData("lMotor", -1 * score.getLeftEncoderPos());
            telemetry.addData("rMotor", score.getRightEncoderPos());
            telemetry.addData("distance: ", distance.getDistance(DistanceUnit.CM));
            telemetry.addData("distanceRed", distance.getNormalizedColors().red);
            telemetry.addData("distanceBlue", distance.getNormalizedColors().blue);
            telemetry.addData("colorRed", color.getNormalizedColors().red);
            telemetry.addData("colorBlue", color.getNormalizedColors().blue);
            telemetry.addData("autoLinkageFlag", autoLinkageFlag);
            telemetry.addData("grabbingFlag", grabFlag);
            telemetry.addData("manualFlag", manualFlag);
            telemetry.addData("shiftLinkageFlag", shiftLinkageFlag);
            telemetry.addData("extended", score.isExtended());
            /*telemetry.addData("colorRed: ", color.getNormalizedColors().red);
            telemetry.addData("colorBlue: ", color.getNormalizedColors().blue);*/
            telemetry.addData("rightServoTarget", score.getRightLinkage());
            telemetry.addData("leftServoTarget", score.getLeftLinkage());
            telemetry.addData("passive", passive);
            telemetry.addData("coneStack", score.getConeStack());
            telemetry.update();

        }


        //Stop
        drive.simpleBrake();

        //score.setLinkagePositionLogistic(0.25, 500);
        score.setLinkagePositionLogistic(Constants.linkageDownV2, 500);
        //score.setLinkagePositionLogistic(0.8, 500);




        score.setGrabberPosition(Constants.open - 0.15);
    }
}
