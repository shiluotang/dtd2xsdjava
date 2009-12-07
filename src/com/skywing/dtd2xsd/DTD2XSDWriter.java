package com.skywing.dtd2xsd;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;

public class DTD2XSDWriter {

	private DTD2XSDConfiguration dtd2XSDConfiguration;

	private DTD2XSDParser dtd2XSDParser;
	
	private OutputStream outStream;

	public DTD2XSDWriter() {
		super();

		dtd2XSDConfiguration = new DTD2XSDConfiguration();
		dtd2XSDParser = new DTD2XSDParser(dtd2XSDConfiguration);
	}

	/**
	 * @return
	 * @see com.skywing.dtd2xsd.DTD2XSDParser#getTargetNamespace()
	 */
	public String getTargetNamespace() {
		return dtd2XSDParser.getTargetNamespace();
	}

	/**
	 * @param targetNamespace
	 * @see com.skywing.dtd2xsd.DTD2XSDParser#setTargetNamespace(java.lang.String)
	 */
	public void setTargetNamespace(String targetNamespace) {
		dtd2XSDParser.setTargetNamespace(targetNamespace);
	}
	

	/**
	 * @return the outStream
	 */
	public OutputStream getOutStream() {
		return outStream;
	}

	/**
	 * @param outStream the outStream to set
	 */
	public void setOutStream(OutputStream outStream) {
		this.outStream = outStream;
	}

	/**
	 * @param list
	 * @see com.skywing.dtd2xsd.DTD2XSDParser#addXsdTypePattern(java.util.List)
	 */
	public void addXsdTypePattern(List<XsdTypePattern> list) {
		dtd2XSDParser.addXsdTypePattern(list);
	}

	/**
	 * @param xsdTypePattern
	 * @see com.skywing.dtd2xsd.DTD2XSDParser#addXsdTypePattern(com.skywing.dtd2xsd.XsdTypePattern)
	 */
	public void addXsdTypePattern(XsdTypePattern xsdTypePattern) {
		dtd2XSDParser.addXsdTypePattern(xsdTypePattern);
	}

	/**
	 * @param xmlInputSource
	 * @throws XNIException
	 * @throws IOException
	 * @see org.apache.xerces.parsers.XMLParser#parse(org.apache.xerces.xni.parser.XMLInputSource)
	 */
	public void parse(XMLInputSource xmlInputSource) throws XNIException,
			IOException {
		dtd2XSDParser.parse(xmlInputSource);
		dtd2XSDParser.writeXsd(outStream);
	}


	public static void main(String[] args) {
		String usage = "usage: java " + DTD2XSDWriter.class.getName() + " <-n tartget_namespace> <-t xsd_type=regex_pattern> dtd_file <xsd_file>";
		if (args.length == 0) {
			System.err.println(usage);
			System.exit(1);
		}
		
		String targetNamespace = null;
		List<XsdTypePattern> listXsdTypePattern = new ArrayList<XsdTypePattern>(); 
		String dtdfile = null;
		String xsdfile = null;
		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			if ("-n".equals(arg)) {
				//namespace
				i++;
				if (i >= args.length) {
					System.err.println(usage);
					System.exit(1);
				}
				targetNamespace = args[i];
			} else if ("-t".equals(arg)) {
				//type mapping
				i++;
				if (i >= args.length) {
					System.err.println(usage);
					System.exit(1);
				}
				StringTokenizer st = new StringTokenizer(args[i], "=");
				if (st.countTokens() != 2) {
					System.err.println(usage);
					System.exit(1);
				}
				String xsdType = st.nextToken();
				if (!xsdType.startsWith("xsd:")) {
					xsdType = "xsd:" + xsdType;
				}
				String regex = st.nextToken();
				Pattern pattern = Pattern.compile(regex);
				listXsdTypePattern.add(new XsdTypePattern(pattern, xsdType));
			} else {
				if (dtdfile == null) {
					dtdfile = arg;
				} else if (xsdfile == null) {
					xsdfile = arg;
				}
			}
		}
		
		OutputStream outStream = null;
		if (xsdfile != null) {
			try {
				outStream = new FileOutputStream(xsdfile);
			} catch (FileNotFoundException e) {
				e.printStackTrace(System.err);
				return;
			}
		} else {
			outStream = System.out;
		}
		
		DTD2XSDWriter writer = new DTD2XSDWriter();
		writer.setTargetNamespace(targetNamespace);
		writer.addXsdTypePattern(listXsdTypePattern);
		writer.setOutStream(outStream);
		try {
			writer.parse(new XMLInputSource(null, dtdfile, null));
		} catch (XNIException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
