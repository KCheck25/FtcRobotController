package org.firstinspires.ftc.teamcode.League1.Autonomous.Vision;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.League1.Subsystems.MecDrive;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.utils.Converters;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Config
public class KevinGodPipeline extends OpenCvPipeline {

    // Configuration variables for isolating pole color
    public static int H1 = 23;
    public static int S1 = 50;
    public static int V1 = 50;
    public static int H2 = 33;
    public static int S2 = 200;
    public static int V2 = 200;

    //Blue cone color
    public static int H3 = 105;
    public static int S3 = 120;
    public static int V3 = 70;
    public static int H4 = 117;
    public static int S4 = 240;
    public static int V4 = 255;

    //Red cone color
    public static int H5 = 0;
    public static int S5 = 140;
    public static int V5 = 100;
    public static int H6 = 7;
    public static int S6 = 255;
    public static int V6 = 255;

    // Config variables for signal pipeline
    public static int YUpper = 190;
    public static int YLower = 160;
    public static int CrUpper = 200;
    public static int CrLower = 130;
    public static int CbUpper = 130;
    public static int CbLower = 170;

    // Config variables for bounding box
    public static int topLeftXRightRed = 225 ;
    public static int topLeftYRightRed = 20;
    public static int boxWidthRightRed = 20;
    public static int boxHeightRightRed = 40;

    public static int topLeftXLeftRed = 140;
    public static int topLeftYLeftRed = 20;
    public static int boxWidthLeftRed = 20;
    public static int boxHeightLeftRed = 40;

    public static int topLeftXRightBlue = 215 ;
    public static int topLeftYRightBlue = 20;
    public static int boxWidthRightBlue = 20;
    public static int boxHeightRightBlue = 40;

    public static int topLeftXLeftBlue = 135;
    public static int topLeftYLeftBlue = 25;
    public static int boxWidthLeftBlue = 20;
    public static int boxHeightLeftBlue = 40;



    private Rect MIDDLE;

    // Define mats
    Mat ycrcb = new Mat();
    Mat temp = new Mat();


    // Define telemetry variable
    Telemetry telemetry;
    MecDrive drive;

    // Define lists
    private ArrayList<Integer> xList, yList, contourLengths;

    // Define ints
    int cX, cY;
    int maxLength = 0;
    int maxLengthIndex = 0;
    int longestContourX = 0;
    int longestContourY = 0;


    // Don't really know what this thing is, but we're defining it
    Moments M;

    // Enums
    public enum ParkPos {
        LEFT,
        RIGHT,
        CENTER
    }

    public enum AutoSide {
        RED_RIGHT,
        RED_LEFT,
        BLUE_RIGHT,
        BLUE_LEFT
    }

    public enum Mode{
        SLEEVE,
        POLE,
        REDCONE,
        BLUECONE
    }

    // The rectangle/submat used to evaluate the signal color
    static final Rect RIGHT_MIDDLE_RED = new Rect(
            new Point(topLeftXRightRed, topLeftYRightRed),
            new Point(topLeftXRightRed + boxWidthRightRed, topLeftYRightRed + boxHeightRightRed)
    );

    static final Rect LEFT_MIDDLE_RED = new Rect(
            new Point(topLeftXLeftRed, topLeftYLeftRed),
            new Point(topLeftXLeftRed + boxWidthLeftRed, topLeftYLeftRed + boxHeightLeftRed)
    );

    static final Rect RIGHT_MIDDLE_BLUE = new Rect(
            new Point(topLeftXRightBlue, topLeftYRightBlue),
            new Point(topLeftXRightBlue + boxWidthRightBlue, topLeftYRightBlue + boxHeightRightBlue)
    );

    static final Rect LEFT_MIDDLE_BLUE = new Rect(
            new Point(topLeftXLeftBlue, topLeftYLeftBlue),
            new Point(topLeftXLeftBlue + boxWidthLeftBlue, topLeftYLeftBlue + boxHeightLeftBlue)
    );


    // Sets default values for pipelineMode and position
    //PipelineMode pipelineMode = PipelineMode.SIGNAL;
    Mode sleeveSense = Mode.SLEEVE;
    ParkPos position = ParkPos.CENTER;

    public KevinGodPipeline(Telemetry telemetry){
        // Set up lists and telemetry
        xList = new ArrayList<>();
        yList = new ArrayList<>();
        contourLengths = new ArrayList<>();
        this.telemetry = telemetry;
    }

    public KevinGodPipeline(Telemetry telemetry, MecDrive drive){
        // Set up lists and telemetry
        xList = new ArrayList<>();
        yList = new ArrayList<>();
        contourLengths = new ArrayList<>();
        this.telemetry = telemetry;
        this.drive = drive;
    }


    public KevinGodPipeline(Telemetry telemetry, MecDrive drive, AutoSide autoSide){
        // Set up lists and telemetry
        xList = new ArrayList<>();
        yList = new ArrayList<>();
        contourLengths = new ArrayList<>();
        this.telemetry = telemetry;
        this.drive = drive;
        if(autoSide == AutoSide.RED_LEFT) {
            MIDDLE = LEFT_MIDDLE_RED;
        } else if(autoSide == AutoSide.RED_RIGHT) {
            MIDDLE = RIGHT_MIDDLE_RED;
        } else if(autoSide == AutoSide.BLUE_LEFT) {
            MIDDLE = LEFT_MIDDLE_BLUE;
        } else {
            MIDDLE = RIGHT_MIDDLE_BLUE;
        }
    }

    @Override
    public Mat processFrame(Mat input) {
        // Check pipelineMode and run corresponding image processing
        if(sleeveSense == Mode.SLEEVE) {

            // Convert image to YCrCb color space and extract the Y channel
            Imgproc.cvtColor(input, ycrcb, Imgproc.COLOR_RGB2YCrCb);
            Core.extractChannel(ycrcb, temp, 0);

            // Make a binary image of values within the desired range and calculate avg color
            Core.inRange(temp, new Scalar(140), new Scalar(200), temp);
            double countY = Core.mean(temp.submat(MIDDLE)).val[0];

            // Extract Cr channel
            Core.extractChannel(ycrcb, temp, 1);

            // Make binary image and calculate avg color
            Core.inRange(temp, new Scalar(130), new Scalar(200), temp);
            double countCr = Core.mean(temp.submat(MIDDLE)).val[0];

            // Extract Cb channel
            Core.extractChannel(ycrcb, temp, 2);

            // Make binary image and calculate avg color
            Core.inRange(temp, new Scalar(100), new Scalar(180), temp);
            double countCb = Core.mean(temp.submat(MIDDLE)).val[0];

            // Telemetry
            telemetry.addData("countY", countY);
            telemetry.addData("countCr", countCr);
            telemetry.addData("countCb", countCb);

            // Check if certain channels are within certain ranges to determine color
            if(countY > 100 && countCb < 90) {
                telemetry.addData("Color", "Yellow - Left");
                position = ParkPos.LEFT;
            } else if(countCr > 200 && countCb > 200) {
                telemetry.addData("Color", "Magenta - Right");
                position = ParkPos.RIGHT;
            } else {
                telemetry.addData("Color", "Cyan - Center ");
                position = ParkPos.CENTER;
            }

            telemetry.update();

            // Display rectangle in detected color
            if(position == ParkPos.LEFT){
                Imgproc.rectangle(
                        input,
                        MIDDLE,
                        new Scalar(255, 255, 0),
                        3

                );

            }else if(position == ParkPos.RIGHT){
                Imgproc.rectangle(
                        input,
                        MIDDLE,
                        new Scalar(255, 0, 255),
                        3

                );

            }else{
                Imgproc.rectangle(
                        input,
                        MIDDLE,
                        new Scalar(0, 155, 255),
                        3

                );
            }

        } else{

            // Convert to HSV color space
            Imgproc.cvtColor(input, temp, Imgproc.COLOR_RGB2HSV);

            // Make binary image of yellow pixels

            if(sleeveSense == Mode.POLE) {
                Core.inRange(temp, new Scalar(H1, S1, V1), new Scalar(H2, S2, V2), temp);
            }else if(sleeveSense == Mode.BLUECONE){
                Core.inRange(temp, new Scalar(H3, S3, V3), new Scalar(H4, S4, V4), temp);
            }else if(sleeveSense == Mode.REDCONE){
                Core.inRange(temp, new Scalar(H5, S5, V5), new Scalar(H6, S6, V6), temp);
            }

            // Blur image to reduce noise
            Imgproc.GaussianBlur(temp, temp, new Size(5, 5), 0);

            // Find all contours in binary image
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(temp, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);


            for(int i = 0; i < contours.size(); i++){
                // Filter out small, irrelevant contours
                if(contours.get(i).toArray().length > 6) {

                    // Draw all contours to the screen
                    Imgproc.drawContours(input, contours, i, new Scalar(230, 191, 254));

                    // Find center of contour and add a point on the screen
                    M = Imgproc.moments(contours.get(i));
                    cX = (int)(M.m10 / M.m00);
                    cY = (int)(M.m01 / M.m00);
                    Imgproc.circle(input, new Point(cX, cY), 3, new Scalar(0, 0, 255));

                    // Save the contour's center in a list
                    xList.add(cX);
                    yList.add(cY);

                    // Calculate the length of the contour and add it to a list
                    contourLengths.add(contours.get(i).toArray().length);

                }
            }

            // Reset maxLength so the program doesn't crash - Krish is a genious
            maxLength = 0;

            // Find largest contour
            for(int i = 0; i < xList.size() && i < contourLengths.size() && i < yList.size(); i++) {
                if(contourLengths.get(i) > maxLength) {
                    maxLength = contourLengths.get(i);
                    maxLengthIndex = i;
                }
            }

            // Make sure the program doesn't crash if no contours are found
            if(contourLengths.size() > 0) {
                // Find x coordinate of largest contour and display it on the screen
                longestContourX = xList.get(maxLengthIndex);
                longestContourY = yList.get(maxLengthIndex);
                Imgproc.circle(input, new Point(xList.get(maxLengthIndex), yList.get(maxLengthIndex)), 3, new Scalar(0, 255, 0));
            }

            // Telemetry stuff
            /*telemetry.addData("Contour X Pos", longestContourX);
            telemetry.update();
*/
            // Clear lists
            contourLengths.clear();
            xList.clear();
            yList.clear();

        }

        return input;
    }

    // Get x coordinate of center of largest contour (pole)
    public int getXContour() {
        return longestContourX;
    }

    public int getPoleYPos(){
        return longestContourY;
    }

    // Get parking position determined by signal mode
    public ParkPos getPosition() {
        return position;
    }

    public int normalize(double power, int target, int tolerance) {
        int xMax = target + tolerance;
        int xMin = target - tolerance;
        double startPos = drive.avgPos();
        int startPolePosition = getXContour();

        if(startPolePosition < xMax){
            power *= -1;
        }

        while(getXContour() > xMax || getXContour() < xMin) {
            /*if(getPolePosition() > xMax) {
                drive.setPowerAuto(power, MecDrive.MovementType.ROTATE);
            } else {
                drive.setPowerAuto(-power, MecDrive.MovementType.ROTATE);
            }*/

            drive.setPowerAuto(power, MecDrive.MovementType.ROTATE);
        }
        drive.simpleBrake();


        if(getXContour() < startPolePosition){
            return -(int)(startPos - drive.avgPos());

        }

        return (int)(startPos - drive.avgPos());



    }



    public void changeMode(Mode mode){
        sleeveSense = mode;
    }

    public void Ynormalize(double power, int target, int tolerance){
        int yMax = target + tolerance;
        int yMin = target - tolerance;
        while(getPoleYPos() > yMax || getPoleYPos() < yMin) {
            if(getPoleYPos() > yMax) {
                drive.setPowerAuto(power, MecDrive.MovementType.STRAIGHT);
            } else {
                drive.setPowerAuto(-power, MecDrive.MovementType.ROTATE);
            }
        }
    }



}