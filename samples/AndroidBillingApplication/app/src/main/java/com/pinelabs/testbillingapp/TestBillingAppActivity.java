package com.pinelabs.testbillingapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.MailTo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.regex.Matcher;

/**
 * 
 * @author Pine Labs
 * @details This application is used to check for billing app functionality of
 *          PlutusPlus. This testing App sends the data as CSV and receives
 *          the response as CSV as well. It connects with PlutusPlus through
 *          Messenger.
 * 
 */
public class TestBillingAppActivity extends Activity implements AdapterView.OnItemSelectedListener {
	/** Called when the activity is first created. */
	
	public static final int RESPONSE_MESSAGE_DIALOG = 33;
	public static final String RESPONSE_MESSAGE = "response_message";
	public static final int PROGRESS_DIALOG_START = 34;
	public static final String PROGRESS_MESSAGE = "progress_message";
	public static final int PROGRESS_DIALOG_END = 35;
	public static final int MAX_MESSAGE_LENGTH = 2048;
	public static final String TXN_VOID = "4006";
	public static final String TXN_SALE = "4001";
	
	public static boolean bIsStressTestClicked = false;

	public static String hostIPAddress = "127.0.0.1";			//local host IP Address
	public static int hostPort = 11892;
	public static Context context;
	public static HashMap<String, String> TransactionTypeMap;
	
	Thread tcpipthread = null;
	public TCPIPforECR objTcpIpECR;
	int it = 0;

	//text box & radio button for main application
	EditText ev_memoNumber;
	EditText ev_bankCode;
	EditText ev_amount;
	EditText ev_invoiceNumber;
	EditText ev_transactionType;

	//text box for settings
	EditText et_ipAddress;
	EditText et_ipPort;

	//text box for print dump
	EditText et_printDump;

	Spinner function_code;
	EditText user_id,user_pin,new_user_id,new_pin,role_id,jsonData;

	//the fields in CSV
	public String strTransactionType = "";
	public String strMemoNumber = "";
	public String strAmount = "";
	public String strBankCode = "";
	public String strTrackData1 = "";
	public String strTrackData2 = "";
	public static String strInvoiceNumber = "";
	public String strIsSwipeEntry = "";
	public String strTerminalID = "";
	public String strRFU1 = "";
	public String strRFU2 = "";

	public String printDump = "";

	public ProgressDialog progressDialog;

	//default value to print dump edit text box
	public String tempCSV = "0;False;True;1;***********************|"
			+ "0;True;True;2;Print Stores|"
			+ "0;Flase;True;3;Pine Labs Pvt. Ltd.|"
			+ "0;Flase;True;4;Noida - 201301|"
			+ "0;False;True;5;***********************|"
			+ "0;False;False;6;Memo#   :        1234|"
			+ "0;False;False;7;DATE    :    21/05/12|"
			+ "0;False;False;8;TIME    :    12:49:52|"
			+ "0;False;False;9;INVOICE :     0000445|" + "0;False;False;10;|"
			+ "0;True;True;11;SALE|"
			+ "0;False;False;12;------------------------|"
			+ "0;False;False;13;  ITEMS           TOTAL|"
			+ "0;False;False;14;------------------------|"
			+ "0;False;False;15;1.Sofa Set     Rs.29000|"
			+ "0;False;False;16;2.Dining Table Rs.17500|"
			+ "0;False;False;17;3.Table Lamp    Rs.3450|"
			+ "0;False;False;18;------------------------|"
			+ "0;False;False;19;Total Amount:   Rs.49950|"
			+ "0;False;False;20;Vat              Rs.1500|"
			+ "0;False;False;21;------------------------|"
			+ "0;True;True;22;Net Payable:   Rs.51450|"
			+ "0;False;False;23;------------------------|"
			+ "0;False;False;24;|" + "0;False;False;25;SIGN: ...............|"
			+ "0;True;True;26;Mayank Aggarwal|" + "0;False;False;27;|"
			+ "0;False;False;28;***********************|"
			+ "0;False;False;29;Thank You for Shopping|"
			+ "0;False;False;30;CoustmerCare:9123456790|"
			+ "0;False;False;31;Price is what you pay..|"
			+ "0;False;False;32;Value is what you get..|"
			+ "0;False;False;33;***********************|"
			+ "0;False;False;34;|" + "1;True;True;35;19000004|"
			+ "0;False;False;36;|" + "2;True;True;37;123456789|"
			+ "0;False;False;38;|" + "0;False;False;39;|"
			+ "0;False;False;40;|";

	public String strCSV = "";
	public static final int BILLING_RESPONSE = 1;
	public static final int BILLING = 1;
	public static final int MASTERAPP = 2;
	public static Messenger mService = null;
	static boolean mBound = false;
	Thread thread;
	public static final int DO_TRANSACTION = 10000;
	public static final int DO_SET_CONNECTION = 10001;
	public static final int DO_RUN_PVM = 10002;
	public static final int DO_PRINT_DUMP = 10003;
	public static final int DO_ACTIVATION = 10004;
	public static final int DO_INITIALIZATION = 10005;
	public static final int DO_SETTLEMENT = 10006;
	public static final int DO_GET_STATUS = 10007;
	public static final int DO_CHANGE_PASSWORD = 10008;
	public static final int DO_LOGIN = 10009;
	public static final int DO_LOGOUT= 10010;
	public static final int DO_ADD_USER= 10011;
	public static final int DO_REMOVE_USER= 10012;
	public static final int START_TRANSACT= 0;

	int pos = -1;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.v("TestBillingApp", "onCreate TestBillingAppActivity");
		context = TestBillingAppActivity.this;

		defineTransactionTypes();

		//get reference
		ev_memoNumber = (EditText) findViewById(R.id.ev_memo_number);
		ev_bankCode = (EditText) findViewById(R.id.ev_bank_code);
		ev_transactionType = (EditText) findViewById(R.id.ev_transaction_type);
		ev_amount = (EditText) findViewById(R.id.ev_amount);
		ev_invoiceNumber = (EditText) findViewById(R.id.ev_invoice_number);

		function_code = (Spinner) findViewById(R.id.function_code);
		user_id = (EditText) findViewById(R.id.user_id);
		user_pin = (EditText) findViewById(R.id.user_pin);
		new_user_id = (EditText) findViewById(R.id.new_user_id);
		new_pin = (EditText) findViewById(R.id.new_pin);
		role_id = (EditText) findViewById(R.id.role_id);
		jsonData = (EditText) findViewById(R.id.jsonData);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.function_codes,android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		function_code.setAdapter(adapter);
		function_code.setOnItemSelectedListener(this);
		//set edit box after click of next button
		ev_memoNumber.setNextFocusDownId(R.id.ev_bank_code);
		ev_bankCode.setNextFocusDownId(R.id.ev_transaction_type);
		ev_transactionType.setNextFocusDownId(R.id.ev_amount);
		ev_amount.setNextFocusDownId(R.id.ev_invoice_number);

		//default values
		ev_memoNumber.setText("12345");
		ev_amount.setText("1000");
		ev_transactionType.setText(TransactionTypeMap.get("Sale"));

		//Binding BillingApplication to PlutusPlus Application
		bindToPlutusPlus();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
		if(mService == null || mBound == false) {
			bindToPlutusPlus();
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					int timeOut = 10000;
					int count = timeOut/1000;
					while (!mBound) {
						try {
							Thread.sleep(1000);
							count--;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(count == 0) {
							break;
						}
					}
					if(mBound) {
						pos = position;
					}
					else {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(TestBillingAppActivity.this, "Could not make connection with PlutusPlus", Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			});
			thread.start();
		}
		else {
			pos = position;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	public void startTransaction(View view) {
	    final Message message = new Message();
        if(mService == null || mBound == false) {
            bindToPlutusPlus();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int timeOut = 10000;
                    int count = timeOut/1000;
                    while (!mBound) {
                        try {
                            Thread.sleep(1000);
                            count--;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(count == 0) {
                            break;
                        }
                    }
                    if(mBound) {
                        message.what = START_TRANSACT;
                        mHandler.sendMessage(message);
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TestBillingAppActivity.this, "Could not make connection with PlutusPlus", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            thread.start();
        }
        else {
            transact();
        }
	}

	public void transact() {
        switch (pos) {
            case 1:
                doTransaction();
                break;
            case 2:

                break;
            case 3:
                onResetButtonClick();
                break;
            case 4:
                setConnection();
                break;
            case 5:
                runPVM();
                break;
            case 6:
                print();
                break;
            case 7:
                activate();
                break;
            case 8:
                initialize();
                break;
            case 9:
                settle();
                break;
            case 10:
                getStatus();
                break;
            case 11:
                changePassword();
                break;
            case 12:
                login();
                break;
            case 13:
                addUser();
                break;
            case 14:
                removeUser();
                break;
            case 15:
                logout();
                break;
			case 16:
				reset();
				break;
        }
    }

	public void bindToPlutusPlus() {
		Intent intent = new Intent("com.pinelabs.plutusplus.ACTION_BIND");
		intent.setPackage("com.pinelabs.plutusplus");
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			mBound = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			mBound = false;
		}
	};

	public void defineTransactionTypes(){
		TransactionTypeMap = new HashMap<String, String>();
		TransactionTypeMap.put("Sale", "4001");
		TransactionTypeMap.put("Refund", "4002");
		TransactionTypeMap.put("Adjust", "4005");
		TransactionTypeMap.put("Void", "4006");
		TransactionTypeMap.put("Pre Auth", "4007");
		TransactionTypeMap.put("Sale Complete", "4008");
		TransactionTypeMap.put("Tip Adjust", "4015");
		TransactionTypeMap.put("GV Activate", "4202");
		TransactionTypeMap.put("GV Redeem", "4203");
		TransactionTypeMap.put("GV Balance", "4204");
	}

	public void print() {
		String dataToPrint = "2;FALSE;FALSE;0;12345678901234567890|0;TRUE;FALSE;1;                JANAKPURI               |0;FALSE;FALSE;2;            NEW DELHI    DEL            |0;TRUE;FALSE;3;DATE :2018-04-18          TIME :17:41:17|0;FALSE;FALSE;4;MID :                      TID :24312413|1;FALSE;FALSE;13;images/im10000014|0;FALSE;FALSE;5;BATCH NUM :                       000007|0;FALSE;FALSE;6;INV. NUM :                        000039|0;FALSE;FALSE;7;Sale|0;FALSE;FALSE;8;552115******7148                    Chip|0;FALSE;FALSE;9;EXP DATE  :XX/XX      CARD TYPE :MAESTRO|0;FALSE;FALSE;10;TXN ID    :                      4082411|0;FALSE;FALSE;11;APP:                     CIMB MasterCard|0;FALSE;FALSE;12;AID|2;FALSE;FALSE;12;12345678341234567890|1;FALSE;FALSE;13;images/im10000001";
		objTcpIpECR = new TCPIPforECR(context, dataToPrint, mHandler);
		objTcpIpECR.startPrintDumpTransaction(dataToPrint);
	}

    public void activate() {
	    String dataToSend = "{\n" +
                "  \"OperationType\": 1001,\n" +
				"\"UserId\":" + user_id.getText().toString() +
                "}\n";

		byte[] csvData = fillCSVRequest(dataToSend,new byte[]{(byte)0x09,(byte)0x87});
        objTcpIpECR = new TCPIPforECR(context, new String(csvData), mHandler);
        objTcpIpECR.sendRequestToPlutusPlus(csvData);
    }

	public void initialize() {
		String dataToSend = "{\n" +
				"\"OperationType\": 1001,\n" +
				"\"UserId\":" + user_id.getText().toString() +
				"}\n";

		byte[] csvData = fillCSVRequest(dataToSend,new byte[]{(byte)0x09,(byte)0x88});
		objTcpIpECR = new TCPIPforECR(context, dataToSend, mHandler);
		objTcpIpECR.sendRequestToPlutusPlus(csvData);
	}

	public void settle() {
		String dataToSend = "{\n" +
				"  \"OperationType\": 1004,\n" +
				"\"UserId\":" + user_id.getText().toString() +
				"}\n";

		 dataToSend = jsonData.getText().toString();
		byte[] csvData = fillCSVRequest(dataToSend,new byte[]{(byte)0x09,(byte)0x89});
		objTcpIpECR = new TCPIPforECR(context, dataToSend, mHandler);
		objTcpIpECR.sendRequestToPlutusPlus(csvData);
	}

	public void getStatus() {
		String dataToSend = "{\n" +
				"\"OperationType\": 1004\n" +
				"}\n";

		byte[] csvData = fillCSVRequest(dataToSend,new byte[]{(byte)0x09,(byte)0x90});
		objTcpIpECR = new TCPIPforECR(context, dataToSend, mHandler);
		objTcpIpECR.sendRequestToPlutusPlus(csvData);
	}

	public void reset() {
		String dataToSend = "{\n" +
				"\"OperationType\":1004,\n" +
				"\"UserId\":" + user_id.getText().toString() + ",\n" +
				"\"UserPin\":" + user_pin.getText().toString() + ",\n" +
				"\"NewUserId\":" + new_user_id.getText().toString() + "\n" +
				"}\n";
		byte[] csvData = fillCSVRequest(dataToSend,new byte[]{(byte)0x09,(byte)0x85});
		objTcpIpECR = new TCPIPforECR(context, dataToSend, mHandler);
		objTcpIpECR.sendRequestToPlutusPlus(csvData);
	}

	public void changePassword() {
		String dataToSend = "{\n" +
				"\"OperationType\": 1005,\n" +
				"\"UserId\":" + user_id.getText().toString() + ",\n" +
				"\"CurrentPin\":" + user_pin.getText().toString() + ",\n" +
				"\"NewPin\":" + new_pin.getText().toString() + "\n" +
				"}\n";

		byte[] csvData = fillCSVRequest(dataToSend,new byte[]{(byte)0x09,(byte)0x92});
		objTcpIpECR = new TCPIPforECR(context, dataToSend, mHandler);
		objTcpIpECR.sendRequestToPlutusPlus(csvData);
	}

	public void login() {
		String dataToSend = "{\n" +
				"\"OperationType\": 1004,\n" +
				"\"UserId\":" + user_id.getText().toString() + ",\n" +
				"\"UserPin\":" + user_pin.getText().toString() + "\n" +
				"}\n";

		byte[] csvData = fillCSVRequest(dataToSend,new byte[]{(byte)0x09,(byte)0x91});
		objTcpIpECR = new TCPIPforECR(context, dataToSend, mHandler);
		objTcpIpECR.sendRequestToPlutusPlus(csvData);
	}

	public void logout(){
		String dataToSend = "{\n" +
				"\"OperationType\": 1004,\n" +
				"\"UserId\":" + user_id.getText().toString() +
				"}\n";

		byte[] csvData = fillCSVRequest(dataToSend,new byte[]{(byte)0x09,(byte)0x86});
		objTcpIpECR = new TCPIPforECR(context, dataToSend, mHandler);
		objTcpIpECR.sendRequestToPlutusPlus(csvData);
	}

	public void addUser() {
		String dataToSend = "{\n" +
				"\"OperationType\": 1006,\n" +
				"\"UserId\":" + user_id.getText().toString() + ",\n" +
				"\"UserPin\":" + user_pin.getText().toString() + ",\n" +
				"\"NewUserId\":" + new_user_id.getText().toString() + ",\n" +
				"\"RoleId\":" + role_id.getText().toString() + "\n" +
				"}\n";

		byte[] csvData = fillCSVRequest(dataToSend,new byte[]{(byte)0x09,(byte)0x93});
		objTcpIpECR = new TCPIPforECR(context, dataToSend, mHandler);
		objTcpIpECR.sendRequestToPlutusPlus(csvData);
	}

	public void removeUser() {
		String dataToSend = "{\n" +
				"  \"OperationType\": 1007,\n" +
				"\"UserId\":" + user_id.getText().toString() + ",\n" +
				"\"UserPin\":" + user_pin.getText().toString() + ",\n" +
				"\"DeleteUserId\":" + new_user_id.getText().toString() +
				"}\n";
		byte[] csvData = fillCSVRequest(dataToSend,new byte[]{(byte)0x09,(byte)0x94});
		objTcpIpECR = new TCPIPforECR(context, dataToSend, mHandler);
		objTcpIpECR.sendRequestToPlutusPlus(csvData);
	}

	public void doTransaction() {
		Log.v("TestBillingApp", "inside onDoAnyTransactionClick");
		bIsStressTestClicked= false;
		strAmount = ev_amount.getText().toString().trim();
		strMemoNumber = ev_memoNumber.getText().toString().trim();
		strBankCode = ev_bankCode.getText().toString().trim();
		strTransactionType = ev_transactionType.getText().toString().trim();
		strInvoiceNumber = ev_invoiceNumber.getText().toString().trim();

		String jsonString = jsonData.getText().toString().trim();
		if (jsonString.isEmpty()) {
			//check for Memo Number
			if (strMemoNumber.equalsIgnoreCase("")) {
				Toast.makeText(getApplicationContext(), "Memo # Not Filled", Toast.LENGTH_SHORT).show();
				ev_memoNumber.requestFocus();
				return;
			}

			//if transaction type is Void Then check for Bank Code too.
			if (strTransactionType.equalsIgnoreCase(TXN_VOID) && strBankCode.equalsIgnoreCase("")) {
				Toast.makeText(getApplicationContext(), "Bank Code Not Filled",
						Toast.LENGTH_SHORT).show();
				ev_bankCode.requestFocus();
				return;
			}

			//check for Transaction Type
			if (strTransactionType.equalsIgnoreCase("")) {
				Toast.makeText(getApplicationContext(), "Txn Type Not Filled", Toast.LENGTH_SHORT).show();
				ev_transactionType.requestFocus();
				return;
			}

			strIsSwipeEntry = "TRUE";
			int iTxnType = Integer.parseInt(strTransactionType);
			if (iTxnType < 4200) {
				strCSV = strTransactionType + ",";            // 0. Transaction Type
				strCSV += strMemoNumber + ",";                // 1. Billing Reference Number
				strCSV += strAmount + ",";                    // 2. Total EFT Amount
				strCSV += strBankCode + ",";                // 3. Bank Code
				strCSV += strTrackData1 + ",";                // 4. Track Data 1
				strCSV += strTrackData2 + ",";                // 5. Track Data 2
				strCSV += strInvoiceNumber + ",";            // 6. Invoice Number
				strCSV += strIsSwipeEntry + ",";            // 7. Is Swipe Entry
				strCSV += strTerminalID + ",";                // 8. Terminal ID
				strCSV += strRFU1 + ",";                    // 9. RFU1
				strCSV += strRFU2 + ",";                    // 10. RFU12
			} else {
				strCSV = strTransactionType + ",";            // 0. Transaction Type
				strCSV += strMemoNumber + ",";                // 1. Billing Reference Number
				strCSV += strAmount + ",";                    // 2. Total EFT Amount
				strCSV += "" + ",";                        // 3. Field Empty
				strCSV += strInvoiceNumber + ",";            // 4. GV Number present in invoice number
				strCSV += strBankCode + ",";                // 5. Entry Mode
				strCSV += "" + ",";                        // 6. Invoice Number
				strCSV += "FALSE" + ",";                    // 7. Is Swipe Entry
				strCSV += strTerminalID + ",";                // 8. Terminal ID
				strCSV += strRFU1 + ",";                    // 9. RFU1
				strCSV += strRFU2 + ",";                    // 10. RFU12
			}
		}else{
			strCSV = jsonString;
		}

		//create thread for TCPIPforECR and start the thread.
		objTcpIpECR = new TCPIPforECR(context, strCSV, mHandler);
		objTcpIpECR.startTransaction();
	}

	public void setConnection() {
		objTcpIpECR = new TCPIPforECR(context, strCSV, mHandler);
		objTcpIpECR.startSetConnectionTransaction();
	}

	public void runPVM() {
		objTcpIpECR = new TCPIPforECR(context, strCSV, mHandler);
		objTcpIpECR.startRunPVMTransaction();
	}

	public void onOpenMenuButtonClick(View v) {
		objTcpIpECR = new TCPIPforECR(context, strCSV, mHandler);
		objTcpIpECR.startOpenMenuTransaction();
	}

	/**
	 * @details: This button sets the IP Address and port
	 * for communication with the PADContoller. Previously filled/default
	 * value is set at start. Ok button changes the values while cancel
	 * button affects nothing. The validation is done on IP Address.
	 * @param v
	 */
	public void onSettingButtonClick(View v) {
		Log.v("TestBillingApp", "inside onSettingButtonClick");

		final Dialog dialogUser = new Dialog(TestBillingAppActivity.this, R.style.Theme_Dialog_Translucent);
		dialogUser.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogUser.setContentView(R.layout.setting);
		dialogUser.setCancelable(true);

		et_ipAddress = (EditText) dialogUser.findViewById(R.id.ev_ipaddress);
		et_ipPort = (EditText) dialogUser.findViewById(R.id.ev_ip_port);
		
		//set with default values from previous use.
		String tempHostAddress = hostIPAddress;
		int tempHostPort = hostPort;
		et_ipAddress.setText(tempHostAddress);
		et_ipPort.setText(tempHostPort + "");

		Button yes = (Button) dialogUser.findViewById(R.id.btn_settingsok);
		yes.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v("TestBillingApp", "settings ok");
				//validation for IP Address
				String strIPAddress = et_ipAddress.getText().toString().trim();
				Matcher matcher = Patterns.IP_ADDRESS.matcher(strIPAddress);
				if (!matcher.matches()) {
					et_ipAddress.requestFocus();
					Toast.makeText(getApplicationContext(),
							"Invalid IP Address", Toast.LENGTH_SHORT).show();
					return;
				}
				//validation for IP Port
				String strIPPort = et_ipPort.getText().toString().trim();
				if (strIPPort.length() <= 1) {
					et_ipPort.requestFocus();
					Toast.makeText(getApplicationContext(), "Invalid IP Port",
							Toast.LENGTH_SHORT).show();
					return;
				}			
				try {
					hostPort = Integer.parseInt(strIPPort);
					hostIPAddress = strIPAddress;
				} catch (NumberFormatException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				dialogUser.cancel();
			}
		});
		Button no = (Button) dialogUser.findViewById(R.id.btn_settingscancel);
		no.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialogUser.cancel();
			}
		});

		dialogUser.show();
		dialogUser.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}
		});

	}

	/**
	 * @details: this activity displays the response string.
	 * @param responseString
	 */
	public static void printResponseString(String responseString,boolean status) {
		Log.v("TestBillingApp", "inside printResponseString");

		final Dialog dialogUser = new Dialog(context, R.style.Theme_Dialog_Translucent);
		dialogUser.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogUser.setContentView(R.layout.response);
		dialogUser.setCancelable(true);

		TextView tv_responseString = (TextView) dialogUser.findViewById(R.id.tv_response_string);
		tv_responseString.setText(responseString/* + "\n\n" + (status ? "									SUCCESSFUL !" : "									FAILED !")*/);

		Button yes = (Button) dialogUser
				.findViewById(R.id.btn_response_code_ok);
		yes.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Log.v("TestBillingApp", "response ok click");
				dialogUser.cancel();
			}
		});

		dialogUser.show();
		dialogUser.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}
		});

	}

	/**
	 * @details: method call on PrintDump button click.
	 * This function fills the data from the edit text to the RFU1 of the CSV
	 * with 4216 as transaction type and sends the data to PADController
	 * over TCP/IP.
	 * @param v
	 */
	public void onPrintDumpButtonClick(View v) {
		Log.v("TestBillingApp", "inside onPrintDumpButtonClick");

		final Dialog dialogUser = new Dialog(TestBillingAppActivity.this,
				R.style.Theme_Dialog_Translucent);
		dialogUser.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialogUser.setContentView(R.layout.printdump);
		dialogUser.setCancelable(true);

		et_printDump = (EditText) dialogUser.findViewById(R.id.ev_printdump);
		et_printDump.setText(tempCSV);
		Button yes = (Button) dialogUser.findViewById(R.id.btn_printdump_ok);
		yes.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Log.v("TestBillingApp", "response ok click");
				printDump = et_printDump.getText().toString();
				if (printDump.trim().equalsIgnoreCase("")) {
					Toast.makeText(getApplicationContext(),
							"Please Enter Some Text", Toast.LENGTH_SHORT)
							.show();
					return;
				}
//				strCSV = "4216" + ",,,,,,,,," + printDump + ",,";
				strCSV = "4216" + ",,,,,,,,,," + printDump + ",";
				//Thread objTcpIpECR = new TCPIPforECR(context, hostIPAddress, hostPort, strCSV, mHandler);
				//objTcpIpECR.start();
				dialogUser.cancel();
			}
		});

		Button no = (Button) dialogUser.findViewById(R.id.btn_printdump_cancel);
		no.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialogUser.cancel();
			}
		});

		dialogUser.show();
		dialogUser.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * @details: This method is used to reset all the fields.
	 * @param
	 */
	public void onResetButtonClick() {
		Log.v("TestBillingApp", "inside onResetButtonClick");
		ev_memoNumber.setText("");
		ev_bankCode.setText("");
		ev_transactionType.setText("");
		ev_amount.setText("");
		ev_invoiceNumber.setText("");
	}

	/**
	 * This object is used to get response string,
	 * Progress dialog start string and progress dialog end context from
	 * TCPIPforECR thread.
	 * on Progress dialog start the progress dialog is displayed with string and
	 * on Progress dialog end the progress dialog is dismissed.
	 */
	private final Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			String progressMessage = "";
			switch (msg.what) {
				case RESPONSE_MESSAGE_DIALOG:
					Bundle bundle = msg.getData();
					String responseMessage = bundle.getString(RESPONSE_MESSAGE);
					boolean status = bundle.getBoolean("STATUS");
					printResponseString(responseMessage,status);
					break;
				case PROGRESS_DIALOG_START:
					progressDialog = new ProgressDialog(TestBillingAppActivity.this);
					progressMessage = msg.getData().getString(PROGRESS_MESSAGE);
					progressDialog.setMessage(" " + progressMessage + " ");
	//				progressDialog.show();
					break;
				case PROGRESS_DIALOG_END:
					progressMessage = msg.getData().getString(PROGRESS_MESSAGE);
	//				if (!progressMessage.trim().equalsIgnoreCase("")) {
	//					Toast.makeText(getApplicationContext(), progressMessage,
	//							Toast.LENGTH_SHORT).show();
	//				}
					if (progressDialog != null && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					break;

				case DO_TRANSACTION:
					doTransaction();
					break;

				case DO_SET_CONNECTION:
					setConnection();
					break;

				case DO_RUN_PVM:
					runPVM();
					break;

				case DO_PRINT_DUMP:
					print();
					break;

                case DO_ACTIVATION:
                    activate();
                    break;

				case DO_INITIALIZATION:
					initialize();
					break;

				case DO_SETTLEMENT:
					settle();
					break;

				case DO_GET_STATUS:
					getStatus();
					break;

				case DO_CHANGE_PASSWORD:
					changePassword();
					break;

				case DO_LOGIN:
                    login();
                    break;

				case DO_LOGOUT:
                    logout();
                    break;

				case DO_ADD_USER:
					addUser();
					break;

				case DO_REMOVE_USER:
					removeUser();
					break;

                case START_TRANSACT:
                    transact();
                    break;
			}
        }
	};

	byte[] fillCSVRequest(String str,byte[] functionCode) {
		byte[] data = str.getBytes();
		int len = data.length;

		byte[] csvData = new byte[MAX_MESSAGE_LENGTH];
		int index = 0;
		csvData[index++] = (byte)0x00;
		csvData[index++] = (byte)0x01;
		csvData[index++] = functionCode[0];
		csvData[index++] = functionCode[1];

		csvData[index++] = (byte)(len>>8);
		csvData[index++] = (byte)(len);

		System.arraycopy(data,0,csvData,index,data.length);
		index = index + data.length;

		csvData[index++] = (byte)0xFF;
		byte[] temp = new byte[index];
		System.arraycopy(csvData,0,temp,0,index);
		return temp;
	}
}