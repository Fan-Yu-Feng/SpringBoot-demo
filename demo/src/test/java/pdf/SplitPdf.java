package pdf;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class SplitPdf {
	public static void main(String[] args) {
		String inputPdfPath = "D:\\businessdata\\bss\\7承接池受让债权确认书20240322.pdf"; // 输入PDF文件的路径
		String outputFolderPath = "D:\\businessdata\\bss\\split"; // 输出文件夹的路径
		
		try {
			splitPdf2(inputPdfPath, outputFolderPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void splitPdf(String inputPdfPath, int pageCount, String outputFolderPath) throws IOException {
		File folder = new File(outputFolderPath);
		if (!folder.exists()) {
			folder.mkdir();
		}
		PDDocument document = PDDocument.load(new File(inputPdfPath));
		int currentPage = 0;
		for (int i = 0; i < document.getNumberOfPages(); i++) {
			if (currentPage % pageCount == 0) {
				File outputFile = new File(outputFolderPath + File.separator + "split_" + (i + 1) + ".pdf");
				PDDocument newDocument = new PDDocument();
				PDPage blankPage = new PDPage(PDRectangle.A4);
				newDocument.addPage(blankPage);
				PDPageContentStream contentStream = new PDPageContentStream(newDocument, blankPage);
				contentStream.setFont(PDType1Font.HELVETICA, 12);
				contentStream.beginText();
				contentStream.newLineAtOffset(25, 700);
				contentStream.showText("Page " + (i + 1));
				contentStream.endText();
				contentStream.close();
				newDocument.save(outputFile);
				newDocument.close();
			} else {
				// document.save(outputFolderPath + File.separator + "page_" + (i + 1) + ".pdf");
			}
			currentPage++;
		}
		document.close();
	}
	
	
	public static void splitPdf2(String inputPdfFilePath, String outputDirectory) throws IOException {
		PDDocument document = PDDocument.load(new File(inputPdfFilePath));
		Splitter splitter = new Splitter();
		splitter.setSplitAtPage(20);
		List<PDDocument> pages = splitter.split(document); // 每个 PDF 包含 20 页

		Iterator<PDDocument> iterator = pages.listIterator();
		int pageNumber = 1;
		while (iterator.hasNext()) {
			PDDocument currentPage = iterator.next();
			String outputPdfFilePath = outputDirectory + File.separator + "output_" + pageNumber + ".pdf";
			currentPage.save(outputPdfFilePath);
			currentPage.close();
			pageNumber++;
		}
		
		document.close();
	}
}
