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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.google.cloud.sql.Driver;




public class StockRetriever {
	
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
                String strFile = "/Users/minglei/Documents/test/sp500.csv";
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
                String strFile = "/Users/minglei/Documents/test/nasdaq1500.csv";
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
		String strsql = "INSERT INTO key_financial (ticker, currentpe, peg, cpe, debtEquitratio, dividendYield, cashPerShare,  grossMargin, returnOnEquity, y3enegrowthrate, currentRatio) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement s = dbconnection.prepareStatement(strsql);
	
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
					
				s.execute();
			
				
			}
			
			
			//s.executeBatch();
			 
			//dbconnection.commit();
		}
		finally 
		{
			s.close();
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
