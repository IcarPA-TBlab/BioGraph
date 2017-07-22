package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.biojava.bio.seq.Feature;
import org.biojavax.Comment;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class MiRBase extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
        System.out.print("\nImporting miRNA entries from " + fileName + " ");

        BufferedReader br = new BufferedReader(new FileReader(fileName)); 
		Namespace ns = RichObjectFactory.getDefaultNamespace();
		RichSequenceIterator seqs = RichSequence.IOTools.readEMBLRNA(br, ns);
		
		Label MIRNA = DynamicLabel.label( "MiRNA" );
		Label MIRNA_MATURE = DynamicLabel.label( "MiRNAmature" );
		//Label PUBMED = DynamicLabel.label( "Pubmed" );
		
		try ( Transaction tx = graphDb.beginTx() ) {
			while (seqs.hasNext()) {
				
				RichSequence entry = seqs.nextRichSequence();
				
	            entryCounter++;
	
				String accession = entry.getAccession();
				String name = entry.getName();
				String description = entry.getDescription();
				//Vector<String> dbReferences = new Vector<String>();
				Vector<String> comments = new Vector<String>();
				
				for (Comment comment : entry.getComments()) {
					String cmt = comment.getComment().replaceAll("\n", " ");
					comments.add(cmt);
				}
				String comment = "";
				if (comments.size() > 0)
					comment = comments.get(0);
	
				/*
				for (RankedCrossRef docRef : entry.getRankedCrossRefs()) {
					String reference = docRef.getCrossRef().getDbname() + " " + docRef.getCrossRef().getAccession();
					dbReferences.add(reference);
				}
				*/
	
				String sequence = entry.getInternalSymbolList().seqString();
				
				Node vmirna = graphDb.createNode( MIRNA );
				vmirna.setProperty("accession", accession);
				vmirna.setProperty("name", name);
				vmirna.setProperty("description", description);
				vmirna.setProperty("comment", comment);
				vmirna.setProperty("sequence", sequence);
	
				/*
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
				*/
	
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
	}
}
