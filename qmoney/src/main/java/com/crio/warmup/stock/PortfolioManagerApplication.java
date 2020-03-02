
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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














  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory,
  //  Create PortfolioManager using PortfoliomanagerFactory,
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.
  //  Test the same using the same commands as you used in module 3
  //  use gralde command like below to test your code
  //  ./gradlew run --args="trades.json 2020-01-01"
  //  ./gradlew run --args="trades.json 2019-07-01"
  //  ./gradlew run --args="trades.json 2019-12-03"
  //  where trades.json is your json fileR
  //  Confirm that you are getting same results as in Module3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
    //  String file = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    //  String contents = readFileAsString(file);
    ObjectMapper objectMapper = getObjectMapper();

    PortfolioTrade[] portfolioTrade = objectMapper.readValue(
        resolveFileFromResources(args[0]), PortfolioTrade[].class);
       
    RestTemplate restTemplate = new RestTemplate();

    PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
       
    return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrade), endDate);
  }


  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  // private static String readFileAsString(String file) throws URISyntaxException, IOException {
  //   File file1 = resolveFileFromResources(file);
  //   Path path = file1.toPath();
  //   List<String> contents = Files.readAllLines(path);
  //   String content = String.join("/n",contents);
  //   return content;
  // }


 
    
  


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());




    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
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


  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }


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
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);
        
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

}

