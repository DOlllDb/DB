import io.vavr.control.Try;
import org.junit.Assert;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Created by Maksim Nikelman on 26.11.17.
 */

/**
 * A support class for storing some variables that are used throughout the project
 */
public class Support {
    private static String inputDir;
    private static String outpudDir;
    private static String startDate;
    private static String endDate;

    private static int instrumentsQuantity = 10; // Quantity of instruments available for trading
    private static int maxSubFilesQuantifier = 2; // Specifies maximum quantity of files that can be generated for each exchange per day;
    private static int generatorOperationsFrequency = 2; // used to set how many operations will be generated per 'generatorOperationsIncrementInterval' minutes.
    private static int generatorOperationsIncrementInterval = 10;
    public static final String[] MARKETS = {"eurex", "xetra", "moex"};
    private static String[] instruments;
    private static double[] initialPrices;
    public static final String MARKET_OPEN_TIME = "08:00:00.00";
    public static final String MARKET_CLOSE_TIME = "16:30:00.00";
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SS");
    private static LocalDate startDateLD;
    private static LocalDate endDateLD;
    public static final String FILE_GENERATION_ERROR = "~! ERROR OCCURED WHILE GENERATING FILE !~";

    public static String getDateFromFileName(String fileName) {
        Pattern pattern = Pattern.compile(".*-(\\d{4}-\\d{2}-\\d{2})(?:-\\d*)?\\.csv");
        Matcher m = pattern.matcher(fileName);
        if (!m.find())
            return "";
        return m.group(1);
    }

    /**
     * Creates some random instruments and prices used for generating trading data
     */
    private static void initInstruments() {
        instruments = new String[instrumentsQuantity];
        initialPrices = new double[instrumentsQuantity];
        for (int i = 0; i < instruments.length; i++) {
            instruments[i] = "RU000" + generateNDigitsNumber(6);
            initialPrices[i] = ((double) new Random().nextInt(100000)) / 100;
        }
    }

    private static String generateNDigitsNumber(int digits) {
        StringBuilder sb = new StringBuilder();
        IntStream.range(0, digits).forEach(i -> sb.append(new Random().nextInt(10)));
        return sb.toString();
    }

    /**
     * Get generated prices for instruments
     * @return
     */
    public static double[] getInitialPrices() {
        if (null == initialPrices)
            initInstruments();
        return initialPrices;
    }

    /**
     * Get generated instruments
     * @return
     */
    public static String[] getInstruments() {
        if (null == instruments)
            initInstruments();
        return instruments;
    }

    /**
     * Get start date object
     * @return {@link LocalDate} object
     */
    public static LocalDate getStartDateLD() {
        if (startDateLD == null) {
            Assert.assertFalse("Start date not specified", getStartDate() == null || getStartDate().isEmpty());
            startDateLD = Try.of(() -> LocalDate.parse(getStartDate(), DATE_FORMAT))
                    .getOrElseThrow(() -> new AutotestException("Couldn't parse start date " + startDate));
        }
        return startDateLD;
    }

    /**
     * Get end date object
     * @return {@link LocalDate} object
     */
    public static LocalDate getEndDateLD() {
        if (endDateLD == null) {
            Assert.assertFalse("End date not specified", getEndDate() == null || getEndDate().isEmpty());
            endDateLD = Try.of(() -> LocalDate.parse(getEndDate(), DATE_FORMAT))
                    .getOrElseThrow(() -> new AutotestException("Couldn't parse end date "+ endDate));
        }
        return endDateLD;
    }

    /**
     * Checks input directory existence and creates it if needed
     *
     * @return {@link Boolean} result
     */
    public static boolean checkInputDirectory() {
        File iDir = new File(getInputDir());
        if (!iDir.exists())
            return iDir.mkdirs();
        return true;
    }

    /**
     * Checks output path directory existence and creates it if needed
     *
     * @return {@link Boolean} result
     */
    public static boolean checkOutputDirectory() {
        File oPath = new File(getOutputPath());
        if (!oPath.getParentFile().exists())
            return oPath.getParentFile().mkdirs();
        return true;
    }

    /**
     * Some setters and getters
     */
    public static void setInstrumentsQuantity(int exchanges) {
        Support.instrumentsQuantity = exchanges;
    }
    public static int setInstrumentsQuantity() {
        return Support.instrumentsQuantity;
    }
    public static void setInputDir(String inputDir) {
        Support.inputDir = inputDir;
        if (!Support.inputDir.endsWith("\\")) /* could probably make a better checks*/
            Support.inputDir += "\\";
    }
    public static String getInputDir() {
        return inputDir;
    }
    public static void setOutpudDir(String outpudDir) {
        Support.outpudDir = outpudDir;
    }
    public static String getOutputPath() {
        return outpudDir;
    }
    public static void setStartDate(String startDate) {
        Support.startDate = startDate;
        getStartDateLD();
    }
    public static String getStartDate() {
        return startDate;
    }
    public static void setEndDate(String endDate) {
        Support.endDate = endDate;
        getEndDateLD();
    }

    public static String getEndDate() {
        return endDate;
    }
    public static int getMaxSubFilesQuantifier() {
        return maxSubFilesQuantifier;
    }
    public static void setMaxSubFilesQuantifier(int maxSubFilesQuantifier) {
        Support.maxSubFilesQuantifier = maxSubFilesQuantifier;
    }
    public static void setGeneratorOperationsFrequency(int generatorOperationsFrequency) {
        Support.generatorOperationsFrequency = generatorOperationsFrequency;
    }
    public static int getGeneratorOperationsFrequency() {
        return generatorOperationsFrequency;
    }
    public static void setGeneratorOperationsIncrementInterval(int generatorOperationsIncrementInterval) {
        Support.generatorOperationsIncrementInterval = generatorOperationsIncrementInterval;
    }
    public static int getGeneratorOperationsIncrementInterval() {
        return generatorOperationsIncrementInterval;
    }
}
