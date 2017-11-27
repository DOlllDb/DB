import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.vavr.control.Try;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Maksim Nikelman on 26.11.17.
 */
public class TradingGenerator {
    private static ExecutorService executor;
    static {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("GenerateTradingEvent-%d")
                .setDaemon(true)
                .build();
        executor = Executors.newFixedThreadPool(10, threadFactory);
    }

    /**
     * Generate input data
     */
    public static List<String> generate(){
        LocalDate start = LocalDate.parse(Support.getStartDate(), Support.DATE_FORMAT);
        LocalDate end = LocalDate.parse(Support.getEndDate(), Support.DATE_FORMAT);
        List<Callable<String>> generators = new ArrayList<>();
        while (!start.isAfter(end)) {
            String date = start.format(Support.DATE_FORMAT);
            for (String market : Support.MARKETS) {
                int subFiles = new Random().nextInt(Support.getMaxSubFilesQuantifier()) + 1;
                if (subFiles == 1) {
                    generators.add(new Generator(market, date, 0));
                } else {
                    IntStream.range(1, subFiles + 1).forEach(i -> generators.add(new Generator(market, date, i)));
                }
            }
            start = start.plusDays(1);
        }
        List<Future<String>> generatorsFutures = Try.of(() -> executor.invokeAll(generators)).get();
        return generatorsFutures
                .stream()
                .filter(Future::isDone)
                .map(f -> Try.of(() -> f.get(1, TimeUnit.MINUTES))
                        .get())
                .collect(Collectors.toList());
    }

    /**
     * A class used for generating input data for specified market, date and subfile
     * Used to run in parallel threads
     */
    private static class Generator implements Callable<String> {
        private static int operationsFrequency;
        private static int incrementInterval;
        String fileName;
        double[] inPrices = Arrays.copyOf(Support.getInitialPrices(), Support.getInitialPrices().length);
        LocalTime genTime;
        LocalTime closeTime;

        public Generator(String market, String date, int subFile) {
            fileName = Support.getInputDir() + market + "-" + date +(subFile == 0 ? "" : "-" + subFile) + ".csv";
            genTime = LocalTime.parse(Support.MARKET_OPEN_TIME, Support.TIME_FORMAT);
            closeTime = LocalTime.parse(Support.MARKET_CLOSE_TIME, Support.TIME_FORMAT);
            operationsFrequency = Support.getGeneratorOperationsFrequency();
            incrementInterval = Support.getGeneratorOperationsIncrementInterval();
        }

        @Override
        public String call() {
            String result = "";
            long l = System.currentTimeMillis();
            File file = new File(fileName);int linesCounter = 0;
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {

                while (genTime.isBefore(closeTime)) {
                    int rand = new Random().nextInt(Support.getInstruments().length);
                    /** Could use TradingEvent object there.
                    TradingEvent event = new TradingEvent();
                    event.setInstrument(Support.getInstruments()[rand]);
                    event.setTime(genTime.plusMinutes(new Random().nextInt(10)).withSecond(new Random().nextInt(60)).format(Support.TIME_FORMAT));
                    event.setPrice(inPrices[rand] * 0.95 + (new Random().nextDouble()) * inPrices[rand] * 0.1);
                    event.setQuantity(new Random().nextInt(30) * 100);
                    bw.write(event.toString());
                     */
                    double randPrice = inPrices[rand] * 0.95 + (new Random().nextDouble()) * inPrices[rand] * 0.1;
                    String resultString = String.format("%s, %s, %.2f, %d",
                            Support.getInstruments()[rand],
                            genTime.plusMinutes(new Random().nextInt(10)).withSecond(new Random().nextInt(60)).format(Support.TIME_FORMAT),
                            randPrice,
                            new Random().nextInt(30) * 100);
                    bw.write(resultString);
                    bw.newLine();
                    linesCounter++;
                    if (linesCounter % operationsFrequency == 0)
                        genTime = genTime.plusMinutes(incrementInterval);
                }
                bw.flush();
            } catch (IOException e) {
                return Support.FILE_GENERATION_ERROR + Thread.currentThread().getName() + "\n"+e.getMessage() ;
            }
            return "File " + fileName + "successfully generated in "+(System.currentTimeMillis() - l) + " ms. "+linesCounter + " lines generated.";
        }
    }
}
