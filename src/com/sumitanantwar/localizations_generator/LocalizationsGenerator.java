package com.sumitanantwar.localizations_generator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by leo on 6/21/17.
 */
public class LocalizationsGenerator {

    private static final Map<String, String> iosMap = new HashMap<>();

    static {
        iosMap.put("en", "Base");
        iosMap.put("it", "it-IT");
        iosMap.put("es", "es");
        iosMap.put("fr", "fr");
        iosMap.put("de", "de");
        iosMap.put("ja", "ja");
        iosMap.put("ko", "ko-KR");
        iosMap.put("pt", "pt");
        iosMap.put("ru", "ru");
        iosMap.put("pl", "pl");
        iosMap.put("zh", "zh-Hans");
        iosMap.put("cs", "cs");
        iosMap.put("ca", "ca");
        iosMap.put("hi", "hi-IN");
    }

    public static void main(String[] args)
    {
        try
        {
            File baseFolder = new File("Localization");

            FileInputStream file = new FileInputStream(new File(baseFolder.getPath() + File.separator + "PopGuide_Translations_V13.xlsx"));

            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);

            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);
            System.out.println("Last Row : " + sheet.getLastRowNum());
            // Store all the headers
            List<String> headers = new ArrayList<>();

            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext())
            {
                Row row = rowIterator.next();

                System.out.print(row.getRowNum() + "/" + sheet.getLastRowNum() + " - ");

                String keyStr = "";
                String enStr = "";
                //For each row, iterate through all the columns
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext())
                {
                    Cell cell = cellIterator.next();

                    if (row.getRowNum() == 0) {
                        // First row has all the field headers
                        headers.add(cell.getStringCellValue().trim());
                    }
                    else if (row.getRowNum() == 1) {
                        // Omit everything in the second row
                    }
                    else {
                        // Check the current column
                        String column = headers.get(cell.getColumnIndex());
                        if (cell.getColumnIndex() == 0){
                            // Column with Keys
                            keyStr = cell.getStringCellValue().trim();
                            System.out.print(keyStr + " - ");
                        }
                        else if (column.equalsIgnoreCase("Usage")) {
                            // Omit all the values in the Usage Column
                            System.out.print(((cell.getCellType() == Cell.CELL_TYPE_STRING) ? cell.getStringCellValue() : cell.getNumericCellValue()) + " - ");
                        }
                        else { // if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                            if (iosMap.containsKey(column)) {
                                String value = cell.getStringCellValue().trim();
                                value = value.replace("\"", "'"); // Replace all the Quotes Ticks with an Apostrophe
                                value = value.replace("’", "'"); // Replace all the Back Ticks with an Apostrophe
                                value = value.replace("…", "..."); // Replace ellipsis character with three dots
                                value = value.replace("%s", "%@"); // Replace format specifiers for iOS
                                // Remember the English String
                                if (column.equalsIgnoreCase("en")) {
                                    enStr = value;
                                }
                                // For all the columns other than "en"
                                if (value.length() <= 0) {
                                    value = enStr;
                                }
                                // Prepare string for Android
                                String andrValue = value.replace("%@", "%s"); // Replace format specifiers for Android
                                andrValue = andrValue.replace("'", "\\'");
                                andrValue = andrValue.replace("&", "&amp;");

                                System.out.print(value + " - ");

                                // Create values folder and strings file for Android
                                File andrFldr = new File(baseFolder.getPath() + File.separator + "Android" + File.separator + "values" + (column.equalsIgnoreCase("en") ? "" : "-"+column) );
                                andrFldr.mkdirs();
                                File andrFile = new File(andrFldr.getPath() + File.separator + "strings.xml");
                                andrFile.createNewFile();

                                FileWriter aw = new FileWriter(andrFile, true);
                                BufferedWriter abw = new BufferedWriter(aw);
                                if (row.getRowNum() == 2)
                                {
                                    abw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                                    abw.newLine();
                                    abw.write("<resources>");
                                    abw.newLine();
                                }
                                abw.write("    <string name=\"" + keyStr + "\">" + andrValue + "</string>");
                                abw.newLine();
                                if (row.getRowNum() >= sheet.getLastRowNum())
                                {
                                    abw.write("</resources>");
                                    abw.newLine();
                                }

                                // Create iOS file
                                File iosFldr = new File(baseFolder.getPath() + File.separator + "iOS" + File.separator + iosMap.get(column) + ".lproj");
                                iosFldr.mkdirs();
                                File iosFile = new File(iosFldr.getPath() + File.separator + "Localizable.strings");
                                iosFile.createNewFile();
                                // "Key" = "String";
                                FileWriter iw = new FileWriter(iosFile, true);
                                BufferedWriter ibw = new BufferedWriter(iw);
                                ibw.write("\"" + keyStr + "\"" + " = " + "\"" + value + "\";");
                                ibw.newLine();

                                ibw.close();
                                iw.close();

                                abw.close();
                                aw.close();
                            }
                        }
                    }
                }
                System.out.println("");
            }
            file.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
