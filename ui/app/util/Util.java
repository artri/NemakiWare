package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import play.Play;
import play.api.http.MediaRange;
import play.libs.Json;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.Request;

import com.fasterxml.jackson.databind.JsonNode;

public class Util {
	public static Session createCmisSession(String id, String password){
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		//TODO enable change a user
		parameter.put(SessionParameter.USER, id);
		parameter.put(SessionParameter.PASSWORD, password);

		// session locale
		parameter.put(SessionParameter.LOCALE_ISO3166_COUNTRY, "");
		parameter.put(SessionParameter.LOCALE_ISO639_LANGUAGE, "");

		// repository
		parameter.put(SessionParameter.REPOSITORY_ID, "bedroom");

		parameter. put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		String coreUrl = Play.application().configuration().getString("nemaki.core.url");
		parameter.put(SessionParameter.ATOMPUB_URL, coreUrl);

    	SessionFactory f = SessionFactoryImpl.newInstance();



    	Session session = f.createSession(parameter);
    	OperationContext operationContext = session.createOperationContext(null,
				true, true, false, IncludeRelationships.BOTH, null, false, null, true, 100);
		session.setDefaultContext(operationContext);

		return session;
	}
	
	
	public static boolean isDocument(CmisObject obj) {
		return obj.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT);
	}

	public static Document convertToDocument(CmisObject obj) {
		Document doc = (Document) obj;
		return doc;
	}
	public static Folder convertToFolder(CmisObject obj) {
		Folder folder = (Folder) obj;
		return folder;
	}

	/**
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static File convertInputStreamToFile(InputStream inputStream)
			throws IOException {

		File file = File.createTempFile(
				String.valueOf(System.currentTimeMillis()), null);
		try {
			// write the inputStream to a FileOutputStream
			OutputStream out = new FileOutputStream(file);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			inputStream.close();
			out.flush();
			out.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return file;
	}

	/**
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static File convertInputStreamToFile(ContentStream contentStream)
			throws IOException {

		InputStream inputStream = contentStream.getStream();

		File file = File.createTempFile(contentStream.getFileName(), "");

		OutputStream out = null;
		try {
			out = new FileOutputStream(file);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}finally{
			inputStream.close();
			out.flush();
			out.close();
		}
		
		file.deleteOnExit();
		return file;
	}

	public static ContentStream convertFileToContentStream(Session session,
			FilePart file) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file.getFile());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ContentStream cs = session.getObjectFactory().createContentStream(
				file.getFilename(), file.getFile().length(),
				file.getContentType(), fis);

		return cs;
	}

	public static String getIconName(CmisObject obj) {
		String fileName = "";

		// Document
		if (isDocument(obj)) {
			Document doc = (Document) obj;
			String mimeType = doc.getContentStreamMimeType();

			if ("application/x-javascript".equals(mimeType)) {
				fileName = "js.gif";
			} else if ("text/plain".equals(mimeType)) {
				fileName = "text-file-32.png";
			} else if ("application/msword".equals(mimeType)) {
				fileName = "doc-file-32.png";
			} else if ("text/xml".equals(mimeType)) {
				fileName = "xml.gif";
			} else if ("image/gif".equals(mimeType)) {
				fileName = "img-file-32.png";
			} else if ("image/jpeg".equals(mimeType)) {
				fileName = "img-file-32.png";
			} else if ("image/jpeg2000".equals(mimeType)) {
				fileName = "jpg.gif";
			} else if ("video/mpeg".equals(mimeType)) {
				fileName = "mpeg.gif";
			} else if ("audio/x-mpeg".equals(mimeType)) {
				fileName = "mpg.gif";
			} else if ("video/mp4".equals(mimeType)) {
				fileName = "mp4.gif";
			} else if ("video/mpeg2".equals(mimeType)) {
				fileName = "mp2.gif";
			} else if ("application/pdf".equals(mimeType)) {
				fileName = "pdf-file-32.png";
			} else if ("image/png".equals(mimeType)) {
				fileName = "img-file-32.png";
			} else if ("application/vnd.powerpoint".equals(mimeType)) {
				fileName = "ppt-file-32.png";
			} else if ("audio/x-wav".equals(mimeType)) {
				fileName = "wmv.gif";
			} else if ("application/vnd.excel".equals(mimeType)) {
				fileName = "xls-file-32.png";
			} else if ("application/zip".equals(mimeType)) {
				fileName = "zip.gif";
			} else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document"
					.equals(mimeType)) {
				fileName = "doc-file-32.png";
			} else if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
					.equals(mimeType)) {
				fileName = "xls-file-32.png";
			} else if ("application/vnd.openxmlformats-officedocument.presentationml.presentation"
					.equals(mimeType)) {
				fileName = "ppt-file-32.png";
			} else {
				fileName = "generic-file-32.png";
			}
			// Folder
			// TODO check cmis:folder
		} else {
			fileName = "folder-icon-default.gif";
		}

		return fileName;
	}

	private static HttpClient buildClient(){
		// configurations
				String userAgent = "My Http Client 0.1";

				// headers
				List<Header> headers = new ArrayList<Header>();
				headers.add(new BasicHeader("Accept-Charset", "utf-8"));
				headers.add(new BasicHeader("Accept-Language", "ja, en;q=0.8"));
				headers.add(new BasicHeader("User-Agent", userAgent));

				// set credential
				//TODO remove hard code
				Credentials credentials = new UsernamePasswordCredentials("admin",
						"admin");
				AuthScope scope = new AuthScope("localhost", 8080);
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(scope, credentials);

				// create client
				HttpClient httpClient = HttpClientBuilder.create()
						.setDefaultHeaders(headers)
						.setDefaultCredentialsProvider(credsProvider)
						.build();

				return httpClient;
	}

	private static JsonNode executeRequest(HttpClient client, HttpRequest request){

		try {
			HttpResponse response = client.execute((HttpUriRequest) request);
			InputStream is = response.getEntity().getContent();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			StringBuilder sb = new StringBuilder();

			String line;

			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			JsonNode jn = Json.parse(sb.toString());

			return jn;

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static JsonNode getJsonResponse(String url) {


		// create client
		HttpClient client = buildClient();

		HttpGet request = new HttpGet(url);

		return executeRequest(client, request);
	}

	public static JsonNode postJsonResponse(String url, Map<String, String>params) {
		// create client
		HttpClient client = buildClient();

		HttpPost request = new HttpPost(url);
		List<NameValuePair> list = new ArrayList<>();
		if(MapUtils.isNotEmpty(params)){
			for(Entry<String, String>entry : params.entrySet()){
				list.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
			}
		}

		try {
			request.setEntity(new UrlEncodedFormEntity(list, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return executeRequest(client, request);
	}
	
	public static JsonNode putJsonResponse(String url, Map<String, String>params) {
		// create client
		HttpClient client = buildClient();

		HttpPut request = new HttpPut(url);
		List<NameValuePair> list = new ArrayList<>();
		if(MapUtils.isNotEmpty(params)){
			for(Entry<String, String>entry : params.entrySet()){
				list.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
			}
		}

		try {
			request.setEntity(new UrlEncodedFormEntity(list, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return executeRequest(client, request);
	}

	public static JsonNode deleteJsonResponse(String url){
		HttpClient client = buildClient();
		HttpDelete request = new HttpDelete(url);
		return executeRequest(client, request);
	}
	
	public static String escapeLikeQuery(String query){
		String result = query;
		result = query.replaceAll("%", "\\%");
		result = query.replaceAll("_", "\\_");
		return result;
	}
	
	public static String escapeContainsQuery(String query){
		String result = query;
		result = query.replaceAll("*", "\\*");
		result = query.replaceAll("?", "\\?");
		return result;
	}
	
	public static String escapeSelector(String selector){
		String result = selector;
		result = selector.replaceAll(":", "\\\\\\\\:");
		return result;
	}
	
	public static List<String> dividePath(String path){
		String[] ary = path.split("/");
		
		if(ary.length == 0){
			return new ArrayList<String>();
		}else{
			List<String> result = new ArrayList<>();
			//TODO error
			//remove first empty element
			for(int i=1; i < ary.length; i++){
				result.add(ary[i]);
			}
			return result;
		}
	}
	
	public static boolean dataTypeIsHtml(List<MediaRange>accepted){
		if(CollectionUtils.isNotEmpty(accepted)){
			for(MediaRange mr : accepted){
				if(mr.toString().equals("text/html")){
					return true;
				}
			}
		}
		return false;
	}
	

	public static List<String> convertToList(JsonNode jsonArray){
		List<String> result = new ArrayList<>();
		
		Iterator<JsonNode> itr = jsonArray.iterator();
		while(itr.hasNext()){
			result.add(itr.next().asText());
		}
		
		return result;
	}
	
	public static Boolean isEditable(Property<?>property){
		Updatability updatability = property.getDefinition().getUpdatability();
		if(Updatability.READWRITE == updatability || Updatability.WHENCHECKEDOUT == updatability){
			return true;
		}else{
			return false;
		}
	}
	
	public static Map<String, Ace> zipWithId(Acl acl){
		Map<String, Ace>result = new HashMap<>();
		
		if(acl != null){
			List<Ace> list = acl.getAces();
			if(CollectionUtils.isNotEmpty(list)){
				for(Ace ace : list){
					result.put(ace.getPrincipalId(), ace);
				}
			}
		}
		
		return result;
	}
	
	public static String getContextPath(Request request){
		String _ctxt = Play.application().configuration().getString("application.context");
		String ctxt = (StringUtils.isBlank(_ctxt))? "" : _ctxt;
		
		return "http://" + request.host() + ctxt;
	}
	
	 public static BaseTypeId getBaseType(Session cmisSession, String objectTypeId){
		 ObjectType objectType = cmisSession.getTypeDefinition(objectTypeId, true);
		 return objectType.getBaseTypeId();
	 }
}