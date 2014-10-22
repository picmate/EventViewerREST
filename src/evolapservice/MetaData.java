package evolapservice;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * MetaData is the public interface to request event related meta-data from the server.
 * Such meta-data provides the back-bone of the application providing users the ability to create queries
 * to request specific event data. A front-end API call should be directed to this class for meta-data. It is assumed that
 * this class is invoked first and foremost before any other information is requested from the related services. This class
 * initializes the schema path to the OLAP schema on which other processes depend.
 * 
 * Author(s):
 * 		Pathum Mudannayake
 * */



@Path("/evmeta")
public class MetaData {
	
		  String metadata;	
	
		  @Context
			 private ServletContext servletContext;
		  
		  /* 
		   * Returns a json string with all the meta-data grouped in to the three distinct dimensions: time, location and theme
		   */	
		  @GET
		  @Produces(MediaType.APPLICATION_JSON)
		  public String returnJsonMetaResults() {
			  
			  OlapConnect.schemaPath = servletContext.getRealPath("EventViewerOLAPSchema.xml");
			  
			  extractMetaData();  
			  return metadata;
		  }
	

		  /*Following unchecked warning applies to assigning returned List<? extends Map<String,Object>> type from MetaDataExtractor 
		   * to ArrayList<HashMap<String, Object>>.Therefore, this warning is suppressed.  
		   * */
		  @SuppressWarnings("unchecked")
		private void extractMetaData(){

			List<ArrayList<HashMap<String,Object>>> results = new ArrayList<ArrayList<HashMap<String,Object>>> ();
			
			String locQueryStr = "WITH "+
								"MEMBER [Measures].[ParentName] AS [Location].CurrentMember.Parent.Name "+
								"MEMBER [Measures].[Label] AS [Location].CurrentMember.Caption "+
								"MEMBER [Measures].[Name] AS [Location].CurrentMember.Name "+	
									"SELECT{"+
									        "[Measures].[ParentName], [Measures].[Label], [Measures].[Name]"+
									        "} ON COLUMNS,"+
									"{EXCEPT([Location].AllMEMBERS ,{{[Location].[All Locations]},Descendants([Location].[NA])})} ON ROWS "+									
									"FROM [Events]";
			

			String themeQueryStr = "WITH "+
					"MEMBER [Measures].[ParentName] AS [Theme].CurrentMember.Parent.Name "+
					"MEMBER [Measures].[Label] AS [Theme].CurrentMember.Caption "+
					"MEMBER [Measures].[Name] AS [Theme].CurrentMember.Name "+	
						"SELECT{"+
						        "[Measures].[ParentName], [Measures].[Label], [Measures].[Name]"+
						        "} ON COLUMNS,"+
						"{EXCEPT([Theme].AllMEMBERS ,{{[Theme].[All Themes]}, {[Theme].[Value Type].Members}, Descendants([Theme].[NA])})} ON ROWS "+									
						"FROM [Events]";
			
			String timeQueryStr = "WITH "+
					"MEMBER [Measures].[ParentName] AS [TimeFilter].CurrentMember.Parent.Name "+
					"MEMBER [Measures].[Label] AS [TimeFilter].CurrentMember.Level.Name "+
					"MEMBER [Measures].[Name] AS [TimeFilter].CurrentMember.Name "+	
						"SELECT{"+
						        "[Measures].[ParentName], [Measures].[Label], [Measures].[Name]"+
						        "} ON COLUMNS,"+
						"{EXCEPT([TimeFilter].AllMEMBERS ,{{[TimeFilter].[All Periods]}, {[TimeFilter].[Day].Members}, Descendants([TimeFilter].[9999])})} ON ROWS "+									
						"FROM [Events]";
			
			
			MetaDataExtractor metaDataExtractor = new MetaDataExtractor();
			
			results.add((ArrayList<HashMap<String, Object>>) metaDataExtractor.extractOlapData(locQueryStr));
			results.add((ArrayList<HashMap<String, Object>>) metaDataExtractor.extractOlapData(themeQueryStr));
			results.add((ArrayList<HashMap<String, Object>>) metaDataExtractor.extractOlapData(timeQueryStr));
			
			
			Gson gson = new Gson();
			metadata = gson.toJson(results);

		  } 
	  

}