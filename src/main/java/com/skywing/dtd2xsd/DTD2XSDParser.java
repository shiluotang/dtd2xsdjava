package com.skywing.dtd2xsd;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.apache.xerces.dom.CoreDOMImplementationImpl;
import org.apache.xerces.parsers.XMLDocumentParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class DTD2XSDParser extends XMLDocumentParser implements XMLDTDHandler,
		XMLDTDContentModelHandler, XMLErrorHandler {

	private static final String PCDATA = "PCDATA";

	private static final String PUBLIC = "public";

	private static final String XSD_NOTATION_ELEMENT = "xsd:notation";

	private static final String REF = "ref";

	private static final String ENTITIES = "ENTITIES";

	private static final String ENTITY = "ENTITY";

	private static final String NMTOKENS = "NMTOKENS";

	private static final String NMTOKEN = "NMTOKEN";

	private static final String IDREFS = "IDREFS";

	private static final String IDREF = "IDREF";

	private static final String ID = "ID";

	private static final String CDATA = "CDATA";

	private static final String DEFAULT = "default";

	private static final String OPTIONAL = "optional";

	private static final String FIXED = "fixed";

	private static final String REQUIRED = "required";

	private static final String USE = "use";

	private static final String FIXED_DTD = "#FIXED";

	private static final String REQUIRED_DTD = "#REQUIRED";

	private static final String TYPE = "type";

	private static final String VALUE = "value";

	private static final String BASE = "base";

	private static final String NOTATION = "NOTATION";

	private static final String ENUMERATION = "ENUMERATION";

	private static final String NAME = "name";

	private static final String XSD_ID = "xsd:ID";

	private static final String XSD_IDREF = "xsd:IDREF";

	private static final String XSD_IDREFS = "xsd:IDREFS";

	private static final String XSD_NMTOKEN = "xsd:NMTOKEN";

	private static final String XSD_NMTOKENS = "xsd:NMTOKENS";

	private static final String XSD_ENTITY = "xsd:ENTITY";

	private static final String XSD_ENTITIES = "xsd:ENTITIES";

	private static final String XSD_NOTATION = "xsd:NOTATION";

	private static final String XSD_ENUMERATION = "xsd:enumeration";

	private static final String XSD_RESTRICTION = "xsd:restriction";

	private static final String XSD_SIMPLE_TYPE = "xsd:simpleType";

	private static final String XSD_ATTRIBUTE = "xsd:attribute";

	private static final String XSD_STRING = "xsd:string";

	private static final String XSD_CHOICE = "xsd:choice";

	private static final String XSD_SEQUENCE = "xsd:sequence";

	private static final String XSD_COMPLEX_TYPE = "xsd:complexType";

	private static final String XSD_ELEMENT = "xsd:element";

	private static final String MAX_OCCURS = "maxOccurs";

	private static final String MIN_OCCURS = "minOccurs";

	private static Logger log = Logger.getLogger("DTD2XSDHandler");

	private String T_TYPE = "";

	private CoreDOMImplementationImpl domImpl;
	private Document doc;
	private Element root;
	private Stack<Element> stackElements;
	private String targetNamespace;

	private HashSet<String> setXmlResourceIdentifier;

	private boolean hasParsed = false;
	private Stack<Boolean> parsedStack;

	private int groupDepth = 0;
	private List<Short> listGroupTypes;
	private List<List<Element>> listGroupElements;
	private Map<String, Element> allElements;

	private Map<String, String> dataTypeMap;
	
	private List<XsdTypePattern> listXsdTypePattern;

	public DTD2XSDParser(XMLParserConfiguration configuration) {
		super(configuration);
		configuration.setErrorHandler(this);

		setXmlResourceIdentifier = new HashSet<String>();
		stackElements = new Stack<Element>();
		listGroupTypes = new ArrayList<Short>();
		listGroupElements = new ArrayList<List<Element>>();
		allElements = new HashMap<String, Element>();
		dataTypeMap = new HashMap<String, String>();
		dataTypeMap.put(CDATA, XSD_STRING);
		dataTypeMap.put(ID, XSD_ID);
		dataTypeMap.put(IDREF, XSD_IDREF);
		dataTypeMap.put(IDREFS, XSD_IDREFS);
		dataTypeMap.put(NMTOKEN, XSD_NMTOKEN);
		dataTypeMap.put(NMTOKENS, XSD_NMTOKENS);
		dataTypeMap.put(ENTITY, XSD_ENTITY);
		dataTypeMap.put(ENTITIES, XSD_ENTITIES);
		dataTypeMap.put(NOTATION, XSD_NOTATION);
		parsedStack = new Stack<Boolean>();
		listXsdTypePattern = new ArrayList<XsdTypePattern>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDContentModelHandler#any(org.apache.xerces
	 * .xni.Augmentations)
	 */
	public void any(Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("any: augs=" + augs);
		}
		if (hasParsed) {
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDContentModelHandler#element(java.lang.String,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void element(String name, Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("element: name=" + name + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

		Element element = doc.createElement(XSD_ELEMENT);
		element.setAttribute(REF, T_TYPE + name);

		if (groupDepth > 0) {
			List<Element> listElement = null;
			if (listGroupElements.size() < groupDepth) {
				listElement = new ArrayList<Element>();
				listGroupElements.add(listElement);
			} else {
				listElement = listGroupElements.get(groupDepth - 1);
			}
			listElement.add(element);
		}
		stackElements.add(element);
		allElements.put(name, element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDContentModelHandler#empty(org.apache.xerces
	 * .xni.Augmentations)
	 */
	public void empty(Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("empty: augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDContentModelHandler#endContentModel(org.apache
	 * .xerces.xni.Augmentations)
	 */
	public void endContentModel(Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("endContentModel: augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

		Element element = stackElements.pop();
		stackElements.peek().appendChild(element);
	}

	private Element createSequenceOrChoice(short type, List<Element> listElement) {
		Element seq_cho = null;
		switch (type) {
		case SEPARATOR_SEQUENCE:
			seq_cho = doc.createElement(XSD_SEQUENCE);
			break;
		case SEPARATOR_CHOICE:
			seq_cho = doc.createElement(XSD_CHOICE);
			break;
		}
		
		List<Element> realListElement = listElement;
		if (type == SEPARATOR_SEQUENCE) {
			realListElement = combineSequenceList(listElement);
		}
		for (Element child : realListElement) {
			seq_cho.appendChild(child);
		}
		return seq_cho;
	}
	
	private List<Element> combineSequenceList(List<Element> listElement) {
		boolean needCombin = false;
		int i = 0;
		Map<String, List<Element>> mapRefList = new LinkedHashMap<String, List<Element>>();
		for (Element element : listElement) {
			if (XSD_ELEMENT.equals(element.getTagName())) {
				String ref = element.getAttribute(REF);
				List<Element> reflist = mapRefList.get(ref);
				if (reflist == null) {
					reflist = new ArrayList<Element>(1);
					reflist.add(element);
					mapRefList.put(ref, reflist);
				} else {
					reflist.add(element);
					needCombin = true;
				}
			} else {
				List<Element> reflist = new ArrayList<Element>(1);
				reflist.add(element);
				mapRefList.put(element.getTagName()+i, reflist);
				i++;
			}
		}
		
		if (!needCombin) {
			return listElement;
		}
		
		List<Element> returnList = new ArrayList<Element>();
		for (List<Element> refList : mapRefList.values()) {
			if (refList.size() == 1) {
				returnList.add(refList.get(0));
			} else {
				Element last = refList.get(refList.size()-1);
				int minOccurs = 1;
				try {
					minOccurs = Integer.parseInt(last.getAttribute(MIN_OCCURS));
					minOccurs += (refList.size()-1);
					last.setAttribute(MIN_OCCURS, Integer.toString(minOccurs));
				}catch (NumberFormatException ne) {
					minOccurs += (refList.size()-1);
					last.setAttribute(MIN_OCCURS, Integer.toString(minOccurs));
					last.setAttribute(MAX_OCCURS, Integer.toString(minOccurs));
				}
				returnList.add(last);
			}
		}
		return returnList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDContentModelHandler#endGroup(org.apache.xerces
	 * .xni.Augmentations)
	 */
	public void endGroup(Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("endGroup: augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

		groupDepth--;

		short type = listGroupTypes.remove(groupDepth);
		List<Element> listElement = listGroupElements.remove(groupDepth);
		stackElements.removeAll(listElement);

		if (listElement.size() == 1
				&& listElement.get(0).getTagName().equals(PCDATA)) {
			// PCDATA/CDATA as string
			stackElements.peek().setAttribute(TYPE, XSD_STRING);
		} else {
			// check and build sequence or choice
			Element seq_cho = createSequenceOrChoice(type, listElement);
			if (groupDepth == 0) {
				//create complexType element, append it to prev element
				Element complexType = doc.createElement(XSD_COMPLEX_TYPE);
				complexType.appendChild(seq_cho);
				stackElements.peek().appendChild(complexType);
			} else {
				// add it to uplevel group
				listGroupElements.get(groupDepth - 1).add(seq_cho);
				stackElements.add(seq_cho);
			}
		}
		if (log.isDebugEnabled()) {
			printDomNode(stackElements.peek()); 
		}

	}

	private void setOccursAttributes(Element element, short type) {
		switch (type) {
		case OCCURS_ZERO_OR_ONE:
			element.setAttribute(MIN_OCCURS, "0");
			element.setAttribute(MAX_OCCURS, "1");
			break;
		case OCCURS_ZERO_OR_MORE:
			element.setAttribute(MIN_OCCURS, "0");
			element.setAttribute(MAX_OCCURS, "unbounded");
			break;
		case OCCURS_ONE_OR_MORE:
			element.setAttribute(MIN_OCCURS, "1");
			element.setAttribute(MAX_OCCURS, "unbounded");
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.xerces.xni.XMLDTDContentModelHandler#occurrence(short,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void occurrence(short type, Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("occurrence: type=" + type + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

		Element element = stackElements.peek();
		setOccursAttributes(element, type);
		if (log.isDebugEnabled()) {
			printDomNode(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDContentModelHandler#pcdata(org.apache.xerces
	 * .xni.Augmentations)
	 */
	public void pcdata(Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("pcdata: augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

		Element pcdata = doc.createElement(PCDATA);
		if (groupDepth > 0) {
			List<Element> listElement = listGroupElements.get(groupDepth - 1);
			listElement.add(pcdata);
		}
		stackElements.add(pcdata);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.xerces.xni.XMLDTDContentModelHandler#separator(short,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void separator(short type, Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("separator: type=" + type + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

		if (listGroupTypes.get(groupDepth - 1) != type) {
			listGroupTypes.set(groupDepth - 1, type);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDContentModelHandler#startContentModel(java
	 * .lang.String, org.apache.xerces.xni.Augmentations)
	 */
	public void startContentModel(String ename, Augmentations augs)
			throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("startContentModel: ename=" + ename + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

		Element element = doc.createElement(XSD_ELEMENT);
		element.setAttribute(NAME, ename);

		stackElements.add(element);
		allElements.put(ename, element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDContentModelHandler#startGroup(org.apache
	 * .xerces.xni.Augmentations)
	 */
	public void startGroup(Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("startGroup: augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

		listGroupElements.add(new ArrayList<Element>());
		listGroupTypes.add(SEPARATOR_SEQUENCE);
		groupDepth++;
	}

	private Element createAttribute(String ename, String aname, String atype, String[] enums,
			String dtype, XMLString dvalue, XMLString nondvalue,
			Augmentations augs) {

		Element attr = doc.createElement(XSD_ATTRIBUTE);
		attr.setAttribute(NAME, aname);
		//process atype
		if (ENUMERATION.equals(atype) || NOTATION.equals(atype)) {
			if (enums != null) {
				String typeName = ename + aname.substring(0, 1).toUpperCase() + aname.substring(1);
				attr.setAttribute(TYPE, T_TYPE + typeName);
				//check exist or not
				Element simpleType = allElements.get(typeName);
				if (simpleType == null) {
					//new simple type element
					simpleType = doc.createElement(XSD_SIMPLE_TYPE);
					simpleType.setAttribute(NAME, typeName);
					Element restriction = doc.createElement(XSD_RESTRICTION);
					if (ENUMERATION.equals(atype)) {
						restriction.setAttribute(BASE, XSD_STRING);
					} else {
						restriction.setAttribute(BASE, XSD_NOTATION);
					}
					for (String value : enums) {
						Element enumeration = doc.createElement(XSD_ENUMERATION);
						enumeration.setAttribute(VALUE, value);
						restriction.appendChild(enumeration);
					}
					simpleType.appendChild(restriction);
					root.appendChild(simpleType);
					allElements.put(typeName, simpleType);
				}
			}
		} else {
			String xsdType = null;
			for (XsdTypePattern xsdPattern : listXsdTypePattern) {
				if (xsdPattern.match(aname)) {
					xsdType = xsdPattern.getXsdType();
					break;
				}
			}
			if (xsdType == null) {
				xsdType = dataTypeMap.get(atype);
			}
			if (xsdType != null) {
				attr.setAttribute(TYPE, xsdType);
			}
		}

		//process dtype
		boolean fixed = false;
		if (REQUIRED_DTD.equals(dtype)) {
			attr.setAttribute(USE, REQUIRED);
		} else if (FIXED_DTD.equals(dtype)) {
			attr.setAttribute(FIXED, dvalue.toString());
			fixed = true;
		} else {
			attr.setAttribute(USE, OPTIONAL);
		}
		
		//process dvalue
		if (!fixed && dvalue != null) {
			attr.setAttribute(DEFAULT, dvalue.toString());
		}
		return attr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.xerces.xni.XMLDTDHandler#attributeDecl(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String[], java.lang.String,
	 * org.apache.xerces.xni.XMLString, org.apache.xerces.xni.XMLString,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void attributeDecl(String ename, String aname, String atype,
			String[] enums, String dtype, XMLString dvalue,
			XMLString nondvalue, Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("attributeDecl: ename=" + ename + ", aname=" + aname
					+ ", atype=" + atype + ", enums=" + Arrays.toString(enums)
					+ ", dtype=" + dtype + ", dvalue=" + dvalue
					+ ", nondvalue=" + nondvalue + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}
		
		Element attr = createAttribute(ename, aname, atype, enums, dtype, dvalue,
				nondvalue, augs);

		Element element = allElements.get(ename);
		if (element != null) {
			if (element.getChildNodes().getLength() > 0) {
				Element child = (Element) element.getChildNodes().item(0);
				child.appendChild(attr);
			}
			else {
				Element complexType = doc.createElement(XSD_COMPLEX_TYPE);
				element.appendChild(complexType);
				complexType.appendChild(attr);
				
				if (XSD_STRING.equals(element.getAttribute(TYPE))) {
					complexType.setAttribute("mixed", "true");
				}
				element.removeAttribute(TYPE);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#comment(org.apache.xerces.xni.XMLString
	 * , org.apache.xerces.xni.Augmentations)
	 */
	public void comment(XMLString text, Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("comment: text=" + text + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

		Comment comment = doc.createComment(text.toString());
		stackElements.peek().appendChild(comment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.xerces.xni.XMLDTDHandler#elementDecl(java.lang.String,
	 * java.lang.String, org.apache.xerces.xni.Augmentations)
	 */
	public void elementDecl(String name, String contentModel, Augmentations augs)
			throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("elementDecl: name=" + name + ", contentModel="
					+ contentModel + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#endAttlist(org.apache.xerces.xni.
	 * Augmentations)
	 */
	public void endAttlist(Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("endAttlist: augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#endConditional(org.apache.xerces.
	 * xni.Augmentations)
	 */
	public void endConditional(Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("endConditional: augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.xerces.xni.XMLDTDHandler#endDTD(org.apache.xerces.xni.
	 * Augmentations)
	 */
	public void endDTD(Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("endDTD: augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

		if (log.isDebugEnabled()) {
			printDomNode(root); 
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#endExternalSubset(org.apache.xerces
	 * .xni.Augmentations)
	 */
	public void endExternalSubset(Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("endExternalSubset: augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#endParameterEntity(java.lang.String,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void endParameterEntity(String name, Augmentations augs)
			throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("endParameterEntity: name=" + name + ", augs=" + augs);
		}
		
		hasParsed = parsedStack.pop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#externalEntityDecl(java.lang.String,
	 * org.apache.xerces.xni.XMLResourceIdentifier,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void externalEntityDecl(String name,
			XMLResourceIdentifier identifier, Augmentations augs)
			throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("externalEntityDecl: name=" + name + ", identifier="
					+ identifier + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#ignoredCharacters(org.apache.xerces
	 * .xni.XMLString, org.apache.xerces.xni.Augmentations)
	 */
	public void ignoredCharacters(XMLString text, Augmentations augs)
			throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("ignoredCharacters: text=" + text + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#internalEntityDecl(java.lang.String,
	 * org.apache.xerces.xni.XMLString, org.apache.xerces.xni.XMLString,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void internalEntityDecl(String name, XMLString text,
			XMLString nonNormalizedText, Augmentations augs)
			throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("internalEntityDecl: name=" + name + ", text=" + text
					+ ", nonNormalizedText=" + nonNormalizedText + ", augs="
					+ augs);
		}
		if (hasParsed) {
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.xerces.xni.XMLDTDHandler#notationDecl(java.lang.String,
	 * org.apache.xerces.xni.XMLResourceIdentifier,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void notationDecl(String name, XMLResourceIdentifier identifier,
			Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("notationDecl: name=" + name + ", identifier="
					+ identifier + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}
		
		Element notation = doc.createElement(XSD_NOTATION_ELEMENT);
		notation.setAttribute(NAME, name);
		if (identifier != null) {
			if (identifier.getLiteralSystemId() != null) {
				notation.setAttribute(PUBLIC, identifier.getLiteralSystemId());		
			}
		}
		stackElements.peek().appendChild(notation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#processingInstruction(java.lang.String
	 * , org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
	 */
	public void processingInstruction(String target, XMLString data,
			Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("processingInstruction: target=" + target + ", data="
					+ data + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.xerces.xni.XMLDTDHandler#startAttlist(java.lang.String,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void startAttlist(String name, Augmentations augs)
			throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("startAttlist: name=" + name + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.xerces.xni.XMLDTDHandler#startConditional(short,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void startConditional(short type, Augmentations augs)
			throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("startConditional: type=" + type + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#startDTD(org.apache.xerces.xni.XMLLocator
	 * , org.apache.xerces.xni.Augmentations)
	 */
	public void startDTD(XMLLocator locator, Augmentations augs)
			throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("startDTD: locator=" + locator + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

		DOMImplementationRegistry registry = null;
		try {
			// get an instance of the DOMImplementation registry
			registry = DOMImplementationRegistry.newInstance();
			// get a DOM implementation the Level 3 XML module
			domImpl = (CoreDOMImplementationImpl) registry
					.getDOMImplementation("XML 3.0");
			doc = domImpl.createDocument("http://www.w3.org/2001/XMLSchema",
					"xsd:schema", null);
			root = doc.getDocumentElement();
			if (targetNamespace != null) {
				//targetNamespace="" xmlns:t=""
				root.setAttribute("targetNamespace", targetNamespace);
				root.setAttribute("xmlns:t", targetNamespace);
				T_TYPE = "t:";
			} else {
				//elementFormDefault="qualified" attributeFormDefault="unqualified"
				root.setAttribute("elementFormDefault", "qualified");
				root.setAttribute("attributeFormDefault", "unqualified");
				T_TYPE = "";
			}
			

			stackElements.add(root);
		} catch (Exception e) {
			log.error("getDOMImplementation error: " + e, e);
			throw new XNIException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#startExternalSubset(org.apache.xerces
	 * .xni.XMLResourceIdentifier, org.apache.xerces.xni.Augmentations)
	 */
	public void startExternalSubset(XMLResourceIdentifier identifier,
			Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("startExternalSubset: identifier=" + identifier
					+ ", augs=" + augs);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#startParameterEntity(java.lang.String
	 * , org.apache.xerces.xni.XMLResourceIdentifier, java.lang.String,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void startParameterEntity(String name,
			XMLResourceIdentifier identifier, String encoding,
			Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("startParameterEntity: name=" + name + ", identifier="
					+ identifier + ", encoding=" + encoding + ", augs=" + augs);
		}
		if (setXmlResourceIdentifier.contains(name)) {
			parsedStack.add(new Boolean(hasParsed));
			hasParsed = true;
		} else {
			parsedStack.add(new Boolean(hasParsed));
			hasParsed = false;
			setXmlResourceIdentifier.add(name);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.xerces.xni.XMLDTDHandler#textDecl(java.lang.String,
	 * java.lang.String, org.apache.xerces.xni.Augmentations)
	 */
	public void textDecl(String version, String encoding, Augmentations augs)
			throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("textDecl: version=" + version + ", encoding=" + encoding
					+ ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.XMLDTDHandler#unparsedEntityDecl(java.lang.String,
	 * org.apache.xerces.xni.XMLResourceIdentifier, java.lang.String,
	 * org.apache.xerces.xni.Augmentations)
	 */
	public void unparsedEntityDecl(String name,
			XMLResourceIdentifier identifier, String notation,
			Augmentations augs) throws XNIException {
		if (log.isDebugEnabled()) {
			log.debug("textDecl: name=" + name + ", identifier=" + identifier
					+ ", notation=" + notation + ", augs=" + augs);
		}
		if (hasParsed) {
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler#error(java
	 * .lang.String, java.lang.String,
	 * com.sun.org.apache.xerces.internal.xni.parser.XMLParseException)
	 */
	public void error(String domain, String key, XMLParseException exception)
			throws XNIException {
		log.error("domain=" + domain + ", key=" + key + ", exception="
				+ exception, exception);
		throw new XNIException("domain=" + domain + ", key=" + key
				+ ", exception=" + exception, exception);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler#fatalError
	 * (java.lang.String, java.lang.String,
	 * com.sun.org.apache.xerces.internal.xni.parser.XMLParseException)
	 */
	public void fatalError(String domain, String key,
			XMLParseException exception) throws XNIException {
		log.error("domain=" + domain + ", key=" + key + ", exception="
				+ exception, exception);
		throw new XNIException("domain=" + domain + ", key=" + key
				+ ", exception=" + exception, exception);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler#warning
	 * (java.lang.String, java.lang.String,
	 * com.sun.org.apache.xerces.internal.xni.parser.XMLParseException)
	 */
	public void warning(String domain, String key, XMLParseException exception)
			throws XNIException {
		log.warn("domain=" + domain + ", key=" + key + ", exception="
				+ exception, exception);
	}

	/**
	 * @return the targetNamespace
	 */
	public String getTargetNamespace() {
		return targetNamespace;
	}

	/**
	 * @param targetNamespace
	 *            the targetNamespace to set
	 */
	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	/**
	 * Write xsd to OutputStream
	 * @param out
	 */
	public void writeXsd(OutputStream out) {
		LSSerializer writer = domImpl.createLSSerializer();
		LSOutput output = domImpl.createLSOutput();
		output.setByteStream(out);
		output.setEncoding("UTF-8");
		writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		writer.write(root, output);
	}
	
	/**
	 * Write xsd to Writer
	 * @param out
	 */
	public void writeXsd(Writer out) {
		LSSerializer writer = domImpl.createLSSerializer();
		LSOutput output = domImpl.createLSOutput();
		output.setCharacterStream(out);
		output.setEncoding("UTF-8");
		writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		writer.write(root, output);
	}
	
	private void printDomNode(Node node) {
		Writer out = new StringWriter();
		LSSerializer writer = domImpl.createLSSerializer();
		LSOutput output = domImpl.createLSOutput();
		output.setCharacterStream(out);
		output.setEncoding("UTF-8");
		writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
		writer.write(node, output);
		String str = output.getCharacterStream().toString();
		log.debug(str);
	}

	public void addXsdTypePattern(List<XsdTypePattern> list) {
		listXsdTypePattern.addAll(list);
	}
	
	public void addXsdTypePattern(XsdTypePattern xsdTypePattern) {
		listXsdTypePattern.add(xsdTypePattern);
	}
	
}
