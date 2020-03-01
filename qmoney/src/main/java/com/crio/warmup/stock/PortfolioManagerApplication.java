
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;



public class PortfolioManagerApplication {




  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    
    try { 
      ObjectMapper mapper = getObjectMapper();
      PortfolioTrade[] obj = mapper.readValue(
        resolveFileFromResources(args[0]), PortfolioTrade[].class);
      List<String> mylist = new ArrayList<String>();
      int number = obj.length;
      int i = 0; 
      while (i < number) {
        mylist.add(obj[i].getSymbol());
        i++;
      }
      return mylist; 
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
  
  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }


  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Copy the relevant code from #mainReadQuotes 
  //to parse the Json into PortfolioTrade list and
  //  Get the latest quotes from TIingo.
  //  Now That you have the list of PortfolioTrade And their data,
  //  With this data, Calculate annualized returns for the 
  //stocks provided in the Json
  //  Below are the values to be considered for calculations.
  //  buy_price = open_price on purchase_date and sell_value 
  //= close_price on end_date
  //  startDate and endDate are already calculated in module2
  //  using the function you just wrote #calculateAnnualizedReturns
  //  Return the list of AnnualizedReturns sorted by 
  //annualizedReturns in descending order.
  //  use gralde command like below to test your code
  //  ./gradlew run --args="trades.json 2020-01-01"
  //  ./gradlew run --args="trades.json 2019-07-01"
  //  ./gradlew run --args="trades.json 2019-12-03"
  //  where trades.json is your json file

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException {
    ObjectMapper mapper = getObjectMapper();
    PortfolioTrade[] obj = mapper.readValue(
      resolveFileFromResources(args[0]), PortfolioTrade[].class);
      
    int i = 0; 
    
    RestTemplate restTemplate = new RestTemplate();
     
    List<AnnualizedReturn> returnable = new ArrayList<AnnualizedReturn>();
    LocalDate endDate = LocalDate.now();
    if (!args[1].isEmpty()) {
      endDate = LocalDate.parse(args[1]);
    }

    while (i < obj.length) {
      String url =  "https://api.tiingo.com/tiingo/daily/" + obj[i].getSymbol() + "/prices?startDate="
          + obj[i].getPurchaseDate().toString() + "&endDate=" + endDate.toString() 
            + "&token=9dfe04619d407e795dcb35f2046ed98d26e04563";            
      String result = restTemplate.getForObject(url,String.class);     
      List<TiingoCandle> collection = mapper.readValue(result,
          new TypeReference<ArrayList<TiingoCandle>>(){});
      double buyValue = collection.get(0).getOpen();
      Double sellValue = collection.get(collection.size() - 1).getClose();
      returnable.add(calculateAnnualizedReturns(endDate, obj[i], buyValue, sellValue));
      i++;
    }
    Collections.sort(returnable, Comparator.comparingDouble(AnnualizedReturn::getAnnualizedReturn)
          .reversed());
    return returnable; 
  }
 
  
  // int totaldays = totalNumDays + totalNumMonths * 31 + totalNumYears * 365;
  // float years = (float)totaldays / 365;
  // double annualizedReturn = Math.pow(1 + totalReturn,1 / (double)years) - 1;  
  // returnable.add(calculateAnnualizedReturns(endDate, obj[i], buyValue, sellValue));

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  annualized returns should be calculated in two steps -
  //  1. Calculate totalReturn = (sell_value - buy_value) / buy_value
  //  Store the same as totalReturns
  //  2. calculate extrapolated annualized returns by scaling the same in years span. The formula is
  //  annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //  Store the same as annualized_returns
  //  return the populated list of AnnualizedReturn for all stocks,
  //  Test the same using below specified command. The build should be successful
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        

    double buyValue =  buyPrice;
            
    double sellValue =  sellPrice;
        
    double totalReturn = (sellValue - buyValue) / buyValue;
        
    LocalDate startDate = trade.getPurchaseDate();
  
    Period diff = Period.between(startDate, endDate);
    int totalNumYears = diff.getYears();
    int totalNumMonths = diff.getMonths();
    int totalNumDays = diff.getDays();
    float years = (float)totalNumYears 
          + ((float)totalNumMonths) / 12 + ((float)totalNumDays) / 365;
          
    double annualizedReturn = Math.pow(1 + totalReturn,1 / years) - 1;
    // double years = (double)totalNumYears 
    //     + ((double)totalNumMonths) / 12 + ((double)totalNumDays) / 365;
    // int totaldays = totalNumDays + totalNumMonths * 31 + totalNumYears * 365;
    // float years = (float)totaldays / 365;

    // double annualizedReturn = Math.pow(1 + totalReturn,1 / (double)years) - 1;
          
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);
          

  }

  public static List<String> mainReadQuotes(String[] args) 
      throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException {
    ObjectMapper mapper = getObjectMapper();
    PortfolioTrade[] obj = mapper.readValue(
        resolveFileFromResources(args[0]), PortfolioTrade[].class);
      
    int i = 0; 
    
    RestTemplate restTemplate1 = new RestTemplate();
    TreeMap<Double, String> map 
            = new TreeMap<Double, String>();
    while (i < obj.length) {
      String url =  "https://api.tiingo.com/tiingo/daily/" + obj[i].getSymbol() + "/prices?startDate="
          + obj[i].getPurchaseDate().toString() + "&endDate=" + args[1] 
            + "&token=9dfe04619d407e795dcb35f2046ed98d26e04563";
      String result = restTemplate1.getForObject(url,String.class);     
      List<TiingoCandle> collection = mapper.readValue(result,
          new TypeReference<ArrayList<TiingoCandle>>(){});
      map.put(collection.get(collection.size() - 1).getClose(),obj[i].getSymbol());
      i++;
    }

    List<String> myList = new ArrayList<String>(map.values());
    return myList;
    
  
  }

  public static List<String> debugOutputs() {
    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "trades.json";
    String toStringOfObjectMapper = getObjectMapper().toString();
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "126";
  

    
    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }
  
  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }



















  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());



    printJsonObject(mainCalculateSingleReturn(args));

  }

}

 

