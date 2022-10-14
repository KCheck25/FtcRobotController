package org.firstinspires.ftc.teamcode.Auto;


import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

//TODO: check if camera angle works
public class DetectionAlgorithmTest extends OpenCvPipeline {
    Telemetry telemetry;
    /*
   YELLOW  = Parking Left
   CYAN    = Parking Middle
   MAGENTA = Parking Right
    */
    // Mat defined/instantiated, percents (for each color) instantiated
    private Mat yelMat = new Mat(), cyaMat = new Mat(), magMat = new Mat(), changed = new Mat(), original = new Mat();
    private double yelPercent, cyaPercent, magPercent;

    // top left point of submat
    public static final Point BOX_TOPLEFT = new Point(100,176);

    // width and height of submat
    public static int BOX_WIDTH = 100;
    public static int BOX_HEIGHT = -88;

    enum ParkingPosition {
        LEFT,
        CENTER,
        RIGHT
    }

    // Running variable storing the parking position
    private volatile ParkingPosition position = ParkingPosition.LEFT;

    // submat to center cone
    Point box_top_left = new Point(
            BOX_TOPLEFT.x,
            BOX_TOPLEFT.y);
    Point box_bottom_right = new Point(
            BOX_TOPLEFT.x + BOX_WIDTH,
            BOX_TOPLEFT.y + BOX_HEIGHT);

    // Lower and upper boundaries for colors -> YCrCb
    private static final Scalar
            lower_yellow_bounds  = new Scalar(convertToY(200, 200, 0)),
            upper_yellow_bounds  = new Scalar(convertToY(255, 255, 130)),
            lower_cyan_bounds    = new Scalar(convertToCb(0, 200, 200)),
            upper_cyan_bounds    = new Scalar(convertToCb(150, 255, 255)),
            lower_magenta_bounds = new Scalar(convertToCr(170, 0, 170)),
            upper_magenta_bounds = new Scalar(convertToCr(255, 60, 255));

    // Color definitions -> RGB
    private final Scalar
            YELLOW  = new Scalar(255, 255, 0),
            CYAN    = new Scalar(0, 255, 255),
            MAGENTA = new Scalar(255, 0, 255);

    public DetectionAlgorithmTest(Telemetry telemetry){
        this.telemetry = telemetry;
    }


    @Override
    public Mat processFrame(Mat input) {
        Mat befChange = new Mat();

        input.copyTo(original);

        if(original.empty()) {
            return input;
        }
        // cyan magenta yellow

        /* colors (scalars):
            magenta -> new Scalar(255, 0, 255)
            yellow -> new Scalar(255, 255, 0)
            cyan -> new Scalar(0, 255, 255)
         */
        changed = original.submat(new Rect(box_top_left, box_bottom_right));
        changed.copyTo(befChange);

        //Core.extractChannel(changed, changed, 1);

        Imgproc.GaussianBlur(changed, changed, new Size(5,5), 0);
        Imgproc.erode(changed, changed, new Mat(), new Point(-1, -1), 2);
        Imgproc.dilate(changed, changed, new Mat(), new Point(-1, -1), 2);
        Imgproc.cvtColor(changed, changed, Imgproc.COLOR_RGB2YCrCb);

        //Y -> brightness, Cr -> red - brightness, Cb -> blue - brightness
        Core.extractChannel(changed, yelMat, 0);
        Core.extractChannel(changed, cyaMat, 2);
        Core.extractChannel(changed, magMat, 1);
//        // Apply Morphology
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
//        Imgproc.morphologyEx(changed, changed, Imgproc.MORPH_CLOSE, kernel);
//


            /* submatrices
            Mat pixel_section = original.submat(rowStart, rowEnd, colStart, colEnd)l

             */

        // https://sistenix.com/rgb2ycbcr.html -> convert between rgb and ycbcr
        // yellow
        Core.inRange(yelMat, lower_yellow_bounds, upper_yellow_bounds, yelMat);
        // cyan
        Core.inRange(cyaMat, lower_cyan_bounds, upper_cyan_bounds, cyaMat);
        // magenta
        Core.inRange(magMat, lower_magenta_bounds, upper_magenta_bounds, magMat);



        // percent "abundance" for each color
        yelPercent = Core.countNonZero(yelMat);
        cyaPercent = Core.countNonZero(cyaMat);
        magPercent = Core.countNonZero(magMat);
        telemetry.addData("yelPercent", yelPercent);
        telemetry.addData("cyaPercent", cyaPercent);
        telemetry.addData("magPercent", magPercent);
        telemetry.update();

        // decides parking position, highlights margin according to greatest abundance color
        if (yelPercent > cyaPercent) {
            if (yelPercent > magPercent) {
                // yellow greatest, position left
                position = ParkingPosition.LEFT;
                //telemetry.addData("park position", position);
                Imgproc.rectangle(befChange, new Rect(0,0,100,88), YELLOW, 2);
            } else {
                // magenta greatest, position right
                position = ParkingPosition.RIGHT;
                //telemetry.addData("park position", position);
                Imgproc.rectangle(befChange, new Rect(0,0,100,88), MAGENTA, 2);
            }
        } else if(cyaPercent > magPercent) {
            // cyan greatest, position center
            position = ParkingPosition.CENTER;
            //telemetry.addData("park position", position);
            Imgproc.rectangle(befChange, new Rect(0,0,100,88), CYAN, 2);
        } else {

            // magenta greatest, positio    n right
            position = ParkingPosition.RIGHT;
            //telemetry.addData("park position", position);
            Imgproc.rectangle(befChange, new Rect(0,0,100,88), MAGENTA, 2);

        }
        telemetry.update();

        // Memory cleanup
        //changed.release();
        //original.release();
        yelMat.release();
        cyaMat.release();
        magMat.release();

        return befChange;
    }
    public static double convertToY(int r, int g, int b) {
        return 16 + (65.738 * r + 129.057 * g + 25.064 * b) / 256;
    }

    public static double convertToCb(int r, int g, int b) {
        return 128 - (37.945 * r - 74.494 * g + 112.439 * b) / 256;
    }

    public static double convertToCr(int r, int g, int b) {
        return 128 + (112.439 * r - 94.154 * g - 18.285 * b) / 256;
    }

    // Returns an enum being the current position where the robot will park
    public ParkingPosition getPosition() {
        return position;
    }
}



