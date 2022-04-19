package com.example.jasper;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

@Component
public class Main {

    public static void start() throws JRException {
        String path = "C:\\Users\\dagis\\IdeaProjects\\jasper\\sec.jrxml";
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        List<A>products = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            A a = new A("АЙДdqqqqqИ "+i, "dfdf "+i);
            products.add(a);
        }

        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(Arrays.asList(

                new Product("666id", "666name", "666price", products)

        ), false);
        Map<String, Object>map = new HashMap<>();
        map.put("name", "qwewqeqwe");
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, beanCollectionDataSource);
        System.out.print("AS");

        JasperExportManager.exportReportToPdfFile(jasperPrint, "aaaaasd.pdf");
//        JRPdfExporter exporter = new JRPdfExporter();
//
//        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
//        exporter.setExporterOutput(
//                new SimpleOutputStreamExporterOutput("qwe.pdf"));

//        SimplePdfReportConfiguration reportConfig
//                = new SimplePdfReportConfiguration();
//        reportConfig.setSizePageToContent(true);
//        reportConfig.setForceLineBreakPolicy(false);

//        SimplePdfExporterConfiguration exportConfig
//                = new SimplePdfExporterConfiguration();
//        exportConfig.setMetadataAuthor("baeldung");
//        exportConfig.setEncrypted(true);
//        exportConfig.setAllowedPermissionsHint("PRINTING");

  //      exporter.setConfiguration(reportConfig);
//        exporter.setConfiguration(exportConfig);

        //exporter.exportReport();
    }
}
