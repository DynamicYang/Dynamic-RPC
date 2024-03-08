package org.dynamic.rpc.config;

import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.IDGenerator;
import org.dynamic.rpc.ProtocolConfig;
import org.dynamic.rpc.RegistryConfig;
import org.dynamic.rpc.compress.Compressor;
import org.dynamic.rpc.compress.CompressorFactory;
import org.dynamic.rpc.loadbalancer.LoadBalancer;
import org.dynamic.rpc.serialization.Serializer;
import org.dynamic.rpc.serialization.SerializerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author: DynamicYang
 * @create: 2024-03-07
 * @Description:
 */
@Slf4j
public class XMLResolver {

    public static  void loadByXML(Configuration configuration) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {

            builder = documentBuilderFactory.newDocumentBuilder();
            documentBuilderFactory.setValidating(false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document document = builder.parse(ClassLoader.getSystemResourceAsStream("rpc.xml"));
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            String expression = "/configuration/port";
            configuration.setPort(Integer.parseInt(parse(document, xPath, expression)));

            expression = "/configuration/appName";
            configuration.setAppName(parse(document, xPath, expression));

            expression = "/configuration/serializer";
            String serializerClass = parse(document, xPath, expression, "class");
            Serializer serializer = (Serializer) Class.forName(serializerClass).getConstructor().newInstance();
            SerializerFactory.addSerializerIfAbsent(serializer);
            configuration.setSerializer(serializer);

            expression = "/configuration/serializeType";
            configuration.setSerializerType(parse(document, xPath, expression, "type"));

            expression = "/configuration/compressorType";
            configuration.setCompressorType(parse(document, xPath, expression, "type"));

            expression= "/configuration/compressor";
            String compressorClass = parse(document, xPath, expression, "class");
            Compressor compressor = (Compressor) Class.forName(compressorClass).getConstructor().newInstance();
            CompressorFactory.addCompressorIfAbsent(compressor);
            configuration.setCompressor(compressor);

            expression = "/configuration/protocolType";
            configuration.setProtocolConfig(new ProtocolConfig(parse(document, xPath, expression, "type")));

            expression = "/configuration/idGenerator";
            String idGeneratorClass = parse(document, xPath, expression, "class");
            Long dataCenterId = Long.parseLong(parse(document, xPath, expression, "dataCenterId"));
            Long machineId = Long.parseLong(parse(document, xPath, expression, "machineId"));
            configuration.setIdGenerator((IDGenerator) Class.forName(idGeneratorClass).getConstructor().newInstance(dataCenterId, machineId));

            expression = "/configuration/loadBalancer";
            String loadBalancerClass = parse(document, xPath, expression, "class");
            configuration.setLoadBalancer((LoadBalancer) Class.forName(loadBalancerClass).getConstructor().newInstance());

            expression = "/configuration/registryCenter";
            String registryCenterClass = parse(document, xPath, expression, "class");
            configuration.setRegistryConfig((RegistryConfig) Class.forName(registryCenterClass).getConstructor().newInstance());




        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            log.info("加载配置文件失败，使用默认配置");
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }


    }

    private static String parse(Document document, XPath xPath, String expression, String attributeName) throws XPathExpressionException {
        XPathExpression expr = xPath.compile(expression);
        Node targetNode = (Node) expr.evaluate(document, XPathConstants.NODE);
        return targetNode.getAttributes().getNamedItem(attributeName).getNodeValue();

    }

    private static String parse(Document document, XPath xPath, String expression) throws XPathExpressionException {
        XPathExpression expr = xPath.compile(expression);
        Node targetNode = (Node) expr.evaluate(document, XPathConstants.NODE);
        return targetNode.getTextContent();
    }

}
