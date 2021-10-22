package teamcode.test.MasonTesting;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

public class BarcodePipeline extends OpenCvPipeline {

    // define position enums
    public enum BarcodePosition
    {
        LEFT,
        CENTER,
        RIGHT
    }

    // define col constants
    static final Scalar BLUE = new Scalar(0, 0, 255);
    static final Scalar GREEN = new Scalar(0, 255, 0);

    // get anchor points for each region
    static final Point REGION1_TOPLEFT_ANCHOR_POINT = new Point(0, 110);
    static final Point REGION2_TOPLEFT_ANCHOR_POINT = new Point(160, 110);
    static final Point REGION3_TOPLEFT_ANCHOR_POINT = new Point(300, 110);
    static final int REGION_WIDTH = 20;
    static final int REGION_HEIGHT = 40;

    // define top left and bottom right region points
    Point region1_pointA = new Point(
            REGION1_TOPLEFT_ANCHOR_POINT.x,
            REGION1_TOPLEFT_ANCHOR_POINT.y);
    Point region1_pointB = new Point(
            REGION1_TOPLEFT_ANCHOR_POINT.x + REGION_WIDTH,
            REGION1_TOPLEFT_ANCHOR_POINT.y + REGION_HEIGHT);
    Point region2_pointA = new Point(
            REGION2_TOPLEFT_ANCHOR_POINT.x,
            REGION2_TOPLEFT_ANCHOR_POINT.y);
    Point region2_pointB = new Point(
            REGION2_TOPLEFT_ANCHOR_POINT.x + REGION_WIDTH,
            REGION2_TOPLEFT_ANCHOR_POINT.y + REGION_HEIGHT);
    Point region3_pointA = new Point(
            REGION3_TOPLEFT_ANCHOR_POINT.x,
            REGION3_TOPLEFT_ANCHOR_POINT.y);
    Point region3_pointB = new Point(
            REGION3_TOPLEFT_ANCHOR_POINT.x + REGION_WIDTH,
            REGION3_TOPLEFT_ANCHOR_POINT.y + REGION_HEIGHT);

    // Create vars
    Mat region1_Cb, region2_Cb, region3_Cb;
    Mat YCrCb = new Mat();
    Mat Cb = new Mat();
    int avg1, avg2, avg3;

    // create pos var, with vol tag due to the var changing at runtime
    volatile BarcodePosition position = BarcodePosition.CENTER;


    // converts rgb frame to ycrcb, extracts cb channel
    void inputToCb(Mat input) {
        Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);
        Core.extractChannel(YCrCb, Cb, 2);
    }

    @Override
    public void init(Mat frame) {
        inputToCb(frame);

        region1_Cb = Cb.submat(new Rect(region1_pointA, region1_pointB));
        region2_Cb = Cb.submat(new Rect(region2_pointA, region2_pointB));
        region3_Cb = Cb.submat(new Rect(region3_pointA, region3_pointB));
    }

    @Override
    public Mat processFrame(Mat input) {
        inputToCb(input);

        avg1 = (int) Core.mean(region1_Cb).val[0];
        avg2 = (int) Core.mean(region2_Cb).val[0];
        avg3 = (int) Core.mean(region3_Cb).val[0];

        Imgproc.rectangle(
                input,
                region1_pointA,
                region1_pointB,
                BLUE,
                2
        );

        Imgproc.rectangle(
                input,
                region2_pointA,
                region2_pointB,
                GREEN,
                2
        );

        Imgproc.rectangle(
                input,
                region3_pointA,
                region3_pointB,
                BLUE,
                2
        );

        int max = Math.max(Math.max(avg1, avg2), avg3);

        if (max == avg1) {

            Imgproc.rectangle(
                    input,
                    region1_pointA,
                    region1_pointB,
                    GREEN,
                    -1
            );

            position = BarcodePosition.LEFT;
        } else if (max == avg2) {

            Imgproc.rectangle(
                    input,
                    region2_pointA,
                    region2_pointB,
                    GREEN,
                    -1
            );

            position = BarcodePosition.CENTER;
        } else if (max == avg3) {

            Imgproc.rectangle(
                    input,
                    region3_pointA,
                    region3_pointB,
                    GREEN,
                    -1
            );

            position = BarcodePosition.RIGHT;
        }

        return input;
    }

    public BarcodePosition getPos() {
        return position;
    }
}
