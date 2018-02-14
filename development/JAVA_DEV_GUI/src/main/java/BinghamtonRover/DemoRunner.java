package BinghamtonRover;

import BinghamtonRover.GuiMain.GuiController;
import BinghamtonRover.Monitors.*;
import eu.hansolo.medusa.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.lang3.Validate;
import org.opencv.core.Core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class DemoRunner extends Application
{
    private static URL gsFile;
    private static final Random RND = new Random();
    private static int            noOfNodes = 0;
    private        Gauge          psurGauge;
    private        Gauge          tempGauge;
    private        Gauge humiGauge;
    private        Gauge          psurGraph;
    private        Clock          clock;
    private        long           lastTimerCall;
    private        AnimationTimer timer;

    private static Color          EERIEBLACK = Color.rgb(0x19,0x19,0x19);
    private static Color          SILVERBLUE = Color.rgb(0x5C,0x8E,0xB5);
    private static Color          ONYX       = Color.rgb(0x38,0x38,0x38);
    private static Color          VERMILLION = Color.rgb(0xE2,0x40,0x3B);
    private static Color          ALMOND     = Color.rgb(0xEF,0xDC,0xCC);
    private static Color          PLATINUM   = Color.rgb(0xE4,0xE1,0xDF);

    // Load in OpenCV3 libraries
    static
    {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override public void init() {
        psurGauge = GaugeBuilder.create()
                .foregroundBaseColor(PLATINUM)
                .barColor(VERMILLION)
                .skinType(Gauge.SkinType.LINEAR)
                .orientation(Orientation.HORIZONTAL)
                .prefSize(260, 200)
                .title("Air Pressure")
                .unit("PSI")
                .startAngle(270)     //Origin 270
                .angleRange(180)    //Origin 270
                .minValue(0)
                .maxValue(2000)
                .averageVisible(true)
                .averagingEnabled(true)
                .averagingPeriod(15)
                .tickLabelLocation(TickLabelLocation.OUTSIDE)
                .tickLabelOrientation(TickLabelOrientation.ORTHOGONAL)
                .tickMarkColor(ALMOND)
                .onlyFirstAndLastTickLabelVisible(true)
                .scaleDirection(Gauge.ScaleDirection.COUNTER_CLOCKWISE)
                .minorTickMarksVisible(false)
                .majorTickMarkType(TickMarkType.BOX)
                .valueVisible(true)
                .knobType(Gauge.KnobType.METAL)
                .interactive(true)
                .onButtonPressed(o -> System.out.println("psur knob Button pressed"))
                .onButtonReleased(o -> System.out.println("psur knob Button released"))
                .needleShape(Gauge.NeedleShape.FLAT)
                .needleColor(Color.CADETBLUE)
                .sectionsVisible(true)
                .animated(true)
                .animationDuration(100)
                .build();
        humiGauge = GaugeBuilder.create()
                .foregroundBaseColor(PLATINUM)
                .barColor(VERMILLION)
                .skinType(Gauge.SkinType.LINEAR)
                .orientation(Orientation.HORIZONTAL)
                .prefSize(260, 200)
                .title("Air Pressure")
                .unit("g/m^3")
                .startAngle(270)     //Origin 270
                .angleRange(180)    //Origin 270
                .minValue(0)
                .maxValue(100)
                .averageVisible(true)
                .averagingEnabled(true)
                .averagingPeriod(15)
                .tickLabelLocation(TickLabelLocation.OUTSIDE)
                .tickLabelOrientation(TickLabelOrientation.ORTHOGONAL)
                .tickMarkColor(ALMOND)
                .onlyFirstAndLastTickLabelVisible(true)
                .scaleDirection(Gauge.ScaleDirection.COUNTER_CLOCKWISE)
                .minorTickMarksVisible(false)
                .majorTickMarkType(TickMarkType.BOX)
                .valueVisible(true)
                .knobType(Gauge.KnobType.METAL)
                .interactive(true)
                .onButtonPressed(o -> System.out.println("psur knob Button pressed"))
                .onButtonReleased(o -> System.out.println("psur knob Button released"))
                .needleShape(Gauge.NeedleShape.FLAT)
                .needleColor(Color.CADETBLUE)
                .sectionsVisible(true)
                .animated(true)
                .animationDuration(100)
                .build();
        tempGauge = GaugeBuilder.create()
                .foregroundBaseColor(PLATINUM)
                .skinType(Gauge.SkinType.LINEAR)
                .orientation(Orientation.HORIZONTAL)
                .barColor(VERMILLION)
                .prefSize(260, 200)
                .title("Temperature")
                .unit("°C")
                .startAngle(270)     //Origin 270
                .angleRange(180)    //Origin 270
                .minValue(0)
                .maxValue(500)
                .averageVisible(true)
                .averagingEnabled(true)
                .averagingPeriod(15)
                .averageColor(Color.ORANGERED)
                .tickLabelLocation(TickLabelLocation.OUTSIDE)
                .tickLabelOrientation(TickLabelOrientation.ORTHOGONAL)
                .minorTickMarksVisible(false)
                .majorTickMarkType(TickMarkType.BOX)
                .valueVisible(true)
                .knobType(Gauge.KnobType.METAL)
                .interactive(true)
                .onButtonPressed(o -> System.out.println("temp knob Button pressed"))
                .onButtonReleased(o -> System.out.println("temp knob Button released"))
                .needleShape(Gauge.NeedleShape.FLAT)
                .needleColor(Color.ORANGERED)
                .sectionsVisible(true)
                .sections(new Section(0, 273, Color.rgb(100, 168, 199, 0.9)),
                        new Section(373, 500, Color.rgb(230, 55, 69, 0.9)))
                .animated(true)
                .animationDuration(100)
                .build();
        psurGraph = GaugeBuilder.create()
                .skinType(Gauge.SkinType.TILE_SPARK_LINE)
                .minValue(psurGauge.getMinValue())
                .maxValue(psurGauge.getMaxValue())
                .barColor(SILVERBLUE)
                .prefSize(125, 125)
                .averageVisible(true)
                .averagingEnabled(true)
                .averagingPeriod(20)
                .animated(true)
                .build();
        clock = ClockBuilder.create()
                .skinType(Clock.ClockSkinType.DIGITAL)
                .running(true)
                .textColor(ALMOND)
                .dateColor(ALMOND)
                .build();

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {

            @Override public void handle(long now) {
                if (now > lastTimerCall + 1_000_000_000) {
                    psurGauge.setValue(RND.nextGaussian() * 34 + 600);
                    tempGauge.setValue(RND.nextGaussian() * 25 + 205);
                    humiGauge.setValue(RND.nextGaussian() * 10 + 80);
                    psurGraph.setValue(tempGauge.getValue());
                    lastTimerCall = now;
                }
            }
        };
    }

    public static void main(String[] args) throws MalformedURLException
    {

        String lsFilePath = "BinghamtonRover/Monitors/python_output.log.json";
        gsFile = (args.length > 0) ? new URL(args[0]) : DemoRunner.class.getClassLoader().getResource(lsFilePath);
        launch(args);
    }

    @Override
    public void start(Stage aoPrimaryStage) throws NullPointerException, IOException
    {

        //Get the path of the FXML file
        URL loFXMLPath = getClass().getClassLoader().getResource("BinghamtonRover/GuiMain/guiScene.fxml");
        System.out.println("FXML Path: " + loFXMLPath.getPath());

        //Initialize a new array of Observer
        ArrayList<InformationObserver> laoObservers = new ArrayList<>();

        //Start the GUI
        FXMLLoader loLoader = new FXMLLoader(loFXMLPath);
        Pane loRoot = loLoader.load();
        loRoot.setBackground(new Background(new BackgroundFill(EERIEBLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        loRoot.getChildren().addAll(humiGauge, psurGauge, tempGauge, clock, psurGraph);
        loRoot.setMinSize(1200.0, 1080);
        humiGauge.relocate(0, 470);
        psurGauge.relocate(0, 560);
        tempGauge.relocate(0, 650);
        psurGraph.relocate(500, 0);
        clock.relocate(1080-280, 700);

        aoPrimaryStage.setTitle("Rover Controller");
        aoPrimaryStage.setScene(new Scene(loRoot, 1080,800));

        aoPrimaryStage.show();
        timer.start();

        //Get the Controller and assign it to each kinds of monitor
        GuiController loController = loLoader.getController();
        Validate.notNull(loController,"GuiController is null");

        laoObservers.add(new DistanceMonitor(loController));
        laoObservers.add(new BatteryMonitor(loController));
        laoObservers.add(new DirectionMonitor(loController));
        laoObservers.add(new PressureMonitor(loController));
        laoObservers.add(new TimeMonitor(loController));
        laoObservers.add(new LocationMonitor(loController));
        laoObservers.add(new TemperatureMonitor(tempGauge));

        FileUpdatingObservable loObservable = new FileUpdatingObservable(gsFile.getPath(), laoObservers);
        loObservable.startFileMonitoringThread();

    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
