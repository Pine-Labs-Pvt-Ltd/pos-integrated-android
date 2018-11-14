package com.pinelabs.testbillingapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static com.pinelabs.testbillingapp.TestBillingAppActivity.MASTERAPP;

/**
 * 
 * @author Pine Labs
 * @details TCPIPforECR thread is used to communicate with PlutusPlus over
 *          Messenger. This thread connects, sends/recieves CSV to and from
 *          PlutusPlus.
 * 
 */
public class TCPIPforECR {
	public static final int MAX_MESSAGE_LENGTH = 2048;

	public static final int PLUTUS_BILLING_PRINT_REQ = 0x0995;
	public static final int PLUTUS_BILLING_PRINT_RES = 0x1995;
	public static final int PLUTUS_BILLING_OPEN_MENU_REQ = 0X0996;
	public static final int PLUTUS_BILLING_OPEN_MENU_RES = 0X1996;
	public static final int PLUTUS_BILLING_DATA_REQ = 0x0997;
	public static final int PLUTUS_BILLING_DATA_RES = 0x1997;
	public static final int PLUTUS_BILLING_SET_CONNECTION_REQ = 0X0998;
	public static final int PLUTUS_BILLING_SET_CONNECTION_RES = 0X1998;
	public static final int PLUTUS_BILLING_RUN_PVM_REQ = 0X0999;
	public static final int PLUTUS_BILLING_RUN_PVM_RES = 0X1999;

	boolean m_bConnected;
	public String m_strCSV;
	static Context context;
	public Handler mHandler;

	public TCPIPforECR(Context ctx,String strCSV, Handler handler) {
		context = ctx;
		m_bConnected = false;
		m_strCSV = strCSV;
		mHandler = handler;
	}

	public TCPIPforECR() {

	}

	public void startTransaction() {
		byte[] byteCSV = m_strCSV.trim().getBytes();

		// sends CSV and get response CSV
		SendECRTxn(byteCSV, byteCSV.length);
	}

	public void startSetConnectionTransaction() {
		byte chBuffer[] = new byte[MAX_MESSAGE_LENGTH];
		int nOffset = 0;

		// Source ID
		chBuffer[nOffset++] = 0x10;
		chBuffer[nOffset++] = 0x00;

		// Func Code
		chBuffer[nOffset++] = (byte)(PLUTUS_BILLING_SET_CONNECTION_REQ >> 8) & 0xFF;
		chBuffer[nOffset++] = (byte) (PLUTUS_BILLING_SET_CONNECTION_REQ & 0xFF);

		// Data Length
		chBuffer[nOffset++] = (byte) ((0 >> 8) & 0xFF);
		chBuffer[nOffset++] = (byte) (0 & 0xFF);

		chBuffer[nOffset++] = (byte) 0xFF;
		boolean nDataRecv = sendDataToPlutusPlus(chBuffer, nOffset);
	}

	public void startPrintDumpTransaction(String data) {
		byte chBuffer[] = new byte[MAX_MESSAGE_LENGTH];
		int nOffset = 0;

		// Source ID
		chBuffer[nOffset++] = 0x10;
		chBuffer[nOffset++] = 0x00;

		// Func Code
		chBuffer[nOffset++] = (byte)(PLUTUS_BILLING_PRINT_REQ >> 8) & 0xFF;
		chBuffer[nOffset++] = (byte) (PLUTUS_BILLING_PRINT_REQ & 0xFF);

		int len = data.length();

		// Data Length
		chBuffer[nOffset++] = (byte) ((len >> 8) & 0xFF);
		chBuffer[nOffset++] = (byte) (len & 0xFF);

		System.arraycopy(data.getBytes(),0, chBuffer,nOffset, data.length());
		nOffset += data.length();

		chBuffer[nOffset++] = (byte) 0xFF;
		boolean nDataRecv = sendDataToPlutusPlus(chBuffer, nOffset);
	}

	public void sendRequestToPlutusPlus(byte[] csvData) {
		Bundle bundle = new Bundle();
		bundle.putByteArray("TXN_REQUEST",csvData);
		Message msg = Message.obtain(null, MASTERAPP);
		msg.replyTo = new Messenger(new TCPIPforECR.ResponseHandler());
		msg.setData(bundle);
		if(TestBillingAppActivity.mService != null) {
			try {
				TestBillingAppActivity.mService.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		else{
			Toast.makeText(TestBillingAppActivity.context,"No Connection Established",Toast.LENGTH_SHORT).show();
		}
	}

	public void startRunPVMTransaction() {
		byte chBuffer[] = new byte[MAX_MESSAGE_LENGTH];
		int nOffset = 0;

		// Source ID
		chBuffer[nOffset++] = 0x10;
		chBuffer[nOffset++] = 0x00;

		// Func Code
		chBuffer[nOffset++] = (byte)(PLUTUS_BILLING_RUN_PVM_REQ >> 8) & 0xFF;
		chBuffer[nOffset++] = (byte) (PLUTUS_BILLING_RUN_PVM_REQ & 0xFF);

		// Data Length
		chBuffer[nOffset++] = (byte) ((0 >> 8) & 0xFF);
		chBuffer[nOffset++] = (byte) (0 & 0xFF);

		chBuffer[nOffset++] = (byte) 0xFF;
		boolean nDataRecv = sendDataToPlutusPlus(chBuffer, nOffset);
	}

	public void startOpenMenuTransaction() {
		byte chBuffer[] = new byte[MAX_MESSAGE_LENGTH];
		int nOffset = 0;

		// Source ID
		chBuffer[nOffset++] = 0x10;
		chBuffer[nOffset++] = 0x00;

		// Func Code
		chBuffer[nOffset++] = (byte)(PLUTUS_BILLING_OPEN_MENU_REQ >> 8) & 0xFF;
		chBuffer[nOffset++] = (byte) (PLUTUS_BILLING_OPEN_MENU_REQ & 0xFF);

		// Data Length
		chBuffer[nOffset++] = (byte) ((0 >> 8) & 0xFF);
		chBuffer[nOffset++] = (byte) (0 & 0xFF);

		chBuffer[nOffset++] = (byte) 0xFF;
		boolean nDataRecv = sendDataToPlutusPlus(chBuffer, nOffset);
	}

	public class ResponseHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			byte[] buffer = bundle.getByteArray("RESPONSE_TEXT");
			int lenBuffer = buffer.length - 8;
			byte[] parsedBuffer = new byte[lenBuffer];
			boolean btxnstatus = buffer[6] == 0x01? true: false;
			System.arraycopy(buffer,7,parsedBuffer,0,lenBuffer);
			String strResp = new String(parsedBuffer).trim() + "\n\n";
			responseMessageHandler(strResp,btxnstatus);
		}
	}

	public void responseMessageHandler(String message, boolean txnStatus) {
		Message msg = mHandler.obtainMessage(TestBillingAppActivity.RESPONSE_MESSAGE_DIALOG);
		Bundle bundle = new Bundle();
		bundle.putString(TestBillingAppActivity.RESPONSE_MESSAGE, message);
		bundle.putBoolean("STATUS",txnStatus);
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * API to established connection with PlutusPlus Appliaction.
	 * 
	 * @param dataBuffer
	 *            data buffer in byte Array
	 * @param dataLength
	 *            buffer length
	 * @return true if successfully write else false
	 */
	public boolean sendDataToPlutusPlus(byte[] dataBuffer, int dataLength) {
		boolean result = false;
		try {
			Message msg = Message.obtain(null, TestBillingAppActivity.BILLING);
			try {
				Bundle bundle = new Bundle();
				bundle.putByteArray("TXN_REQUEST",dataBuffer);
				msg.replyTo = new Messenger(new ResponseHandler());
				msg.setData(bundle);
				if(TestBillingAppActivity.mService != null) {
					TestBillingAppActivity.mService.send(msg);

				}
				else{
					Toast.makeText(TestBillingAppActivity.context,"No Connection Established",Toast.LENGTH_SHORT).show();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			result = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return result;
	}

	/**
	 * @details: This API sends the prepared CSV to PlutusPlus and receives
	 *           return message from PlutusPlus
	 * @param byteCSV
	 *            CSV in byte Array
	 * @param nDataLength
	 *            CSV data length
	 * @return response string, if error or no response empty string would be
	 *         sent
	 */
	public String SendECRTxn(byte[] byteCSV, int nDataLength) {

		String strResponse = "";
		byte bArrBuffer[] = new byte[MAX_MESSAGE_LENGTH];
		int nOffset = 0;

		// Source ID
		bArrBuffer[nOffset++] = 0x10;
		bArrBuffer[nOffset++] = 0x00;

		// Func Code
		bArrBuffer[nOffset++] = (byte)(PLUTUS_BILLING_DATA_REQ >> 8) & 0xFF;
		bArrBuffer[nOffset++] = (byte) (PLUTUS_BILLING_DATA_REQ & 0xFF);

		// Data Length
		bArrBuffer[nOffset++] = (byte) ((nDataLength >> 8) & 0xFF);
		bArrBuffer[nOffset++] = (byte) (nDataLength & 0xFF);

		// The CSV prepared will be sent as data to this packet.
		System.arraycopy(byteCSV, 0, bArrBuffer, nOffset, nDataLength);
		nOffset += nDataLength;

		// last byte set
		bArrBuffer[nOffset++] = (byte) 0xFF;

		Log.i("TestBillingApp", "message to be sent: " + new String(byteCSV).trim());

		boolean nDataRecv = sendDataToPlutusPlus(bArrBuffer, nOffset);

		// if data send unsuccessful return with empty response string
		if (!nDataRecv) {
			strResponse = "";
			return strResponse;
		}
		return strResponse;
	}

}
