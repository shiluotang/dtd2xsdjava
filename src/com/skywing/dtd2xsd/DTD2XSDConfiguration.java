package com.skywing.dtd2xsd;

import java.io.EOFException;
import java.io.IOException;
import java.util.Locale;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLDTDScannerImpl;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.ParserConfigurationSettings;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

public class DTD2XSDConfiguration extends ParserConfigurationSettings implements
		XMLParserConfiguration {

	// properties

	/**
	 * Entity resolver
	 * ("http://apache.org/xml/properties/internal/entity-resolver").
	 */
	private static final String ENTITY_RESOLVER = Constants.XERCES_PROPERTY_PREFIX
			+ Constants.ENTITY_RESOLVER_PROPERTY;

	/** Locale property identifier ("http://apache.org/xml/properties/locale"). */
	private static final String LOCALE = Constants.XERCES_PROPERTY_PREFIX
			+ "locale";

	// settings

	/** Registered document handler. */
	private XMLDocumentHandler xmlDocumentHandler;

	/** Registered error handler. */
	private XMLErrorHandler xmlErrorHandler;

	/** Registered entity resolver. */
	private XMLEntityResolver xmlEntityResolver;

	/** Registered dtd handler. */
	private XMLDTDHandler xmlDTDHandler;
	
	/** Registered dtd content model handler. */
	private XMLDTDContentModelHandler xmlDTDContentModelHandler;

	/** Symbol table. */
	private SymbolTable fSymbolTable = new SymbolTable();

	/** DTD scanner. */
	private XMLDTDScannerImpl fScanner = new XMLDTDScannerImpl();

	/** Entity manager. */
	private XMLEntityManager fEntityManager = new XMLEntityManager();

	/** Error reporter. */
	private XMLErrorReporter fErrorReporter = new XMLErrorReporter();

	public DTD2XSDConfiguration() {
		super();

		// add default features
		String[] featureNames = { "http://xml.org/sax/features/validation", };
		Boolean[] featureValues = { Boolean.FALSE, };
		addRecognizedFeatures(featureNames);
		for (int i = 0; i < featureNames.length; i++) {
			Boolean featureValue = featureValues[i];
			if (featureValue != null) {
				String featureName = featureNames[i];
				setFeature(featureName, featureValue.booleanValue());
			}
		}

		// add default properties
		String[] propertyNames = {
				"http://apache.org/xml/properties/internal/symbol-table",
				"http://apache.org/xml/properties/internal/entity-manager",
				"http://apache.org/xml/properties/internal/entity-resolver",
				"http://apache.org/xml/properties/internal/error-reporter",
				"http://apache.org/xml/properties/internal/error-handler",
				LOCALE, };
		Object[] propertyValues = { fSymbolTable, fEntityManager, null,
				fErrorReporter, null, Locale.getDefault(), };
		addRecognizedProperties(propertyNames);
		for (int i = 0; i < propertyNames.length; i++) {
			Object propertyValue = propertyValues[i];
			if (propertyValue != null) {
				String propertyName = propertyNames[i];
				setProperty(propertyName, propertyValue);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.parser.XMLParserConfiguration#getDocumentHandler()
	 */
	public XMLDocumentHandler getDocumentHandler() {
		return xmlDocumentHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.parser.XMLParserConfiguration#getDTDContentModelHandler
	 * ()
	 */
	public XMLDTDContentModelHandler getDTDContentModelHandler() {
		return xmlDTDContentModelHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.xerces.xni.parser.XMLParserConfiguration#getDTDHandler()
	 */
	public XMLDTDHandler getDTDHandler() {
		return xmlDTDHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.parser.XMLParserConfiguration#getEntityResolver()
	 */
	public XMLEntityResolver getEntityResolver() {
		return xmlEntityResolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.parser.XMLParserConfiguration#getErrorHandler()
	 */
	public XMLErrorHandler getErrorHandler() {
		return xmlErrorHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.xerces.xni.parser.XMLParserConfiguration#getLocale()
	 */
	public Locale getLocale() {
		Locale locale = null;
		try {
			locale = (Locale) getProperty(LOCALE);
		} catch (Exception e) {
			// ignore
		}
		return locale;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.parser.XMLParserConfiguration#parse(org.apache.
	 * xerces.xni.parser.XMLInputSource)
	 */
	public void parse(XMLInputSource source) throws XNIException, IOException {
		fScanner.reset(this);
		fEntityManager.reset(this);
		fErrorReporter.reset(this);
		fScanner.setInputSource(source);
		try {
			fScanner.scanDTDExternalSubset(true);
		} catch (EOFException e) {
			// ignore
			// NOTE: This is to work around a problem in the Xerces
			// DTD scanner implementation when used standalone. -Ac
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.parser.XMLParserConfiguration#setDocumentHandler
	 * (org.apache.xerces.xni.XMLDocumentHandler)
	 */
	public void setDocumentHandler(XMLDocumentHandler handler) {
		xmlDocumentHandler = handler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.parser.XMLParserConfiguration#setDTDContentModelHandler
	 * (org.apache.xerces.xni.XMLDTDContentModelHandler)
	 */
	public void setDTDContentModelHandler(XMLDTDContentModelHandler handler) {
		xmlDTDContentModelHandler = handler;
		// set handlers
		fScanner.setDTDContentModelHandler(xmlDTDContentModelHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.parser.XMLParserConfiguration#setDTDHandler(org
	 * .apache.xerces.xni.XMLDTDHandler)
	 */
	public void setDTDHandler(XMLDTDHandler handler) {
		xmlDTDHandler = handler;
		// set handlers
		fScanner.setDTDHandler(xmlDTDHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.parser.XMLParserConfiguration#setEntityResolver
	 * (org.apache.xerces.xni.parser.XMLEntityResolver)
	 */
	public void setEntityResolver(XMLEntityResolver resolver) {
		xmlEntityResolver = resolver;
		setProperty(ENTITY_RESOLVER, xmlEntityResolver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.parser.XMLParserConfiguration#setErrorHandler(org
	 * .apache.xerces.xni.parser.XMLErrorHandler)
	 */
	public void setErrorHandler(XMLErrorHandler handler) {
		xmlErrorHandler = handler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.xerces.xni.parser.XMLParserConfiguration#setLocale(java.util
	 * .Locale)
	 */
	public void setLocale(Locale locale) throws XNIException {
		try {
			setProperty(LOCALE, locale);
			fErrorReporter.setLocale(locale);
		} catch (Exception e) {
			// ignore
		}
	}

}
