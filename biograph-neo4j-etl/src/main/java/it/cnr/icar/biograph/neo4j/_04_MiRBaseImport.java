package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojavax.Comment;
import org.biojavax.Namespace;
import org.biojavax.RankedCrossRef;
import org.biojavax.RankedDocRef;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class _04_MiRBaseImport {

	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	        }
	    } );
	}
	
	private static String timeConversion(long seconds) {

	    final int MINUTES_IN_AN_HOUR = 60;
	    final int SECONDS_IN_A_MINUTE = 60;

	    long minutes = seconds / SECONDS_IN_A_MINUTE;
	    seconds -= minutes * SECONDS_IN_A_MINUTE;

	    long hours = minutes / MINUTES_IN_AN_HOUR;
	    minutes -= hours * MINUTES_IN_AN_HOUR;

	    return hours + " hours " + minutes + " minutes " + seconds + " seconds";
	}
	
	static String DB_PATH = "/biograph.db";
	
	public static void main(String[] args) throws IOException, NoSuchElementException, BioException {
		String homeDir = System.getProperty("user.home");
		File dbPath = new File(homeDir + DB_PATH);
		
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
		registerShutdownHook( graphDb );
		
        int entryCounter = 0;
        int edgeCounter = 0;
        long startTime = System.currentTimeMillis();
        String fileName = homeDir + "/biodb/miRNA.dat";
        
        System.out.print("\nImporting miRNA entries from " + fileName + " ");

        BufferedReader br = new BufferedReader(new FileReader(fileName)); 
		Namespace ns = RichObjectFactory.getDefaultNamespace();
		RichSequenceIterator seqs = RichSequence.IOTools.readEMBLRNA(br, ns);
		
		Label MIRNA = DynamicLabel.label( "MiRNA" );
		Label MIRNA_MATURE = DynamicLabel.label( "MiRNAmature" );
		Label PUBMED = DynamicLabel.label( "Pubmed" );
		
		try ( Transaction tx = graphDb.beginTx() ) {
			while (seqs.hasNext()) {
				RichSequence entry = seqs.nextRichSequence();
	
	            entryCounter++;
	
				String accession = entry.getAccession();
				String name = entry.getName();
				String description = entry.getDescription();
				Vector<String> dbReferences = new Vector<String>();
				Vector<String> comments = new Vector<String>();
				
				for (Comment comment : entry.getComments()) {
					String cmt = comment.getComment().replaceAll("\n", " ");
					comments.add(cmt);
				}
				String comment = "";
				if (comments.size() > 0)
					comment = comments.get(0);
	
				for (RankedCrossRef docRef : entry.getRankedCrossRefs()) {
					String reference = docRef.getCrossRef().getDbname() + " " + docRef.getCrossRef().getAccession();
					dbReferences.add(reference);
				}
	
				String sequence = entry.getInternalSymbolList().seqString();
				
				Node vmirna = graphDb.createNode( MIRNA );
				vmirna.setProperty("accession", accession);
				vmirna.setProperty("name", name);
				vmirna.setProperty("description", description);
				vmirna.setProperty("comment", comment);
				vmirna.setProperty("sequence", sequence);
	
				for (RankedDocRef docRef : entry.getRankedDocRefs()) {
					if (docRef.getDocumentReference().getCrossref() != null) {
						String db = docRef.getDocumentReference().getCrossref().getDbname();
						
						if (db.equals("PUBMED")) {
							String reference = docRef.getDocumentReference().getCrossref().getAccession();
							
							ResourceIterator<Node> it = graphDb.findNodes( PUBMED, "pubmedId", reference );
							Node citation;
							
							if (it.hasNext())
								citation = it.next();
							else {
								citation = graphDb.createNode( PUBMED );
								citation.setProperty("pubmedId", reference);
							}
							
							vmirna.createRelationshipTo(citation, RelationTypes.CITED_IN);
			        		edgeCounter++;
						}
					}
				}
	
				Iterator<Feature> itf = entry.getFeatureSet().iterator();
				
				while (itf.hasNext()) {
					Feature f = itf.next();
					
					String location = f.getLocation().toString();
					String subSequence = sequence.substring(f.getLocation().getMin()-1, f.getLocation().getMax());
					
					entryCounter++;
					
					Node mature = graphDb.createNode( MIRNA_MATURE );
					mature.setProperty("location", location);
					mature.setProperty("sequence", subSequence);
					
					@SuppressWarnings("unchecked")
					Map<Object, ?> map = f.getAnnotation().asMap();
					Set<Object> keys = map.keySet();
					for (Object key : keys) {
						String keyString = key.toString();
						String value = (String) map.get(key);
						
						mature.setProperty(keyString.substring(keyString.lastIndexOf(":")+1), value);
					}
					
					edgeCounter++;
					
					vmirna.createRelationshipTo(mature, RelationTypes.PRECURSOR_OF);
				}
				
	            if (entryCounter % 1000 == 0) {
	            	System.out.print("."); System.out.flush();
	            }
			}
			tx.success();
		}
		
        long stopTime = (System.currentTimeMillis()-startTime)/1000;
        System.out.println("\n\nCreated " + entryCounter + " vertices and " + edgeCounter + " edges in " + timeConversion(stopTime));

		graphDb.shutdown();
	}
}
