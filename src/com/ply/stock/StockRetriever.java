package com.ply.stock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.google.cloud.sql.Driver;




public class StockRetriever {
	
	
	
	@Test
	public void retrieve_EarningFromAdvfn() throws ClientProtocolException, IOException, Exception
	{
	    
		try {
	
			Class.forName("com.mysql.jdbc.Driver");
	
		} catch (ClassNotFoundException e) {
	
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
	
		}
	
		System.out.println("MySQL JDBC Driver Registered!");
		Connection dbconnection = null;
	
		try {
			dbconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
			"root", "M1ng@2011");
	
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
	
		if (dbconnection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}


		ArrayList<String> tickets = new ArrayList<String>();

		try
		{
	
			//csv file containing data
			String strFile = "/Users/minglei/Documents/StockDataInMysql/sp500.csv";
			//String strFile = "c:\\test\\nasdaq2000.csv";
			//create BufferedReader to read csv file
			BufferedReader br = new BufferedReader( new FileReader(strFile));
			String strLine = "";
			StringTokenizer st = null;
			int lineNumber = 0, tokenNumber = 0;
		
			//read comma separated file line by line
			while( (strLine = br.readLine()) != null)
			{
				if(!strLine.isEmpty()&& strLine.length()>0)
				tickets.add(strLine);
			}
	
	
		}
		catch(Exception e)
		{
			System.out.println("Exception while reading csv file: " + e);
		}
		List<String> tickers = new ArrayList<String>();
		tickers.add("GOOG"); tickers.add("JPM"); //tickers.add("AAPL"); tickers.add("GE"); tickers.add("F");
		for(String ticker: tickets)
		{	
			String str_url = "http://ih.advfn.com/p.php?pid=financials&btn=quarterly_reports&ctl00%24sb3%24tbq1=Get+Quote&as_values_IH=&ctl00%24sb3%24stb1=Search+iHub&mode=&symbol=" +ticker;
			System.out.println(str_url);
		
		    try
		    {
				Document doc = Jsoup.connect(str_url).timeout(60000).get();
			
				Elements els = doc.select("span").attr("style", "text-transform: capitalize");
			
				for(Element el: els)
				{	
					Elements tables = el.select("table");
					if(tables!=null)
					{	
						if(tables.size()>1)
						{	
							Element tb1 = tables.get(1);
							//System.out.println(tb1);
							Elements trs = tb1.select("tr");
							Element tr2 = trs.get(0);
							Elements tds2 = tr2.select("td");
							Element td2 = tds2.get(0);
							Element tb2 = td2.select("table").get(0);
							Elements tr2s = tb2.select("tr");
						
							Map<String, List<String>> aMap = new HashMap<String, List<String>> ();
						
							for(Element tr: tr2s)
							{
								List<String> alist = new ArrayList<String>();
								Elements tds = tr.select("td");
								int i =0;
								for(Element td: tds)
								{
									if(td.hasAttr("colspan"))
									{
										continue;
								
									}
									else
									{	
										if(i==0)
										{	
											aMap.put(td.text(), alist);
										}
										if(i>0)
										{
											alist.add(td.text());
										}
									}	
							
									System.out.println(td.text()+ " i=" +i);
									i++;		
								}
							}
							System.out.println(aMap);
						
							List<String> timeperiods = new ArrayList<String>();
							List<String> totalrevenes = new ArrayList<String>();
							List<String> grossmagins = new ArrayList<String>();
							List<String> basicEPSs = new ArrayList<String>();
							List<String> depreciates = new ArrayList<String>();
						
							List<String> cashflows = new ArrayList<String>();
						
							List<String> peToIndustrys = new ArrayList<String>();
							List<String> salesToIndustrys = new ArrayList<String>();
						
							List<String> earningToIndustrys = new ArrayList<String>();
							List<String> grossmarginToIndustrys = new ArrayList<String>();
							List<String> pricecashflowToIndustrys = new ArrayList<String>();
							List<String> roeToIndustrys = new ArrayList<String>();
							for(String key: aMap.keySet())
							{	
								if("date preliminary data loaded".equalsIgnoreCase(key)){
									timeperiods = aMap.get(key);
								}
								if("total revenue".equalsIgnoreCase(key)){
									totalrevenes = aMap.get(key);
								}if("gross margin".equalsIgnoreCase(key)){
									grossmagins = aMap.get(key);
								}if("Depreciation".equalsIgnoreCase(key)){
									depreciates = aMap.get(key);
								}if("Basic EPS - Total".equalsIgnoreCase(key)){
									basicEPSs = aMap.get(key);
								}if("Cash Flow".equalsIgnoreCase(key)){
									cashflows = aMap.get(key);
								}if("% Of PE-to-Industry".equalsIgnoreCase(key)){
									peToIndustrys = aMap.get(key);
								}if("% Of Sales-to-Industry".equalsIgnoreCase(key)){
									salesToIndustrys = aMap.get(key);
								}if("% Of Earnings-to-Industry".equalsIgnoreCase(key)){
									earningToIndustrys = aMap.get(key);
								}if("% Of Gross Profit Margin-to-Industry".equalsIgnoreCase(key)){
									grossmarginToIndustrys = aMap.get(key);
								}if("% Of Price/Cashflow-to-Industry".equalsIgnoreCase(key)){
									pricecashflowToIndustrys = aMap.get(key);
								}if("% of ROE-to-Industry".equalsIgnoreCase(key)){
									roeToIndustrys = aMap.get(key);
								}
							}	
							for(int k=0; k<5; k++)
							{	
							
								String insertsql = "INSERT INTO stock.hubreport (ticker,timeperiod,totalrevenue,grossmagin,depreciation,basicEPS, cashflow, peToIndustry, salsToIndustry, earningToIndustry, grossmaginToIndustry, pricecashflowToIndustry,roeToIndustry) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
								PreparedStatement s = dbconnection.prepareStatement(insertsql);
								s.setString(1,ticker);
								if(timeperiods.size()>0)
								{
									s.setString(2,timeperiods.get(k));  
									System.out.println(timeperiods.get(k));
								}
								else
								{
									s.setString(2,""); 
								}
								if(totalrevenes.size()>0)
								{	
									s.setString(3,totalrevenes.get(k));
									System.out.println(totalrevenes.get(k));
								}
								else
								{
									s.setString(3,"");
								}
								if(grossmagins.size()>0)
								{
									s.setString(4,grossmagins.get(k));
								}
								else
								{
									s.setString(4,"");
								}
								if(depreciates.size()>0)
								{	
									s.setString(5,depreciates.get(k));
								}
								else
								{
									s.setString(5,"");
								}
								if(basicEPSs.size()>0)
								{
									s.setString(6,basicEPSs.get(k));
								}
								else
								{
									s.setString(6,"");
								}
								if(cashflows.size()>0)
								{				
									s.setString(7,cashflows.get(k));
								}	
								else
								{
									s.setString(7,"");
								}
								if(peToIndustrys.size()>0)
								{	
									s.setString(8,peToIndustrys.get(k));
								}
								else
								{
									s.setString(8,"");
								}
								if(salesToIndustrys.size()>0)
								{	
									s.setString(9,salesToIndustrys.get(k));
								}
								else
								{
									s.setString(9,"");
								}
								if(earningToIndustrys.size()>0)
								{	
									s.setString(10,earningToIndustrys.get(k));
								}
								else
								{
									s.setString(10,"");
								}
								if(grossmarginToIndustrys.size()>0)
								{	
									s.setString(11,grossmarginToIndustrys.get(k));
								}
								else
								{
									s.setString(11,"");
								}
								if(pricecashflowToIndustrys.size()>0)
								{	
									s.setString(12,pricecashflowToIndustrys.get(k));
								}
								else
								{
									s.setString(12,"");
								}
								if(roeToIndustrys.size()>0)
								{	
									s.setString(13,roeToIndustrys.get(k));
								}
								else
								{
									s.setString(13,"");
								}
								s.execute();
								s.close();
						
							}	
						}
					}	
			
				}
		    }
		    catch(HttpStatusException ste)
		    {
		    	System.out.println("ticker http error");
		    }
		}

	dbconnection.close();
	}	

	@Test
	public void retrieve_EarningSupriseQuaFromAdvfn() throws ClientProtocolException, IOException, Exception
    {
		try {
			 
			Class.forName("com.mysql.jdbc.Driver");

		} catch (ClassNotFoundException e) {

			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;

		}

		System.out.println("MySQL JDBC Driver Registered!");
		Connection dbconnection = null;

		try {
			dbconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
							"root", "M1ng@2011");

		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}

		if (dbconnection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		
		
		ArrayList<String> tickets = new ArrayList<String>();
		
		 try
	     {
	            
	             //csv file containing data
	            String strFile = "/Users/minglei/Documents/StockDataInMysql/sp500.csv";
			 	//String strFile = "c:\\test\\nasdaq2000.csv";
	             //create BufferedReader to read csv file
	             BufferedReader br = new BufferedReader( new FileReader(strFile));
	             String strLine = "";
	             StringTokenizer st = null;
	             int lineNumber = 0, tokenNumber = 0;
	            
	             //read comma separated file line by line
	             while( (strLine = br.readLine()) != null)
	             {
	            	 if(!strLine.isEmpty()&& strLine.length()>0)
	            	 tickets.add(strLine);
	             }
	            
	            
	     }
	     catch(Exception e)
	     {
	             System.out.println("Exception while reading csv file: " + e);                  
	     }
		List<String> tickers = new ArrayList<String>();
		tickers.add("GOOG"); tickers.add("JPM"); //tickers.add("AAPL"); tickers.add("GE"); tickers.add("F");
		for(String ticker: tickers)
		{	
			String str_url = "http://ih.advfn.com/p.php?pid=financials&btn=quarterly_reports&ctl00%24sb3%24tbq1=Get+Quote&as_values_IH=&ctl00%24sb3%24stb1=Search+iHub&mode=&symbol=" +ticker;
			System.out.println(str_url);
			
			Document doc = Jsoup.connect(str_url).timeout(60000).get();
			
			Elements els = doc.select("span").attr("style", "text-transform: capitalize");
			
			for(Element el: els)
			{	Elements tables = el.select("table");
				if(tables!=null)
				{	
					if(tables.size()>1)
					{	
						Element tb1 = tables.get(1);
						//System.out.println(tb1);
						Elements trs = tb1.select("tr");
						Element tr2 = trs.get(1);
						Elements tds2 = tr2.select("td");
						Element td2 = tds2.get(1);
						Element tb2 = td2.select("table").get(1);
						Elements tr2s = tb2.select("tr");

						Map<String, List<String>> aMap = new HashMap<String, List<String>> ();
						
						for(Element tr: tr2s)
						{
							List<String> alist = new ArrayList<String>();
							Elements tds = tr.select("td");
							int i =0;
							for(Element td: tds)
							{
								if(td.hasAttr("colspan"))
								{
									continue;
									
								}
								else
								{	
									if(i==0)
									{	
										aMap.put(td.text(), alist);
									}
									if(i>0)
									{
										alist.add(td.text());
									}
								}	
								
								System.out.println(td.text()+ "    i=" +i);
								i++;
								
//								if(txt.equalsIgnoreCase("gross operating profit")||txt.equalsIgnoreCase("depreciation")||txt.equalsIgnoreCase("operating revenue"))
//								{
//									System.out.println(tr);
//								}
									
							}
						}
						System.out.println(aMap);
						
						List<String> timeperiods = new ArrayList<String>();
						List<String> totalrevenes = new ArrayList<String>();
						List<String> grossmagins = new ArrayList<String>();
						List<String> basicEPSs = new ArrayList<String>();
						List<String> depreciates = new ArrayList<String>();
							
						List<String> cashflows = new ArrayList<String>();
							
						List<String> peToIndustrys = new ArrayList<String>();
						List<String> salesToIndustrys = new ArrayList<String>();
							
						List<String> earningToIndustrys = new ArrayList<String>();
						List<String> grossmarginToIndustrys = new ArrayList<String>();
						List<String> pricecashflowToIndustrys = new ArrayList<String>();
						List<String> roeToIndustrys = new ArrayList<String>();
						for(String key: aMap.keySet())
						{	
							if("Date Preliminary Data Loaded".equals(key)){
								timeperiods = aMap.get(key);
							}
							if("Total Revenue".equals(key)){
								totalrevenes = aMap.get(key);
							}if("Gross Margin".equals(key)){
								grossmagins = aMap.get(key);
							}if("Depreciation".equals(key)){
								depreciates = aMap.get(key);
							}if("Basic EPS - Total".equals(key)){
								basicEPSs = aMap.get(key);
							}if("Cash Flow".equals(key)){
								cashflows = aMap.get(key);
							}if("% Of PE-To-Industry".equals(key)){
								peToIndustrys = aMap.get(key);
							}if("% Of Sales-To-Industry".equals(key)){
								salesToIndustrys = aMap.get(key);
							}if("% Of Earnings-To-Industry".equals(key)){
								earningToIndustrys = aMap.get(key);
							}if("% Of Gross Profit Margin-To-Industry".equals(key)){
								grossmarginToIndustrys = aMap.get(key);
							}if("% Of Price/Cashflow-To-Industry".equals(key)){
								pricecashflowToIndustrys = aMap.get(key);
							}if("% Of ROE-To-Industry".equals(key)){
								roeToIndustrys = aMap.get(key);
							}
						}	
						for(int k=0; k<5; k++)
						{	
							
							String insertsql = "INSERT INTO stock.hubreport (ticker,timeperiod,totalrevenue,grossmagin,depreciation,basicEPS, cashflow, peToIndustry, salsToIndustry, earningToIndustry, grossmaginToIndustry, pricecashflowToIndustry,roeToIndustry) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
							PreparedStatement s = dbconnection.prepareStatement(insertsql);
							s.setString(1,ticker);
							s.setString(2,timeperiods.get(k));
							s.setString(3,totalrevenes.get(k));
							s.setString(4,grossmagins.get(k));
							s.setString(5,depreciates.get(k));
							s.setString(6,basicEPSs.get(k));
							s.setString(7,cashflows.get(k));
							s.setString(8,peToIndustrys.get(k));
							s.setString(9,salesToIndustrys.get(k));
							s.setString(10,earningToIndustrys.get(k));
							s.setString(11,grossmarginToIndustrys.get(k));
							s.setString(12,pricecashflowToIndustrys.get(k));
							s.setString(13,roeToIndustrys.get(k));
							s.execute();
							s.close();
								
						}	
					}
				}	
					
			}	
		}
		
		dbconnection.close();
}	

	@Test
	public void industryget() throws ClientProtocolException, IOException, Exception
	{
		try {
			 
			Class.forName("com.mysql.jdbc.Driver");

		} catch (ClassNotFoundException e) {

			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;

		}

		System.out.println("MySQL JDBC Driver Registered!");
		Connection dbconnection = null;

		try {
			dbconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
							"root", "M1ng@2011");

		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}

		if (dbconnection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		
		
		ArrayList<String> tickets = new ArrayList<String>();
		
		 try
        {
               
                //csv file containing data
               String strFile = "/Users/minglei/Documents/StockDataInMysql/sp500.csv";
			 	//String strFile = "c:\\test\\nasdaq2000.csv";
                //create BufferedReader to read csv file
                BufferedReader br = new BufferedReader( new FileReader(strFile));
                String strLine = "";
     
                //read comma separated file line by line
                while( (strLine = br.readLine()) != null)
                {
               	 if(!strLine.isEmpty()&& strLine.length()>0)
               	 tickets.add(strLine);
                }
               
               
        }
        catch(Exception e)
        {
                System.out.println("Exception while reading csv file: " + e);                  
        }
		//tickers.add("GOOG"); tickers.add("JPM"); //tickers.add("AAPL"); tickers.add("GE"); tickers.add("F");
		for(String ticker: tickets)
		{	
			String str_url = "http://finance.yahoo.com/q/in?s=" +ticker+ "+Industry";
			System.out.println(str_url);
	
	
			Document doc = Jsoup.connect(str_url).timeout(60000).get();
	
			Elements tables = doc.getElementsByClass("yfnc_datamodoutline1");
			
			if(tables.size()>0)
			{	
	
				Element tb = tables.get(0);
		
				System.out.println(tb);
		
				Elements tb2s = tb.select("table");
		
				Element tb2 = tb2s.get(0);
		
		
				Elements tds = tb2.select("td");
				String insertsql = "INSERT INTO stock.industrymapping (ticker,sector, industry) "
		                   
							                    + " VALUES (?, ?, ?)" ;
				PreparedStatement ps = dbconnection.prepareStatement(insertsql);
				ps.setString(1, ticker);
		    
				for(int i=1; i<tds.size(); i++)
				{		
					ps.setString(i+1, tds.get(i).text());	
				}
				ps.execute();
				ps.close();
			}

		}
		dbconnection.close();

	}

	
	
	@Test
	public void retrieve_PEG() throws ClientProtocolException, IOException, Exception 
	{

		try {
			 
			Class.forName("com.mysql.jdbc.Driver");

		} catch (ClassNotFoundException e) {

			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;

		}

		System.out.println("MySQL JDBC Driver Registered!");
		Connection dbconnection = null;

		try {
			dbconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
							"root", "M1ng@2011");

		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}

		if (dbconnection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		
		
		//Map<String, Map<Date, Map<String, String>>> ValuationM = new TreeMap<String, Map<Date, Map<String, String>>>();
		
		
		Map<String, String> competitorMap = new TreeMap<String, String>();
		
		String key ="";
		
		ArrayList<String> tickets = new ArrayList<String>();
		
		 try
	     {
	            
	             //csv file containing data
	            String strFile = "/Users/minglei/Documents/StockDataInMysql/sp501.csv";
			 	//String strFile = "c:\\test\\nasdaq2000.csv";
	             //create BufferedReader to read csv file
	             BufferedReader br = new BufferedReader( new FileReader(strFile));
	             String strLine = "";
	             StringTokenizer st = null;
	             int lineNumber = 0, tokenNumber = 0;
	            
	             //read comma separated file line by line
	             while( (strLine = br.readLine()) != null)
	             {
	            	 if(!strLine.isEmpty()&& strLine.length()>0)
	            	 tickets.add(strLine);
	             }
	            
	            
	     }
	     catch(Exception e)
	     {
	             System.out.println("Exception while reading csv file: " + e);                  
	     }
		
		 for(String ticker: tickets)
		 {	 
			String str_url = "http://ih.advfn.com/p.php?pid=financials&btn=quarterly_reports&ctl00%24sb3%24tbq1=Get+Quote&as_values_IH=&ctl00%24sb3%24stb1=Search+iHub&mode=&%20%3E%3E%20ih.advfnih.advfn.com/p.php?pid=financials&btn=quarterly_reports&ctl00$sb3$tbq1=Get+Quote&as_values_IH=&ctl00$sb3$stb1=Search+iHub&mode=&symbol=" +ticker;
			System.out.println(str_url);
			
	
			Document doc = Jsoup.connect(str_url).timeout(60000).get();
			Element eles = doc.getElementById("divPrint");
		 }
	}

	
	@Test
	public void retrieve_EarningSuprise() throws ClientProtocolException, IOException, Exception 
	{

		try {
			 
			Class.forName("com.mysql.jdbc.Driver");

		} catch (ClassNotFoundException e) {

			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;

		}

		System.out.println("MySQL JDBC Driver Registered!");
		Connection dbconnection = null;

		try {
			dbconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
							"root", "M1ng@2011");

		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}

		if (dbconnection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		
		
		//Map<String, Map<Date, Map<String, String>>> ValuationM = new TreeMap<String, Map<Date, Map<String, String>>>();
		
		
		Map<String, String> competitorMap = new TreeMap<String, String>();
		
		String key ="";
		
		ArrayList<String> tickets = new ArrayList<String>();
		
		 try
	     {
	            
	             //csv file containing data
	            String strFile = "/Users/minglei/Documents/StockDataInMysql/sp501.csv";
			 	//String strFile = "c:\\test\\nasdaq2000.csv";
	             //create BufferedReader to read csv file
	             BufferedReader br = new BufferedReader( new FileReader(strFile));
	             String strLine = "";
	             StringTokenizer st = null;
	             int lineNumber = 0, tokenNumber = 0;
	            
	             //read comma separated file line by line
	             while( (strLine = br.readLine()) != null)
	             {
	            	 if(!strLine.isEmpty()&& strLine.length()>0)
	            	 tickets.add(strLine);
	             }
	            
	            
	     }
	     catch(Exception e)
	     {
	             System.out.println("Exception while reading csv file: " + e);                  
	     }
		
		 for(String ticker: tickets)
		 {	 
			String str_url = "http://zacks.thestreet.com/CompanyView.php?ticker=" +ticker;
			System.out.println(str_url);
			
	
			Document doc = Jsoup.connect(str_url).timeout(60000).get();
			Element eles = doc.getElementById("divPrint");
	
	
			//System.out.println(eles);
	
			Elements headers = eles.getElementsByClass("bordered");
	
			int i =0;
			for(Element header1: headers) 
			{
				if(i>0)
				{
					System.out.println(header1);
					Elements trs = header1.select("tr");
					int size = trs.size();
					int j=0;
					for(j=1; j<size; j++) 
					{
						Element tr = trs.get(j);
						Elements tds = tr.select("td");
						String insertsql = "INSERT INTO stock.earningsuperise (ticker, reportday,period,estimated,reported,superise,superisepercent) "
				                   
					                    + " VALUES (?,?,?,?,?,?,?)" ;
						PreparedStatement ps = dbconnection.prepareStatement(insertsql);
						ps.setString(1, ticker);
						for(int k=0; k<tds.size(); k++)
						{
							Element td =  tds.get(k);
		    
							ps.setString(k+2, td.text());
					 
							   
							System.out.println(td.text());
						}
						 ps.execute();
						 ps.close();
					}
	
				}
				i++;
			}

		 }	
		 dbconnection.close();
	}

@Test
public void retrieve_comp() throws ClientProtocolException, IOException, Exception
{
	try {
		 
		Class.forName("com.mysql.jdbc.Driver");

	} catch (ClassNotFoundException e) {

		System.out.println("Where is your MySQL JDBC Driver?");
		e.printStackTrace();
		return;

	}

	System.out.println("MySQL JDBC Driver Registered!");
	Connection dbconnection = null;

	try {
		dbconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
						"root", "M1ng@2011");

	} catch (SQLException e) {
		System.out.println("Connection Failed! Check output console");
		e.printStackTrace();
		return;
	}

	if (dbconnection != null) {
		System.out.println("You made it, take control your database now!");
	} else {
		System.out.println("Failed to make connection!");
	}
	
	
	//Map<String, Map<Date, Map<String, String>>> ValuationM = new TreeMap<String, Map<Date, Map<String, String>>>();
	
	
	Map<String, String> competitorMap = new TreeMap<String, String>();
	
	String key ="";
	
	ArrayList<String> tickets = new ArrayList<String>();
	
	 try
     {
            
             //csv file containing data
            String strFile = "/Users/minglei/Documents/StockDataInMysql/sp51.csv";
		 	//String strFile = "c:\\test\\nasdaq2000.csv";
             //create BufferedReader to read csv file
             BufferedReader br = new BufferedReader( new FileReader(strFile));
             String strLine = "";
             StringTokenizer st = null;
             int lineNumber = 0, tokenNumber = 0;
            
             //read comma separated file line by line
             while( (strLine = br.readLine()) != null)
             {
            	 if(!strLine.isEmpty()&& strLine.length()>0)
            	 tickets.add(strLine);
             }
            
            
     }
     catch(Exception e)
     {
             System.out.println("Exception while reading csv file: " + e);                  
     }
	 
	 for(String ticker: tickets)
	 {		 

		 String str_url = "http://finance.yahoo.com/q/co?s=" + ticker+ "+Competitors";
		 System.out.println(str_url);
	
		 Document doc = Jsoup.connect(str_url).get();
		 Elements eles = doc.getElementsByClass("yfnc_datamodoutline1");
	
		 if(eles!=null&&eles.size()>0)
		 {	 
			 Element data1 = eles.get(0);
		
			 //System.out.println(data1);
		
			 Elements headers = data1.getElementsByClass("yfnc_tablehead1");
		
			 int i =0;
			 for(Element header1: headers) 
			 {
				if(i>0){
					//System.out.println(header1);
					Element link = header1.select("a").first();
					
					//System.out.println(link);
					if(link!=null)
					{	
						System.out.println(link.text());
						if(i==1)
						{
							key = link.text();
						}
						if(i>1)
						{
							 String insertsql = "INSERT INTO stock.competitor (ticker,competitor) "
					                   
					                    + " VALUES (?,?)" ;
							    PreparedStatement ps = dbconnection.prepareStatement(insertsql);
					
							    ps.setString(1, key);
							    ps.setString(2, link.text());
					 
							    ps.execute();
							    ps.close();
 
						}
					}	
				}
				i++;
			 }
		 }	 
	 }
	
	 dbconnection.close();

	 
}	


	
	
		@Test
	    public void marketReactionTest() throws SQLException, ParseException {
			
			 try {
	
					Class.forName("com.mysql.jdbc.Driver");
		 
				} catch (ClassNotFoundException e) {
		 
					System.out.println("Where is your MySQL JDBC Driver?");
					e.printStackTrace();
					return;
		 
				}
	 
			 	System.out.println("MySQL JDBC Driver Registered!");
			 	Connection dbconnection = null;
	 
			 	try {
			 		dbconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
								"root", "M1ng@2011");
	 
			 	} catch (SQLException e) {
					System.out.println("Connection Failed! Check output console");
					e.printStackTrace();
					return;
			 	}

			 	Map<String, String> ticketsMap = new HashMap<String, String>();

				try
				{
	                
					//csv file containing data
				    String strFile = "/Users/minglei/Documents/StockDataInMysql/finvizWithDateClean.txt";
						       //String strFile = "c:\\test\\nasdaq2000.csv";
					//create BufferedReader to read csv file
					BufferedReader br = new BufferedReader( new FileReader(strFile));
					String strLine = "";
	
					//read comma separated file line by line
					while( (strLine = br.readLine()) != null)
					{
						if(!strLine.isEmpty()&& strLine.length()>0)
						{
							String ticker = strLine.substring(0, strLine.indexOf(","));
							String dates = strLine.substring(strLine.indexOf(",")+1, strLine.length());
							ticketsMap.put(ticker, dates);
						}	
					}
	                         
				}
				catch (Exception e){

				}
				
				double datebeforePrice=0, datebeafterPrice=0, weekbeforeAvg =0, weekbeforeMax =0, weekbeforeMin= 0, weekafterAvg= 0, weekafterMax=0, weekAfterMin=0;
			    double inx_datebeforePrice=0, inx_datebeafterPrice=0, inx_weekbeforeAvg=0, inx_weekbeforeMax=0, inx_weekbeforeMin =0,  inx_weekafterAvg=0,  inx_weekafterMax=0, inx_weekAfterMin =0;
			    int i=1;
				for(String tick: ticketsMap.keySet())
				{	

					
				    // String strsql = "INSERT INTO earningSurprise (day, ticker, reportEps, Consensus_EPS, suprise) VALUES (?,?,?,?,?)";
				    // PreparedStatement s = dbconnection.prepareStatement(strsql);
			    
					String days = ticketsMap.get(tick);
				    Date datein = new SimpleDateFormat("MM/dd/yyyy").parse(days);
				    java.sql.Date sqldate = new java.sql.Date(datein.getTime());
				    
				    String close_sql = "select close from stock.Pricehistory where ticker = ? and day = ?";
				    String a_three_sql = "select MIN(close), MAX(close), AVG(close) from stock.Pricehistory where ticker = ? and day >= ? and day <= DATE_ADD(?, INTERVAL 7 DAY)";
				    PreparedStatement s = dbconnection.prepareStatement(a_three_sql);
				    s.setString(1, tick);
				    
				    s.setDate(2, sqldate);
				    s.setDate(3, sqldate);
				    ResultSet rs = s.executeQuery();
				    while(rs.next()) {
				    	weekAfterMin = rs.getDouble(1);
				    	weekafterMax = rs.getDouble(2);
				    	weekafterAvg = rs.getDouble(3);
				    }
				    rs.close();
				    s.close();
				    
				    s = dbconnection.prepareStatement(close_sql);
				    s.setString(1, tick);
				    s.setDate(2, sqldate);
				    rs = s.executeQuery();
				    while(rs.next()) {
				    	datebeforePrice = rs.getDouble(1);
				    }
				    rs.close();
				    s.close();
				    
				    String b_three_sql = "select MIN(close), MAX(close), AVG(close) from stock.Pricehistory where ticker = ? and day <= ? and day >= DATE_SUB(?, INTERVAL 7 DAY)";
				    s = dbconnection.prepareStatement(b_three_sql);
				    s.setString(1, tick);			    
				    s.setDate(2, sqldate);
				    s.setDate(3, sqldate);
				    rs = s.executeQuery();
				    while(rs.next()) {
				    	weekbeforeMin = rs.getDouble(1);
				    	weekbeforeMax = rs.getDouble(2);
				    	weekbeforeAvg = rs.getDouble(3);
				    }
				    rs.close();
				    s.close();
				    
				    String ind_b_three_sql = "select MIN(close), MAX(close), AVG(close) from stock.Pricehistory where ticker = 'spy' and day <= ? and day >= DATE_SUB(?, INTERVAL 7 DAY)";
				    s = dbconnection.prepareStatement(ind_b_three_sql);			    
				    s.setDate(1, sqldate);
				    s.setDate(2, sqldate);
				    rs = s.executeQuery();
				    while(rs.next()) {
				    	
				    	inx_weekbeforeAvg= rs.getDouble(3); 
				    	inx_weekbeforeMax= rs.getDouble(2);
				    	inx_weekbeforeMin = rs.getDouble(1);
				    }
				    rs.close();
				    s.close();
				    
				    String ind_a_three_sql = "select MIN(close), MAX(close), AVG(close) from stock.Pricehistory where ticker = 'spy'  and day >= ? and day <= DATE_ADD(?, INTERVAL 7 DAY)";
				    s = dbconnection.prepareStatement(ind_a_three_sql);
				    s.setDate(1, sqldate);
				    s.setDate(2, sqldate);
				    rs = s.executeQuery();
				    while(rs.next()) {
				    	inx_weekafterAvg=rs.getDouble(3);  
				    	inx_weekafterMax=rs.getDouble(2);
				    	inx_weekAfterMin =rs.getDouble(1);
				    }
				    rs.close();
				    s.close();
				    
				    
				    
				    String insertsql = "INSERT INTO stock.marketReaction (ticker,reportDate,surpriseDirection, "
                                       + " surpriseMagnitude, " 
                                       + " priceOneDayBeforeEarning, "
                                       + " priceOneDayAfterEarning, "
                                       + " averagePriceOneWeekBeforeEarning, "
                                       + " averagePriceOneWeekAfterEarning, "
                                       + " maxPriceOneWeekBeforeEarning, "
                                       + " maxPriceOneWeekAfterEarning, "
                                       + " minPriceOneWeekBeforeEarning, "
                                       + " minPriceOneWeekAfterEarning, "
                                       + " benchmarkPriceOneDayBeforeEarning, "
                                       + " benchmarkPriceOneDayAfterEarning, "
                                       + " benchmarkAveragePriceOneWeekBeforeEarning, "
                                       + " benchmarkAveragePriceOneWeekAfterEarning, "
                                       + " benchmarkMaxPriceOneWeekBeforeEarning, "
                                       + " benchmarkMaxPriceOneWeekAfterEarning, "
                                       + " benchmarkMinPriceOneWeekBeforeEarning, "
                                       + " benchmarkMinPriceOneWeekAfterEarning) "
                                       + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
				    PreparedStatement ps = dbconnection.prepareStatement(insertsql);
				   
				    ps.setString(1, tick);
				    ps.setDate(2, sqldate);
				    ps.setInt(3, 1);
				    ps.setDouble(4, 0.25);
				    ps.setDouble(5, datebeforePrice);
				    ps.setDouble(6, datebeafterPrice);
				    ps.setDouble(7, weekbeforeAvg);
				    ps.setDouble(8, weekafterAvg);
				    ps.setDouble(9, weekbeforeMax);
				    ps.setDouble(10, weekafterMax);
				    ps.setDouble(11, weekbeforeMin);
				    ps.setDouble(12, weekAfterMin);
				    ps.setDouble(13, inx_datebeforePrice);
				    ps.setDouble(14, inx_datebeafterPrice);
				    ps.setDouble(15, inx_weekbeforeAvg);
				    ps.setDouble(16, inx_weekafterAvg);
				    ps.setDouble(17, inx_weekbeforeMax);
				    ps.setDouble(18, inx_weekafterMax);
				    ps.setDouble(19, inx_weekbeforeMin);
				    ps.setDouble(20, inx_weekAfterMin);
				    
				    
				    ps.execute();
				    ps.close();
				    i++;
				    
				}
			    dbconnection.close();

	 

		}

	
	@Test
	public void TestGoogleCloundSQL() {
		com.google.cloud.sql.jdbc.Connection c = null;
	    try {
	      DriverManager.registerDriver(new com.google.cloud.sql.Driver());
	      c = (com.google.cloud.sql.jdbc.Connection) DriverManager.getConnection("jdbc:google:rdbms://limestoneadvisor:stock/test");
	      System.out.println("google cloud sql connect success");
	      String statement ="select * from pricehistory where ticker = 'AAPL'";
	      Statement stmt = c.createStatement();
	      int success = 2;
	      ResultSet rs = stmt.executeQuery(statement);
	      
	     while(rs.next())
	     {
	    	 System.out.println(rs.getDouble("open"));
	     }
	      
	     
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        if (c != null) 
	          try {
	            c.close();
	            } catch (SQLException ignore) {
	         }
	      } 
	}
	
	@Test
	public void priceHistoryCollect() throws IOException, SQLException {
		
		try {
			 
			Class.forName("com.mysql.jdbc.Driver");
 
		} catch (ClassNotFoundException e) {
 
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
 
		}
 
		System.out.println("MySQL JDBC Driver Registered!");
		Connection dbconnection = null;
 
		try {
			dbconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
							"root", "M1ng@2011");
 
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
 
		if (dbconnection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		
	
		ArrayList<String> tickets = new ArrayList<String>();
		
		 try
         {
                
                 //csv file containing data
                String strFile = "/Users/minglei/Documents/StockDataInMysql/sp500.txt";
			 	//String strFile = "c:\\test\\nasdaq2000.csv";
                 //create BufferedReader to read csv file
                 BufferedReader br = new BufferedReader( new FileReader(strFile));
                 String strLine = "";
      
                 //read comma separated file line by line
                 while( (strLine = br.readLine()) != null)
                 {
                	 if(!strLine.isEmpty()&& strLine.length()>0)
                	 tickets.add(strLine);
                 }
                
                
         }
         catch(Exception e)
         {
                 System.out.println("Exception while reading csv file: " + e);                  
         }
		 
		 for(String t: tickets)
		 {
			 String tablename = "yahoodata_" +t;
			 String strsqlinput = "select day, open, high, low, close, volume,  adj_close from  " + tablename;
			 Statement st = dbconnection.createStatement();
			 ResultSet rs = st.executeQuery(strsqlinput);
			 System.out.println(t);
			 while(rs.next()) {
				 
				 java.sql.Date day1 = rs.getDate("day");
				 double open = rs.getDouble("open");
				 double high = rs.getDouble("high");
				 double low  = rs.getDouble("low");
				 double close = rs.getDouble("close");
				 long volume = rs.getLong("volume");
				 double adj_close = rs.getDouble("adj_close");
			 
				 String strsql = "INSERT INTO pricehistory (ticker, day, open, high, low, close, volume,  adj_close) VALUES (?,?,?,?,?,?,?,?)";
				 PreparedStatement ps = dbconnection.prepareStatement(strsql);
				 ps.setString(1,  t);
				 ps.setDate(2,  day1);
				 ps.setDouble(3, open);
				 ps.setDouble(4, high);
				 ps.setDouble(5, low);
				 ps.setDouble(6, close);
				 ps.setLong(7, volume);
				 ps.setDouble(8, adj_close);
			 
				 ps.execute();
			 }
		 }
		
	}
	
	@Test
	public void zacksDataCollect() throws IOException {
		
		Map<String, Map<String, String>> ValuationM = new TreeMap<String, Map<String, String>>();
		ArrayList<String> tickets = new ArrayList<String>();
		tickets.add("AAPL");
		for(String s: tickets)
		{	
			String str_url = "http://www.zacks.com/stock/quote/"+s+ "/detailed-estimates";
			Document doc = Jsoup.connect(str_url).get();
			
			Elements tables = doc.getElementsByClass("jstable");
			Iterator<Element> it = tables.iterator();
			Map<String, String> datamap = new TreeMap<String, String>();
			while(it.hasNext())
			{	
				Element el = it.next();
				Elements trs = el.getElementsByTag("tr");
				System.out.println(trs);
				Iterator<Element> tr_it = trs.iterator();
				while(tr_it.hasNext())
				{
					Element tr = tr_it.next();
					
				}
			}
			System.out.println(tables);
		}	
	}
	@Test
	public void EaringsCollect() throws IOException, SQLException, ParseException {
		try {
			 
			Class.forName("com.mysql.jdbc.Driver");
 
		} catch (ClassNotFoundException e) {
 
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
 
		}
 
		System.out.println("MySQL JDBC Driver Registered!");
		Connection dbconnection = null;
 
		try {
			dbconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
							"root", "M1ng@2011");
 
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
 
		if (dbconnection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		
		
		//Map<String, Map<Date, Map<String, String>>> ValuationM = new TreeMap<String, Map<Date, Map<String, String>>>();
		
		
		
		
		ArrayList<String> tickets = new ArrayList<String>();
		
		 try
         {
                
                 //csv file containing data
                String strFile = "/Users/minglei/Documents/StockDataInMysql/sp500.csv";
			 	//String strFile = "c:\\test\\nasdaq2000.csv";
                 //create BufferedReader to read csv file
                 BufferedReader br = new BufferedReader( new FileReader(strFile));
                 String strLine = "";
                 StringTokenizer st = null;
                 int lineNumber = 0, tokenNumber = 0;
                
                 //read comma separated file line by line
                 while( (strLine = br.readLine()) != null)
                 {
                	 if(!strLine.isEmpty()&& strLine.length()>0)
                	 tickets.add(strLine);
                 }
                
                
         }
         catch(Exception e)
         {
                 System.out.println("Exception while reading csv file: " + e);                  
         }
//	    tickets.add("AMZN");
//		tickets.add("AA");
//		tickets.add("AAPL");
		ArrayList<String> reportDate= new ArrayList<String>();
		ArrayList<String> estimatedEPS= new ArrayList<String>();
		ArrayList<String> reportedEPS= new ArrayList<String> ();
		for(String s: tickets)
		{	
			reportDate = new ArrayList<String>();
			estimatedEPS = new ArrayList<String>();
			reportedEPS = new ArrayList<String> ();
			String str_url = "http://www.smartmoney.com/quote/"+s+ "/?story=earningsForecast";
			Document doc = Jsoup.connect(str_url).get();
			Elements eles = doc.getElementsByClass("smData");
			
			Iterator<Element> it = eles.iterator();
			Map<String, String> datamap = new TreeMap<String, String>();
			while(it.hasNext())
			{	
				Element el = it.next();
				Elements hs = el.getElementsByTag("h3");
				if(hs.text().equalsIgnoreCase("Earnings vs. Expectations (cont.)"))
				{
				
					System.out.println(el);
					Elements tables = el.getElementsByTag("table");
					Element table = tables.first();
					Element hrad_tr = table.getElementsByTag("thead").first().getElementsByTag("tr").first();
					Elements tds = hrad_tr.getElementsByTag("td");
					int i =0;
					for(Element td: tds)
					{
						if(i>0)
						{
							reportDate.add(td.text());
							
						}
						i++;
					}
					Element es_tr = table.getElementsByTag("tbody").first().getElementsByTag("tr").first();
					Elements tds2 = es_tr.getElementsByTag("td");
					for(Element td: tds2)
					{
						
						estimatedEPS.add(td.text());
						
					}
					Element rp_tr = table.getElementsByTag("tbody").first().getElementsByTag("tr").get(1);
					Elements tds3 = rp_tr.getElementsByTag("td");
					for(Element td: tds3)
					{
						
						reportedEPS.add(td.text());
						
					}
				}
				
			}
			for(int i=0; i<reportDate.size(); i++)
			{
				String strsql = "INSERT INTO earningReported (ticker, reportTime, estimated_EPS, reported_EPS) VALUES (?,?,?,?)";
				PreparedStatement ps = dbconnection.prepareStatement(strsql);
				ps.setString(1, s);
				ps.setString(2, reportDate.get(i));
				String esEPS= estimatedEPS.get(i);
				String reEPS = reportedEPS.get(i);
				String patternOne = "#,##0.00";
				DecimalFormat nf = new DecimalFormat(patternOne);
				double esEPSd;
				if(esEPS==null || esEPS.equalsIgnoreCase("NA")) esEPSd = -999.0;
				else esEPSd = nf.parse(esEPS).doubleValue();
				ps.setDouble(3, esEPSd);
				
				double reEPSd;
				if(reEPS==null || reEPS.equalsIgnoreCase("NA")) reEPSd = -999.0;
				else reEPSd = nf.parse(reEPS).doubleValue();
				ps.setDouble(4, reEPSd);
				
				ps.execute();
				
			}
			
		}
		
//		for(String t:ValuationM.keySet())
//		   System.out.println(t +" : "+ ValuationM.get(t));
//		System.out.println(reportDate);
//		System.out.println(estimatedEPS);
//    	System.out.println(reportedEPS);
		
	}
	
	@Test
	public void dataCollect() throws IOException, SQLException, ParseException {
		
		
		try {
			 
			Class.forName("com.mysql.jdbc.Driver");
 
		} catch (ClassNotFoundException e) {
 
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
 
		}
 
		System.out.println("MySQL JDBC Driver Registered!");
		Connection dbconnection = null;
 
		try {
			dbconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
							"root", "M1ng@2011");
 
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
 
		if (dbconnection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		
		Map<String, Map<String, String>> ValuationM = new TreeMap<String, Map<String, String>>();
		
		
		
		
		ArrayList<String> tickets = new ArrayList<String>();
		
		 try
         {
                
                 //csv file containing data
                String strFile = "/Users/minglei/Documents/StockDataInMysql/sp500.csv";
			 	//String strFile = "c:\\test\\nasdaq2000.csv";
                 //create BufferedReader to read csv file
                 BufferedReader br = new BufferedReader( new FileReader(strFile));
                 String strLine = "";
                 StringTokenizer st = null;
                 int lineNumber = 0, tokenNumber = 0;
                
                 //read comma separated file line by line
                 while( (strLine = br.readLine()) != null)
                 {
                	 if(!strLine.isEmpty()&& strLine.length()>0)
                	 tickets.add(strLine);
                 }
                
                
         }
         catch(Exception e)
         {
                 System.out.println("Exception while reading csv file: " + e);                  
         }
//	    tickets.add("AMZN");
//		tickets.add("AA");
//		tickets.add("AAPL");
		for(String s: tickets)
		{	
			String str_url = "http://finance.yahoo.com/q/ks?s=" +s;
			Document doc = Jsoup.connect(str_url).get();
			Elements eles = doc.getElementsByClass("yfnc_datamodoutline1");
			
			Iterator<Element> it = eles.iterator();
			Map<String, String> datamap = new TreeMap<String, String>();
			while(it.hasNext())
			{	
				Element el = it.next();
				Elements teles = el.getElementsByClass("yfnc_tablehead1");
				//System.out.println(teles.toString());
				
				Elements seles = el.getElementsByClass("yfnc_tabledata1");
				//System.out.println(seles.toString());
				int size = teles.size();
				for(int i=0; i< size; i++){
					datamap.put(teles.get(i).text(), seles.get(i).text());
				}
				
				//System.out.println(ValuationM);
			}
			ValuationM.put(s, datamap);
		}
		
		for(String t:ValuationM.keySet())
		   System.out.println(t +" : "+ ValuationM.get(t));
		System.out.println(ValuationM.keySet());
		System.out.println(ValuationM.keySet().size());
		
		//dbconnection.setAutoCommit(false);
		String strsql = "INSERT INTO key_financial (ticker, currentpe, peg, cpe, debtEquitratio, dividendYield, cashPerShare,  grossMargin, returnOnEquity, y3enegrowthrate, currentRatio, enterprisevalue) VALUES (?,?,?,?,?,?,?,?,?,?,?, ?)";
		
	
		try
		{
//			NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
//			nf.setMaximumFractionDigits(3);
			final int batchSize = 10;
			int count = 0;
			String patternOne = "#,##0.00";
			DecimalFormat nf = new DecimalFormat(patternOne);
			NumberFormat pencenf = NumberFormat.getPercentInstance(Locale.ENGLISH);
			for(String t: ValuationM.keySet())
			{
				PreparedStatement s = dbconnection.prepareStatement(strsql);
				//s.setInt(1, id);
				s.setString(1, t);
				Map valMap = ValuationM.get(t);
				Double current_pe;
				String strpe = (String)valMap.get("Trailing P/E (ttm, intraday):");
				if(strpe==null || strpe.equalsIgnoreCase("N/A")) current_pe = -999.0;
				else current_pe = nf.parse(strpe).doubleValue();
				s.setDouble(2, current_pe);
				Double peg;
				String strpeg = (String)valMap.get("PEG Ratio (5 yr expected)1:");
				if(strpeg==null || strpeg.equalsIgnoreCase("N/A")) peg = -999.0;
				else peg= nf.parse(strpeg).doubleValue();
			
				s.setDouble(3, peg);
				
				Double cpe;
				String strcpe = (String)valMap.get("Price/Book (mrq):");
				if(strcpe==null || strcpe.equalsIgnoreCase("N/A")) cpe = -999.0;
				else cpe= nf.parse(strcpe).doubleValue();
				s.setDouble(4, cpe);
				
				
				Double debtequitratio;
				String strdebtequitratio = (String)valMap.get("Total Debt/Equity (mrq):");
				if(strdebtequitratio==null || strdebtequitratio.equalsIgnoreCase("N/A")) debtequitratio = -999.0;
				else debtequitratio= nf.parse(strdebtequitratio).doubleValue();
				s.setDouble(5, debtequitratio);
				Double DividendYield;
				String strDividendYield = (String)valMap.get("5 Year Average Dividend Yield4:");
				if(strDividendYield ==null ||strDividendYield.equalsIgnoreCase("N/A")) DividendYield = -999.0;
				else DividendYield = pencenf.parse(strDividendYield).doubleValue();
				s.setDouble(6, DividendYield);
				Double cashpershare;
				String strCashPerShare = (String)valMap.get("Total Cash Per Share (mrq):");
				if(strCashPerShare==null || strCashPerShare.equalsIgnoreCase("N/A")) cashpershare = -999.0;
				else cashpershare = nf.parse(strCashPerShare).doubleValue();
				s.setDouble(7, cashpershare);
				Double GrossMargin;
				String strGrossMargin = (String)valMap.get("Profit Margin (ttm):");
				if(strGrossMargin  ==null || strGrossMargin .equalsIgnoreCase("N/A")) GrossMargin = -999.0;
//				else {
//					String margin = strGrossMargin.substring(0, strGrossMargin.length()-2);
//					GrossMargin = Float.parseFloat(margin);
//				}
				else GrossMargin = pencenf.parse(strGrossMargin).doubleValue();
				s.setDouble(8, GrossMargin);
				
				Double returnOnEquity;
				String strreturnOnEquity = (String)valMap.get("Return on Equity (ttm):");
				if(strreturnOnEquity ==null || strreturnOnEquity.equalsIgnoreCase("N/A")) returnOnEquity = -999.0;
//				else {
//					String strreturn = strreturnOnEquity.substring(0, strreturnOnEquity.length()-2);
//				
//					returnOnEquity = Float.parseFloat(strreturn);
//				}
				else returnOnEquity = pencenf.parse(strreturnOnEquity).doubleValue();
				s.setDouble(9, returnOnEquity);
				
				Double y3EarningGrowthrate;
				String stry3EarningGrowthrate = (String)valMap.get("Qtrly Revenue Growth (yoy):");
				if(stry3EarningGrowthrate ==null || stry3EarningGrowthrate.equalsIgnoreCase("N/A")) y3EarningGrowthrate = -999.0;
				else {
//					String stry3return = stry3EarningGrowthrate.substring(0, stry3EarningGrowthrate.length()-2);
//				
//					y3EarningGrowthrate = Float.parseFloat(stry3return);
					y3EarningGrowthrate = pencenf.parse(stry3EarningGrowthrate).doubleValue();
				}
				s.setDouble(10, y3EarningGrowthrate);
				
				Double currentRatio;
				String strycurrentRatio = (String)valMap.get("Current Ratio (mrq):");
				if(strycurrentRatio ==null ||strycurrentRatio.equalsIgnoreCase("N/A")) currentRatio = -999.0;
				else currentRatio = nf.parse(strycurrentRatio).doubleValue();
				s.setDouble(11, currentRatio);
				
				String strenterprisevalue = (String)valMap.get("Enterprise Value (Mar 9, 2013)3:");
				s.setString(12, strenterprisevalue);
					
				s.execute();
			
				s.close();
			}
			
	
		}
		finally 
		{
			
			dbconnection.close();
		}
		
	}
	
	@Test
	public void suprisedataCollect() throws IOException, SQLException, ParseException 
	{
		
		ArrayList<String> dates = new ArrayList<String>();
		
		try
        {
               
                //csv file containing data
               String strFile = "/Users/minglei/Documents/test/time.csv";
			 	//String strFile = "c:\\test\\nasdaq2000.csv";
                //create BufferedReader to read csv file
                BufferedReader br = new BufferedReader( new FileReader(strFile));
                String strLine = "";
                StringTokenizer st = null;
                int lineNumber = 0, tokenNumber = 0;
               
                //read comma separated file line by line
                while( (strLine = br.readLine()) != null)
                {
               	 if(!strLine.isEmpty()&& strLine.length()>0)
               		dates.add(strLine);
                }
               
               
        }
        catch(Exception e)
        {
                System.out.println("Exception while reading csv file: " + e);                  
        }
		
		Map<String, Map<String, Map<String, String>>> ValuationM = new TreeMap<String, Map<String, Map<String, String>>>();
		
		
		for(String s: dates)
		{
			String str_url = "http://biz.yahoo.com/z/" + s+ ".html";
			System.out.println(str_url);
			Document doc = Jsoup.connect(str_url).get();
			Elements table_eles = doc.getElementsByTag("table");
			Element el = table_eles.get(3);
			Elements trs = el.getElementsByTag("tr");
			int size = trs.size();
			Map<String, Map<String, String>> vmap = new TreeMap<String, Map<String, String>>();
				for(int i=2; i<size; i++)
				{
					Element tr = trs.get(i);
					Map<String, String> datamap = new TreeMap<String, String>();
					
					Elements tds = tr.getElementsByTag("td");
					int tdsize = tds.size();
					if(tdsize>=7)
					{	
						String tick = tds.get(1).getElementsByTag("a").text();
						for(int j=1; j<tds.size(); j++)
						{
							Element td = tds.get(j);
							if(j==2)
							{	
								datamap.put("Suprise", td.text());
							} else if(j==3) 
							{	
								datamap.put("Reported_EPS", td.text());
							}
							else if (j==4)
							{	
								datamap.put("Consensus_EPS", td.text());
							}	
						}
						
						vmap.put(tick, datamap);
						ValuationM.put(s, vmap);
					}
					
				 }
			
		}
		
		System.out.println(ValuationM);
		try {
			 
			Class.forName("com.mysql.jdbc.Driver");
 
		} catch (ClassNotFoundException e) {
 
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
 
		}
 
		System.out.println("MySQL JDBC Driver Registered!");
		Connection dbconnection = null;
 
		try {
			dbconnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
							"root", "M1ng@2011");
 
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
 
		if (dbconnection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		
		String strsql = "INSERT INTO earningSurprise (day, ticker, reportEps, Consensus_EPS, suprise) VALUES (?,?,?,?,?)";
		PreparedStatement s = dbconnection.prepareStatement(strsql);
		try
		{

			final int batchSize = 10;
			int count = 0;
			String patternOne = "#,##0.00";
			DecimalFormat nf = new DecimalFormat(patternOne);
			
			for(String t: ValuationM.keySet())
			{
				
				SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
				java.util.Date dt = formater.parse(t);
				java.sql.Date d = new java.sql.Date(dt.getTime());
				s.setDate(1, d);
		
				Map<String, Map<String,String>> valMap = ValuationM.get(t);
				for(String ticket: valMap.keySet())
				{
					s.setString(2,  ticket);
					Map<String,String> dMap = valMap.get(ticket);
					String strrepEPS= dMap.get("Reported_EPS");
					Double report_EPS;
					if(strrepEPS==null || strrepEPS.equalsIgnoreCase("N/A")) report_EPS = -999.0;
					else report_EPS = nf.parse(strrepEPS).doubleValue();
					s.setDouble(3, report_EPS);
					String strConEPS = dMap.get("Consensus_EPS");
					Double con_EPS;
					if(strConEPS==null || strConEPS.equalsIgnoreCase("N/A")) con_EPS = -999.0;
					else con_EPS = nf.parse(strConEPS).doubleValue();
					s.setDouble(4, con_EPS);
					
					String strSuprise = dMap.get("Suprise");
					Double con_Sup;
					if(strSuprise==null || strSuprise.equalsIgnoreCase("N/A")) con_Sup = -999.0;
					else con_Sup = nf.parse(strSuprise).doubleValue();
					s.setDouble(5, con_Sup);
					
					s.execute();
					
					System.out.println("save ticker:" +ticket);
				}
//				Double current_pe;
//				String strpe = (String)valMap.get("Trailing P/E (ttm, intraday):");
//				if(strpe==null || strpe.equalsIgnoreCase("N/A")) current_pe = -999.0;
//				else current_pe = nf.parse(strpe).doubleValue();
//				s.setDouble(3, current_pe);
//				Double peg;
//				String strpeg = (String)valMap.get("PEG Ratio (5 yr expected)1:");
//				if(strpeg==null || strpeg.equalsIgnoreCase("N/A")) peg = -999.0;
//				else peg= nf.parse(strpeg).doubleValue();
//			
//				s.setDouble(4, peg);
//				
//				Double cpe;
//				String strcpe = (String)valMap.get("Price/Book (mrq):");
//				if(strcpe==null || strcpe.equalsIgnoreCase("N/A")) cpe = -999.0;
//				else cpe= nf.parse(strcpe).doubleValue();
//				s.setDouble(5, cpe);
			}
		}	
		finally 
		{
			s.close();
			dbconnection.close();
		}
		

	}
	
	
	
	
	@Test
	public void retrieve() throws ClientProtocolException, IOException, Exception {
		
		System.out.println("-------- MySQL JDBC Connection Testing ------------");
		 
		try {
 
			Class.forName("com.mysql.jdbc.Driver");
 
		} catch (ClassNotFoundException e) {
 
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
 
		}
 
		System.out.println("MySQL JDBC Driver Registered!");
		Connection connection = null;
 
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stock",
							"root", "M1ng@2011");
 
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
 
		if (connection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		
		String str_url = "http://finance.yahoo.com/d/quotes.csv?s=goog,ge,f,c,hpq&f=snl1jk";
		HttpClient client = new DefaultHttpClient();
		// Prepare a request object
		HttpGet httpget = new HttpGet(str_url);

		 // Execute the request
		 HttpResponse response = client.execute(httpget);

		 // Examine the response status
		 System.out.println(response.getStatusLine());

		 // Get hold of the response entity
		 HttpEntity entity = response.getEntity();
		 
		 StringBuffer sb= new StringBuffer();

		 // If the response does not enclose an entity, there is no need
		 // to worry about connection release
		 if (entity != null) {
		     InputStream instream = entity.getContent();
		     try {

		         BufferedReader reader = new BufferedReader(
		                 new InputStreamReader(instream));
		         // do something useful with the response
		         while(((reader.readLine()) != null)) {
		        	 sb.append(reader.readLine());
			         System.out.println(sb);
		         }
		         
		         

		     } catch (IOException ex) {

		         // In case of an IOException the connection will be released
		         // back to the connection manager automatically
		         throw ex;

		     } catch (RuntimeException ex) {

		         // In case of an unexpected exception you may want to abort
		         // the HTTP request in order to shut down the underlying
		         // connection and release it back to the connection manager.
		         httpget.abort();
		         throw ex;

		     } finally {

		         // Closing the input stream will trigger connection release
		         instream.close();

		     }

		     // When HttpClient instance is no longer needed,
		     // shut down the connection manager to ensure
		     // immediate deallocation of all system resources
		     client.getConnectionManager().shutdown();
		     
		     StringTokenizer sk = new StringTokenizer(sb.toString(),",");
		     
		     List<String> values = new ArrayList<String>();
		     while(sk.hasMoreTokens()) {
		    	 String next  = sk.nextToken(",");
		    	 values.add(next);
		    	 System.out.println(next);
		    	 
		     }
		     
//		     PreparedStatement s = connection.prepareStatement("INSERT INTO stock_profile (idstock_profile, ticker, fullname, previousClosePrice, 52weekLow, 52weekHigh) VALUES (?,?,?,?,?,?)");
//		     s.setInt(1, 16578768);
//		     s.setString(2, values.get(0));
//		     s.setString(3, values.get(1));
//		     s.setFloat(4, Float.valueOf(values.get(2)));
//		     s.setFloat(5, Float.valueOf(values.get(3)));
//		     s.setFloat(6, Float.valueOf(values.get(4)));
//		     int count = s.executeUpdate ();
//		     s.close ();
//		     System.out.println (count + " rows were inserted");
		    
		
		 }
	
	}

}
