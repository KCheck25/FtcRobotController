package teamcode.Competition;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;

import org.checkerframework.checker.units.qual.A;

import java.util.List;

import teamcode.common.AbstractOpMode;
import teamcode.common.Constants;
import teamcode.common.Localizer;
import teamcode.common.RobotPositionStateUpdater;
import teamcode.common.Utils;
import teamcode.test.ColorSensorTest;

import static teamcode.common.Constants.*;

public class ArmSystem {

    //House Servo values
    private static final double INTAKE_POSITION = 0.24;
    private static final double HOUSING_POSITION = 0.37; //these values are great, the scoring one MAYBE move up a lil but no more than 0.66 because it grinds at that point
    private static final double SCORING_POSITION = 0.62;

    private static final double LINKAGE_DOWN = 0.26; //these values need to be refined but they are good ballparks. AYUSH: No longer a final constant.
    private static final double LINKAGE_SCORE = 0.7;

    private static final float GREEN_THRESHOLD = 255; //not needed for now
    private static final float RED_THRESHOLD = 255;
    private static final float BLUE_THRESHOLD = 255;
    private static final int YELLOW_THRESHOLD = 02552550;
    private static final int WHITE_THRESHOLD = 0255255255;

    private static final double SLIDE_POWER = 1.0;

    private DcMotor leftIntake, rightIntake, winchMotor, winchEncoder;
    private Servo house, linkage;
    private CRServo carousel;
    private NormalizedColorSensor sensor;
    RobotPositionStateUpdater.RobotPositionState currentState;
    private Stage stage;


    public ArmSystem(HardwareMap hardwareMap, boolean isTeleOp){
        leftIntake = hardwareMap.dcMotor.get("LeftIntake");
        rightIntake = hardwareMap.dcMotor.get("RightIntake");
        winchMotor = hardwareMap.dcMotor.get("Winch");
        winchEncoder = hardwareMap.dcMotor.get("FrontLeftDrive");

        house = hardwareMap.servo.get("House");
        linkage = hardwareMap.servo.get("Linkage");
        carousel = hardwareMap.get(CRServo.class, "Carousel");

        sensor = hardwareMap.get(NormalizedColorSensor.class, "color");
        sensor.setGain(280); //325 is tested value but i think I trust this one more

        winchEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        winchEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        carousel.setDirection(DcMotorSimple.Direction.REVERSE);

        if(isTeleOp){
            house.setPosition(INTAKE_POSITION);
        }else{
            house.setPosition(HOUSING_POSITION);
        }
        linkage.setPosition(LINKAGE_SCORE);
        stage = Stage.IDLE;
    }

    public void intakeDumb(double power){
        leftIntake.setPower(power);
        rightIntake.setPower(-power);
    }



    public void intake(double intakePower, boolean isAuto){
        if(isAuto){
            lowerLinkage();
        }

        if(stage == stage.HOUSED){
            stage = stage.INTAKING;
        }

        if(stage == stage.INTAKING || stage == Stage.IDLE) {
            boolean detectedElement = false;
            NormalizedRGBA colors = sensor.getNormalizedColors();
            double green = colors.green;
            double blue = colors.blue;

            AbstractOpMode.currentOpMode().telemetry.addData("green", green);
            AbstractOpMode.currentOpMode().telemetry.addData("blue", blue);
            AbstractOpMode.currentOpMode().telemetry.update();
            if (green > 0.9) {
                if (blue > 0.9) {
                    detectedElement = true;
                } else {
                    detectedElement = true;
                }
            } else {
                detectedElement = false;
            }

            intakeDumb(intakePower);
            stage = Stage.INTAKING;
            if(detectedElement) {
                preScore();
            }
        }
    }
    //will be merged into intake() later
    public void preScore(){
        intakeDumb(0);
        house.setPosition(HOUSING_POSITION);
        Utils.sleep(250);
        linkage.setPosition(LINKAGE_SCORE);
        stage = Stage.HOUSED;


    }

    public boolean isLinkageInPreScore(){
        return linkage.getPosition() != LINKAGE_SCORE;
    }



    public void raise(double position) {
        if(linkage.getPosition() != LINKAGE_SCORE){
            preScore();
        }
        moveSlide(SLIDE_POWER, position);
        stage = stage.EXTENDED;
    }

    //temporary tele op scoring function w/o color sensor
    public void score(){
        house.setPosition(SCORING_POSITION);
    }

    public void retract(){
        moveSlide(-SLIDE_POWER, 500);
        house.setPosition(INTAKE_POSITION);
        stage = Stage.IDLE;

    }


    public void setWinchPower(double v) {
        winchMotor.setPower(v);
    }

    public void lowerLinkage() {
        house.setPosition(INTAKE_POSITION);
        linkage.setPosition(LINKAGE_DOWN);
    }

    private enum Stage{
        INTAKING, IDLE, HOUSED, EXTENDED
    }

    //tele op scoring function, assumes the freight is encapsulated in the house already and that the
    //linkage is raised (not scoring). This method also assumes the Conveyor exists as well so if we
    //get rid of the conveyor we need to change this
    //uses color sensor data
//    public void scoreCS(){
//        if(stage == Stage.CUBE_HOUSED) {
//
//        }else if(stage == Stage.BALL_HOUSED){
//            linkage.setPosition(LINKAGE_SCORE);
//            moveSlide(SLIDE_POWER, TOP_POSITION);
//            Utils.sleep(200);
//            house.setPosition(SCORING_POSITION);
//            Utils.sleep(500);
//            moveSlide(-SLIDE_POWER, BOTTOM_POSITION);
//
//        }
//        house.setPosition(INTAKE_POSITION);
//        linkage.setPosition(LINKAGE_DOWN);
//        stage = Stage.IDLE;

    //}

    public void moveSlide(double power, double position){
        AbstractOpMode.currentOpMode().telemetry.clear();
        while (Math.abs(winchEncoder.getCurrentPosition() - position) > 100) {
//            AbstractOpMode.currentOpMode().telemetry.addData("position", winchEncoder.getCurrentPosition());
//            AbstractOpMode.currentOpMode().telemetry.update();
            winchMotor.setPower(power);

        }
        winchMotor.setPower(0);

    }

    //needs to be rewritten if the conveyor is implemented
    public void scoreAuto(BarcodePipeline.BarcodePosition position){
        linkage.setPosition(LINKAGE_SCORE);
        Utils.sleep(200);
        if(position == BarcodePipeline.BarcodePosition.LEFT){ ;
        }else if(position == BarcodePipeline.BarcodePosition.CENTER || position == BarcodePipeline.BarcodePosition.LEFT){
            moveSlide(SLIDE_POWER, MEDIUM_POSITION);

        }else if(position == BarcodePipeline.BarcodePosition.RIGHT){
            moveSlide(SLIDE_POWER, TOP_POSITION);
        }
        house.setPosition(SCORING_POSITION);
        Utils.sleep(500);
        moveSlide(-SLIDE_POWER, BOTTOM_POSITION);

    }

    public int getLinearSlidePosition(){
        return winchEncoder.getCurrentPosition();
    }

    public Stage getStage(){
        return stage;
    }

}
