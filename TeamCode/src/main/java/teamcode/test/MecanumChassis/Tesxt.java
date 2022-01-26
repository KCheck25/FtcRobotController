package teamcode.test.MecanumChassis;

import android.speech.tts.TextToSpeech;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.robotcore.external.android.AndroidTextToSpeech;

import java.util.Locale;

import teamcode.common.AbstractOpMode;
@Disabled
@Autonomous(name="Tesxt")
public class Tesxt extends AbstractOpMode {
    TextToSpeech tts;

    @Override
    protected void onInitialize() {
        tts = new TextToSpeech(hardwareMap.appContext, null);
        tts.setLanguage(Locale.US);
        telemetry.addData("finished", "");
        telemetry.update();

    }

    @Override
    protected void onStart() {

        while(opModeIsActive());
    }

    @Override
    protected void onStop() {

    }
}
