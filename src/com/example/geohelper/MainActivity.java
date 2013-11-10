package com.example.geohelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	Geocoder geocoder;
	List<EventInfo> eventInfoList = new ArrayList<EventInfo>();;
	double NEAR_DISTANCE = 100;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 geocoder = new Geocoder(this, Locale.ENGLISH);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void GetText_Click(View view) {
		 final TextView txtView = (TextView) findViewById(R.id.textView1);
		 StringBuilder sb = new StringBuilder();
			for (EventInfo eI : eventInfoList) {
				sb.append(eI.eventType + eI.Address + "\n");
			}
			txtView.setText(sb.toString() + eventInfoList.size());
			findnearestEvents();
			/*String add ="1410 South Museum Campus Drive Chicago, IL 60605";
				add = 	replaceString(add);
				Toast.makeText(getBaseContext(), add,
						Toast.LENGTH_SHORT).show();
			List<Address> addressList;
			try {
				addressList=geocoder.getFromLocationName(add, 1);
				if(addressList.size() >0){
					txtView.setText(addressList.get(0).getLatitude() + "");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	}
	
	public String replaceString(String address){
		int length = address.length();
		int index = 0;
		for(int i=0;i<length;i++){
			if(!((int)address.charAt(i) >= 48 && (int)address.charAt(i) <=57)){
				index =i;
				break;
			}
		}
		return address.substring(index, length);
	}
	public void findnearestEvents(){
		double userLatitude = 40.1097;
		double userLongitude = -88.2042;
		List<String> userInterestList = new ArrayList<String>();
		userInterestList.add("Sports");
		List<Address> addressList;
		for(EventInfo eventInfo:eventInfoList){
			for(String interest:userInterestList){
				if(eventInfo.eventType.contains(interest))
				{
					try {
						addressList=geocoder.getFromLocationName(eventInfo.Address.replaceAll("(?m)^\\d+\\.\\s*|\\s*-\\s*.*?$", ""), 1);
						if(addressList.size() >0){
							double lat = addressList.get(0).getLatitude();
							double lon = addressList.get(0).getLongitude();
							
							if(getDistance(userLatitude, userLongitude, lat, lon) <= NEAR_DISTANCE)
							{
								//popup Notification
								String notification = eventInfo.title + "@" + eventInfo.Address;
								Toast.makeText(getBaseContext(), notification,
										Toast.LENGTH_SHORT).show();
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
		
		
	}
	

	
	public double getDistance(double x1, double y1,double x2, double y2){
		double temp =Math.pow((x2-x1),2) + Math.pow((y2-y1),2);
	return Math.sqrt(temp);
	}
	public void GetFeed_Click(View view) {
		// eventInfoList= new ArrayList<EventInfo>();
		
		final List<Thread> threadList= new ArrayList<Thread>();
		SimpleRSSParser simpleRSSParser = new SimpleRSSParser(
				"http://rss.metrodata.com/eventguide-chicago-today.xml",
				new SimpleRSSParserCallBack() {
					@Override
					public void onFeedParsed(List<RSSItem> items) {
						for (int i = 0; i < items.size(); i++) {

							DownloadTask downloadTask = new DownloadTask(items.get(i).getLink().toString(),
									items.get(i).getTitle());
							Thread thread = new Thread(downloadTask);
							threadList.add(thread);
							thread.start();
						}
					}

					@Override
					public void onError(Exception ex) {
						Toast.makeText(getBaseContext(), ex.getMessage(),
								Toast.LENGTH_SHORT).show();
						Log.d("Error", ex.getMessage());
					}
				});
		simpleRSSParser.parseAsync();
		for(Thread thread:threadList){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

	public class DownloadTask implements Runnable{
		String link,title;
		public DownloadTask(String link, String title){
			this.link=link;
			this.title=title;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				getAddress(link,title);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void getAddress(String link, String title) throws IOException {
			EventInfo eventInfo = new EventInfo();
			eventInfo.title = title;

			Log.d("getAddress", link);
			Document doc = Jsoup.connect(link).get();
			Log.d("AftergetAddress", link);
			Elements elements = doc.getAllElements();
			boolean Vflag = false;
			boolean eflag = false;
			String href = null;
			boolean invalidAddress =false;
			for (Element ele : elements) {
				String checkString = ele.text();
				if (eflag) {
					eventInfo.eventType = checkString;
					break;
				}
				if (Vflag) {
					Elements e1 = ele.children();
					href = e1.toString();
					
					try{
						href = href.substring(href.indexOf('"') + 1,
								href.lastIndexOf('"'));
					}
					catch(StringIndexOutOfBoundsException ex)
					{
						eventInfo.Address =ele.text();
						invalidAddress=true;
						//Log.d("hrefError",ele.text() + ex.getMessage());
					}
					Vflag = false;
				}
				if (checkString.equalsIgnoreCase("Venue:")) {
					Vflag = true;
				}
				if (checkString.equalsIgnoreCase("Event Type:")) {
					eflag = true;
				}
			}

			if(!invalidAddress){
			doc = Jsoup.connect(href).get();
			elements = doc.getElementsByTag("address");
			eventInfo.Address = elements.text();
			}
			eventInfoList.add(eventInfo);
		}
		
	}
	
	

}
