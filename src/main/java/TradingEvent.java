import java.time.LocalTime;

/**
 * Created by Maksim Nikelman on 26.11.17.
 */

/**
 * A class for storing information about one single trading operation
 */
public class TradingEvent {
    String instrument;
    String time;
    LocalTime timeLT;
    double price;
    int quantity;
    String csvRowValue;

    /**
     * A constructor for creating {@link TradingEvent} from csv line
     * @param csvRowValue
     */
    public TradingEvent(String csvRowValue) {
        this.csvRowValue = csvRowValue;
        parseCsvValue();
    }

    /**
     * Parse trading information from one csv-line line
     */
    private void parseCsvValue() {
        String[] data = csvRowValue.split(",");
        instrument = data[0].trim();
        time = data[1].trim();
        price = Double.valueOf(data[2].trim());
        quantity = Integer.valueOf(data[3].trim());
    }

    /**
     * Creates csv-like line from trading event data
     *
     * @return a {@link String} that could be used to write data about trading operation into csv file
     */
    @Override
    public String toString() {
        return String.format("%s, %s, %.2f, %d",
                instrument,
                time,
                price,
                quantity);
    }


    public LocalTime getTimeLT() {
        if (timeLT == null)
            timeLT = LocalTime.parse(time, Support.TIME_FORMAT);
        return timeLT;
    }

    /**
     * Some setters and getters
     */
    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }
    public String getInstrument() {
        return instrument;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getTime() {
        return time;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public double getPrice() {
        return price;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public int getQuantity() {
        return quantity;
    }
}
