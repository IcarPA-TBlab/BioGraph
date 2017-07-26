package it.cnr.icar.biograph.neo4j;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import it.cnr.icar.biograph.models.go.Header;
import it.cnr.icar.biograph.models.go.Source;
import it.cnr.icar.biograph.models.go.Term;
import it.cnr.icar.biograph.models.go.Typedef;
import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class GeneOntology extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws IOException, XMLStreamException, JAXBException {
		HashMap<String, Node> idVertexMap = new HashMap<String, Node>();
		
    	HashMap<String, List<String>> termParentsMap = new HashMap<String, List<String>>();
    	HashMap<String, List<String>> regulatesMap = new HashMap<String, List<String>>();
    	HashMap<String, List<String>> negativelyRegulatesMap = new HashMap<String, List<String>>();
    	HashMap<String, List<String>> positivelyRegulatesMap = new HashMap<String, List<String>>();
    	HashMap<String, List<String>> partOfMap = new HashMap<String, List<String>>();
    	HashMap<String, List<String>> hasPartMap = new HashMap<String, List<String>>();
    	
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(fileName));
        xsr.nextTag(); // Advance to statements element

        JAXBContext jc = JAXBContext.newInstance(Header.class, Source.class, Term.class, Typedef.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        
        System.out.println("\nReading GO entries from " + fileName + "\n");
        System.out.print("inserting term nodes ");
        
        Label GO = DynamicLabel.label( "Go" );
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while (xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
	            Object entry = unmarshaller.unmarshal(xsr);
	            
	            if (entry instanceof Term) {
	            	Term term = (Term)entry;
	            	
	            	String goId = term.getId();
	            	String goName = (term.getName() != null) ? term.getName() : "";
	            	String goDefinition = ((term.getDef() != null) && (term.getDef().getDefstr() != null)) ? term.getDef().getDefstr() : "";
	            	String goComment = (term.getComment() != null) ? term.getComment() : "";
	            	String goIsObsolete = "";
	            	if (term.getIsObsolete() != null) {
	            		goIsObsolete = (term.getIsObsolete() == 1) ? "true" : "false";
	            	}
	            	String goNamespace = (term.getNamespace() != null) ? term.getNamespace() : "";
	            	            	
	                entryCounter++;
	                
	                Node t = graphDb.createNode( GO );
	                t.setProperty("goId", goId);
	                t.setProperty("name", goName);
	                t.setProperty("namespace", goNamespace);
	                t.setProperty("definition", goDefinition);
	                t.setProperty("obsolete", goIsObsolete);
	                t.setProperty("comment", goComment);
		
	                idVertexMap.put(goId, t);
	                
	            	termParentsMap.put(goId, term.getIsA());
	            	
	        		for (Term.Relationship rel : term.getRelationship()) {
	            		String goRelationshipType = rel.getType();
	            		String goRelationshipTo = rel.getTo();
	            		
	            		List<String> tempArray = null;
	            		
	            		switch (goRelationshipType) {
	            			case "regulates":
	            				tempArray = regulatesMap.get(goId);
	            				if (tempArray == null) {
						          tempArray = new ArrayList<String>();
						          regulatesMap.put(goId, tempArray);
	            			    }
	            			    tempArray.add(goRelationshipTo);
	            				break;
	            			case "positively_regulates":
	            				tempArray = positivelyRegulatesMap.get(goId);
	            				if (tempArray == null) {
	        			          tempArray = new ArrayList<String>();
	        			          positivelyRegulatesMap.put(goId, tempArray);
	            			    }
	            			    tempArray.add(goRelationshipTo);
	            				break;
	            			case "negatively_regulates":
	            				tempArray = negativelyRegulatesMap.get(goId);
	            				if (tempArray == null) {
	            					tempArray = new ArrayList<String>();
	            					negativelyRegulatesMap.put(goId, tempArray);
	            				}
	            				tempArray.add(goRelationshipTo);
	            				break;
	            			case "part_of":
	            				tempArray = partOfMap.get(goId);
	            				if (tempArray == null) {
	            					tempArray = new ArrayList<String>();
	            					partOfMap.put(goId, tempArray);
	            				}
	            				tempArray.add(goRelationshipTo);
	            				break;
	            			case "has_part":
	            				tempArray = hasPartMap.get(goId);
	            				if (tempArray == null) {
	            					tempArray = new ArrayList<String>();
	            					hasPartMap.put(goId, tempArray);
	            				}
	            				tempArray.add(goRelationshipTo);
	            				break;
	            		}
	        		}
	            }
	
	            if (entryCounter % 1000 == 0) {
	            	System.out.print("."); System.out.flush();
	            }
	        }
	
	        List<String> tempArray = null;
	        Set<String> keys = null;
	
	        System.out.print("\ncreating 'is_a' relationships ");
	        keys = termParentsMap.keySet();
	        for (String key : keys) {
	        	//Vertex tempGoTerm = graph.getVertices("go_Term.id", key).iterator().next();
	        	Node tempGoTerm = idVertexMap.get(key);
	        	tempArray = termParentsMap.get(key);
	        	for (String string : tempArray) {
	        		Node tempGoTerm2 = idVertexMap.get(string);
	        		
	        		tempGoTerm.createRelationshipTo( tempGoTerm2, RelationTypes.IS_A );
	        		
	        		edgeCounter++;
	                if (edgeCounter % 1000 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
	        
	        System.out.print("\ncreating 'regulates' relationships ");
	        keys = regulatesMap.keySet();
	        for (String key : keys) {
	        	//Vertex tempGoTerm = graph.getVertices("go_Term.id", key).iterator().next();
	        	Node tempGoTerm = idVertexMap.get(key);
	        	tempArray = regulatesMap.get(key);
	        	for (String string : tempArray) {
	        		Node tempGoTerm2 = idVertexMap.get(string);
	        		tempGoTerm.createRelationshipTo( tempGoTerm2, RelationTypes.REGULATES );
	        		edgeCounter++;
	                if (edgeCounter % 100 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
	        
	        System.out.print("\ncreating 'negatively_regulates' relationships ");
	        keys = negativelyRegulatesMap.keySet();
	        for (String key : keys) {
	        	//Vertex tempGoTerm = graph.getVertices("go_Term.id", key).iterator().next();
	        	Node tempGoTerm = idVertexMap.get(key);
	        	tempArray = negativelyRegulatesMap.get(key);
	        	for (String string : tempArray) {
	        		Node tempGoTerm2 = idVertexMap.get(string);
	        		tempGoTerm.createRelationshipTo( tempGoTerm2, RelationTypes.NEGATIVELY_REGULATES );
	        		edgeCounter++;
	                if (edgeCounter % 100 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
	        
	        System.out.print("\ncreating 'positively_regulates' relationships ");
	        keys = positivelyRegulatesMap.keySet();
	        for (String key : keys) {
	        	//Vertex tempGoTerm = graph.getVertices("go_Term.id", key).iterator().next();
	        	Node tempGoTerm = idVertexMap.get(key);
	        	tempArray = positivelyRegulatesMap.get(key);
	        	for (String string : tempArray) {
	        		Node tempGoTerm2 = idVertexMap.get(string);
	        		tempGoTerm.createRelationshipTo( tempGoTerm2, RelationTypes.POSITIVELY_REGULATES );
	        		edgeCounter++;
	                if (edgeCounter % 100 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
	        
	        System.out.print("\ncreating 'part_of' relationships ");
	        keys = partOfMap.keySet();
	        for (String key : keys) {
	        	//Vertex tempGoTerm = graph.getVertices("go_Term.id", key).iterator().next();
	        	Node tempGoTerm = idVertexMap.get(key);
	        	tempArray = partOfMap.get(key);
	        	for (String string : tempArray) {
	        		Node tempGoTerm2 = idVertexMap.get(string);
	        		tempGoTerm.createRelationshipTo( tempGoTerm2, RelationTypes.PART_OF );
	        		edgeCounter++;
	                if (edgeCounter % 100 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
	        
	        System.out.print("\ncreating 'has_part_of' relationships ");
	        keys = hasPartMap.keySet();
	        for (String key : keys) {
	        	//Vertex tempGoTerm = graph.getVertices("go_Term.id", key).iterator().next();
	        	Node tempGoTerm = idVertexMap.get(key);
	        	tempArray = hasPartMap.get(key);
	        	for (String string : tempArray) {
	        		Node tempGoTerm2 = idVertexMap.get(string);
	        		tempGoTerm.createRelationshipTo( tempGoTerm2, RelationTypes.HAS_PART );
	        		edgeCounter++;
	                if (edgeCounter % 100 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
        	tx.success();
        }
	}
}
