package com.crewmeister.cmcodingchallenge.commons.helper;

import java.util.Collections;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

/**
 * XML Namespace resolver class
 * 
 */

public class NamespaceResolver implements NamespaceContext {
	private Document sourceDocument;

	public NamespaceResolver(Document document) {
		sourceDocument = document;
	}

	public String getNamespaceURI(String prefix) {
		if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
			return sourceDocument.lookupNamespaceURI(null);
		} else {
			return sourceDocument.lookupNamespaceURI(prefix);
		}
	}

	public String getPrefix(String namespaceURI) {
		return sourceDocument.lookupPrefix(namespaceURI);
	}

	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String namespaceURI) {
		return Collections.emptyIterator();
	}
}
