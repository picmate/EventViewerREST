package evolapservice;

import java.util.ArrayList;
import java.util.List;

import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.Position;

/**
 * QueryDataExtractor facilitates the extraction of data from the OLAP server:
 * Each invocation of an QueryDataExtractor's extractOlapData() method reads the data
 * pertaining to a single OLAP query from the data-store and return the data back to the calling method.
 * 
 * Author(s):
 * 		Pathum Mudannayake
 * */

public class QueryDataExtractor {

	public List<ArrayList<String>> extractOlapData(String queryStr){
		
		List<ArrayList<String>> multiDJson = new ArrayList<ArrayList<String>>();
		
		ArrayList<String> event; 
		CellSet cellSet = null;
		
		
		/*Read in the positions each data element is in the 
		 * returned data-set and use these position information to call each cell for their values
		 * */
		try{
			
			cellSet = OlapConnect.getOlapCon().query(queryStr);

			for(Position rowPos : cellSet.getAxes().get(1)){
				
				event = new ArrayList<String> ();
				
				for(Position colPos : cellSet.getAxes().get(0)){
						Cell measureCell = cellSet.getCell(colPos, rowPos);
						Object cellValue = measureCell.getValue();
						
						event.add(cellValue.toString());
					}
				
				multiDJson.add(event);
				
			}
			
		}catch(Exception e){
			System.err.println(e);
		}
		
		return multiDJson;
		
	}

}

