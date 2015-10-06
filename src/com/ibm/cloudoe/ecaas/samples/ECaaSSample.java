package com.ibm.cloudoe.ecaas.samples;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ECaaSSample
 * A ElasticCaching Java Native APIs Sample application.
 * <p>
 * This sample application demonstrates how to use ElasticCaching Java Native
 * APIs in a Java Web application and deploy it on Bluemix.
 * 
 * You can refer to the Elastic Caching Java Native API Specification
 * http://pic.dhe.ibm.com/infocenter/wdpxc/v2r5/index.jsp?topic=%2Fcom.ibm.websphere.datapower.xc.doc%2Fcxslibertyfeats.html
 * 
 */
@WebServlet(urlPatterns ="/ecaas", loadOnStartup=1)
public class ECaaSSample extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ECaaSSample() {
		super();
		ECacheConnection.initECaaS();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * Process /ecaas reuqest and return the relevant processing results
	 *  
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//set request and response configuration
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(200);
		try {
			//get request data
			String key = request.getParameter("key");
			String operation = request.getParameter("operation");
			String newValue = request.getParameter("value");
			Object retrievedValue;
			String mapName = "sample.NONE.P";
			//Process operation value and return processing results 
			if ("get".equals(operation)) {
				// get value of this key.
				retrievedValue = ECacheConnection.getData(mapName, key);
				response.getWriter().write(retrievedValue == null ? "null" : retrievedValue.toString());
				System.out.println("retrieved: " + retrievedValue);
			} else if ("put".equals(operation)) {
				// update or insert this value.
				ECacheConnection.postData(mapName, key, newValue);
				response.getWriter().write("Put successfull.");
				System.out.println("put key=" + key + " value=" + newValue);
			} else if ("delete".equals(operation)) {
				// delete this key/value.
				ECacheConnection.deleteData(mapName, key);
				response.getWriter().write("Remove successfull.");
				System.out.println("deleted key=" + key);
			} else if ("all".equals(operation)) {
				// get all key/value
				List<ECache> list = ECacheConnection.getAllData(mapName);
				String res = list.toString();
				response.getWriter().write(res);
				System.out.println("grid entries:" + res);
				System.out.println("grid entries size:" + list == null ? 0: list.size());
			}
		} catch (Exception e) {
			System.out.println("Failed to perform operation on map.");
			e.printStackTrace();
			response.setStatus(500);
		}
	}
}
