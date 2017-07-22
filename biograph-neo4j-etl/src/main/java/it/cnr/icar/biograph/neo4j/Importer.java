package it.cnr.icar.biograph.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;

public abstract class Importer {

	protected int entryCounter = 0;
	protected int edgeCounter = 0;

	private String timeConversion(long seconds) {
	    final int MINUTES_IN_AN_HOUR = 60;
	    final int SECONDS_IN_A_MINUTE = 60;

	    long minutes = seconds / SECONDS_IN_A_MINUTE;
	    seconds -= minutes * SECONDS_IN_A_MINUTE;

	    long hours = minutes / MINUTES_IN_AN_HOUR;
	    minutes -= hours * MINUTES_IN_AN_HOUR;

	    return hours + " hours " + minutes + " minutes " + seconds + " seconds";
	}

	public void importData(GraphDatabaseService graphDb, String fileName) throws Exception {
        long startTime = System.currentTimeMillis();

        importer(graphDb, fileName);
        
        long stopTime = (System.currentTimeMillis()-startTime)/1000;
        System.out.println("\n\nCreated " + entryCounter + " vertices and " + edgeCounter + " edges in " + timeConversion(stopTime));
	}
	
	protected abstract void importer(GraphDatabaseService graphDb, String fileName) throws Exception;
}
