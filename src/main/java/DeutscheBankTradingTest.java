import org.junit.Assert;

import java.util.List;

/**
 * Created by Maksim Nikelman on 26.11.17.
 */
public class DeutscheBankTradingTest {
    public static void main(String[] args) {
        Assert.assertEquals("Incorrect input args. ", 4, args.length);
        Support.setInputDir(args[0]);
        Assert.assertTrue(Support.checkInputDirectory());
        Support.setOutpudDir(args[1]);
        Assert.assertTrue(Support.checkOutputDirectory());
        Support.setStartDate(args[2]);
        Support.setEndDate(args[3]);

//        Support.setInputDir("F:\\DeutscheBankTradingTest\\input\\");
//        Assert.assertTrue(Support.checkInputDirectory());
//        Support.setOutpudDir("F:\\DeutscheBankTradingTest\\output\\output.csv");
//        Assert.assertTrue(Support.checkOutputDirectory());
//        Support.setStartDate("2017-11-25");
//        Support.setEndDate("2017-11-28");

        long l = System.currentTimeMillis();

        List<String> generatedData = TradingGenerator.generate();
//        generatedData.forEach(System.out::println);
        System.out.println("It took "+(System.currentTimeMillis() - l) + " ms to generate all input files.");

        l = System.currentTimeMillis();
        List<TradingEvents> allEvents = TradingParser.parseCsvData();
        System.out.println("It took "+(System.currentTimeMillis() - l) + " ms to parse all input files.");

        l = System.currentTimeMillis();
        String result = TradingParser.calculateResultString(allEvents);
        TradingParser.writeResult(result);
        System.out.println("It took "+(System.currentTimeMillis() - l) + " ms to calculate result and write it.");
    }
}
