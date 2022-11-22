package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {
  public static RestTemplate restTemplate=new RestTemplate();
  public static PortfolioManager portfolioManager= PortfolioManagerFactory.getPortfolioManager(restTemplate);


   public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

      File file=resolveFileFromResources(args[0]);
      ObjectMapper objectMapper= getObjectMapper();
      PortfolioTrade[] trades=objectMapper.readValue(file, PortfolioTrade[].class);
     List<String> symbols=new ArrayList<String>();
      for(PortfolioTrade t:trades){
       symbols.add(t.getSymbol());
        System.out.println(t.toString());
      }
  
       return symbols;
    }
  
  
    private static void printJsonObject(Object object) throws IOException {
      Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
      ObjectMapper mapper = new ObjectMapper();
      logger.info(mapper.writeValueAsString(object));
    }
  
    private static File resolveFileFromResources(String filename) throws URISyntaxException {
      return Paths.get(
          Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
    }
  
    private static ObjectMapper getObjectMapper() {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      return objectMapper;
    }
  
    public static List<String> debugOutputs() {
  
       String valueOfArgument0 = "trades.json";
       String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/shivam29061998-ME_QMONEY_V2/qmoney/bin/main/trades.json";
       String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@7c6908d7";
       String functionNameFromTestFileInStackTrace = "PortfolioManagerApplication.mainReadFile(String[])";
       String lineNumberFromTestFileInStackTrace = "141";
  
  
      return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
          toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
          lineNumberFromTestFileInStackTrace});
    }
  
  
    // Note:
    // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  
    public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
  
      File f = resolveFileFromResources(args[0]);
      ObjectMapper om = getObjectMapper();
      PortfolioTrade[] trades = om.readValue(f, PortfolioTrade[].class);
      RestTemplate rt = new RestTemplate();
      List<TotalReturnsDto> ls = new ArrayList<TotalReturnsDto>();
      for(PortfolioTrade pf:trades)
      {
        //  LocalDate start = pf.getPurchaseDate();
         String sym = pf.getSymbol();
         LocalDate localDate = LocalDate.parse(args[1]);
         String Url = prepareUrl(pf,localDate,getToken());
         TiingoCandle[] tc = rt.getForObject( Url, TiingoCandle[].class);
         if(tc==null)
         {                 
           continue;
         }           // candle helper object to sort symbols according to their current prices -> 
         TotalReturnsDto temp = new TotalReturnsDto(sym,tc[tc.length-1].getClose());
         ls.add(temp);
      }
      Collections.sort(ls, new Comparator<TotalReturnsDto>() {
         @Override
         public int compare(TotalReturnsDto p1, TotalReturnsDto p2) {
             return (int)(p1.getClosingPrice().compareTo(p2.getClosingPrice()));
         }
     });
     List<String> ans = new ArrayList<>();
      for(int i=0;i<ls.size();i++)
      {
         ans.add(ls.get(i).getSymbol());
      }
      return ans;   
     }
  
  
    
  
    // TODO:
    //  Build the Url using given parameters and use this function in your code to cann the API.
    public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
      return  "https://api.tiingo.com/tiingo/daily/"+ trade.getSymbol() +"/prices?startDate="+ 
      trade.getPurchaseDate() +"&endDate="+ endDate +"&token=" + token;
    }


  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest

  //return the opening price(using getOpen function) at the start date(date at the first index in the candles list)
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {  
    Double openPrice=  (candles.get(0)).getOpen();
     return openPrice;
  }

  //return the closing price(using getClose function) at the end date(date at the last index in the candles list)
  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    Double closePrice=  (candles.get(candles.size()-1)).getClose();
    return closePrice;
  }

  // create an object of RestTemplate class
  // get a url using prepareUrl function (pass startDate, endDate and portFolioTrade type object in it)
  // take a TingoCandle type of array and call the getForObject method using object of RestTemplate. (Pass url and TingoCandle.class in it)
  // return the array as list using streams 
  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {

    RestTemplate rt = new RestTemplate();
    LocalDate purchaseDate =trade.getPurchaseDate();
    
     if(purchaseDate.compareTo(endDate)>=0)
     {
       throw new RuntimeException();
     }
        String Url = prepareUrl(trade,endDate,getToken());
        TiingoCandle[] tc = rt.getForObject( Url, TiingoCandle[].class);
        if(tc!=null){
        List<Candle> list = Arrays.asList(tc);
        return list;
        }
        else
        return Collections.emptyList();
        
  }

  public static String getToken()
  {
    return "e3e6d50fa6a2691870a2aabe40042a042da81758";
  }



  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

        LocalDate endDate=LocalDate.parse(args[1]);
        File file= resolveFileFromResources(args[0]);
        List<AnnualizedReturn> annualizedReturns=new ArrayList<>();

        ObjectMapper objectMapper=getObjectMapper();

        PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);

        for(PortfolioTrade pf:trades)
        {
          annualizedReturns.add(getAnnualizedReturn(pf,endDate,getToken()));
        }

        Collections.sort(annualizedReturns, new Comparator<AnnualizedReturn>() {
          @Override
          public int compare(AnnualizedReturn p1, AnnualizedReturn p2) {
              return (int)(p2.getAnnualizedReturn().compareTo(p1.getAnnualizedReturn()));
          }
      });

        
     return annualizedReturns;
  }

  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  private static AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate,
      String token) {

        // RestTemplate rt = new RestTemplate();
         LocalDate purchaseDate =trade.getPurchaseDate();

        if(purchaseDate.compareTo(endDate)>=0)
        {
          throw new RuntimeException();
        }
        List<Candle> candles=fetchCandles(trade, endDate, token);
            String sym = trade.getSymbol();
          //  String Url = prepareUrl(trade,endDate,getToken());
          //  TiingoCandle[] tc = rt.getForObject( Url, TiingoCandle[].class);
            if(!candles.isEmpty())
            {    
            // TiingoCandle stockStartDate= tc[0];
            // TiingoCandle stockEndDate=tc[tc.length-1];  
            
            // Double buyPrice= stockStartDate.getOpen();
            // Double sellPrice=stockEndDate.getClose();

            Double buyPrice= getOpeningPriceOnStartDate(candles);
            Double sellPrice=getClosingPriceOnEndDate(candles);
            AnnualizedReturn annualizedReturn=calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
            return annualizedReturn;
            }     
           else{
            return new AnnualizedReturn(sym,Double.NaN,Double.NaN);
           }
        
  }


  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {

        Double absReturn=(sellPrice-buyPrice)/buyPrice;
        String symbol=trade.getSymbol();

        LocalDate purchaseDate= trade.getPurchaseDate();

        Double numYears= (double)ChronoUnit.DAYS.between(purchaseDate,endDate)/365;

        Double annularizedReturn=Math.pow((1+absReturn),(1/numYears))-1;

      return new AnnualizedReturn(symbol,  annularizedReturn, absReturn);
  }


  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  private static String readFileAsString(String file) throws IOException, URISyntaxException {
    //ObjectMapper om = getObjectMapper();
    return new String(Files.readAllBytes(resolveFileFromResources(file).toPath()));
  }

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       

       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades=objectMapper.readValue(contents, PortfolioTrade[].class);
      return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
      }
  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile   ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File file=resolveFileFromResources(filename);
    ObjectMapper objectMapper= getObjectMapper();
    PortfolioTrade[] trades=objectMapper.readValue(file, PortfolioTrade[].class);
   List<PortfolioTrade> list=new ArrayList<PortfolioTrade>();
    for(PortfolioTrade t:trades){
    list.add(t);
    }
     return list;
   //  return Collections.emptyList();
  }

 
  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}



