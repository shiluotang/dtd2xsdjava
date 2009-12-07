package com.skywing.dtd2xsd;

import java.util.regex.Pattern;

public class XsdTypePattern {
	private Pattern pattern;
	private String xsdType;
	
	
	public XsdTypePattern() {
		super();
	}
	
	public XsdTypePattern(Pattern pattern, String xsdType) {
		super();
		this.pattern = pattern;
		this.xsdType = xsdType;
	}
	
	/**
	 * @return the pattern
	 */
	public Pattern getPattern() {
		return pattern;
	}
	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	/**
	 * @return the xsdType
	 */
	public String getXsdType() {
		return xsdType;
	}
	/**
	 * @param xsdType the xsdType to set
	 */
	public void setXsdType(String xsdType) {
		this.xsdType = xsdType;
	}
	
	/**
	 * @param name
	 * @return match or not
	 */
	public boolean match(String name) {
		if (pattern == null) {
			return false;
		}
		return pattern.matcher(name).matches();
	}
}
