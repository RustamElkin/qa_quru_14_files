import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import domain.CyberpunkGamePOJO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class ReadingAndCheckFilesFromZIP {

    // classLoader поможет сохранить независимость от файловой системы
    ClassLoader classLoader = ReadingAndCheckFilesFromZIP.class.getClassLoader();

    // проверил zip на наличие в нем файлов
    @DisplayName("Show zip contents test")
    @Test
    void showZipContents() throws Exception {
        try (var is = classLoader.getResourceAsStream("files.zip")) {
            assert is != null;
            var zis = new ZipInputStream(is);

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                System.out.printf(
                        "Item: %s \nType: %s \nSize: %d\n%n",
                        entry.getName(),
                        entry.isDirectory() ? "directory" : "file",
                        entry.getSize());
            }
        }
    }

    // переменные с названиями файлов в архиве
    String zipFileName = "files.zip";
    String csvFileName = "file_example_CSV_5000.csv";
    String pdfFileName = "sample.pdf";
    String xlsFileName = "file_example_XLS_50.xls";

    // Получение файла по имени
    InputStream getFileFromArchive(String fileName) throws Exception {
        var zipFileDirectory = new File("src/test/resources/" + zipFileName);
        var zip = new ZipFile(zipFileDirectory);
        return zip.getInputStream(zip.getEntry(fileName));
    }

    @DisplayName("Reading and check csv file from zip")
    @Test
    void getCSVFileFromZipAndCheckTest() throws Exception {
        // Обработка исключений в блоке try
        try (var csvFileStream = getFileFromArchive(csvFileName)) {
            var csvReader = new CSVReader(new InputStreamReader(csvFileStream, UTF_8));
            var csv = csvReader.readAll();
            // В фигурных скобках указать ресурсы, которые надо закрыть
            assertThat(csv).contains(
                    new String[]{"1", "Dulce", "Abril", "Female", "United States", "32", "15/10/2017", "1562"});
        }
    }

    @DisplayName("Reading and check pdf file from zip")
    @Test
    void getPDFFileFromZipAndCheckTest() throws Exception {
        try (var pdfFileStream = getFileFromArchive(pdfFileName)) {

            var pdf = new PDF(pdfFileStream);

            assertThat(pdf.numberOfPages).isEqualTo(2);
            assertThat(pdf.text).containsAnyOf("A Simple PDF File");
        }

    }

    @DisplayName("Reading and check xls file from zip")
    @Test
    void getXLSFileFromZipAndCheckTest() throws Exception {
        try (var xlsFileStream = getFileFromArchive(xlsFileName)) {

            var xls = new XLS(xlsFileStream);

            assertThat(xls.excel.getSheetAt(0).getRow(13).getCell(0).getNumericCellValue()).isEqualTo(13);
            assertThat(xls.excel.getSheetAt(0).getRow(13).getCell(4).getStringCellValue()).contains("Great Britain");
            assertThat(xls.excel.getSheetAt(0).getRow(13).getCell(7).getNumericCellValue()).isEqualTo(3256.0);
            assertThat(xls.excel.getSheetAt(0).getPhysicalNumberOfRows()).isEqualTo(51);
        }
    }

    @DisplayName("Serialization and check json")
    @Test
    void parseJSONTest() throws Exception {
        try (var is = classLoader.getResourceAsStream("cyberpunkGame.json")) {

            var objectMapper   = new ObjectMapper();
            var cyberpunkValues  = objectMapper.readValue(is, CyberpunkGamePOJO.class);
            var functions      = new String[]{"buy", "setup", "play", "delete"};

            assertThat(cyberpunkValues.getName()).contains("cyberpunk");
            assertThat(cyberpunkValues.isAvailable()); // BooleanAssert Params: actual – the actual value. Returns: the created assertion object
            // в Gson проверка на Boolean показалась более понятной.
            assertThat(cyberpunkValues.getPrice()).isEqualTo(60);
            assertThat(cyberpunkValues.getVersion()).isEqualTo(1.6);
            assertThat(cyberpunkValues.getFunctions()).isEqualTo(functions);
        }
    }
}
