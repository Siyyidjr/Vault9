package services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import models.Page;

public class SearchService implements Runnable {

	private WebService ws;
	private DbService dbs;
	private String url;
	
	public SearchService(String url) {
		//getServices
		this.ws = new WebService();
		this.dbs = new DbService();
		this.url = url;
	}

	@Override
	public void run() {
				//getWebsite
				Document doc = ws.getWebsite(url);	
				
				//transform
				String docAsString = doc.text();
				String dochAsHtml = doc.html();
		
				try {
					//owner
					int indexOfOwnerName = docAsString.indexOf("Owner: ");
					String name = docAsString.substring(indexOfOwnerName, indexOfOwnerName + 20);
					String owner = name.replaceAll("[^0-9]+", "");
					
					//title
					Element title = doc.select("h2").get(0);
					String titleString = title.toString().replaceAll("<h2>", "").replaceAll("</h2>", "");
					
					//number of links
					int latestVersion = 1;
					int numberOfLinks = 0;
//					Elements links = doc.select("a[href]");
//					numberOfLinks = 0;
//					for (Element element : links) {
//						String check = element.toString().toLowerCase();
//						if(!check.contains(">Latest version</a>")){
//							latestVersion = 1;
//						}
//						boolean doCount = true;	
//						
//						for (String string : Utilities.ignoredStrings) {
//							String a = string.toLowerCase();
//							String b = check.toLowerCase();
//							if(check.contains(string.toLowerCase()))
//								doCount = false;
//						}
//						
//						if(doCount)
//							numberOfLinks++;
//						
//						
//						System.out.println("Link: " + element.toString());
//					}
					
					if(dochAsHtml.contains(">Latest version</a>")){
						latestVersion = 0;
					}
					
					
					//number of attachments
					Element attachmentsString = doc.select("h3").get(4);
					Element ul = attachmentsString.nextElementSibling();
					Elements lis = ul.select("li");
					int numberOfAttachments = 0;
					for (Element element : lis) {
						numberOfAttachments++;
					}

					
					//number of previous Versions
					int numberOfPrevVersions = 0;
					try{
						int indexOfPrevVersions = docAsString.indexOf("Previous versions:");
						String prevVersions = docAsString.substring(indexOfPrevVersions, indexOfPrevVersions + 200);
						String[] ary = prevVersions.split("\\|");
						numberOfPrevVersions = ary.length - 2;
					}catch(Exception e){
						
					}

					
					
					//create the page
					Page page = new Page(titleString, docAsString, owner, numberOfLinks, numberOfAttachments, numberOfPrevVersions, url, latestVersion);
					
					
					//add to DB
					boolean success = dbs.addPageWithOwner(page, owner);
				} catch (Exception e) {
					System.out.println("This is no right page: " + url);
				}
	}

}
