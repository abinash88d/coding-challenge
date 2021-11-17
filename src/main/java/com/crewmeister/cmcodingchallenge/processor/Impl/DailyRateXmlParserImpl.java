package com.crewmeister.cmcodingchallenge.processor.Impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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

import com.crewmeister.cmcodingchallenge.constant.CurrencyConstant;
import com.crewmeister.cmcodingchallenge.entity.DailyRate;
import com.crewmeister.cmcodingchallenge.exception.FileParsingException;
import com.crewmeister.cmcodingchallenge.helper.NamespaceResolver;
import com.crewmeister.cmcodingchallenge.processor.DailyRateFileParser;
import com.crewmeister.cmcodingchallenge.utility.DailyRateUtiliity;

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
		List<DailyRate> dailyRateList = new ArrayList<DailyRate>();

		try {
			File inputFile = new File(dailyRateFolder + fileName + CurrencyConstant.RATE_FILE_EXTENSION);
			DocumentBuilderFactory dBfactory = DocumentBuilderFactory.newInstance();
			dBfactory.setNamespaceAware(true);

			DocumentBuilder builder = dBfactory.newDocumentBuilder();
			Document document = builder.parse(inputFile);
			document.getDocumentElement().normalize();

			XPathFactory xpathfactory = XPathFactory.newInstance();
			XPath xpath = xpathfactory.newXPath();
			xpath.setNamespaceContext(new NamespaceResolver(document));
			XPathExpression stdCurrencyExp = xpath
					.compile("//generic:SeriesKey/generic:Value[@id='BBK_STD_CURRENCY']/@value");
			Node stdCurrencyNode = (Node) stdCurrencyExp.evaluate(document, XPathConstants.NODE);
			String sourceCurrency = stdCurrencyNode.getNodeValue();

			XPathExpression partnerCurrencyExp = xpath
					.compile("//generic:SeriesKey/generic:Value[@id='BBK_ERX_PARTNER_CURRENCY']/@value");
			Node partnerCurrencyNode = (Node) partnerCurrencyExp.evaluate(document, XPathConstants.NODE);
			String targetCurrency = partnerCurrencyNode.getNodeValue();

			NodeList genericNodeList = document.getElementsByTagName("generic:Obs");

			for (int i = 0; i < genericNodeList.getLength(); i++) {
				Node node = genericNodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					DailyRate dailyRate = new DailyRate();
					dailyRate.setSourceCurrency(sourceCurrency);
					dailyRate.setTargetCurrency(targetCurrency);

					Element genericObsElement = (Element) node;
					NodeList genericDimensionList = genericObsElement.getElementsByTagName("generic:ObsDimension");
					if (genericDimensionList.getLength() > 0) {
						Element obsDimensionElement = (Element) genericDimensionList.item(0);
						dailyRate.setRateDate(DailyRateUtiliity
								.getDateValue(obsDimensionElement.getAttribute(CurrencyConstant.XML_VALUE_ATTRIBUTE)));
					}

					NodeList obsValueList = genericObsElement.getElementsByTagName("generic:ObsValue");
					if (obsValueList.getLength() > 0) {
						Element obsValueElement = (Element) obsValueList.item(0);
						dailyRate.setExchangeRate(
								new BigDecimal(obsValueElement.getAttribute(CurrencyConstant.XML_VALUE_ATTRIBUTE)));
					}

					NodeList attributeNodeList = genericObsElement.getElementsByTagName("generic:Attributes");
					for (int j = 0; j < attributeNodeList.getLength(); j++) {
						Node node2 = attributeNodeList.item(j);
						if (node2.getNodeType() == Node.ELEMENT_NODE) {
							Element attributeElement = (Element) node2;
							NodeList genericValueList = attributeElement.getElementsByTagName("generic:Value");
							Element genericValueElement = (Element) genericValueList.item(0);
							if (CurrencyConstant.RATE_FILE_STATUS_ELEMENT
									.equals(genericValueElement.getAttribute(CurrencyConstant.XML_ID_ATTRIBUTE))) {
								dailyRate.setHolidayStatus(
										genericValueElement.getAttribute(CurrencyConstant.XML_VALUE_ATTRIBUTE));
								dailyRate.setExchangeRateDifference(new BigDecimal(CurrencyConstant.DEFAULT_FX_RATE));
								dailyRate.setExchangeRate(new BigDecimal(CurrencyConstant.DEFAULT_FX_RATE));
							} else if (CurrencyConstant.RATE_FILE_DIFF_ELEMENT
									.equals(genericValueElement.getAttribute(CurrencyConstant.XML_ID_ATTRIBUTE))) {
								dailyRate.setHolidayStatus("N");
								dailyRate.setExchangeRateDifference(new BigDecimal(
										genericValueElement.getAttribute(CurrencyConstant.XML_VALUE_ATTRIBUTE)));
							}
						}

					}
					if (null != dailyRate.getExchangeRate() && 0 == attributeNodeList.getLength()) {
						dailyRate.setHolidayStatus("N");
						dailyRate.setExchangeRateDifference(new BigDecimal(CurrencyConstant.DEFAULT_FX_RATE));
					}
					dailyRate.setUpdatedTimestamp(new Timestamp(System.currentTimeMillis()));
					dailyRate.setUpdatedUser("DAILY_RATE_PROCESSOR");
					dailyRateList.add(dailyRate);
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException exception) {
			LOGGER.error(exception);
			throw new FileParsingException(" Unable to parse File : " + fileName, exception);

		}
		return dailyRateList;
	}

}
