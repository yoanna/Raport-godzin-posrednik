package PDF;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

class PDF_MyFooter extends PdfPageEventHelper {
    Font ffont =  FontFactory.getFont("times", BaseFont.CP1250, BaseFont.EMBEDDED, 10);
 
    
    public void onEndPage(PdfWriter writer, Document document) {
    	int i=writer.getPageNumber();
        PdfContentByte cb = writer.getDirectContent();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        Phrase header = new Phrase("Fabryka Automat�w Tokarskich we Wroc�awiu S.A.    " + dateFormat.format(date), ffont);
        Phrase footer = new Phrase(String.format("Page %s", i), ffont);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                header,
                document.right()-120,
                document.top() + 10, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                footer,
                (document.right() - document.left()) / 2 + document.leftMargin(),
                document.bottom() - 10, 0);
    
    }

}