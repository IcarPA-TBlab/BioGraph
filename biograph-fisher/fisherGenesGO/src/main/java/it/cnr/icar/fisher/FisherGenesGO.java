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

public class FisherGenesGO extends AbstractHandler {

	Client client = null;
	LinkedHashMap<String,String> goNames = null;
	LinkedHashMap<String,String> goNamespaces = null;
	LinkedHashMap<String,Long> moleculesInAnnotations = null;
	int overallGenesNumber;
	
    public static void main(String[] args) throws Exception {
    	Cluster cluster = Cluster.open();
    	Client client = cluster.connect();
    	
        Server server = new Server(8184);
        server.setHandler(new FisherGenesGO(client));
        server.start();
        server.join();
    }

    public FisherGenesGO(Client c) {
    	this.client = c;
    	goNames = new LinkedHashMap<String,String>();
    	goNamespaces = new LinkedHashMap<String,String>();
    	moleculesInAnnotations = new LinkedHashMap<String,Long>();
    	initAnnotations();
    	overallGenesNumber = getOverallGenesNumber();
    }

    int getOverallGenesNumber() {
    	int n = 0;
    	ResultSet results = client.submit("g.V().hasLabel('Gene').count()");
    	n = results.stream().findFirst().get().getInt();
    	return n;
    }

	void initAnnotations() {
    	ResultSet results = client.submit("g.V().hasLabel('Go').as('goId','name','namespace','entities').select('goId','name','namespace','entities').by('goId').by('name').by('namespace').by(__.out('ANNOTATES').hasLabel('Gene').dedup().count())");
    	results.stream().forEach((Result p) -> {
    		@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> m = (LinkedHashMap<String, Object>) p.getObject();
    		goNames.put((String)m.get("goId"), (String)m.get("name"));
    		goNamespaces.put((String)m.get("goId"), (String)m.get("namespace"));
    		moleculesInAnnotations.put((String)m.get("goId"), (Long)m.get("entities"));
    	});
    }
    
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	String geneList = request.getParameter("genes");
    	//System.out.println(geneList);

    	//JsonValue geneArray = Json.parse(geneList);
    	
    	//System.out.println(geneArray);
    	
    	response.setHeader("Access-Control-Allow-Origin", "*");
    	response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        String jsonString = "[]";
        
        if (geneList != null) {
        	String[] genesArray = geneList.split(",");
        	String opts = "";
        	
        	ArrayList<String> focusGenes = new ArrayList<String>();
        	ArrayList<String> focusAnnotations = new ArrayList<String>();

        	for (int i=0; i<genesArray.length; i++) {
        		//System.out.println(genesArray[i]);
        		focusGenes.add(genesArray[i]);
        		opts += ".option('" + genesArray[i] + "',__.as('" + genesArray[i] + "'))";
        	}

    		ResultSet results = client.submit("g.V().hasLabel('Gene').choose(values('nomenclatureAuthoritySymbol'))" + opts + ".in('ANNOTATES').dedup().values('goId')");
        	results.stream().forEach((Result p) -> focusAnnotations.add(p.getString()) );
        	
        	//JsonArray jsonResults = new JsonArray();
        	ArrayList<JsonObject> jsonValues = new ArrayList<JsonObject>();

    		for (String fp : focusAnnotations) {
    			ArrayList<String> foundEntities = new ArrayList<String>();
    			
    			results = client.submit("g.V().hasLabel('Go').has('goId','" + fp + "').out('ANNOTATES').hasLabel('Gene').choose(values('nomenclatureAuthoritySymbol'))" + opts + ".values('nomenclatureAuthoritySymbol').dedup()");
            	results.stream().forEach((Result p) -> { 
            		foundEntities.add(p.getString());
            	});

            	int a = foundEntities.size();
    			//int a = results.stream().findFirst().get().getInt();
    			int row1 = moleculesInAnnotations.get(fp).intValue();
        		int b = row1 - a;       		
        		int c = genesArray.length - a;
        		int d = overallGenesNumber - row1 - c;
        		
        		//System.out.println("a = " + a + ", b = " + b + ", c = " + c + ", d = " + d);
        		
        		try {
            		double pValue = getPValue(a, b, c, d);

            		String foundEntitiesString = String.join(",", foundEntities);
            		
            		JsonObject obj = Json.object().add("id", fp).add("name", goNames.get(fp)).add("namespace", goNamespaces.get(fp)).add("entitiesFound", a).add("entitiesList", foundEntitiesString).add("entitiesTotal", row1).add("pValue", pValue);
            		//System.out.println(obj);
            		
            		jsonValues.add(obj);
            		//jsonResults.add(obj);
        		} catch (Exception e) {
        			
        		}
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
