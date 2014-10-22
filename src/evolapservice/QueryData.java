package evolapservice;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;


/**
 * QueryData is the public interface to request event data for custom queries from the server.
 * 
 * Author(s):
 * 		Pathum Mudannayake
 * */


@Path("/evquery")
public class QueryData {
	  
	  /* 
	   * Returns a json string with all the event data for a given query ordered in a hierarchical layout
	   */	  
	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  
	  @Path("/")
	  public String returnJsonQResults(@QueryParam("filter") String queryFilter) {

		  return executeQueries(queryFilter);

	  }

	  
	  private String executeQueries(String queryFilterJson){  
		  
		Gson gson = new Gson();
		
		List<Object> queryResults = new ArrayList<Object> ();
		
		@SuppressWarnings("unchecked")
		List<ArrayList<String>> queryObject = gson.fromJson(queryFilterJson, ArrayList.class);
		
		Deque<String> paramStack = new LinkedList<String>();
			
		/*The initial list index is 0. So that, the traversal starts from the first object*/
		fillData(queryResults, queryObject, paramStack, 0);  

		return gson.toJson(queryResults);

	  }
	  
	  
	  
	  /* Following method creates query filters for each set of query parameters. This is essentially a cross-product between all
	   * the query parameters. Does so, using a recursive call to the method from the parent levels and an iterative traversal of leafs.
	   * The data thus extracted is used to populate the q_results object alongside the relevant event attributes.
	   * 
	   * Parameters:
	   * 		queryResults: A List that holds the results from the query.
	   * 		requestList: A List with the query parameters. The list in fact is composed of several sub-lists with filters
	   * 		parentParamStack: A stack to keep track of the previous parameters that were explored as a result of creating the query  
	   * 		listIndex: An integer index to keep track of the explored sub-list
	   * */
	  private void fillData(List<Object> queryResults, List<ArrayList<String>> requestList, Deque<String> parentParamStack, int listIndex){

			if(listIndex >= (requestList.size()-1)){

				StringBuilder sb = new StringBuilder();
				Iterator<String> listIterator = parentParamStack.iterator();
				
				Map<String,Object> innerMostMap = null;
				Map<String,Object> frontMap = null;
				
				if(innerMostMap == null)
					innerMostMap = new HashMap<String, Object> ();
				
				if(frontMap == null)
					frontMap = innerMostMap;
				
				while(listIterator.hasNext()){
					String dimName = listIterator.next();
					
					sb.append(","+ dimName);
					
					Map<String,Object> newMap = new HashMap<String, Object> ();

					innerMostMap.put(dimName, newMap);
					innerMostMap = newMap;
					
				}
				
				for(String dimElement: requestList.get(listIndex)){

					String qFilter = dimElement+sb.toString();
					innerMostMap.put(dimElement, extractQueryData(qFilter));

				}	
				
				queryResults.add(frontMap);
				
				return;
			}
			
			
			 for(String s: requestList.get(listIndex)){
				parentParamStack.push(s);
				fillData(queryResults, requestList, parentParamStack, ++listIndex);
				parentParamStack.pop();
				--listIndex;
			}
			 
		}

	  
	 
	  private List<ArrayList<String>> extractQueryData(String queryFilter){

		/* An event is described with a start and end time. There can be other attributes to it, such as the magnitude of a certain property.
		 * A single query template is used to query the OLAP server to extract event attributes for different types of filters.
		 * */  
		String customQueryStr = "SELECT NON EMPTY({[Measures].[Start_Timestamp], [Measures].[End_Timestamp], [Measures].[Change]})  ON COLUMNS, NON EMPTY([StartTime].[Day].Members) ON ROWS FROM [Events] WHERE ("+queryFilter+")";
		
		QueryDataExtractor olapDataExtractor = new QueryDataExtractor();
		
		return olapDataExtractor.extractOlapData(customQueryStr);
	  } 
	
}