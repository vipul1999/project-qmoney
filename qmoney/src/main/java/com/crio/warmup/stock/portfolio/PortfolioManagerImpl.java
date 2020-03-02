package com.crio.warmup.stock.portfolio;

// import static java.time.temporal.ChronoUnit.DAYS;
// import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.Period;
// import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
// import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
// import java.util.concurrent.ExecutionException;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.concurrent.Future;
// import java.util.concurrent.TimeUnit;
// import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  public List<AnnualizedReturn> calculateAnnualizedReturn(
      List<PortfolioTrade> portfolioTrades, LocalDate endDate)
        throws JsonMappingException, JsonProcessingException {
    int i = 0;
    int n = portfolioTrades.size();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    List<AnnualizedReturn> returnable = new ArrayList<AnnualizedReturn>();
    while (i < n) {
      PortfolioTrade obj = portfolioTrades.get(i);
      // String url = "https://api.tiingo.com/tiingo/daily/" +
      // obj.getSymbol().toLowerCase() + "/prices?"
      // + "startDate=" + obj.getPurchaseDate().toString() + "&endDate=" +
      // endDate.toString()
      // + "&token=9dfe04619d407e795dcb35f2046ed98d26e04563";
      // String result = restTemplate.getForObject(url,String.class);
      // List<TiingoCandle> collection = objectMapper.readValue(result,
      // new TypeReference<ArrayList<TiingoCandle>>(){});

      List<Candle> collection = getStockQuote(obj.getSymbol(), obj.getPurchaseDate(), endDate);
      double buyValue = collection.get(0).getOpen();

      Double sellValue = collection.get(collection.size() - 1).getClose();
      returnable.add(calculateAnnualizedReturns(endDate, obj, buyValue, sellValue));
      i++;
    }
    Collections.sort(returnable, 
        Comparator.comparingDouble(AnnualizedReturn::getAnnualizedReturn).reversed());
    return returnable;
  }

  public static AnnualizedReturn calculateAnnualizedReturns(
      LocalDate endDate, PortfolioTrade trade, Double buyPrice,
        Double sellPrice) {

    double buyValue = buyPrice;

    double sellValue = sellPrice;

    double totalReturn = (sellValue - buyValue) / buyValue;

    LocalDate startDate = trade.getPurchaseDate();

    Period diff = Period.between(startDate, endDate);
    int totalNumYears = diff.getYears();
    int totalNumMonths = diff.getMonths();
    int totalNumDays = diff.getDays();
    float years = (float) totalNumYears 
        + ((float) totalNumMonths) / 12 + ((float) totalNumDays) / 365;

    double annualizedReturn = Math.pow(1 + totalReturn, 1 / years) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);
  }

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // Now we want to convert our code into a module, so we will not call it from main anymore.
  // Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and make sure that it
  // follows the method signature.
  // Logic to read Json file and convert them into Objects will not be required further as our
  // clients will take care of it, going forward.
  // Test your code using Junits provided.
  // Make sure that all of the tests inside PortfolioManagerTest using command below -
  // ./gradlew test --tests PortfolioManagerTest
  // This will guard you against any regressions.
  // run ./gradlew build in order to test yout code, and make sure that
  // the tests and static code quality pass.

  //CHECKSTYLE:OFF






  // private Comparator<AnnualizedReturn> getComparator() {
  //   return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  // }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo thirdparty APIs to a separate function.
  //  It should be split into fto parts.
  //  Part#1 - Prepare the Url to call Tiingo based on a template constant,
  //  by replacing the placeholders.
  //  Constant should look like
  //  https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=?&endDate=?&token=?
  //  Where ? are replaced with something similar to <ticker> and then actual url produced by
  //  replacing the placeholders with actual parameters.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    
    ObjectMapper mapper = new ObjectMapper();
    // mapper.registerModule(new JavaTimeModule());
  
    String url = buildUri(symbol, from, to);

    RestTemplate restTemplate = new RestTemplate();
    
    String result = restTemplate.getForObject(url, String.class);
    List<Candle> collection = mapper.readValue(result, 
        new TypeReference<ArrayList<Candle>>(){}); 
    

    return collection;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {

    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol.toLowerCase() + "/prices?"
        + "startDate=" + startDate.toString() + "&endDate=" + endDate.toString() 
          + "&token=9dfe04619d407e795dcb35f2046ed98d26e04563";
    return uriTemplate;
        
  }
}
