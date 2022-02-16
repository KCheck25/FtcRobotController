package teamcode.test.CalibrationClasses;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import teamcode.common.AbstractOpMode;

@Disabled
@TeleOp(name="Intake")
public class IntakeTest extends AbstractOpMode {
    DcMotor intakeMotorLeft, intakeMotorRight;

    @Override
    protected void onInitialize() {
        intakeMotorLeft = hardwareMap.dcMotor.get("LeftIntake");
        intakeMotorRight = hardwareMap.dcMotor.get("RightIntake");
    }

    @Override
    protected void onStart() {
        double startTime = AbstractOpMode.currentOpMode().time;
        while(opModeIsActive()){
            double elapsedTime = AbstractOpMode.currentOpMode().time - startTime;
            //            if(gamepad1.left_trigger > 0.3){
//                intakeMotorLeft.setPower(gamepad1.left_trigger);
//                intakeMotorRight.setPower(-gamepad1.left_trigger);
//            }else if(gamepad1.right_trigger > 0.3){
//                intakeMotorLeft.setPower(-gamepad1.right_trigger);
//                intakeMotorRight.setPower(gamepad1.right_trigger);
//            }else if(gamepad1.dpad_up){
//                intakeMotorRight.setPower(intakeMotorRight.getPower() + 0.1);
//                intakeMotorLeft.setPower(intakeMotorLeft.getPower() - 0.1);
//                while(gamepad1.dpad_up);
//            }else if(gamepad1.dpad_down){
//                intakeMotorRight.setPower(intakeMotorRight.getPower() - 0.1);
//                intakeMotorLeft.setPower(intakeMotorLeft.getPower() + 0.1);
//                while(gamepad1.dpad_down);
//            }else if(gamepad1.a){
//                intakeMotorLeft.setPower(0);
//                intakeMotorRight.setPower(0);
//            }

            telemetry.addData("left", intakeMotorLeft.getPower());
            telemetry.addData("right", intakeMotorRight.getPower());
            telemetry.update();
        }
    }

    @Override
    protected void onStop() {

    }
}