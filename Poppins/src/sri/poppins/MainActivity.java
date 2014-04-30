package sri.poppins;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.Builder;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

public class MainActivity extends Activity {
	
	private ProfilePictureView profilePictureView;
	private TextView user_fullname;
	private ListView statusMessagesList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

	
		final ArrayList<String> resultList = new ArrayList<String>();
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,resultList);

		// start Facebook Login
		Session.openActiveSession(this, true, new Session.StatusCallback() {
			// callback when session changes state
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (!session.isOpened()) {
					OpenRequest op = new Session.OpenRequest((Activity) MainActivity.this);

			        op.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
			        //op.setCallback(null);

			        List<String> permissions = new ArrayList<String>();
			        permissions.add("user_status");
			        permissions.add("user_likes");
			        permissions.add("email");
			        permissions.add("user_birthday");
			        permissions.add("user_friends");
			        permissions.add("read_stream");
			        permissions.add("friends_status");
			        op.setPermissions(permissions);

			        session = new Builder(MainActivity.this).build();
			        Session.setActiveSession(session);
			        session.openForPublish(op);

				}
				else if (session.isOpened()) {
			 	//Start Collecting all requests
			 		RequestBatch reqB = new RequestBatch();
			 		
			 		reqB.add(
	            // make request to the /me API
			 		Request.newMeRequest(session, new Request.GraphUserCallback() {
			 			// callback after Graph API response with user object
			 			@Override
			 			public void onCompleted(GraphUser user, Response response) {
			 				if (user != null) {
			 					//Find the elements from UI
			 					user_fullname = (TextView) findViewById(R.id.welcome);
			 					// Find the user's profile picture custom view
			 					profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);

			 					user_fullname.setText("Hey There! " + user.getFirstName() + "!" );
			 					profilePictureView.setCropped(true);
			                    profilePictureView.setProfileId(user.getId());
			 				}
			 			}
	            	})
	            	);
			 		
			 		//Add the request for friends info into the batch variable
			 	//	String fqlQuery = "SELECT source_id, message FROM stream where filter_key='app_2915120374' "
			 		//		+ "and not attachment.media and source_id IN "
			 			//	+ "(SELECT uid1 FROM friend WHERE uid2=me()) ORDER BY created_time DESC LIMIT 50";
			 		
			 		String fqlQuery = "/me/friends";
			 		
			 		//String query_for_username;
			 		
//			 	        Bundle params = new Bundle();	
//			 	        params.putString("q", fqlQuery);
			 		
			 		Bundle params = new Bundle();
			 		params.putString("fields", "statuses.limit(10).fields(message,from),name,picture");
			 		params.putString("limit", "10");
			 		
			 		
			 		
			 	       // session = Session.getActiveSession();
			 		reqB.add(new Request(session,
			 	            fqlQuery,                         
			 	            params,                         
			 	            HttpMethod.GET,                 
			 	            new Request.Callback(){         
								public void onCompleted(Response response) 
			 	                {
									//GraphObject go = response.getGraphObject();	
			 	                    Log.i("Test","Result: " + response.toString());
			 	                    try {
			 	                    	
			 	                    	JSONArray result = response.getGraphObject().getInnerJSONObject().getJSONArray("data");
																			
										for(int i=0; i < result.length(); i++)
										{
											//query_for_username = "select name from user where uid = " + result.getJSONObject(i).getString("source_id");
											//resultList.add(result.getJSONObject(i).getString("name"));
											if(result.getJSONObject(i).has("statuses"))
											{
												JSONObject statusObj = result.getJSONObject(i).getJSONObject("statuses");
												if(statusObj.has("data"))
												{
													JSONArray getSecondArray = statusObj.getJSONArray("data");
													for(int j=0; j<getSecondArray.length(); j++)
													{
														String posted_by_name="";
														if(getSecondArray.getJSONObject(j).has("from"))
														{
															JSONObject posted_by = getSecondArray.getJSONObject(j).getJSONObject("from");
															posted_by_name = posted_by.getString("name");
														}
														if(getSecondArray.getJSONObject(j).has("message"))
														{
															Log.i("test","Val:"+getSecondArray.getJSONObject(j).getString("message"));
															resultList.add(posted_by_name+": "+getSecondArray.getJSONObject(j).getString("message"));
														}
													}
												}
											}
										}
										statusMessagesList = (ListView) findViewById(R.id.statusMessagesList);
										//adapter.addAll(resultList);
										statusMessagesList.setAdapter(adapter);
										
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
			 	                }                  
			 	        })
			 		);
			 	
			 		reqB.executeAsync();
			 	}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
}
