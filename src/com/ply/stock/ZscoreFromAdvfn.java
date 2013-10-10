package com.ply.stock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

public class ZscoreFromAdvfn {
	
	@Test
	public void retrieve_DebpershareFromAdvfn_quarterly() throws ClientProtocolException, IOException, DataFormatException, Exception
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
			String strFile = "/Users/minglei/Documents/StockDataInMysql/sp4571.csv";
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
		//List<String> tickers = new ArrayList<String>();
		//tickers.add("GOOG"); tickers.add("JPM"); //tickers.add("AAPL"); tickers.add("GE"); tickers.add("F");
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
											if(!td.text().equals(""))
												alist.add(td.text());
										}
									}	
							
									System.out.println(td.text()+ " i=" +i);
									i++;		
								}
							}
							System.out.println(aMap);
						
							List<String> longdebt = new ArrayList<String>();
							List<String> shortdebt = new ArrayList<String>();
							List<String> totalshare = new ArrayList<String>();
							List<String> timeperiods = new ArrayList<String>();
							List<String> totallias = new ArrayList<String>();
							
							for(String key: aMap.keySet())
							{	
								if("quarter end date".equalsIgnoreCase(key)){
									timeperiods = aMap.get(key);
								}
								if("long-term debt".equalsIgnoreCase(key)){
									longdebt = aMap.get(key);
								}
								if("short-term debt".equalsIgnoreCase(key)){
									shortdebt = aMap.get(key);
								}
								if("Total Liabilities".equalsIgnoreCase(key)){
									totallias = aMap.get(key);
								}
								
								if("total common shares out".equalsIgnoreCase(key)){
									totalshare = aMap.get(key);
								}
							}	
							String patternOne = "#,##0.00";
							DecimalFormat nf = new DecimalFormat(patternOne);
							for(int k=0; k<longdebt.size(); k++)
							{	
							
								String insertsql = "INSERT INTO stock.debtPerShare (ticker, day, year, quarter, longdebt, shortdebt, totalshare, totalLiability) VALUES (?,?,?,?,?,?,?,?)";
								PreparedStatement s = dbconnection.prepareStatement(insertsql);
								s.setString(1,ticker);
								
								s.setString(2,timeperiods.get(k));  
								System.out.println(timeperiods.get(k));
								if(!(timeperiods.get(k).equals("") || timeperiods.get(k).equals("0000/00") ||timeperiods.get(k).equals("0000/0") || timeperiods.get(k).equals("0000-00") ||timeperiods.get(k).equals("0000-0")))
								{	
									SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM");
									Date day = new Date();
									try
									{
										day = dateFormatter.parse(timeperiods.get(k));
									}	
									catch(ParseException e)
									{
										SimpleDateFormat dateFormatter2 = new SimpleDateFormat("yyyy-MM");
										day = dateFormatter2.parse(timeperiods.get(k));
									}
										
									s.setInt(3, (day.getYear()+1900));
									if(((day.getMonth()+1)<=3) && ((day.getMonth()+1)>=1))
									{	
										s.setInt(4, 1);
									}	
									if(((day.getMonth()+1)<=6) && ((day.getMonth()+1)>=4))
									{	
										s.setInt(4, 2);
									}	
									if(((day.getMonth()+1)<=9) && ((day.getMonth()+1)>=6))
									{	
										s.setInt(4, 3);
									}	
									if(((day.getMonth()+1)<=12) && ((day.getMonth() +1)>=10))
									{	
										s.setInt(4, 4);
									}	
								}
							
								else
								{
									s.setString(2,""); 
								}
								
								if(longdebt.size()>0)
								{	
									if(!longdebt.get(k).equals(""))
									{	
										s.setDouble(5,nf.parse(longdebt.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(5, 0.0);
									}
									System.out.println(longdebt.get(k));
								}
								else
								{
									s.setDouble(5, 0.0);
								}
								if(shortdebt.size()>0)
								{
									if(!shortdebt.get(k).equals(""))
									{
										s.setDouble(6,nf.parse(shortdebt.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(6, 0.0);
									}
									System.out.println(shortdebt.get(k));
								}
								else
								{
									s.setDouble(6, 0.0);
								}
								if(totalshare.size()>0)
								{	
									if(!totalshare.get(k).equals(""))
									{	
										s.setDouble(7,nf.parse(totalshare.get(k)).doubleValue());
									}	
									else
									{
										s.setDouble(7, 0.0);
									}
									System.out.println(totalshare.get(k));
								}
								else
								{
									s.setDouble(7, 0.0);
								}
								if(totallias.size()>0)
								{	
									if(!totallias.get(k).equals(""))
									{	
										s.setDouble(8,nf.parse(totallias.get(k)).doubleValue());
									}	
									else
									{
										s.setDouble(8, 0.0);
									}
									System.out.println(totallias.get(k));
								}
								else
								{
									s.setDouble(8, 0.0);
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
	public void retrieve_ZScoreFromAdvfn_year() throws ClientProtocolException, IOException, DataFormatException, Exception
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
		//List<String> tickers = new ArrayList<String>();
		//tickers.add("GOOG"); tickers.add("JPM"); //tickers.add("AAPL"); tickers.add("GE"); tickers.add("F");
		for(String ticker: tickets)
		{	
			String str_url = "http://ih.advfn.com/p.php?pid=financials&btn=annual_reports&ctl00%24sb3%24tbq1=Get+Quote&as_values_IH=&ctl00%24sb3%24stb1=Search+iHub&mode=&symbol=" +ticker;
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
											if(!td.text().equals(""))
												alist.add(td.text());
										}
									}	
							
									System.out.println(td.text()+ " i=" +i);
									i++;		
								}
							}
							System.out.println(aMap);
						
							List<String> workingcapitals = new ArrayList<String>();
							List<String> totalassets = new ArrayList<String>();
							List<String> retainearnings = new ArrayList<String>();
							List<String> ebits = new ArrayList<String>();
							List<String> marketvalues = new ArrayList<String>();						
							List<String> totalliabilities = new ArrayList<String>();						
							List<String> sales = new ArrayList<String>();
							List<String> timeperiods = new ArrayList<String>();
							
							for(String key: aMap.keySet())
							{	
								if("Year end date".equalsIgnoreCase(key)){
									timeperiods = aMap.get(key);
								}
								if("working capital".equalsIgnoreCase(key)){
									workingcapitals = aMap.get(key);
								}
								if("total assets".equalsIgnoreCase(key)){
									totalassets = aMap.get(key);
								}if("retained earnings".equalsIgnoreCase(key)){
									retainearnings = aMap.get(key);
								}if("EBIT".equalsIgnoreCase(key)){
									ebits = aMap.get(key);
								}if("total equity".equalsIgnoreCase(key)){
									marketvalues = aMap.get(key);
								}if("total liabilities".equalsIgnoreCase(key)){
									totalliabilities = aMap.get(key);
								}if("operating revenue".equalsIgnoreCase(key)){
									sales = aMap.get(key);
								}
							}	
							String patternOne = "#,##0.00";
							DecimalFormat nf = new DecimalFormat(patternOne);
							for(int k=0; k<workingcapitals.size(); k++)
							{	
							
								String insertsql = "INSERT INTO stock.zscore_year (ticket, day, year, workingcapital, totalasset, retainedearning, ebit,maketvalueequity, totalliablities, sales) VALUES (?,?,?,?,?,?,?,?,?,?)";
								PreparedStatement s = dbconnection.prepareStatement(insertsql);
								s.setString(1,ticker);
								if(timeperiods.size()>0)
								{
									s.setString(2,timeperiods.get(k));  
									System.out.println(timeperiods.get(k));
									if(!(timeperiods.get(k).equals("") || timeperiods.get(k).equals("0000/00") ||timeperiods.get(k).equals("0000/0") || timeperiods.get(k).equals("0000-00") ||timeperiods.get(k).equals("0000-0")))
									{	
										SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM");
										Date day = new Date();
										try
										{
											day = dateFormatter.parse(timeperiods.get(k));
											if(day.equals("")) break;
										}	
										catch(ParseException e)
										{
											SimpleDateFormat dateFormatter2 = new SimpleDateFormat("yyyy-MM");
											day = dateFormatter2.parse(timeperiods.get(k));
										}
											
										s.setInt(3, (day.getYear()+1900));
									}
								}
								else
								{
									s.setString(2,""); 
								}
								if(workingcapitals.size()>0)
								{	
									if(!workingcapitals.get(k).equals(""))
									{	
										s.setDouble(4,nf.parse(workingcapitals.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(4, 0.0);
									}
									System.out.println(workingcapitals.get(k));
								}
								else
								{
									s.setDouble(4, 0.0);
								}
								if(totalassets.size()>0)
								{
									if(!totalassets.get(k).equals(""))
									{
										s.setDouble(5,nf.parse(totalassets.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(5, 0.0);
									}
									System.out.println(totalassets.get(k));
								}
								else
								{
									s.setDouble(5, 0.0);
								}
								if(retainearnings.size()>0)
								{	
									if(!retainearnings.get(k).equals(""))
									{	
										s.setDouble(6,nf.parse(retainearnings.get(k)).doubleValue());
									}	
									else
									{
										s.setDouble(6, 0.0);
									}
									System.out.println(retainearnings.get(k));
								}
								else
								{
									s.setDouble(6, 0.0);
								}
								if(ebits.size()>0)
								{
									if(!ebits.get(k).equals(""))
									{	
										s.setDouble(7,nf.parse(ebits.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(7, 0.0);
									}
									System.out.println(ebits.get(k));
								}
								else
								{
									s.setDouble(7, 0.0);
								}
								if(marketvalues.size()>0)
								{
									if(!marketvalues.get(k).equals(""))
									{	
										s.setDouble(8,nf.parse(marketvalues.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(8, 0.0);
									}
									System.out.println(marketvalues.get(k));
								}
								else
								{
									s.setDouble(8, 0.0);
								}
								if(totalliabilities.size()>0)
								{
									if(!totalliabilities.get(k).equals(""))
									{	
										s.setDouble(9,nf.parse(totalliabilities.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(9, 0.0);
									}
									System.out.println(totalliabilities.get(k));
								}
								else
								{
									s.setDouble(9, 0.0);
								}
								if(sales.size()>0)
								{
									if(!sales.get(k).equals(""))
									{	
										s.setDouble(10,nf.parse(sales.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(10, 0.0);
									}
									System.out.println(sales.get(k));
								}
								else
								{
									s.setDouble(10, 0.0);
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
	public void retrieve_ZScoreFromAdvfn() throws ClientProtocolException, IOException, DataFormatException, Exception
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
		//List<String> tickers = new ArrayList<String>();
		//tickers.add("GOOG"); tickers.add("JPM"); //tickers.add("AAPL"); tickers.add("GE"); tickers.add("F");
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
						
							List<String> workingcapitals = new ArrayList<String>();
							List<String> totalassets = new ArrayList<String>();
							List<String> retainearnings = new ArrayList<String>();
							List<String> ebits = new ArrayList<String>();
							List<String> marketvalues = new ArrayList<String>();						
							List<String> totalliabilities = new ArrayList<String>();						
							List<String> sales = new ArrayList<String>();
							List<String> timeperiods = new ArrayList<String>();
							
							for(String key: aMap.keySet())
							{	
								if("quarter end date".equalsIgnoreCase(key)){
									timeperiods = aMap.get(key);
								}
								if("working capital".equalsIgnoreCase(key)){
									workingcapitals = aMap.get(key);
								}
								if("total assets".equalsIgnoreCase(key)){
									totalassets = aMap.get(key);
								}if("retained earnings".equalsIgnoreCase(key)){
									retainearnings = aMap.get(key);
								}if("EBIT".equalsIgnoreCase(key)){
									ebits = aMap.get(key);
								}if("total equity".equalsIgnoreCase(key)){
									marketvalues = aMap.get(key);
								}if("total liabilities".equalsIgnoreCase(key)){
									totalliabilities = aMap.get(key);
								}if("operating revenue".equalsIgnoreCase(key)){
									sales = aMap.get(key);
								}
							}	
							String patternOne = "#,##0.00";
							DecimalFormat nf = new DecimalFormat(patternOne);
							for(int k=0; k<workingcapitals.size(); k++)
							{	
							
								String insertsql = "INSERT INTO stock.zscore (ticket, day, year, quater, workingcapital, totalasset, retainedearning, ebit,maketvalueequity, totalliablities, sales) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
								PreparedStatement s = dbconnection.prepareStatement(insertsql);
								s.setString(1,ticker);
								if(timeperiods.size()>0)
								{
									s.setString(2,timeperiods.get(k));  
									System.out.println(timeperiods.get(k));
									if(!(timeperiods.get(k).equals("") || timeperiods.get(k).equals("0000/00") ||timeperiods.get(k).equals("0000/0") || timeperiods.get(k).equals("0000-00") ||timeperiods.get(k).equals("0000-0")))
									{	
										SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM");
										Date day = new Date();
										try
										{
											day = dateFormatter.parse(timeperiods.get(k));
										}	
										catch(ParseException e)
										{
											SimpleDateFormat dateFormatter2 = new SimpleDateFormat("yyyy-MM");
											day = dateFormatter2.parse(timeperiods.get(k));
										}
											
										s.setInt(3, (day.getYear()+1900));
										if(((day.getMonth()+1)<=3) && ((day.getMonth()+1)>=1))
										{	
											s.setInt(4, 1);
										}	
										if(((day.getMonth()+1)<=6) && ((day.getMonth()+1)>=4))
										{	
											s.setInt(4, 2);
										}	
										if(((day.getMonth()+1)<=9) && ((day.getMonth()+1)>=6))
										{	
											s.setInt(4, 3);
										}	
										if(((day.getMonth()+1)<=12) && ((day.getMonth() +1)>=10))
										{	
											s.setInt(4, 4);
										}	
									}
								}
								else
								{
									s.setString(2,""); 
								}
								if(workingcapitals.size()>0)
								{	
									if(!workingcapitals.get(k).equals(""))
									{	
										s.setDouble(5,nf.parse(workingcapitals.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(5, 0.0);
									}
									System.out.println(workingcapitals.get(k));
								}
								else
								{
									s.setDouble(5, 0.0);
								}
								if(totalassets.size()>0)
								{
									if(!totalassets.get(k).equals(""))
									{
										s.setDouble(6,nf.parse(totalassets.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(6, 0.0);
									}
									System.out.println(totalassets.get(k));
								}
								else
								{
									s.setDouble(6, 0.0);
								}
								if(retainearnings.size()>0)
								{	
									if(!retainearnings.get(k).equals(""))
									{	
										s.setDouble(7,nf.parse(retainearnings.get(k)).doubleValue());
									}	
									else
									{
										s.setDouble(7, 0.0);
									}
									System.out.println(retainearnings.get(k));
								}
								else
								{
									s.setDouble(7, 0.0);
								}
								if(ebits.size()>0)
								{
									if(!ebits.get(k).equals(""))
									{	
										s.setDouble(8,nf.parse(ebits.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(8, 0.0);
									}
									System.out.println(ebits.get(k));
								}
								else
								{
									s.setDouble(8, 0.0);
								}
								if(marketvalues.size()>0)
								{
									if(!marketvalues.get(k).equals(""))
									{	
										s.setDouble(9,nf.parse(marketvalues.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(9, 0.0);
									}
									System.out.println(marketvalues.get(k));
								}
								else
								{
									s.setDouble(9, 0.0);
								}
								if(totalliabilities.size()>0)
								{
									if(!totalliabilities.get(k).equals(""))
									{	
										s.setDouble(10,nf.parse(totalliabilities.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(10, 0.0);
									}
									System.out.println(totalliabilities.get(k));
								}
								else
								{
									s.setDouble(10, 0.0);
								}
								if(sales.size()>0)
								{
									if(!sales.get(k).equals(""))
									{	
										s.setDouble(11,nf.parse(sales.get(k)).doubleValue());
									}
									else
									{
										s.setDouble(11, 0.0);
									}
									System.out.println(sales.get(k));
								}
								else
								{
									s.setDouble(11, 0.0);
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
		//List<String> tickers = new ArrayList<String>();
		//tickers.add("GOOG"); tickers.add("JPM"); //tickers.add("AAPL"); tickers.add("GE"); tickers.add("F");
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


}
