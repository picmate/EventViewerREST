package evolapservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.olap4j.CellSet;
import org.olap4j.OlapException;
import org.olap4j.Position;

/**
 * MetaExtractor provides methods to extract data from the OLAP server:
 * Each invocation of an MetaDataExtractor's extractOlapData() method reads the data
 * pertaining to a single OLAP query from the data-store and organizes them into 
 * parent-child hierarchies which will then be returned to the calling method
 * to be easily parsed as json strings.
 * 
 * Author(s):
 * 		Pathum Mudannayake
 * */

public class MetaDataExtractor {

	/*Reads data from the Eventviewer OLAP data-store and executes the insertChild() method iteratively for each record 
	 * to organize the data into a unified parent-child hierarchy.
	 * 
	 * Parameters: 
	 * 		queryStr: A string that represents a valid MDX query
	 * 
	 * Throws: 
	 * 		IllegalArgumentException: If the query string is empty
	 * 		IllegalStateException: If the returned data-set has inconsistencies 
	 * */
	
	public List<? extends Map<String,Object>> extractOlapData(String queryStr) throws IllegalArgumentException, IllegalStateException{
		
			if(queryStr == null)
				throw new IllegalArgumentException("Empty OLAP Query");
		
			/*This list holds the result-set returned from the OLAP server in a parent-child hierarchy*/
		  	List<? extends Map<String,Object>> multiDList = new ArrayList<HashMap<String,Object>>();

			CellSet cellSet = null;
			

				try {
					cellSet = OlapConnect.getOlapCon().query(queryStr);
				} catch (OlapException olapException) {
					throw new IllegalStateException("Eventviewer OLAP Exception", olapException);
				}
				
				for(Position rowPos : cellSet.getAxes().get(1)){
					
					List<Position> colPositions = cellSet.getAxes().get(0).getPositions();

					String parentName = cellSet.getCell(colPositions.get(0),rowPos).getValue().toString();
					String label = cellSet.getCell(colPositions.get(1),rowPos).getValue().toString();
					String name = cellSet.getCell(colPositions.get(2),rowPos).getValue().toString();
					
					if(!insertChild(multiDList, parentName, label, name))
						throw new IllegalStateException("Eventviewer Data Store Inconsistency");
					
				}
				
			return multiDList;

	  }
	
	/*An overloaded method of the insertChild(List, Map, Queue) method. This will call insertChild(List, Map, Queue) method to organize the data into the parent-child hierarchy.
	 * Construct a queue to be used in a list traversal and arranges the input in a way that is more readable.
	 * 
	 * Parameters:
	 * 		parentNode: A List that holds the root element(s) of a parent-child hierarchy. Sub lists inside this hold the children.
	 * 		parentName: ParentName of the current member as specified by the OLAP data-store
	 * 		label: Caption of the current member as specified by the OLAP data-store
	 * 		name: name of the current member as specified by the OLAP data-store
	 * */
	
	private boolean insertChild(List<? extends Map<String,Object>> parentNode, String parentName, String label, String name){
	  	
		Queue<ArrayList<HashMap<String,Object>>> pathQueue = new LinkedList<ArrayList<HashMap<String,Object>>>();
		HashMap<String,String> childAttributes = new HashMap<String,String> ();
		
		childAttributes.put("label",label);
		childAttributes.put("name",name);
		childAttributes.put("parentName",parentName);
		
		return insertChild(parentNode, childAttributes, pathQueue);
	}
	
	
	/*Inserts the data given by the row into the correct position in the parent-child hierarchy
	 * 
	 * Parameters:
	 *		parentNode: A List that holds the root element(s) of a parent-child hierarchy. Sub lists inside this hold the children.
	 *		childAttributes: parentName, label and name returned from the data-store 	
	 *		pathQueue: Queue that holds the remaining, unvisited paths
	 * 
	 * */
	
	/*The format into which the data is organized is:
	 * 
	 * label: "label"
	 * name : "name"
	 * list: [children]
	 * 
	 * Lists hold the elements in a given level of a specific sub-tree. The two attributes, label and name specifies the node while the
	 * list holds the children (each child is a map with a list pointing to its children).
	 * 
	 * The format of the returned data-set from the OLAP server guarantees the level hierarchy defined in the OLAP schema specification. 
	 * Therefore, always a parent comes before its children. This method strictly assumes that behavior.
	 * 
	 * To insert an incoming record, the method traverses the tree (specified by maps and lists) in a breadth first fashion. When the
	 * correct parent is found, by comparing the parent name of the current member with the name of the node, a new node is created and
	 * inserted into the list element of the parent.
	 * 
	 * If the pathQueue gets empty before the child is inserted correctly, such a condition signifies a data inconsistency. Therefore,
	 * the method returns false.  
	 * */
	
	
	/*Following unchecked warning applies to casting the type of nextElement.get("list") from Object to ArrayList<Object>.
	 *The code ensures the type of nextElement.get("list") to be ArrayList<Object>. Therefore, this warning
	 *is suppressed.  
	 * */
	@SuppressWarnings({ "unchecked" })
	private boolean insertChild(List<? extends Map<String,Object>> parentNode, Map<String, String> childAttributes, Queue<ArrayList<HashMap<String,Object>>> pathQueue){	
		
		/*Since called internally, assumes not to be true at all times*/
		if(parentNode == null)
			return false;
		
		/*Short circuit the search if the level is an year. Time behaves differently to other dimensions. Each year represents
		 * a separately rooted tree*/
		if(parentNode.isEmpty() || childAttributes.get("label").equals("Year")){
			
			Map<String, Object> rootElement = new HashMap<String, Object> ();
			
			rootElement.put("name", childAttributes.get("name"));
			rootElement.put("label", childAttributes.get("label"));
			rootElement.put("list", new ArrayList<Object>());
			
			((ArrayList<Object>)parentNode).add(rootElement);
			return true;
		}
		
		ListIterator<? extends Map<String,Object>> listIterator = parentNode.listIterator();
		
		
		/*Checks whether each of the elements in the given level (of a specific parent) is the parent of the
		 * current member and inserts if true. Else, add each of the children of the nodes to the pathQueue to be
		 * traversed later
		 */
		while(listIterator.hasNext()){
			Map<String,Object> nextElement = listIterator.next();
			
			if(nextElement.get("name").equals(childAttributes.get("parentName"))){
				
				HashMap<String, Object> child = new HashMap<String, Object> ();
				child.put("name", childAttributes.get("name"));
				child.put("label", childAttributes.get("label"));
				child.put("list", new ArrayList<Object>());
				
				((ArrayList<Object>)nextElement.get("list")).add(child);

				return true;
			}
			else if(!((ArrayList<Object>)nextElement.get("list")).isEmpty()){	
				pathQueue.add((ArrayList<HashMap<String,Object>>)nextElement.get("list"));
			}
			
		}
		
		if(insertChild(pathQueue.poll(), childAttributes, pathQueue)){
				return true;
		}		
		else{
			/*The queue is empty. According to the data set, queue should not be empty before a child is appropriately inserted. 
			A data inconsistency.*/
			return false;
		}

	}
	
}