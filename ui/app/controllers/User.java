package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import util.NemakiConfig;
import util.Util;
import views.html.user.blank;
import views.html.user.index;

import com.fasterxml.jackson.databind.JsonNode;

import constant.PropertyKey;


@Authenticated(Secured.class)
public class User extends Controller {

	private static String coreRestUri = NemakiConfig.getValue(PropertyKey.NEMAKI_CORE_URI) + "rest/";
	private static String endPoint = coreRestUri + "user/";
	
	
	public static Result index(){
		List<model.User>emptyList = new ArrayList<model.User>();
		
	    	return ok(index.render(emptyList));
	    }

	public static Result search(String term){
    	JsonNode result = Util.getJsonResponse(endPoint + "search?query=" + term);

    	//TODO check status
    	JsonNode users = result.get("result");
    	
    	List<model.User> list = new ArrayList<model.User>();
    	
    	if(users == null){
    		users = Json.parse("[]");
    	}else{
    		Iterator<JsonNode>itr = users.elements();
        	while(itr.hasNext()){
        		JsonNode node = itr.next();
        		
        		model.User user = new model.User();
        		user.id = node.get("userId").asText();
        		user.name = node.get("userName").asText();
        		user.firstName = node.get("firstName").asText();
        		user.lastName = node.get("lastName").asText();
        		user.email = node.get("email").asText();
        		
        		list.add(user);
        	}
    	}
    	
    	
    	//render
    	if(Util.dataTypeIsHtml(request().acceptedTypes())){
    		return ok(index.render(list));
    	}else{
    		return ok(users);
    		
    	}
    	
    }

	public static Result showBlank(){
		
		return ok(blank.render());
	}

	public static Result showDetail(String id){
		JsonNode result = Util.getJsonResponse(endPoint + "show/" + id);
		
		if("success".equals(result.get("status").asText())){
			JsonNode _user = result.get("user");
			model.User user = new model.User(_user);
			return ok(views.html.user.detail.render(user));
		}else{
			//TODO
			return ok();
		}
	}
	
	public static Result create(){
    	Map<String, String>params = buildParams();
    	JsonNode result = Util.postJsonResponse(endPoint + "create/" + params.get("id"), params);

    	if(isSuccess(result)){
    		flash("flash message");
    		return redirect(routes.User.index());
    	}else{
    		//TODO error
    		return ok();
    	}
	}
	
	public static Result update(String id){
    	
    	Map<String, String>params = buildParams();
    	
    	JsonNode result = Util.putJsonResponse(endPoint + "update/" + id , params);
    	
    	if(isSuccess(result)){
    		return redirect(routes.User.index());
    	}else{
    		//TODO
    		return ok();
    	}
	}

	public static Result delete(String id){
		JsonNode result = Util.deleteJsonResponse(endPoint + "delete/" + id);
		
		//TODO error
		return redirect(routes.User.index());
	}
	
	private static boolean isSuccess(JsonNode result){
		return "success".equals(result.get("status").asText());
	}
	
	private static Map<String, String> buildParams(){
		DynamicForm input = Form.form();
    	input = input.bindFromRequest();
    	//Extract form parameters
    	String userId = input.get("userId");
    	String password = input.get("password");
    	String userName = input.get("userName");
    	String firstName = input.get("firstName");
    	String lastName = input.get("lastName");
    	String email = input.get("email");


    	Map<String, String>params = new HashMap<String, String>();
    	params.put("id", userId);
    	params.put("name", userName);
    	params.put("firstName", firstName);
    	params.put("lastName", lastName);
    	params.put("password", password);
    	params.put("email", email);
    	
    	return params;
	}
}
