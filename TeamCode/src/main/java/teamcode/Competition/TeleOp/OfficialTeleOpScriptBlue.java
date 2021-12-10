package teamcode.Competition.TeleOp;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import teamcode.Competition.Subsystems.ArmSystem;
import teamcode.Competition.Subsystems.EndgameSystems;
import teamcode.common.AbstractOpMode;
import teamcode.common.Constants;
import teamcode.common.Localizer;
import teamcode.common.Vector2D;
import teamcode.common.WestCoastDriveTrain;


@TeleOp(name="tele op BLUE")
public class OfficialTeleOpScriptBlue extends AbstractOpMode {

    WestCoastDriveTrain drive;
    ArmSystem arm;
    EndgameSystems system;
    Thread driveThread, driverTwoThread;
    Thread armThread;
    BNO055IMU imu;
    Localizer localizer;



    private static final double INTAKE_POWER = 1.0;
    private static final double SPRINT_LINEAR_MODIFIER = 1.0;
    private static final double NORMAL_LINEAR_MODIFIER = 1.0;
    private static final double SPRINT_ROTATIONAL_MODIFIER = 1.0;
    private static final double NORMAL_ROTATIONAL_MODIFIER = 0.5;
    private boolean isSprint;
    private long scoredSampleTime;

    private ScoredButtonState state;
    private PulleyState pulleyState;
    private LinkageState linkageState;

    private boolean moveOnCarousel;
    private boolean isCarousel;


    @Override
    protected void onInitialize() {
        arm = new ArmSystem(hardwareMap, true);
        drive = new WestCoastDriveTrain(hardwareMap);
        system = new EndgameSystems(hardwareMap, true); //TODO make a copy of tele op

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        isSprint = true;

        localizer = new Localizer(hardwareMap, new Vector2D(0,0), 0,10);
        localizer.liftOdo();

        moveOnCarousel = true;

        //Initialize IMU parameters

        state = ScoredButtonState.RETRACTING;
        pulleyState = PulleyState.RETRACTED;
        linkageState = linkageState.RAISED;

        driveThread = new Thread(){
            public void run(){
                while(opModeIsActive()){
                    driveUpdate();
                }
            }
        };
        armThread = new Thread(){
            public void run(){
                while(opModeIsActive()){
                    armUpdate();
                }
            }
        };
        driverTwoThread = new Thread(){
            public void run(){
                while(opModeIsActive()){
                    driverTwoUpdate();
                }
            }
        };

    }

    private void driverTwoUpdate() {
        if(gamepad1.left_bumper){
            while(gamepad1.left_bumper) {
                system.runCarousel(1);
            }
        }else if(gamepad1.right_bumper){
            isCarousel = true;
            if(moveOnCarousel) {
                moveOnCarousel = false;
                drive.setPower(-0.1, 0);
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            system.scoreDuck();
            isCarousel = false;

        }else {
            system.runCarousel(0);
        }

//        telemetry.addData("stage", arm.getStage());
//        telemetry.update();

    }

    private double startTime;
    private void armUpdate() {
        if(gamepad1.right_trigger > 0.3){
            startTime = AbstractOpMode.currentOpMode().time;
            while(gamepad1.right_trigger > 0.3) {
                double elapsedTime = AbstractOpMode.currentOpMode().time - startTime;
                if(elapsedTime < 0.5 || linkageState == linkageState.RAISED){
                    arm.lowerLinkage();
                    linkageState = LinkageState.LOWERED;
                }else{
                    arm.intakeDumb(0.3 * Math.abs(Math.sin(2 * elapsedTime)) + 0.5);
                }

            }
        }else if(gamepad1.a){
            //add something to move the linkage outta the way
            arm.intakeDumb(-0.6);
        }else if(gamepad1.x){
            long currentSampleTime = System.currentTimeMillis();
            if(currentSampleTime - scoredSampleTime > 200) {
                if(pulleyState != PulleyState.RETRACTED) {
                    if (state == ScoredButtonState.RETRACTING) {
                        state = ScoredButtonState.SCORED;
                        arm.score();
                    } else if (state == ScoredButtonState.SCORED) {
                        state = ScoredButtonState.RETRACTING;
                        arm.retract();
                        pulleyState = PulleyState.RETRACTED;
                    }
                }
                scoredSampleTime = System.currentTimeMillis();
            }
        } else if(gamepad1.b) {
            arm.preScore();
            linkageState = LinkageState.RAISED;
        } else if (gamepad1.dpad_up) {
                arm.setWinchPower(0.5);
        } else if (gamepad1.dpad_down) {
            arm.setWinchPower(-0.5);
        } else if(gamepad1.left_trigger > 0.3) {
            if (pulleyState == PulleyState.RETRACTED && linkageState == LinkageState.RAISED) {
                arm.raise(Constants.TOP_POSITION + 600);
                pulleyState = PulleyState.HIGH_GOAL;
                linkageState = LinkageState.RAISED;
            }
        }else if(gamepad1.dpad_right ){
            if(pulleyState == PulleyState.RETRACTED && linkageState == LinkageState.RAISED) {
                arm.raise(Constants.MEDIUM_POSITION);
                pulleyState = PulleyState.MID_GOAL;
                linkageState = linkageState.RAISED;
            }
        }else if(gamepad1.y){
            arm.score();
            arm.runConveyorPos(1.0, 2000);
            arm.idleServos();
        }else if(gamepad1.dpad_left){
            arm.resetWinchEncoder();
        }else{
            arm.intakeDumb(0);
            arm.setWinchPower(0);
        }
    }

    private static final double ROTATE_DPAD = 0.3;
    private static final double LINEAR_DPAD = 0.5;

    //TODO change this if necessary
    private void driveUpdate() {
        if(!isCarousel) {
            if (gamepad1.right_stick_button) {
                drive.setPower(NORMAL_LINEAR_MODIFIER * gamepad1.left_stick_y, SPRINT_ROTATIONAL_MODIFIER * gamepad1.right_stick_x);
            } else if (gamepad1.left_stick_button) {
                drive.setPower(0.15 * gamepad1.left_stick_y, NORMAL_ROTATIONAL_MODIFIER * gamepad1.right_stick_x);
            } else {
                drive.setPower(NORMAL_LINEAR_MODIFIER * gamepad1.left_stick_y, NORMAL_ROTATIONAL_MODIFIER * gamepad1.right_stick_x);
            }
        }

    }

    private enum ScoredButtonState{
        SCORED, RETRACTING
    }

    private enum PulleyState{
        HIGH_GOAL, MID_GOAL, RETRACTED
    }

    private enum LinkageState{
        RAISED, LOWERED
    }

    @Override
    protected void onStart() {
        driveThread.start();
        armThread.start();
        driverTwoThread.start();
        while(opModeIsActive()){
        }
    }

    @Override
    protected void onStop() {
        driveThread.interrupt();
        driverTwoThread.interrupt();
        armThread.interrupt();

    }
}
