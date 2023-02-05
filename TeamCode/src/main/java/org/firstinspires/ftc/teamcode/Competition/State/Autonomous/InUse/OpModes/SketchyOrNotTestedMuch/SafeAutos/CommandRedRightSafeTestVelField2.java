package org.firstinspires.ftc.teamcode.Competition.State.Autonomous.InUse.OpModes.SketchyOrNotTestedMuch.SafeAutos;

import com.acmerobotics.dashboard.FtcDashboard;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.Competition.Interleagues.Common.Constants;
import org.firstinspires.ftc.teamcode.Competition.State.Autonomous.InUse.CustomCommands.DriveInSafe;
import org.firstinspires.ftc.teamcode.Competition.State.Subsystems.Current.KevinGodPipelineAprilTag;
import org.firstinspires.ftc.teamcode.Competition.State.Subsystems.Current.MecDriveV2;
import org.firstinspires.ftc.teamcode.Competition.State.Subsystems.Current.ScoringSystemV2EpicLift;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.concurrent.atomic.AtomicBoolean;


//Didnt change camera pole height cuz tuned for 1 + 5
//Checked pipeline parameters


//@Disabled
@Autonomous(name = "Red Right (Safe) Field 2")
public class CommandRedRightSafeTestVelField2 extends LinearOpMode {
    MecDriveV2 drive;
    ScoringSystemV2EpicLift score;
    ElapsedTime time = new ElapsedTime();
    AtomicBoolean hold, armUp, armDown, finalMove, linkageUp;
    int cycles, rotateTics;
    ColorRangeSensor distance;


    Servo cameraServo;

    OpenCvWebcam camera;
    KevinGodPipelineAprilTag pipeline;
    KevinGodPipelineAprilTag.ParkPos parkPos;

    boolean failed;
    boolean preloadSuccess = false;


    @Override
    public void runOpMode() throws InterruptedException {
        distance = hardwareMap.get(ColorRangeSensor.class, "distance");

        drive = new MecDriveV2(hardwareMap, false, telemetry, true);
        score = new ScoringSystemV2EpicLift(hardwareMap, telemetry, false);
        hold = new AtomicBoolean(false);
        armUp = new AtomicBoolean(false);
        finalMove = new AtomicBoolean(false);
        linkageUp = new AtomicBoolean(false);
        armDown = new AtomicBoolean(false);

        score.setLinkagePosition(Constants.linkageScoreV2 - 0.07);
        score.setGrabberPosition(Constants.grabbing);


        cameraServo = hardwareMap.get(Servo.class, "camera");
        failed = false;


        distance.setGain(300);

        cycles = 5;


        WebcamName webcamName = hardwareMap.get(WebcamName.class, "Webcam 1");
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName, cameraMonitorViewId);
        pipeline = new KevinGodPipelineAprilTag(telemetry, drive, KevinGodPipelineAprilTag.AutoSide.RED_RIGHT, false);

        camera.setPipeline(pipeline);


        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(320, 176, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addData("Camera Init Error", errorCode);
                telemetry.update();

            }
        });

        FtcDashboard.getInstance().startCameraStream(camera, 0);


        cameraServo.setPosition(Constants.sleeveV2);


        waitForStart();

        CommandScheduler.getInstance().schedule(
                new InstantCommand(() -> pipeline.changeMode(KevinGodPipelineAprilTag.Mode.POLE)),
                new InstantCommand(() -> cameraServo.setPosition(Constants.poleV2 - 0.033)),
                new InstantCommand(() -> parkPos = pipeline.getPosition()),
                new InstantCommand(() -> drive.goTOPIDPosWithRampUp(-2250, 1, MecDriveV2.MovementType.STRAIGHT, 1)),
                new WaitCommand(100),
                new InstantCommand(() -> drive.tankRotatePID((5 * Math.PI) / 8, 1, false)),
                //new InstantCommand(() -> drive.simpleMoveToPosition(-780, MecDriveV2.MovementType.ROTATE, 1)),
                new InstantCommand(() -> rotateTics = pipeline.normalize(0.25, 156, 2)),
                new ParallelCommandGroup(
                        new InstantCommand(() -> score.setLinkagePositionLogistic(Constants.linkageUpV2Auto, 300, 100)),
                        new InstantCommand(() -> score.newLiftPID(940, 1)),
                        new InstantCommand(() -> pipeline.changeMode(KevinGodPipelineAprilTag.Mode.REDCONE)),
                        new InstantCommand(() -> cameraServo.setPosition(Constants.coneV2))
                ),

                new InstantCommand(() -> drive.simpleMoveToPosition(-150, MecDriveV2.MovementType.STRAIGHT, 0.4)),
                new WaitCommand(100),
                new InstantCommand(() -> score.setGrabberPosition(Constants.score + 0.1)),
                new WaitCommand(250),


                new InstantCommand(() -> score.setLinkagePositionLogistic(Constants.linkageUpV2Auto - 0.1, 300, 100)),
                new InstantCommand(() -> score.setGrabberPosition(Constants.openV2)),

                new WaitCommand(200),


                new InstantCommand(() -> score.moveToPosition(0, 0.75)),
                new InstantCommand(() -> drive.tankRotatePIDSpecial(Math.PI / 2, 1, false, 0.9)),

                //new InstantCommand(() -> drive.tankRotatePID(Math.PI/2, 1, false, 0.9)),
                //new InstantCommand(() -> drive.simpleMoveToPosition(160 + rotateTics, MecDriveV2.MovementType.ROTATE, 1)),
                //new InstantCommand(() -> drive.tankRotate(Math.PI/2, 0.25)),
                new InstantCommand(() -> sleep(5)),
                new InstantCommand(() -> pipeline.normalizeStrafe(0.3, 151, 2)),
                new InstantCommand(() -> score.setLinkagePositionLogistic(0.242, 100)),

                new ParallelCommandGroup(
                        new InstantCommand(() -> drive.simpleMoveToPosition(585, MecDriveV2.MovementType.STRAIGHT, 1)),
                        new InstantCommand(() -> pipeline.changeMode(KevinGodPipelineAprilTag.Mode.POLE)),
                        new InstantCommand(() -> cameraServo.setPosition(Constants.poleV2 - 0.033))
                ),

                new DriveInSafe(drive, distance, false, 0.35),
                new InstantCommand(() -> score.setGrabberPosition(Constants.grabbing)),
                new WaitCommand(125),
                new InstantCommand(() -> score.setLinkagePositionLogistic(Constants.linkageUpV2Auto, 300, 100))

        );

        for (int i = 0; i < 3; i++) {
            int finalI = i;
            if (i != 2) {
                CommandScheduler.getInstance().schedule(


                        new InstantCommand(() -> drive.goTOPIDPosWithRampUp(-1360, 1, MecDriveV2.MovementType.STRAIGHT, 1)),
                        //new InstantCommand(() -> pipeline.normalizeStraight(0.3, 82, 1)),
                        new InstantCommand(() -> drive.tankRotatePID((5.4 * Math.PI) / 8, 1, false)),
                        //new InstantCommand(() -> drive.simpleMoveToPosition(-200, MecDriveV2.MovementType.ROTATE, 1)),
                        new InstantCommand(() -> rotateTics = pipeline.normalize(0.25, 156, 2)),
                        new ParallelCommandGroup(
                                new InstantCommand(() -> score.setLinkagePositionLogistic(Constants.linkageUpV2Auto + 0.02, 300, 100)),
                                new InstantCommand(() -> score.newLiftPID(930, 0.85)),
                                new InstantCommand(() -> pipeline.changeMode(KevinGodPipelineAprilTag.Mode.REDCONE)),
                                new InstantCommand(() -> cameraServo.setPosition(Constants.coneV2))
                        ),

                        //new InstantCommand(() -> drive.simpleMoveToPosition(-40, MecDriveV2.MovementType.STRAIGHT, 0.4)),
                        new WaitCommand(100),
                        new InstantCommand(() -> score.setGrabberPosition(Constants.score + 0.1)),
                        new WaitCommand(250),


                        new InstantCommand(() -> score.setLinkagePositionLogistic(Constants.linkageUpV2Auto - 0.1, 300, 100)),
                        new InstantCommand(() -> score.setGrabberPosition(Constants.openV2)),

                        new WaitCommand(200),


                        new InstantCommand(() -> score.moveToPosition(0, 0.75)),
                        new InstantCommand(() -> drive.tankRotatePIDSpecial(Math.PI / 2, 1, false, 0.9)),

                        //new InstantCommand(() -> drive.tankRotatePID(Math.PI / 2, 1, false, 0.9)),
                        //new InstantCommand(() -> drive.simpleMoveToPosition(160 + rotateTics, MecDriveV2.MovementType.ROTATE, 1)),
                        //new InstantCommand(() -> drive.tankRotate(Math.PI/2, 0.3)),
                        new InstantCommand(() -> sleep(5)),
                        new InstantCommand(() -> pipeline.normalizeStrafe(0.3, 151, 2)),
                        new InstantCommand(() -> score.setLinkagePositionLogistic(0.242 - ((finalI + 2) * 0.025), 100)),

                        new ParallelCommandGroup(
                                new InstantCommand(() -> drive.simpleMoveToPosition(585, MecDriveV2.MovementType.STRAIGHT, 1)),
                                new InstantCommand(() -> pipeline.changeMode(KevinGodPipelineAprilTag.Mode.POLE)),
                                new InstantCommand(() -> cameraServo.setPosition(Constants.poleV2 - 0.033))
                        ),

                        new DriveInSafe(drive, distance, true, 0.35),
                        new InstantCommand(() -> score.setGrabberPosition(Constants.grabbing)),
                        new InstantCommand(() -> sleep(125)),
                        new InstantCommand(() -> score.setLinkagePositionLogistic(Constants.linkageUpV2Auto, 300, 100))


                );
            } else {



                    CommandScheduler.getInstance().schedule(


                            new InstantCommand(() -> drive.goTOPIDPosWithRampUp(-1360, 1, MecDriveV2.MovementType.STRAIGHT, 1)),
                            //new InstantCommand(() -> pipeline.normalizeStraight(0.3, 82, 1)),
                            new InstantCommand(() -> drive.tankRotatePID((5.4 * Math.PI) / 8, 1, false)),
                            //new InstantCommand(() -> drive.simpleMoveToPosition(-200, MecDriveV2.MovementType.ROTATE, 1)),
                            new InstantCommand(() -> rotateTics = pipeline.normalize(0.25, 156, 2)),
                            new ParallelCommandGroup(
                                    new InstantCommand(() -> score.setLinkagePositionLogistic(Constants.linkageUpV2Auto + 0.02, 300, 100)),
                                    new InstantCommand(() -> score.newLiftPID(930, 0.85)),
                                    new InstantCommand(() -> pipeline.changeMode(KevinGodPipelineAprilTag.Mode.BLUECONE)),
                                    new InstantCommand(() -> cameraServo.setPosition(Constants.coneV2))
                            ),

                            //new InstantCommand(() -> drive.simpleMoveToPosition(-40, MecDriveV2.MovementType.STRAIGHT, 0.4)),
                            new WaitCommand(100),
                            new InstantCommand(() -> score.setGrabberPosition(Constants.score + 0.1)),
                            new WaitCommand(250),


                            new InstantCommand(() -> score.setLinkagePositionLogistic(Constants.linkageUpV2Auto - 0.1, 300, 100)),
                            new InstantCommand(() -> score.setGrabberPosition(Constants.openV2)),

                            new WaitCommand(200),


                            new InstantCommand(() -> score.moveToPosition(0, 0.75)),
                            new InstantCommand(() -> drive.tankRotatePIDSpecial(Math.PI / 2, 1, false, 0.9)),

                            //new InstantCommand(() -> drive.tankRotatePID(Math.PI / 2, 1, false, 0.9)),
                            //new InstantCommand(() -> drive.simpleMoveToPosition(160 + rotateTics, MecDriveV2.MovementType.ROTATE, 1)),
                            //new InstantCommand(() -> drive.tankRotate(Math.PI/2, 0.3)),

                            new InstantCommand(() -> sleep(5))
                    );


                }



        }

        CommandScheduler.getInstance().run();


        if (parkPos == KevinGodPipelineAprilTag.ParkPos.LEFT) {
            drive.simpleMoveToPosition(-440, MecDriveV2.MovementType.STRAIGHT, 1);



        } else if (parkPos == KevinGodPipelineAprilTag.ParkPos.RIGHT) {
            drive.simpleMoveToPosition(850, MecDriveV2.MovementType.STRAIGHT, 1);
        } else {
            drive.simpleMoveToPosition(150, MecDriveV2.MovementType.STRAIGHT, 1);
        }

        sleep(50);


    }
}