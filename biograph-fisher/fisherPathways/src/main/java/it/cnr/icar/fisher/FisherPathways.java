package it.cnr.icar.fisher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.eclipse.jetty.server.Server;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

public class FisherPathways extends AbstractHandler {

	Client client = null;
	LinkedHashMap<String,String> pathwaysNames = null;
	LinkedHashMap<String,Long> moleculesInPathways = null;
	int overallProteinsNumber;
	
    public static void main(String[] args) throws Exception {
    	Cluster cluster = Cluster.open();
    	Client client = cluster.connect();
    	
        Server server = new Server(8185);
        server.setHandler(new FisherPathways(client));
        server.start();
        server.join();
    }

    public FisherPathways(Client c) {
    	this.client = c;
    	pathwaysNames = new LinkedHashMap<String,String>();
    	moleculesInPathways = new LinkedHashMap<String,Long>();
    	initPathways();
    	overallProteinsNumber = getOverallProteinsNumber();
    }

    int getOverallProteinsNumber() {
    	int n = 0;
    	ResultSet results = client.submit("g.V().hasLabel('Protein').count()");
    	n = results.stream().findFirst().get().getInt();
    	return n;
    }

	void initPathways() {
    	ResultSet results = client.submit("g.V().hasLabel('Pathway').as('pathwayId','name','entities').select('pathwayId','name','entities').by('pathwayId').by('name').by(__.out('CONTAINS').hasLabel('Protein').dedup().count())");
    	results.stream().forEach((Result p) -> {
    		@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> m = (LinkedHashMap<String, Object>) p.getObject();
    		pathwaysNames.put((String)m.get("pathwayId"), (String)m.get("name"));
    		moleculesInPathways.put((String)m.get("pathwayId"), (Long)m.get("entities"));
    	});
    }
    
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	String geneList = request.getParameter("genes");
    	
    	response.setHeader("Access-Control-Allow-Origin", "*");
    	response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        String jsonString = "[]";
        
        if (geneList != null) {
        	String[] genesArray = geneList.split(",");
        	String opts = "";
        	
        	ArrayList<String> focusGenes = new ArrayList<String>();
        	ArrayList<String> focusPathways = new ArrayList<String>();

        	for (int i=0; i<genesArray.length; i++) {
        		focusGenes.add(genesArray[i]);
        		opts += ".option('" + genesArray[i] + "',__.as('" + genesArray[i] + "'))";
        	}

    		ResultSet results = client.submit("g.V().hasLabel('Gene').choose(values('nomenclatureAuthoritySymbol'))" + opts + ".out('CODING').in('CONTAINS').dedup().values('pathwayId')");
        	results.stream().forEach((Result p) -> focusPathways.add(p.getString()) );
        	
        	//JsonArray jsonResults = new JsonArray();
        	ArrayList<JsonObject> jsonValues = new ArrayList<JsonObject>();

    		for (String fp : focusPathways) {
    			ArrayList<String> foundEntities = new ArrayList<String>();
    			
    			results = client.submit("g.V().hasLabel('Pathway').has('pathwayId','" + fp + "').out('CONTAINS').in('CODING').choose(values('nomenclatureAuthoritySymbol'))" + opts + ".values('nomenclatureAuthoritySymbol').dedup()");
            	results.stream().forEach((Result p) -> { 
            		foundEntities.add(p.getString());
            	});

    			int a = foundEntities.size();
            	
//    			results = client.submit("g.V().hasLabel('Pathway').has('pathwayId','" + fp + "').out('CONTAINS').in('CODING').choose(values('nomenclatureAuthoritySymbol'))" + opts + ".count()");
//    			int a = results.stream().findFirst().get().getInt();
    			
    			int row1 = moleculesInPathways.get(fp).intValue();
        		int b = row1 - a;       		
        		int c = genesArray.length - a;
//        		int d = overallGenesNumber - row1 - c;
        		int d = overallProteinsNumber - row1 - c;
        		
        		try {
            		double pValue = getPValue(a, b, c, d);

        			String foundEntitiesString = String.join(",", foundEntities);

            		JsonObject obj = Json.object().add("id", fp).add("name", pathwaysNames.get(fp)).add("entitiesFound", a).add("entitiesList", foundEntitiesString).add("entitiesTotal", row1).add("pValue", pValue);
            		
            		jsonValues.add(obj);        			
        		} catch (Exception e) {
        			
        		}
        		//jsonResults.add(obj);
    		}
    		
    		Collections.sort( jsonValues, new Comparator<JsonObject>() {
    			private static final String KEY_NAME = "pValue";

				@Override
				public int compare(JsonObject a, JsonObject b) {
					Double valA = a.getDouble(KEY_NAME, 100);
					Double valb = b.getDouble(KEY_NAME, 100);
					
					return valA.compareTo(valb);
				}
    		});
    		   		
    		jsonString = jsonValues.toString();
    	}
        response.getWriter().println(jsonString);
    }

    double getPValue(int a, int b, int c, int d){
        int m=a+b+c+d;
    	double[] logFactorial = null;
        logFactorial = new double[m+1];
        logFactorial[0] = 0.0;
        for (int i = 1; i <= m; i++) {
            logFactorial[i] = logFactorial[i-1] + Math.log(i);
        }
        return fisher(a, b, c, d, logFactorial);
    }

    /** Calculate a p-value for Fisher's Exact Test. */
    private double fisher(int a, int b, int c, int d, double[] logFactorial) {
        if (a * d > b * c) {
            a = a + b; b = a - b; a = a - b; 
            c = c + d; d = c - d; c = c - d;
        }
        if (a > d) { a = a + d; d = a - d; a = a - d; }
        if (b > c) { b = b + c; c = b - c; b = b - c; }

        int a_org = a;
        double p_sum = 0.0d;

        double p = fisherSub(a, b, c, d, logFactorial);
        double p_1 = p;

        while (a >= 0) {
            p_sum += p;
            if (a == 0) break;
            --a; ++b; ++c; --d;
            p = fisherSub(a, b, c, d, logFactorial);
        }

        a = b; b = 0; c = c - a; d = d + a;
        p = fisherSub(a, b, c, d, logFactorial);

        while (p < p_1) {
            if (a == a_org) break;
            p_sum += p;
            --a; ++b; ++c; --d;
            p = fisherSub(a, b, c, d, logFactorial);
        }
        return p_sum;
    }

    private double fisherSub(int a, int b, int c, int d, double[] logFactorial) {
        return Math.exp(logFactorial[a + b] +
                        logFactorial[c + d] +
                        logFactorial[a + c] +
                        logFactorial[b + d] -
                        logFactorial[a + b + c + d] -
                        logFactorial[a] -
                        logFactorial[b] -
                        logFactorial[c] -
                        logFactorial[d]);
    }
}
