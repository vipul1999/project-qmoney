
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {
  private static List collection1;

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








  // TODO: CRIO_TASK_MODULE_REST_API
  //  Copy the relavent code from #mainReadFile to parse the Json into PortfolioTrade list.
  //  Now That you have the list of PortfolioTrade already populated in module#1
  //  For each stock symbol in the portfolio trades,
  //  Call Tiingo api (https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=&endDate=&token=)
  //  with
  //   1. ticker = symbol in portfolio_trade
  //   2. startDate = purchaseDate in portfolio_trade.
  //   3. endDate = args[1]
  //  Use RestTemplate#getForObject in order to call the API,
  //  and deserialize the results in List<Candle>
  //  Note - You may have to register on Tiingo to get the api_token.
  //    Please refer the the module documentation for the steps.
  //  Find out the closing price of the stock on the end_date and
  //  return the list of all symbols in ascending order by its close value on endDate
  //  Test the function using gradle commands below
  //   ./gradlew run --args="trades.json 2020-01-01"
  //   ./gradlew run --args="trades.json 2019-07-01"
  //   ./gradlew run --args="trades.json 2019-12-03"
  //  And make sure that its printing correct results.
  public static void fillHash(Map<Double, String> hmap, Double stockPrice, String stockName) {
    hmap.put(stockPrice, stockName);
  }

  public static List<Double> getArray(JsonNode node, String endDate, 
      Map<Double, String> hmap) throws IOException, URISyntaxException {
    ObjectMapper mapper = getObjectMapper();
    List<Double> priceInDouble = new ArrayList<Double>();
    for (int i = 0; i < node.size(); i++) {
      String stockName = node.get(i).get("symbol").asText().toLowerCase();
      String startDate = node.get(i).get("purchaseDate").asText();
      String result = getApiResult(stockName, endDate, startDate);
      JsonNode node2 = mapper.readTree(result);
      priceInDouble.add(i, node2.get(node2.size() - 1).get("close").asDouble());
      fillHash(hmap, node2.get(node2.size() - 1).get("close").asDouble(), stockName.toUpperCase());
    }
    return priceInDouble;
  }

  public static String getApiResult(String stockName, String endDate, String startDate) 
      throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
    final String uri = "https://api.tiingo.com/tiingo/daily/" + stockName + "/prices?startDate=" 
        + startDate + "&endDate=" + endDate + "&token=65dc52f9de296af4b9735257386549ad4dd1fbaa";
    String result = restTemplate.getForObject(uri, String.class);
    return result;
  }

  public static List<Double> getStockPrice(File file, String endDate, Map<Double, String> hmap)
      throws IOException, URISyntaxException {
    ObjectMapper mapper = getObjectMapper();
    JsonNode node = mapper.readTree(file);
    List<Double> priceInDouble = getArray(node, endDate, hmap);
    Collections.sort(priceInDouble);
    return priceInDouble;
  }

  public static List<Double> getStockPriceUnsorted(File file, String endDate, 
      Map<Double, String> hmap) throws IOException, URISyntaxException {
    ObjectMapper mapper = getObjectMapper();
    JsonNode node = mapper.readTree(file);
    List<Double> priceInDouble = getArray(node, endDate, hmap);
    //Collections.sort(priceInDouble);
    return priceInDouble;
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    List<String> stockPrice = new ArrayList<String>();
    Map<Double, String> hmap = new HashMap<Double, String>();
    List<Double> priceInDouble = getStockPrice(file, args[1], hmap);
    for (int i = 0; i < priceInDouble.size(); i++) {
      stockPrice.add(hmap.get(priceInDouble.get(i)));
    }
    return stockPrice;  
  
  }









  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    printJsonObject(mainReadQuotes(args));


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

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }
  
  public static List<String> debugOutputs() {
    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = 
        "/home/crio-user/workspace/vipul07-mathuria-ME_QMONEY/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@66ac5762";
    String functionNameFromTestFileInStackTrace = "PortfolioManagerApplication.main()";
    String lineNumberFromTestFileInStackTrace = "126";
  

    
    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }





}

