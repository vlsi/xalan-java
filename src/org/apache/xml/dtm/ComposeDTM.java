/**
 * A simple testcase used to test DTM
 *   - construct a DTM document
 *   - set DTMStringPool pointers
 *   - create element and text nodes with default append
 *   - dump the content of the DTM document created
 *<P>Status - work in progress</p>
 */
import org.apache.xml.dtm.CustomStringPool;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMDocumentImpl;
import org.apache.xml.dtm.DTMStringPool;

public class ComposeDTM {
	DTMDocumentImpl newDoc;

        // %REVIEW% Justify why CustomStringPool rather than DTMStringPool.
        // (See %REVIEW% issue in CustomStringPool's comments.)
	DTMStringPool symbolTable = new CustomStringPool();

	public ComposeDTM() {};

	public static void main(String[] argv) {
		//try {
		ComposeDTM cdtm = new ComposeDTM();
		cdtm.constructDoc();

		cdtm.newDoc.setNsNameTable(cdtm.symbolTable);
		cdtm.newDoc.setElementNameTable(cdtm.symbolTable);
		cdtm.newDoc.setAttributeNameTable(cdtm.symbolTable);
		cdtm.newDoc.setPrefixNameTable(cdtm.symbolTable);
		cdtm.composeDoc();

		cdtm.treeDump();
		/*} catch (Exception e) {
			 System.out.println("DOMCompose::Exception: " + e);
		}*/
	}

	public void printNode(int nodeHandle, java.io.PrintWriter pw)
	{
		pw.print("Node " + nodeHandle +" ) ");
		int type = newDoc.getNodeType(nodeHandle);
		pw.print("[type: "+ type +" (");

		switch (type) {
		case DTM.ATTRIBUTE_NODE: pw.print("ATTRIBUTE_NODE"); break;
		case DTM.CDATA_SECTION_NODE: pw.print("CDATA_SECTION_NODE"); break;
		case DTM.COMMENT_NODE: pw.print("COMMENT_NODE"); break;
		case DTM.DOCUMENT_FRAGMENT_NODE: pw.print("DOCUMENT_FRAGMENT_NODE"); break;
		case DTM.DOCUMENT_NODE: pw.print("DOCUMENT_NODE"); break;
		case DTM.DOCUMENT_TYPE_NODE: pw.print("DOCUMENT_TYPE_NODE"); break;
		case DTM.ELEMENT_NODE: pw.print("ELEMENT_NODE"); break;
		case DTM.ENTITY_NODE: pw.print("ENTITY_NODE"); break;
		case DTM.ENTITY_REFERENCE_NODE: pw.print("ENTITY_REFERENCE_NODE"); break;
		case DTM.NOTATION_NODE: pw.print("NOTATION_NODE"); break;
		case DTM.PROCESSING_INSTRUCTION_NODE: pw.print("PROCESSING_INSTRUCTION_NODE"); break;
		case DTM.TEXT_NODE: pw.print("TEXT_NODE"); break;
		default: pw.print("???");
		}
		pw.print(")]");
		pw.print("[node name: " + newDoc.getNodeName(nodeHandle)+"]");
		pw.print("[node value: " + newDoc.getNodeValue(nodeHandle)+"]");
		pw.println("");
	}

	// instantiate a DTM document
	public void constructDoc() {

		newDoc = new DTMDocumentImpl();

	}

	// compose a PurchaseOrder document
	public void composeDoc() {
		int root, h, c1, c2, c3, c4, c1_text, c2_text, c3_text, c4_text;

		root = newDoc.createElement("PurchaseOrderList", null);
		// root.createAttribute("version", "1.1"));

		for (int i = 0; i < 10; i++) {

			h = newDoc.createElement("PurchaseOrder", null);

			c1 = newDoc.createElement("Item", null);
			// c1.createAttribute();
			c1_text = newDoc.createTextNode("Basketball" + " - " + i);
			newDoc.endElement(null, "Item");

			c2 = newDoc.createElement("Description", null);
			// c2.createAttribute();
			c2_text = newDoc.createTextNode("Professional Leather Michael Jordan Signatured Basketball");
			newDoc.endElement(null, "Description");

			c3 = newDoc.createElement("UnitPrice", null);
			c3_text = newDoc.createTextNode("$12.99");
			newDoc.endElement(null, "UnitPrice");

			c4 = newDoc.createElement("Quanity", null);
			c4_text = newDoc.createTextNode("50");
			newDoc.endElement(null, "Quanity");

			newDoc.endElement(null, "PurchaseOrder");
		}

		newDoc.endElement(null, "PurchaseOrderList");
	}

	// traverse the PurchaseOrder document and print out the content
	public void treeDump()
	{
		int root, node, node_child, node_sibling, node_name, node_value, node_type;
 
		java.io.PrintWriter pwrt = null;
		try {
			java.io.FileOutputStream os = new java.io.FileOutputStream("dtmTreeDump.txt");
			pwrt = new java.io.PrintWriter(os);

			root = newDoc.getDocumentRoot();
			System.out.println("Print out the document root element");
			printNode(root, pwrt);

			// root has children
			if (newDoc.hasChildNodes(root)) {
				// traverse through all children of the root
				System.out.println("Print out first generation children");
				node = newDoc.getFirstChild(root);
				node_sibling = node;
				int i= 1;
				while (node_sibling != -1) {
					System.out.println("Child " + i + " is:");
					printNode(node_sibling, pwrt);
					node_sibling = newDoc.getNextSibling(node_sibling);
					i++;
				}
				// traverse through all descendant of the root, do this later after more generations
				// of children are added to the root
			}
		} catch (java.io.IOException ioe) {
			System.out.println("Could not dump DTM");
		} finally {
			pwrt.close();
		}
	}

}

