package org.example.java.pattern.behavioral.observer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Observable;
import java.util.Observer;

//(Observable对象,也就是所股票数据发生了变化,它就要通知所有和它有关系的交易实体做相应的变化)
class StockData extends Observable {
  private String symbol;
  private float close;
  private float high;
  private float low;
  private long volume;

  public StockData() {
    super();
  }

  public String getSymbol() {
    return symbol;
  }

  public float getClose() {
    return close;
  }

  public float getHigh() {
    return high;
  }

  public float getLow() {
    return low;
  }

  public long getVolume() {
    return volume;
  }

  private void sendStockData() {
    setChanged();
    notifyObservers();
  }

  public void setStockData(String symbol, float close, float high, float low, long volume) {
    this.symbol = symbol;
    this.close = close;
    this.high = high;
    this.low = low;
    this.volume = volume;
    sendStockData();
  }
}

//(Observer对象, 实现了Observer接口)
class BigBuyer implements Observer {
  private String symbol;
  private float close;
  private float high;
  private float low;
  private long volume;

  public BigBuyer(Observable observable) {
    observable.addObserver(this); //注册关系
  }

  public void update(Observable observable, Object args) {
    if (observable instanceof StockData) {
      StockData stockData = (StockData) observable;
      this.symbol = stockData.getSymbol();
      this.close = stockData.getClose();
      this.high = stockData.getHigh();
      this.low = stockData.getLow();
      this.volume = stockData.getVolume();
      display();
    }
  }

  private void display() {
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    DecimalFormat volumeFormat = new DecimalFormat("###,###,###,###", dfs);
    DecimalFormat priceFormat = new DecimalFormat("###.00", dfs);
    System.out.println("Big Buyer reports... ");
    System.out.println("\tThe lastest stock quote for " + symbol + " is:");
    System.out.println("\t$" + priceFormat.format(close) + " per share (close).");
    System.out.println("\t$" + priceFormat.format(high) + " per share (high).");
    System.out.println("\t$" + priceFormat.format(low) + " per share (low).");
    System.out.println("\t" + volumeFormat.format(volume) + " shares traded.\n");
  }

}

//TradingFool (Observer对象, 实现了Observer接口)
class TradingFool implements Observer {
  private String symbol;
  private float close;

  public TradingFool(Observable observable) {
    observable.addObserver(this);//注册关系
  }

  public void update(Observable observable, Object args) {
    if (observable instanceof StockData) {
      StockData stockData = (StockData) observable;
      this.symbol = stockData.getSymbol();
      this.close = stockData.getClose();
      display();
    }
  }

  public void display() {
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    DecimalFormat priceFormat = new DecimalFormat("###.00", dfs);
    System.out.println("Trading Fool says... ");
    System.out.println("\t" + symbol + " is currently trading at $" + priceFormat.format(close) + " per share.\n");
  }
}

//StockQuote 测试类
public class StockQuotes {
  public static void main(String[] args) {
    System.out.println("\n-- Stock Quote Application --\n");
    StockData stockData = new StockData();
    // register observers...
    new TradingFool(stockData);
    new BigBuyer(stockData);
    // generate changes to stock data...
    stockData.setStockData("JUPM", 16.10f, 16.15f, 15.34f, (long) 481172);
    stockData.setStockData("SUNW", 4.84f, 4.90f, 4.79f, (long) 68870233);
    stockData.setStockData("MSFT", 23.17f, 23.37f, 23.05f, (long) 75091400);
  }
}