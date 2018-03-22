package PDF;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Section;

public class PDF_Godziny_wgGodzin {
static Connection connection= WB.Connection2DB.dbConnector();
	

	
	private static Font catFont ;
	private static Font smallFont;
	private static Font smallFont2 = new Font(Font.FontFamily.TIMES_ROMAN, 10);
	private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
	        Font.BOLD);
	

	public static void createRaport(String start, String end) throws SQLException, FileNotFoundException, DocumentException{
		//URL fName = PDF_Godziny_wgGodzin.class.getClassLoader().getResource("times.tff");
		//String fName = "//192.168.90.203/Logistyka/Tosia/Projekty JAVA";
		FontFactory.register(PDF_Godziny_wgGodzin.class.getClassLoader().getResource("times.ttf").toString(), "times");
		catFont = FontFactory.getFont("times", BaseFont.CP1250, BaseFont.EMBEDDED, 18);
		smallFont = FontFactory.getFont("times", BaseFont.CP1250, BaseFont.EMBEDDED, 12);
		Document doc = new Document();
		SimpleDateFormat doNazwy = new SimpleDateFormat("yyyy.MM.dd");
		SimpleDateFormat godz = new SimpleDateFormat("HH;mm");
		Calendar date = Calendar.getInstance();
		String path = Parameters.getPathToSaveHours()+"/"+doNazwy.format(date.getTime())+"/";
		
		File theDir = new File(path);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
		    try{
		        theDir.mkdir();
		    } 
		    catch(SecurityException se){
		        //handle it
		    }
		}
		int ilePracownikow=1;
		String [] headers = new String [5];
		int ileKolumn = headers.length;
		
		headers[0] = "Numer pracownika";
		headers[1] = "Nazwisko Imię";
		headers[2] = "Zarejestrowane [h]";
		headers[3] = "Stanowiska";
		headers[4] = "Zakończonych serii";
		
		String name = "Posrednik_Raport godzin od "+start+" do "+end+".pdf";
		File f = new File(path+name);
		if(f.exists() && !f.isDirectory())
			name = godz.format(date.getTime())+" "+name;
		PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(path+name));
		doc.open();
		writer.setPageEvent(new PDF_MyFooter());
		
		//Zrobienie naglowka
		
		 Paragraph preface = new Paragraph();
        // We add one empty line
		 preface.add("\n");
        // Lets write a big header
        preface.add(new Paragraph("Raport godzin pracowników zatrudnionych przez posrednika pracy", catFont));

        preface.add("\n");
        // Will create: Report generated by: _name, _date
        preface.add(new Paragraph("Od: "+start+" do:  "+end, smallBold));
        preface.add("\n");

        preface.add("\n");
        doc.add(preface);
        
        PdfPTable tabPDF = new PdfPTable(ileKolumn);
        float widths[] = new float[] { 6, 6, 6, 6, 6};
		
		//wybierz pracowników płaconych wg godzin
        
        	//ilosc pracownikow w określonym przedziale czasowym
    			String a = "select count(*) from pracownicy where werkregime = 4";
    			Statement a1 = connection.createStatement();
    			ResultSet rs2 = a1.executeQuery(a);
    			while(rs2.next()){
    				ilePracownikow= rs2.getInt(1);
    			}
    			a1.close();
    			rs2.close();
    			
    	System.out.println(ilePracownikow);
        
		String sql1 = "SELECT werknemer, naam from pracownicy where werkregime = 4 order by naam";
		String [][] tab = new String [5][ilePracownikow];
		Statement st1 = connection.createStatement();
		ResultSet rs1 = st1.executeQuery(sql1);
		int numerPorzadkowy = -1;
		while(rs1.next()){
			numerPorzadkowy++;
			String numer = rs1.getString(1);
			String nazwisko = rs1.getString(2);
			String sql2 = "Select sum(tijd) from Rejestracja where datum >= '"+start+"' and datum <= '"+end+"' and werknemer = '"+numer+"' and verwerkt = 1";
			Statement stm1 = connection.createStatement();
			ResultSet rs3 = stm1.executeQuery(sql2);
			tab[0][numerPorzadkowy] = numer;
			tab[1][numerPorzadkowy] = nazwisko;
			
			while(rs3.next()){
				
				System.out.println(nazwisko);
				int MinutWykonano = 0;
				MinutWykonano = rs3.getInt("SUM(TIJD)");
				
				String sql3 = "Select count(*) from Rejestracja where datum >= '"+start+"' and datum <= '"+end+"' and werknemer = '"+numer+"' and verwerkt = 1 and status = 90";
				String sql4 = "Select werkpost from Rejestracja where datum >= '"+start+"' and datum <= '"+end+"' and werknemer = '"+numer+"' and verwerkt = 1 group by werkpost";
				
				//sprawdzenie ile serii
				Statement stm5 = connection.createStatement();
				ResultSet rs5 = stm5.executeQuery(sql3);
				String  ileSerii = "";
				while(rs5.next()){
					ileSerii = rs5.getString(1);
				}
				rs5.close();
				stm5.close();
				System.out.println(ileSerii);
				
				//Sprawdzenie ile maszyn
				Statement stm4 = connection.createStatement();
				ResultSet rs4 = stm4.executeQuery(sql4);
				String ileMaszyn = "";
				while(rs4.next()){
					System.out.println(rs4.getString(1));
					ileMaszyn = ileMaszyn + rs4.getString(1) + ", ";
				}
				stm4.close();
				System.out.println(ileMaszyn);
				if(ileMaszyn.length()>0) ileMaszyn = ileMaszyn.substring(0, ileMaszyn.length()-2);
				System.out.println(ileMaszyn);
				
				tab[2][numerPorzadkowy]="";
				if(MinutWykonano%60>=10)
					tab[2][numerPorzadkowy] += Integer.toString(MinutWykonano/60)+":"+Integer.toString(MinutWykonano%60);
				else
					tab[2][numerPorzadkowy] += Integer.toString(MinutWykonano/60)+":0"+Integer.toString(MinutWykonano%60);
				
				tab[3][numerPorzadkowy] = ileMaszyn;
				tab[4][numerPorzadkowy] = ileSerii;
				if(MinutWykonano == 0) {
					numerPorzadkowy--;
					ilePracownikow--;
				}
			}//koniec podsumowania pracownikow 
		}
			
		//Zrobienie PDFa
		for(int i = 0; i<ileKolumn; i++){
			PdfPCell c1 = new PdfPCell(new Phrase(headers[i], smallFont));
			c1.setMinimumHeight(30);
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
			c1.setBackgroundColor(BaseColor.ORANGE);
			tabPDF.addCell(c1);
		}
		for(int i = 0; i<ilePracownikow; i++){
			for(int j = 0; j<ileKolumn; j++){
				String zawartosc = tab[j][i];
				PdfPCell c2 = new PdfPCell(new Phrase(zawartosc, smallFont2));
				c2.setMinimumHeight(30);
				c2.setRowspan(2);
				c2.setHorizontalAlignment(Element.ALIGN_CENTER);
				c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
				tabPDF.addCell(c2);
			}
		}
		tabPDF.setWidths(widths);
		tabPDF.setHeaderRows(1);
		tabPDF.setWidthPercentage(100);
		tabPDF.setHorizontalAlignment(Element.ALIGN_CENTER);
		tabPDF.setHorizontalAlignment(Element.ALIGN_CENTER);
		doc.add(tabPDF);
		doc.close();
		
		return;
	}

	
	
}