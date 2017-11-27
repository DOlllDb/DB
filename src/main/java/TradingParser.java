import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.vavr.control.Try;
import org.junit.Assert;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by Maksim Nikelman on 26.11.17.
 */
public class TradingParser {
    /**
     * Could probably move this executor service to support.
     * Decided to leave it right there for the time being as {@link TradingGenerator} uses different namings for its threads
     */
    private static ExecutorService executor;
    static {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("ParseTradingEvent-%d")
                .setDaemon(true)
                .build();
        executor = Executors.newFixedThreadPool(10, threadFactory);
    }

    /** A {@link FilenameFilter} filter to filter files by date. Also ignores incorrect pattern names*/
    static FilenameFilter filesFilterByDate = (dir, name) -> {
        String date = Support.getDateFromFileName(name);
        if (date.isEmpty())
            return false;
        LocalDate fileDate = LocalDate.parse(date, Support.DATE_FORMAT);
        return !fileDate.isBefore(Support.getStartDateLD()) && !fileDate.isAfter(Support.getEndDateLD());
    };

    /**
     * Collect all files from specified directory and apply filter for file names
     * A date in fileName should be between start and end dates
     *
     * @return an array of collected files
     */
    public static File[] collectInputFiles() {
        File file = new File(Support.getInputDir());
        File[] files = file.listFiles(filesFilterByDate);
        Assert.assertFalse("There are no files in specified directory "+Support.getInputDir(), files == null);
        return files;
    }

    /**
     * Parse all csv files in specified directories, filtered by start date and end date
     *
     * @return list of all {@link TradingEvents} objects
     */
    public static List<TradingEvents> parseCsvData() {
        File[] files = collectInputFiles();
        List<Callable<TradingEvents>> parsers = Arrays.stream(files).map(TradingFileParser::new).collect(Collectors.toList());
        List<Future<TradingEvents>> futures = Try.of(() -> executor.invokeAll(parsers)).get();
        return futures.parallelStream().filter(Future::isDone).map(f -> Try.of(() -> f.get(1, TimeUnit.MINUTES)).get()).collect(Collectors.toList());
    }

    /**
     * Calculates result for all specified files ordered by exchange, date and ISIN.
     * Result contains info about close/max/min prices and volume per instrument traded
     *
     * @param allEvents - list of all {@link TradingEvents} objects
     * @return {@link String} - result string
     */
    public static String calculateResultString(List<TradingEvents> allEvents) {
        List<Callable<List<String>>> resultsData = new ArrayList<>();
        for (String exchange : Arrays.stream(Support.MARKETS).sorted().collect(Collectors.toList())) {
            LocalDate cd = Support.getStartDateLD();
            while (!cd.isAfter(Support.getEndDateLD())) {
                String fileDate = cd.format(Support.DATE_FORMAT);
                List<TradingEvents> ev = new ArrayList<>(allEvents).stream()
                        .filter(oneDayEvents -> oneDayEvents.getFileName().contains(exchange + "-" + fileDate))
                        .collect(Collectors.toList());
                if (!ev.isEmpty()) {
                    ev.parallelStream().forEach(e -> e.setExchange(exchange));
                    resultsData.add(new CalcTradingsResult(ev));
                }
                cd = cd.plusDays(1);
            }
        }
        resultsData.size();
        List<Future<List<String>>> calcResultsFutures = Try.of(() -> executor.invokeAll(resultsData)).get();
        List<List<String>> resultStrings = calcResultsFutures
                .stream()
                .filter(Future::isDone)
                .map(f -> Try.of(() -> f.get(1, TimeUnit.MINUTES))
                        .get())
                .collect(Collectors.toList());
        return resultStrings.stream().map(list -> list.stream().collect(Collectors.joining(System.lineSeparator()))).collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Write result string into output
     *
     * @param result - result string
     */
    public static void writeResult(String result) {
        File file = new File(Support.getOutputPath());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(result);
            bw.newLine();
        } catch (IOException e) {
            // should probably log some errors here
        }
    }

    /**
     * Parse data from a specified file and store it in TradingEvents object
     *
     * @param file - file to parse
     * @return {@link TradingEvents} - Object that stores parsed data of tradings
     */
    private static TradingEvents parseEventsFromFile(File file) {
        TradingEvents events;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
             events = new TradingEvents(br.lines().map(TradingEvent::new).collect(Collectors.toList()));
             events.setFileName(file.getName());
             return events;
        } catch (IOException e) {
            // should probably log some errors here
        }
        return null;
    }

    /**
     * Inner Callable class used to parse data from a single csv file
     */
    private static class TradingFileParser implements Callable<TradingEvents> {
        File file;

        public TradingFileParser(File file) {
            this.file = file;
        }

        @Override
        public TradingEvents call() throws Exception {
            return parseEventsFromFile(file);
        }
    }

    /**
     * Inner Callable class used to calculate result data for one trading day (close/max/min prices and volume)
     */
    private static class CalcTradingsResult implements Callable<List<String>> {
        TradingEvents events;

        public CalcTradingsResult(List<TradingEvents> events) {
            this.events = mergeOneDayResults(events);
        }

        @Override
        public List<String> call() throws Exception {
            return events.getTradedInstruments().stream().sorted().parallel().map(i -> events.calculateResult(i)).collect(Collectors.toList());
        }
    }

    /**
     * Merge results if there are more than one file with operations for one day.
     * If there is just one file, return it as is.
     *
     * @param input - List of trading events for a single day
     * @return {@link TradingEvents} - Merged object with all trading events for a single day
     */
    private static TradingEvents mergeOneDayResults(List<TradingEvents> input) {
        if (input.size() == 1) {
            return input.get(0);
        } else {
            TradingEvents mainEventsFile = input.remove(0);
            mainEventsFile.mergeResults(input);
            return mainEventsFile;
        }
    }
}
