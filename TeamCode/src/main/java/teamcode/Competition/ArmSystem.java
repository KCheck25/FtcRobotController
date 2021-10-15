package teamcode.Competition;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.List;

import teamcode.common.Constants;
import teamcode.common.Localizer;
import teamcode.common.RobotPositionStateUpdater;
import teamcode.common.Utils;

import static teamcode.common.Constants.*;

public class ArmSystem {

    //House Servo values
    private static final double INTAKE_POSITION = 0.24;
    private static final double HOUSING_POSITION = 0.42; //these values are great, the scoring one MAYBE move up a lil but no more than 0.66 because it grinds at that point
    private static final double SCORING_POSITION = 0.6;

    private static final double LINKAGE_DOWN = 0.3; //these values need to be refined but they are good ballparks, will be better to tune when the rigid mount is done
    private static final double LINKAGE_SCORE = 0.69;

    private static final float GREEN_THRESHOLD = 255; //not needed for now
    private static final float RED_THRESHOLD = 255;
    private static final float BLUE_THRESHOLD = 255;
    private static final int YELLOW_THRESHOLD = 02552550;
    private static final int WHITE_THRESHOLD = 0255255255;

    private static final double SLIDE_POWER = 1.0;

    private Localizer localizer;
    private DcMotor leftIntake, rightIntake, winchMotor, carouselEncoder;
    private Servo house, linkage;
    RobotPositionStateUpdater.RobotPositionState currentState;
    private Stage stage;


    public ArmSystem(HardwareMap hardwareMap, Localizer localizer, boolean isTeleOp){
        leftIntake = hardwareMap.dcMotor.get("LeftIntake");
        rightIntake = hardwareMap.dcMotor.get("RightIntake");
        winchMotor = hardwareMap.dcMotor.get("Winch");
        //carouselEncoder = hardwareMap.dcMotor.get("conveyor");
        house = hardwareMap.servo.get("House");
        linkage = hardwareMap.servo.get("Linkage");
        this.localizer = localizer;
        currentState = localizer.getCurrentState();
        if(isTeleOp){
            house.setPosition(INTAKE_POSITION);
        }else{
            house.setPosition(HOUSING_POSITION);
        }
        linkage.setPosition(LINKAGE_DOWN);
        stage = Stage.IDLE;
    }

    public void intakeDumb(double power){
        leftIntake.setPower(power);
        rightIntake.setPower(-power);
    }

    @Deprecated //using a color sensor which is currently not on the robot
    //deprecated until further notice
    public void intake(double intakePower){
        house.setPosition(INTAKE_POSITION);
        intakeDumb(intakePower);
        linkage.setPosition(LINKAGE_DOWN);
        stage = Stage.INTAKING;
        NormalizedRGBA houseRGBA = localizer.getCurrentState().getHouseRGBA();
        while(houseRGBA.green < 0.9 && houseRGBA.red < 0.9){ //TODO calibrate the Gain
            houseRGBA = localizer.getCurrentState().getHouseRGBA();
        }
        if(houseRGBA.blue >= 0.9){
            //balls
            stage = Stage.BALL_HOUSED;

        }else{
            //cubes
            stage = Stage.CUBE_HOUSED;
        }

        linkage.setPosition(LINKAGE_SCORE);
        Utils.sleep(100);
        house.setPosition(HOUSING_POSITION);
    }


    public void raise(double position) {
        house.setPosition(HOUSING_POSITION);
        moveSlide(SLIDE_POWER, position);
        house.setPosition(SCORING_POSITION);
    }

    private enum Stage{
        INTAKING, IDLE, BALL_HOUSED, CUBE_HOUSED
    }

    //tele op scoring function, assumes the freight is encapsulated in the house already and that the
    //linkage is raised (not scoring). This method also assumes the Conveyor exists as well so if we
    //get rid of the conveyor we need to change this
    public void score(){
        if(stage == Stage.CUBE_HOUSED) {

        }else if(stage == Stage.BALL_HOUSED){
            linkage.setPosition(LINKAGE_SCORE);
            moveSlide(SLIDE_POWER, TOP_POSITION);
            Utils.sleep(200);
            house.setPosition(SCORING_POSITION);
            Utils.sleep(500);
            moveSlide(-SLIDE_POWER, BOTTOM_POSITION);

        }
        house.setPosition(INTAKE_POSITION);
        linkage.setPosition(LINKAGE_DOWN);
        stage = Stage.IDLE;

    }

    public void moveSlide(double power, double position){
        while (Math.abs(currentState.getLinearSlidePosition() - position) > 0.1) {
            currentState = localizer.getCurrentState();
            winchMotor.setPower(power);
        }
        winchMotor.setPower(0);

    }

    //needs to be rewritten if the conveyor is implemented
    public void scoreAuto(BarcodeReaderPipeline.BarcodePosition position){
        linkage.setPosition(LINKAGE_SCORE);
        Utils.sleep(200);
        if(position == BarcodeReaderPipeline.BarcodePosition.LEFT){
            moveSlide(SLIDE_POWER, LOW_POSITION);

        }else if(position == BarcodeReaderPipeline.BarcodePosition.CENTER){
            moveSlide(SLIDE_POWER, MEDIUM_POSITION);

        }else if(position == BarcodeReaderPipeline.BarcodePosition.RIGHT){
            moveSlide(SLIDE_POWER, TOP_POSITION);
        }
        house.setPosition(SCORING_POSITION);
        Utils.sleep(500);
        moveSlide(-SLIDE_POWER, BOTTOM_POSITION);

    }

    public Stage getStage(){
        return stage;
    }

}
