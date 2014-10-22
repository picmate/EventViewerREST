package evolapservice;

import java.sql.Connection;
import java.sql.DriverManager;

import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;

/**
 * OlapConnect provides utility methods for other classes to establish a connection to the OLAP server and eventually request data
 * through the OLAP server by passing in MDX queries.
 * 
 * Author(s):
 * 		Pathum Mudannayake
 * */

public final class OlapConnect {
	
    private Connection connection;
    private OlapConnection olapConnection;
    private static OlapConnect olapCon;
    private CellSet cellSet;
    
    static String schemaPath;
    
    /*The visibility of the constructor is restricted. Therefore, other classes are not supposed
    to instantiate this class. All the utility methods are static and public.*/
    private OlapConnect() {
     	
    	//Connection from outside to the dev and test db
        String pgOlapConStr= "jdbc:mondrian:Jdbc=jdbc:postgresql://eventviewer.asap.um.maine.edu:5432/EventViewerDM_Dev;JdbcUser=postgres;JdbcPassword=123;Catalog=file:"+OlapConnect.schemaPath+";JdbcDrivers=org.postgresql.Driver";
        String userName = "postgres";
        String password = "123";
    	
    	//Connection from localhost(production/test server) to the dev and test db
/*    	String pgOlapConStr= "jdbc:mondrian:Jdbc=jdbc:postgresql://localhost:5432/EventViewerDM_Dev;JdbcUser=postgres;JdbcPassword=123;Catalog=file:"+OlapConnect.schemaPath+";JdbcDrivers=org.postgresql.Driver";
        String userName = "postgres";
        String password = "123";*/
    	
        try {
        	Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
			connection = DriverManager.getConnection(pgOlapConStr,userName,password);
			olapConnection = connection.unwrap(OlapConnection.class);
        }
        catch (Exception conExp) {
        	conExp.printStackTrace();
        }
    }
    
    /**
     *
     * @return olap connection object
     */
    public static synchronized OlapConnect getOlapCon() {
        if ( olapCon == null ) {
        	olapCon = new OlapConnect();
        }
        return olapCon;
    }
    
    /**
     *
     * @param query String The query to be executed
     * @return a CellSet object containing the results or null if not available
     * @throws OlapException
     */
    public CellSet query(String query) throws OlapException{
    	cellSet = olapCon.olapConnection.createStatement().executeOlapQuery(query);
        return cellSet;
    }
  
}