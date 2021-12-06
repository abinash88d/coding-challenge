package com.crewmeister.cmcodingchallenge.commons.fileprocessor;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crewmeister.cmcodingchallenge.commons.helper.NamespaceResolver;
import com.crewmeister.cmcodingchallenge.commons.utility.CurrencyConstant;
import com.crewmeister.cmcodingchallenge.commons.utility.DailyRateUtiliity;
import com.crewmeister.cmcodingchallenge.data.entity.DailyRate;
import com.crewmeister.cmcodingchallenge.exception.FileParsingException;

/**
 * Processor implementation for XML parsing
 * 
 */

@Component
public class DailyRateXmlParserImpl implements DailyRateFileParser {

	private static final Logger LOGGER = LogManager.getLogger(DailyRateXmlParserImpl.class.getName());

	@Value("${app.rate.file.target}")
	private String dailyRateFolder;

	/**
	 * XML parsing function
	 * 
	 * @parm fileName Input file name
	 * 
	 * @return List<DailyRate> List of Daily rate
	 *
	 * @throws FileParsingException
	 * 
	 */
	public List<DailyRate> parseXml(String fileName) throws FileParsingException {
		LOGGER.info("Parsing rate filename {}. " + fileName);
		return parseXml(fileName, null);
	}

	public List<DailyRate> parseXml(String fileName, Date filterDate) throws FileParsingException {
		List<DailyRate> rates;
		try {
			Document document = this.buildDocument(fileName);
			XPath xpath = this.buildXPath(document);

			// Fetching source (FX) currency code using xpath Query
			String sourceCurrency = this.executeXpathQuery(xpath, document,
					CurrencyConstant.XML_STANDARD_CURRENCY_PATH);

			// Fetching target (EUR) currency code using xpath Query
			String targetCurrency = this.executeXpathQuery(xpath, document, CurrencyConstant.XML_FX_CURRENCY_PATH);
			NodeList nodes;
			if (null != filterDate) {
				XPathExpression expression = xpath
						.compile(String.format(CurrencyConstant.XML_FILTER_RATE_WITH_DATE, filterDate));
				Object result = expression.evaluate(document, XPathConstants.NODESET);
				nodes = (NodeList) result;
			} else {
				// Fetching all rate nodes using Root tag
				nodes = document.getElementsByTagName(CurrencyConstant.XML_ROOT_NODE);
			}

			rates = readRateNodes(nodes, sourceCurrency, targetCurrency);

		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException exception) {
			LOGGER.error(exception);
			throw new FileParsingException(" Unable to parse File : " + fileName, exception);

		}
		return rates;
	}

	private List<DailyRate> readRateNodes(NodeList genericNodeList, String sourceCurrency, String targetCurrency) {
		List<DailyRate> dailyRateList = new ArrayList<DailyRate>();

		for (int i = 0; i < genericNodeList.getLength(); i++) {
			Node node = genericNodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				// New Entity for each rate instance
				DailyRate dailyRate = new DailyRate();
				dailyRate.setSourceCurrency(sourceCurrency);
				dailyRate.setTargetCurrency(targetCurrency);

				// Fetching exchange rate date
				Element genericObsElement = (Element) node;
				NodeList genericDimensionList = genericObsElement
						.getElementsByTagName(CurrencyConstant.XML_RATE_DATE_TAG);
				if (genericDimensionList.getLength() > CurrencyConstant.ZERO) {
					Element obsDimensionElement = (Element) genericDimensionList.item(CurrencyConstant.ZERO);
					dailyRate.setRateDate(DailyRateUtiliity.getDateValue(
							getAttributeValue(obsDimensionElement, CurrencyConstant.XML_VALUE_ATTRIBUTE)));
				}

				// Fetching exchange rate
				NodeList obsValueList = genericObsElement.getElementsByTagName(CurrencyConstant.XML_EXCHANGE_RATE_TAG);
				if (obsValueList.getLength() > CurrencyConstant.ZERO) {
					Element obsValueElement = (Element) obsValueList.item(CurrencyConstant.ZERO);
					dailyRate.setExchangeRate(
							new BigDecimal(getAttributeValue(obsValueElement, CurrencyConstant.XML_VALUE_ATTRIBUTE)));
				}

				// Handling Holiday logic, exchange rate will not be available
				NodeList attributeNodeList = genericObsElement
						.getElementsByTagName(CurrencyConstant.XML_ATTRIBUTE_ROOT_TAG);
				for (int j = 0; j < attributeNodeList.getLength(); j++) {
					Node node2 = attributeNodeList.item(j);
					if (node2.getNodeType() == Node.ELEMENT_NODE) {
						Element attributeElement = (Element) node2;
						NodeList genericValueList = attributeElement
								.getElementsByTagName(CurrencyConstant.XML_VALUE_ROOT_TAG);
						Element genericValueElement = (Element) genericValueList.item(CurrencyConstant.ZERO);
						if (CurrencyConstant.RATE_FILE_STATUS_ELEMENT
								.equals(getAttributeValue(genericValueElement, CurrencyConstant.XML_ID_ATTRIBUTE))) {
							dailyRate.setHolidayStatus(
									getAttributeValue(genericValueElement, CurrencyConstant.XML_VALUE_ATTRIBUTE));
							dailyRate.setExchangeRateDifference(new BigDecimal(CurrencyConstant.DEFAULT_FX_RATE));
							dailyRate.setExchangeRate(new BigDecimal(CurrencyConstant.DEFAULT_FX_RATE));
						} else if (CurrencyConstant.RATE_FILE_DIFF_ELEMENT
								.equals(getAttributeValue(genericValueElement, CurrencyConstant.XML_ID_ATTRIBUTE))) {
							dailyRate.setHolidayStatus(CurrencyConstant.RATE_NONHOLIDAY_FLAG);
							dailyRate.setExchangeRateDifference(new BigDecimal(
									getAttributeValue(genericValueElement, CurrencyConstant.XML_VALUE_ATTRIBUTE)));
						}
					}

				}
				if (null != dailyRate.getExchangeRate() && 0 == attributeNodeList.getLength()) {
					dailyRate.setHolidayStatus(CurrencyConstant.RATE_NONHOLIDAY_FLAG);
					dailyRate.setExchangeRateDifference(new BigDecimal(CurrencyConstant.DEFAULT_FX_RATE));
				}
				dailyRate.setUpdatedTimestamp(new Timestamp(System.currentTimeMillis()));
				dailyRate.setUpdatedUser(CurrencyConstant.RATE_PROCESSOR_USER);
				dailyRateList.add(dailyRate);
			}
		}
		return dailyRateList;
	}

	/**
	 * Method to build document object from XML file
	 * 
	 * @parm fileName Input XML file name
	 * 
	 * @return Document documents object
	 *
	 * @throws ParserConfigurationException, SAXException, IOException
	 * 
	 */
	private Document buildDocument(String fileName) throws ParserConfigurationException, SAXException, IOException {
		File inputFile = new File(dailyRateFolder + fileName + CurrencyConstant.RATE_FILE_EXTENSION);
		DocumentBuilderFactory dBfactory = DocumentBuilderFactory.newInstance();
		dBfactory.setNamespaceAware(true);

		DocumentBuilder builder = dBfactory.newDocumentBuilder();
		Document document = builder.parse(inputFile);
		document.getDocumentElement().normalize();
		return document;
	}

	/**
	 * Method to build XPATH object from document object.Will be helpful to query
	 * xml nodes.
	 * 
	 * @parm Document document object
	 * 
	 * @return XPath xpath object
	 * 
	 */
	private XPath buildXPath(Document document) {
		XPathFactory xpathfactory = XPathFactory.newInstance();
		XPath xpath = xpathfactory.newXPath();
		xpath.setNamespaceContext(new NamespaceResolver(document));
		return xpath;
	}

	/**
	 * Method to query XPATH object and fetch value
	 * 
	 * @parm XPath XPath object
	 * @parm Document XML document object
	 * @parm query Query to execute
	 * 
	 * @return String value
	 * 
	 */
	private String executeXpathQuery(XPath xpath, Document document, String query) throws XPathExpressionException {
		XPathExpression expression = xpath.compile(query);
		Node node = (Node) expression.evaluate(document, XPathConstants.NODE);
		return node.getNodeValue();
	}

	/**
	 * Method to fetch attribute Value from Document element
	 * 
	 * @parm Element document element
	 * @parm attributeName attribute name to fetch value
	 * 
	 * @return String value
	 * 
	 */
	private String getAttributeValue(Element element, String attributeName) {
		return element.getAttribute(attributeName);
	}

	@Override
	public Boolean verifyFile(String fileName, Date filterDate) throws FileParsingException {
		Date preparedDate;
		Date currentDate;
		try {
			Document document = this.buildDocument(fileName);
			XPath xpath = this.buildXPath(document);

			// Get file prepared date
			String fetchedDate = this.executeXpathQuery(xpath, document, CurrencyConstant.XML_GET_PREPARED_DATE);
			preparedDate = DailyRateUtiliity.getDateValue(fetchedDate);
			currentDate = DailyRateUtiliity.getLastWorkingDate(DailyRateUtiliity.getCurrentDate());

		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException exception) {
			LOGGER.error(exception);
			throw new FileParsingException(" Unable to parse File : " + fileName, exception);

		}
		return (preparedDate.compareTo(currentDate) == 0);
	}

}
