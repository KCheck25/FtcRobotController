package teamcode.test.MasonTesting;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import teamcode.Competition.Subsystems.ArmSystem;
import teamcode.Competition.Subsystems.EndgameSystems;
import teamcode.common.AbstractOpMode;
import teamcode.common.MecanumDriveTrain;
import teamcode.common.Utils;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

@Disabled
@Autonomous(name = "DuckCamera")
public class DuckCamera extends AbstractOpMode {

    // TODO : Add MecanumDrive Classes and other external devices

    private final double VELOCITY = 10;
    private EndgameSystems system;
    private MecanumDriveTrain drive;
    private ArmSystem arm;

    private OpenCvCamera camera;
    private WebcamName webcam;

    private DuckPipeline duckPip = new DuckPipeline();

    @Override
    protected void onInitialize() {
        arm = new ArmSystem(hardwareMap, false);
        system = new EndgameSystems(hardwareMap, false);
        drive = new MecanumDriveTrain(hardwareMap, true, system, arm);

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = hardwareMap.get(WebcamName.class, "Webcam");

        camera = OpenCvCameraFactory.getInstance().createWebcam(webcam, cameraMonitorViewId);
        camera.setPipeline(duckPip);

        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addData("Camera Init Error", errorCode);
                telemetry.update();
            }
        });
    }

    double multiplier = 0.5;
    @Override
    protected void onStart() {
        // camera.stopStreaming();

        while (!duckPip.isCentered()) {
            telemetry.addData("DIR: ", duckPip.direction());
            telemetry.update();
            drive.setStrafe(duckPip.direction() * multiplier);
        }
        drive.brake();
        Utils.sleep(2000);
        arm.lowerLinkage();
        arm.intakeDumb(1.0);
        drive.moveDistanceDEVelocity(800, 0, VELOCITY / 2.0);
    }

    @Override
    protected void onStop() {
        drive.brake();
        drive.cleanup();
        camera.stopStreaming();
    }
}