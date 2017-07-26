package it.cnr.icar.biograph.neo4j;

import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import it.cnr.icar.biograph.models.uniprot.CommentType;
import it.cnr.icar.biograph.models.uniprot.Entry;
import it.cnr.icar.biograph.models.uniprot.GeneType;
import it.cnr.icar.biograph.models.uniprot.IsoformType;
import it.cnr.icar.biograph.models.uniprot.OrganismType;
import it.cnr.icar.biograph.models.uniprot.ProteinType;
import it.cnr.icar.biograph.models.uniprot.SequenceType;
import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class Uniprot extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
		System.out.print("\nReading proteins entries from " + fileName + " ");

        //Label PUBMED = DynamicLabel.label( "Pubmed" );
        Label PROTEIN = DynamicLabel.label( "Protein" );
        Label PROTEIN_NAME = DynamicLabel.label( "ProteinName" );
        
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(fileName));
        xsr.nextTag(); // Advance to statements element
        
        JAXBContext jc = JAXBContext.newInstance(Entry.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        
        while (xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
            Entry entry = (Entry) unmarshaller.unmarshal(xsr);
            
            OrganismType organism = entry.getOrganism();
            String organismTaxonomyId = ((organism != null) && (!organism.getDbReference().isEmpty())) ? organism.getDbReference().get(0).getId() : "";
            
            if (organismTaxonomyId.equals("9606")) {
            	if (entry.getAccession().isEmpty())
            		continue;
            	
                try ( Transaction tx = graphDb.beginTx() ) {
	            	ProteinType prot = entry.getProtein();
	            	
	            	//String accession = entry.getAccession().get(0);
	            	String name = entry.getName().get(0);
	            	String fullName = ((prot.getRecommendedName() != null) && (prot.getRecommendedName().getFullName() != null)) ? prot.getRecommendedName().getFullName().getValue() : "";
	            	String alternativeName = ((!prot.getAlternativeName().isEmpty()) && (prot.getAlternativeName().get(0).getFullName() != null)) ? prot.getAlternativeName().get(0).getFullName().getValue() : "";
	            	
	            	String gene = "";
	            	if (!entry.getGene().isEmpty()) {
	            		GeneType geneType = entry.getGene().get(0);
	            		if (!geneType.getName().isEmpty()) {
	            			gene = geneType.getName().get(0).getValue();
	            		}
	            	}
	            	
	            	SequenceType seq = entry.getSequence();
	            	String sequence = seq.getValue();
	            	int sequenceLenght = seq.getLength();
	            	int sequenceMass = seq.getMass();
	            	
	            	Node protein = graphDb.createNode( PROTEIN );
	            	protein.setProperty("name", name);
	            	protein.setProperty("fullName", fullName);
	            	protein.setProperty("alternativeName", alternativeName);
	            	protein.setProperty("gene", gene);
	            	protein.setProperty("sequence", sequence);
	            	protein.setProperty("sequenceLenght", sequenceLenght);
	            	protein.setProperty("sequenceMass", sequenceMass);
	            	
	                entryCounter++;
	
	            	for (String accessionName : entry.getAccession()) {
	            		Node accession = graphDb.createNode( PROTEIN_NAME );
	            		accession.setProperty("name", accessionName);
	
	            		accession.createRelationshipTo(protein, RelationTypes.REFERS_TO);
	            		
	                    entryCounter++;
	                    edgeCounter++;
	            	}
	
	            	for (CommentType comment : entry.getComment()) {
	            		if (comment.getType().equals("function")) {
	            			protein.setProperty("function", comment.getText().get(0).getValue());
	            		} else if (comment.getType().equals("pathway")) {
	            			protein.setProperty("pathway", comment.getText().get(0).getValue());	            			
	            		} else if (comment.getType().equals("subunit")) {
	            			protein.setProperty("subunit", comment.getText().get(0).getValue());	            			
	            		} else if (comment.getType().equals("tissue specificity")) {
	            			protein.setProperty("tissue", comment.getText().get(0).getValue());	            			
	            		} else if (comment.getType().equals("PTM")) {
	            			protein.setProperty("ptm", comment.getText().get(0).getValue());	            			
	            		} else if (comment.getType().equals("similarity")) {
	            			protein.setProperty("similarity", comment.getText().get(0).getValue());	            			
	            		} else
	            		if (!comment.getIsoform().isEmpty()) {
	            			for (IsoformType isoform : comment.getIsoform()) {
	            				for (String isoId : isoform.getId()) {
	            					Node accession = graphDb.createNode( PROTEIN_NAME );
	            					accession.setProperty("name", isoId);
	
	            					accession.createRelationshipTo(protein, RelationTypes.REFERS_TO);
	
	                                entryCounter++;
	                                edgeCounter++;
	            				}
	            			}
	            		}
	            	}
	            	
	            	/*
	            	for (ReferenceType refType: entry.getReference()) {
	            		CitationType citation = refType.getCitation();
	            		if ((citation != null) && (citation.getType().equals("journal article"))) {
	            			
	            			if (citation.getTitle() != null)			
	                	        for (DbReferenceType dbref: citation.getDbReference()) {
	                	        	if (dbref.getType().equals("PubMed")) {
	                	        			String pubmedId = dbref.getId();
	                	        			
	                        				if (!pubmedId.equals("")) {
	                        	            	ResourceIterator<Node> it = graphDb.findNodes(PUBMED, "pubmedId", pubmedId);
	
	                        	            	Node cit = null;
	                        	            	if (it.hasNext())
	                        	            		cit = it.next();
	                        	            	else {
	                        	            		cit = graphDb.createNode( PUBMED );
	                        	            		cit.setProperty("pubmedId", pubmedId);
	                        	            		entryCounter++;
	                        	            	}
	                    	            		protein.createRelationshipTo(cit, RelationTypes.CITED_IN);
	                        	        		edgeCounter++;
	                        				}
	                	        	}
	                	        }                	        
	            		}
	            	}
	            	*/
		            	
	                if (entryCounter % 2000 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	                tx.success();
                }
        	}
        }
	}
	
}
