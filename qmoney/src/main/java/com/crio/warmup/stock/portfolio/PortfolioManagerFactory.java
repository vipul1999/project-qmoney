
package com.crio.warmup.stock.portfolio;

// import java.time.LocalDate;
// import java.time.Period;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.Comparator;
// import java.util.List;

// import com.crio.warmup.stock.dto.AnnualizedReturn;
// import com.crio.warmup.stock.dto.PortfolioTrade;
// import com.crio.warmup.stock.dto.TiingoCandle;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.JsonMappingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Implement the method in such a way that it will return new Instance of
  // PortfolioManager using RestTemplate provided.
  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {
    PortfolioManager p1 = new PortfolioManagerImpl(restTemplate);
    return p1;
  }


  // public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
  //     PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        

  //   double buyValue =  buyPrice;
            
  //   double sellValue =  sellPrice;
        
  //   double totalReturn = (sellValue - buyValue) / buyValue;
        
  //   LocalDate startDate = trade.getPurchaseDate();
  
  //   Period diff = Period.between(startDate, endDate);
  //   int totalNumYears = diff.getYears();
  //   int totalNumMonths = diff.getMonths();
  //   int totalNumDays = diff.getDays();
  //   float years = (float)totalNumYears 
  //         + ((float)totalNumMonths) / 12 + ((float)totalNumDays) / 365;
          
  //   double annualizedReturn = Math.pow(1 + totalReturn,1 / years) - 1;
  //   // double years = (double)totalNumYears 
  //   //     + ((double)totalNumMonths) / 12 + ((double)totalNumDays) / 365;
  //   // int totaldays = totalNumDays + totalNumMonths * 31 + totalNumYears * 365;
  //   // float years = (float)totaldays / 365;

  //   // double annualizedReturn = Math.pow(1 + totalReturn,1 / (double)years) - 1;
          
  //   return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);
  // }


}
