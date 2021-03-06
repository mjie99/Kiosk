package tcpKioskClient;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import kioskapp.order.Order;
import kioskapp.ordereditem.OrderedItem;
import kioskapp.ordertransaction.OrderTransaction;

public class TCPKioskClientApplication {

	public static void main(String[] args) throws InterruptedException {
		
		//set frame to visible
		KioskFrame kioskFrame = new KioskFrame();
		kioskFrame.setVisible(true);
		
		while(true) {
			try {

			kioskFrame.waitForInput();
			
			//connect to order server
			Socket socket = new Socket(InetAddress.getLocalHost(),4228);

			//open an outputStream
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

			//get objectTransaction and credit card number from kiosk frame
			OrderTransaction orderTransaction = kioskFrame.getOrderTransaction();
			String creditCardNo = kioskFrame.getCreditCardNumber();

			//send orderTransaction to order server
			outputStream.writeObject(orderTransaction);
			outputStream.writeUTF(creditCardNo);
			outputStream.flush();

		
			//open an inputStream
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

			orderTransaction = (OrderTransaction)inputStream.readObject();
			boolean result = orderTransaction.isTransactionStatus();
		
			kioskFrame.setTransactionStatus(result);
			kioskFrame.release();
		
			// print receipt
			if(result)
			{	
				//take transaction id and order id as file name 
				String targetSource = Integer.toString(orderTransaction.getOrderTransactionId()) + Integer.toString(orderTransaction.getOrder().getOrderId()) + ".txt";
				
				//write receipt into text file
				KioskReceipt receipt = new KioskReceipt ();
				String receiptContent = receipt.writeReceiptContent(orderTransaction);
				FileWriter fileWriter = new FileWriter (targetSource);
				fileWriter.write(receiptContent);
				fileWriter.flush();
				fileWriter.close();
			
			}
			
			
			
			} catch (InterruptedException | IOException | ClassNotFoundException e) {
				kioskFrame.release();
				e.printStackTrace();
			}
		}
			
	}
}
