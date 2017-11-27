import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Maksim Nikelman on 26.11.17.
 */
public class TradingEvents {
    private String fileName;
    private String exchange;
    private List<TradingEvent> events;
    private Set<String> tradedInstruments;

    /** Comparator to sort events by time*/
    private static Comparator<TradingEvent> sortByTime = (te1, te2) -> {
        if (te1.getTimeLT().isBefore(te2.getTimeLT()))
            return -1;
        if (te1.getTimeLT().isAfter(te2.getTimeLT()))
            return 1;
        return 0;
    };

    /** Comparator to sort events by descending price*/
    private static Comparator<TradingEvent> sortByPriceDesc = (te1, te2) -> {
        if (te1.getPrice() > te2.getPrice())
            return -1;
        if (te1.getPrice() < te2.getPrice())
            return 1;
        return 0;
    };

    /** Comparator to sort events by ascending price */
    private static Comparator<TradingEvent> sortByPriceAsc = (te1, te2) -> {
        if (te1.getPrice() < te2.getPrice())
            return -1;
        if (te1.getPrice() > te2.getPrice())
            return 1;
        return 0;
    };

    /**
     * Merges results of other events. For example from subfiles into current {@link TradingEvents} object
     *
     * @param events - a list of other trading events objects
     * @return merged {@link TradingEvents} object
     */
    public TradingEvents mergeResults(List<TradingEvents> events) {
        for (TradingEvents ev : events) {
            this.events.addAll(ev.getEvents());
        }
        return this;
    }

    public TradingEvents(List<TradingEvent> events) {
        this.events = events;
    }

    /**
     * Get close market price for specified instrument
     *
     * @param instrument - instrument
     * @return last {@link TradingEvent} of the day for specified instrument.
     */
    public TradingEvent getCloseMarketEvent(String instrument) {
        return events.stream()
                .filter(e -> e.getInstrument().equals(instrument))
                .sorted(sortByTime).findFirst().get();
    }

    /**
     * Get mid market price for specified instrument
     *
     * @param instrument - instrument
     * @return min {@link TradingEvent} of the day for specified instrument.
     */
    public TradingEvent getMinPrice(String instrument) {
        return events.stream()
                .filter(e -> e.getInstrument().equals(instrument))
                .min(sortByPriceAsc).get();
    }

    /**
     * Get max market price for specified instrument
     *
     * @param instrument - instrument
     * @return max {@link TradingEvent} of the day for specified instrument.
     */
    public TradingEvent getMaxPrice(String instrument) {
        return events.stream()
                .filter(e -> e.getInstrument().equals(instrument))
                .min(sortByPriceDesc).get(); // could replace it with .max(sortByPriceAsc)
    }

    /**
     * Get a total amount (volume) of specified instrument traded
     *
     * @param instrument - instrument
     * @return a {@link BigInteger} value
     */
    public BigInteger getVolume(String instrument) {
        return events.stream()
                .filter(e -> e.getInstrument().equals(instrument))
                .map(te -> BigInteger.valueOf(te.getQuantity()))
                .reduce(BigInteger.ZERO, BigInteger::add);
    }

    /**
     * Gets the result string for scv output for specified instrument.
     * String consists of exchange name, date of tradings, instrument, close price, max price, min price and volume
     *
     * @param instrument
     * @return a result {@link String} that is used to write info to output csv file
     */
    public String calculateResult(String instrument) {
        return String.format("%s, %s, %s, %.2f, %.2f, %.2f, %s",
                exchange,
                getDate(),
                instrument,
                getCloseMarketEvent(instrument).getPrice(),
                getMaxPrice(instrument).getPrice(),
                getMinPrice(instrument).getPrice(),
                getVolume(instrument).toString());
    }

    /**
     * Get a set of traded instruments for the day
     *
     * @return a {@link Set} of instruments
     */
    public Set<String> getTradedInstruments() {
        if (this.tradedInstruments == null)
            this.tradedInstruments = new HashSet<>(events.stream().map(TradingEvent::getInstrument).collect(Collectors.toSet()));
        return tradedInstruments;
    }

    /**
     *  Some setters and getters
     */
    private String getDate() {
        return Support.getDateFromFileName(fileName);
    }
    public List<TradingEvent> getEvents() {
        return events;
    }
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
    public String getExchange() {
        return exchange;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileName() {
        return fileName;
    }

}
