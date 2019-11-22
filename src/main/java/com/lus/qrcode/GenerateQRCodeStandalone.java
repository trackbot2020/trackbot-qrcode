package com.lus.qrcode;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;



public class GenerateQRCodeStandalone {
	
	public static final String FS_PROJECT_ID= "smartmonitor-test";
	public static final String APARTMENT_ID= "1002-SJR Verity";
	public static final String QR_CODE_OUTPUT_DEST_ROOT = "C:\\D-Drive\\Pet-Projects\\LetUSense\\000-PHASE-2\\QR-CODE-Output\\"+APARTMENT_ID+"\\";
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GenerateQRCodeStandalone qr = new GenerateQRCodeStandalone();
		qr.checkAndCreateDirectory();
		//qr.createQRCodeWithImage();
		ArrayList <QRCodeBean> qrCodeBeanList = new ArrayList<QRCodeBean>();
		try {
			qr.extractDataFromFS(qrCodeBeanList);
			if(!qrCodeBeanList.isEmpty()) {
				List<QRImageTagsBean> qrImageTagsLst = new ArrayList<>();
				for (QRCodeBean qrCodeBean : qrCodeBeanList) {
					String qrCodeString = APARTMENT_ID+"~"+qrCodeBean.getDeviceIdentifier();
					String qrCodeLabel = "System: "+qrCodeBean.getServiceName()+"\n"+"Component: "+qrCodeBean.getComponentName()+"\n"+"Device Name: "+qrCodeBean.getDeviceName();
					qr.createQRCodeWithImage(qrCodeBean.getDeviceIdentifier(),qrCodeString,qrCodeLabel,qrImageTagsLst);
				}
				qr.createMergedPDF(qrImageTagsLst);
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	public void createQRCodeWithImage(String deviceId,String qrCodeString, String qrCodeLabel, List<QRImageTagsBean> qrImageTagsLst) {
	//	public void createQRCodeWithImage() {
		Map hints = new HashMap();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		QRCodeWriter writer = new QRCodeWriter();
		BitMatrix bitMatrix = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
		    // Create a qr code with the url as content and a size of 250x250 px
			///apartments/1002-SJR Verity/SystemEquipmentDetails/
			//String strMessage = "1002-SJR Verity~001_stp_1_filter_press_filter_press_motor";
		    bitMatrix = writer.encode(qrCodeString.trim(), BarcodeFormat.QR_CODE, 250, 250, hints);
		    MatrixToImageConfig config = new MatrixToImageConfig(MatrixToImageConfig.BLACK, MatrixToImageConfig.WHITE);
		    // Load QR image
		    BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);
		    // Load logo image
		    File file = new File("C:\\D-Drive\\Pet-Projects\\LetUSense\\trackbot_mid.png");
		    BufferedImage logoImage = ImageIO.read(file);
		    // Calculate the delta height and width between QR code and logo
		    int deltaHeight = qrImage.getHeight() - logoImage.getHeight();
		    int deltaWidth = qrImage.getWidth() - logoImage.getWidth();
		    // Initialize combined image
		    BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
		    Graphics2D g = (Graphics2D) combined.getGraphics();
		    // Write QR code to new image at position 0/0
		    g.drawImage(qrImage, 0, 0, null);
		    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		    // Write logo into combine image at position (deltaWidth / 2) and
		    // (deltaHeight / 2). Background: Left/Right and Top/Bottom must be
		    // the same space for the logo to be centered
		    g.drawImage(logoImage, (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2), null);
		    // Write combined image as PNG to OutputStream
		    File f = new File(QR_CODE_OUTPUT_DEST_ROOT+deviceId.trim()+".png");
		    ImageIO.write(combined, "png", f);
		    Rectangle rect = new Rectangle(qrImage.getHeight(), qrImage.getWidth()+100);
		    QRImageTagsBean qrImgBean = new QRImageTagsBean();
		    
		    qrImgBean.setFileName(f.getAbsolutePath());
		    qrImgBean.setImageLabel(qrCodeLabel);
		    qrImgBean.setRect(rect);
		    
		    qrImageTagsLst.add(qrImgBean);
		    
		    System.out.println("ImageBean Added to List...");
		} catch (Exception e) {
		    System.out.println(e);
		}  
	}
	
    public void createMergedPDF(List<QRImageTagsBean> qrImageTagsLst) throws DocumentException, MalformedURLException, IOException {
    	if(qrImageTagsLst != null && qrImageTagsLst.size() >0) {
    		Image img = Image.getInstance(qrImageTagsLst.get(0).getFileName());
        	Document document = new Document(img);
    	    PdfWriter.getInstance(document, new FileOutputStream(QR_CODE_OUTPUT_DEST_ROOT+"QR_Final.pdf"));
    	    document.open();
    	    
        	for(QRImageTagsBean qrImageTags:qrImageTagsLst) {
        		 
        	    img = Image.getInstance(qrImageTags.getFileName());
                document.setPageSize(qrImageTags.getRect());
                document.newPage();
                img.setAbsolutePosition(0, 0);
                document.add(img);
                Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 10,
                        Font.BOLD);
               
                Paragraph subPara = new Paragraph(qrImageTags.getImageLabel(),subFont);
               
                document.add(subPara);
        	}
    	    
        
    	    document.close();
    	}
    	
    }
	public void extractDataFromFS(ArrayList <QRCodeBean> qrCodeBeanList) throws InterruptedException, ExecutionException{
		FirestoreOptions firestoreOptions =
				FirestoreOptions.getDefaultInstance().toBuilder()
				.setProjectId(FS_PROJECT_ID)
				.build();

		Firestore db = firestoreOptions.getService();
		CollectionReference collRef = db.collection("apartments").document(APARTMENT_ID).collection("SystemEquipmentDetails");

		///apartments/1003-HM Symphony/DailyReadings/004_swimming_pool_main_pool_chlorine/lus_sys_data/004_swimming_pool_main_pool_chlorine

		ApiFuture<QuerySnapshot> query2 =collRef.get();
		// ...
		QuerySnapshot querySnapshot2 = query2.get();
		List<QueryDocumentSnapshot> documents2 = querySnapshot2.getDocuments();
		System.out.println("List Size...."+documents2.size());
		
		
		try { 
			
			QRCodeBean qrBean = null;
			for (QueryDocumentSnapshot document2 : documents2) {
				qrBean = new QRCodeBean();
				System.out.println("Doc Name: " + document2.getId());
				String docID = document2.getId();
				Timestamp ts = document2.getUpdateTime();
				
				try {
					qrBean.setComponentName(document2.getString("ComponentName"));
					qrBean.setServiceName(document2.getString("ServiceName"));
					qrBean.setDeviceName(document2.getString("EquipmentName"));
					qrBean.setDeviceIdentifier(docID);
					System.out.println("Adding docId to Arraylist:::"+docID);
					qrCodeBeanList.add(qrBean);
					
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
			}
			
		} 
	    catch (Exception e) { 
	        // TODO Auto-generated catch block 
	        e.printStackTrace(); 
	    }
		  
	}
	
	public void checkAndCreateDirectory() {
		Path path = Paths.get(QR_CODE_OUTPUT_DEST_ROOT);
        //if directory exists?
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                //fail to create directory
                e.printStackTrace();
            }
        }
	}

}
