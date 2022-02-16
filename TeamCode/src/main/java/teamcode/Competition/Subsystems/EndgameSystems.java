package teamcode.Competition.Subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.checkerframework.checker.units.qual.A;

import teamcode.common.AbstractOpMode;
import teamcode.common.Debug;
import teamcode.common.Utils;
import teamcode.common.WestCoastDriveTrain;

public class EndgameSystems {
    /**
     * Class for the Carousel and the Capstone mech
     * Electronics Schematic,
     *
     * Capstone Mechanism
     * 1x crServo
     * 2xServo
     *
     * Carousel
     * 1xCRServo
     * 1xEncoder
     */

    private CRServo carouselBlue, carouselRed, capstoneExtension;
    private Servo xCap, yCap;
    private DcMotor carouselEncoderBlue, carouselEncoderRed;
    private boolean isBlue;


    public void runCarousel(double power) {

        carouselRed.setPower(power );
        carouselBlue.setPower(power);
    }

    public double getCarouselPos(){
        return carouselEncoderRed.getCurrentPosition();
    }




    public EndgameSystems(HardwareMap hardwareMap, boolean isBlue){
        carouselBlue = hardwareMap.crservo.get("CarouselBlue");
        carouselEncoderBlue = hardwareMap.dcMotor.get("Intake");
        carouselRed = hardwareMap.crservo.get("CarouselRed");
        carouselEncoderRed = hardwareMap.dcMotor.get("Winch");
        xCap = hardwareMap.servo.get("xCap");
        yCap = hardwareMap.servo.get("yCap");
        capstoneExtension = hardwareMap.crservo.get("capExtension");

        //carouselEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        //carouselEncoder.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        this.isBlue = isBlue;
        zeroCap();

    }

    public void setXCapPosition(double pos){
        xCap.setPosition(pos);
    }

    public double getXCapPosition() {
        return xCap.getPosition();
    }

    public void setCapstoneExtensionPower(double pow){
        capstoneExtension.setPower(pow);
    }

    public void setYCapPosition(double pos){
        yCap.setPosition(pos);
    }

    public double getYCapPosition() {
        return yCap.getPosition();
    }

    public void zeroCap() {
        xCap.setPosition(0);
        yCap.setPosition(1);
    }

    public void scoreDuck() {
        double multiplier;
        if(isBlue){
            multiplier = -1;
        }else{
            multiplier = 1;
        }
        carouselEncoderRed.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        carouselEncoderBlue.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        int pose = 18000;
        long currentTime = System.currentTimeMillis();

        carouselEncoderRed.setTargetPosition(pose);

        carouselEncoderRed.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        carouselEncoderBlue.setTargetPosition(pose);

        carouselEncoderBlue.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        //while(Math.abs(carouselEncoderRed.getCurrentPosition()) < Math.abs(carouselEncoderRed.getTargetPosition()) && AbstractOpMode.currentOpMode().opModeIsActive() && Math.abs(carouselEncoderBlue.getCurrentPosition()) < Math.abs(carouselEncoderBlue.getTargetPosition())){
        while (Math.abs(carouselEncoderRed.getCurrentPosition()) < Math.abs(carouselEncoderRed.getTargetPosition()) && AbstractOpMode.currentOpMode().opModeIsActive()) {
            AbstractOpMode.currentOpMode().telemetry.addData("target", pose);
            AbstractOpMode.currentOpMode().telemetry.addData("current", Math.abs(carouselEncoderRed.getCurrentPosition()));
            AbstractOpMode.currentOpMode().telemetry.update();
            // first 8000 ticks go normal, then speed
            if (Math.abs(carouselEncoderRed.getCurrentPosition()) < carouselEncoderRed.getTargetPosition() * 0.44) {
                carouselBlue.setPower(0.25  * multiplier);
                carouselRed.setPower(0.25 * multiplier);
            } else {
                carouselBlue.setPower(1  * multiplier);
                carouselRed.setPower(1 * multiplier);
            }

//            if(carouselEncoder.getCurrentPosition() > carouselEncoder.getTargetPosition() * 0.1){
//                if(carouselEncoder.getCurrentPosition() > carouselEncoder.getTargetPosition() * 0.2){
//                    carousel.setPower(1 );
//                }else{
//                    carousel.setPower(0.75 );
//                }
//            }else{
//                carousel.setPower(0.5 );
//            }


        }

        carouselEncoderRed.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        carouselEncoderBlue.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        carouselBlue.setPower(0);
        carouselRed.setPower(0);
    }

    public void setCarouselMode(DcMotor.RunMode mode){
        carouselEncoderRed.setMode(mode);
        carouselEncoderBlue.setMode(mode);
    }

    public void scoreDuckAuto() {
        Debug.log("here");
        carouselEncoderRed.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        int pose = -25000;
        double direction;
        if(isBlue){
            direction = -1;
        }else {
            direction = 1;
        }
        pose *= direction;
        carouselEncoderRed.setTargetPosition(pose);
        carouselEncoderRed.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        while(Math.abs(carouselEncoderRed.getCurrentPosition()) < Math.abs(carouselEncoderRed.getTargetPosition()) && AbstractOpMode.currentOpMode().opModeIsActive()){
            if(Math.abs(carouselEncoderRed.getCurrentPosition()) < 15000){
                carouselRed.setPower(.15 * direction);
                carouselBlue.setPower(.15 * direction);

            }else{
                carouselRed.setPower(1 * direction);
                carouselBlue.setPower(1 * direction);

            }
            AbstractOpMode.currentOpMode().telemetry.addData("curr", carouselEncoderRed.getCurrentPosition());
            AbstractOpMode.currentOpMode().telemetry.addData("tar", carouselEncoderRed.getTargetPosition());
            AbstractOpMode.currentOpMode().telemetry.update();

        }
        carouselRed.setPower(0);
        carouselBlue.setPower(0.1 * direction);
        carouselEncoderRed.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public DcMotor getBlueCarouselEncoder() {
        return carouselEncoderBlue;
    }
    public DcMotor getRedCarouselEncoder() {
        return carouselEncoderRed;
    }

}