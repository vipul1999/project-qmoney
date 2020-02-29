
package com.crio.warmup.stock.dto;

public class AnnualizedReturn {

  private final String symbol;
  private final Double annualReturn;
  private final Double totalReturns;

  public AnnualizedReturn(String symbol, Double annualizedReturn, Double totalReturns) {
    this.symbol = symbol;
    this.annualReturn = annualizedReturn;
    this.totalReturns = totalReturns;
  }

  public String getSymbol() {
    return symbol;
  }

  public Double getAnnualizedReturn() {
    return annualReturn;
  }

  public Double getTotalReturns() {
    return totalReturns;
  }
}
