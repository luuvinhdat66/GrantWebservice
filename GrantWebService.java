package ***.*******;


import gov.grants.apply.services.applicantwebservices_v2.ApplicantWebServicesPortType;
import gov.grants.apply.services.applicantwebservices_v2.ApplicantWebServicesV20;
import gov.grants.apply.services.applicantwebservices_v2.ErrorMessage;
import gov.grants.apply.services.applicantwebservices_v2.GetApplicationInfoRequest;
import gov.grants.apply.services.applicantwebservices_v2.GetApplicationInfoResponse;
import gov.grants.apply.services.applicantwebservices_v2.GetApplicationListRequest;
import gov.grants.apply.services.applicantwebservices_v2.GetApplicationListResponse;
import gov.grants.apply.services.applicantwebservices_v2.GetApplicationStatusDetailRequest;
import gov.grants.apply.services.applicantwebservices_v2.GetApplicationStatusDetailResponse;
import gov.grants.apply.services.applicantwebservices_v2.GetApplicationZipRequest;
import gov.grants.apply.services.applicantwebservices_v2.GetApplicationZipResponse;
import gov.grants.apply.services.applicantwebservices_v2.GetOpportunitiesExpandedRequest;
import gov.grants.apply.services.applicantwebservices_v2.GetOpportunitiesExpandedResponse;
import gov.grants.apply.services.applicantwebservices_v2.GetOpportunityListRequest;
import gov.grants.apply.services.applicantwebservices_v2.GetOpportunityListResponse;
import gov.grants.apply.services.applicantwebservices_v2.SubmitApplicationRequest;
import gov.grants.apply.services.applicantwebservices_v2.SubmitApplicationResponse;
import gov.grants.apply.services.applicantwebservices_v2.GetApplicationListResponse.ApplicationInfo;
import gov.grants.apply.services.applicantwebservices_v2.GetOpportunitiesExpandedResponse.OpportunityInfo;
import gov.grants.apply.services.applicantwebservices_v2.GetSubmissionListRequest;
import gov.grants.apply.services.applicantwebservices_v2.GetSubmissionListResponse;
import gov.grants.apply.system.applicantcommonelements_v1.OpportunityDetails;
import gov.grants.apply.system.applicantcommonelements_v1.OpportunityFilter;
import gov.grants.apply.system.applicantcommonelements_v1.SubmissionDetails;
import gov.grants.apply.system.applicantcommonelements_v1.SubmissionFilter;
import gov.grants.apply.system.applicantcommonelements_v1.SubmissionFilterType;
import gov.grants.apply.system.grantscommonelements_v1.ApplicationFilter;
import gov.grants.apply.system.grantscommonelements_v1.Attachment;

import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.axis.client.Call;

import com.sun.xml.internal.ws.developer.JAXWSProperties;
import com.sun.xml.internal.ws.developer.StreamingDataHandler;

import ***.*******.logger.System_Logger;


public class GrantWebService extends Object
{
	
	public static final int SORT_UP_CFDA_NUMBER = 1;
    public static final int SORT_DOWN_CFDA_NUMBER = 2;

    public static final int SORT_UP_OPPORTUNITY_ID = 3;
    public static final int SORT_DOWN_OPPORTUNITY_ID = 4;

    public static final int SORT_UP_COMPETITION_ID = 5;
    public static final int SORT_DOWN_COMPETITION_ID = 6;

    public static final int SORT_UP_GRANTS_GOV_TRACKING_NUMBER = 7;
    public static final int SORT_DOWN_GRANTS_GOV_TRACKING_NUMBER = 8;

    public static final int SORT_UP_RECEIVED_DATETIME = 9;
    public static final int SORT_DOWN_RECEIVED_DATETIME = 10;

    public static final int SORT_UP_GRANTS_GOV_APPLICATION_STATUS = 11;
    public static final int SORT_DOWN_GRANTS_GOV_APPLICATION_STATUS  = 12;

    public static final int SORT_UP_STATUS_DATETIME = 13;
    public static final int SORT_DOWN_STATUS_DATETIME  = 14;

    public static final int SORT_UP_AGENCY_TRACKING_NUMBER = 15;
    public static final int SORT_DOWN_AGENCY_TRACKING_NUMBER = 16;
    
    public static final int SORT_UP_SUBMISSION_TITLE = 17;
    public static final int SORT_DOWN_SUBMISSION_TITLE = 18;    
    
    public static final int SORT_UP_PROPOSAL_NUMBER = 19;
    public static final int SORT_DOWN_PROPOSAL_NUMBER = 20;

    public static final int SORT_UP_PI_NAME = 21;
    public static final int SORT_DOWN_PI_NAME = 22;

    public static final int SORT_UP_RESPONSIBLE_PA_NAME = 23; 
    public static final int SORT_DOWN_RESPONSIBLE_PA_NAME = 24;

    public static final int SORT_UP_COMMITTEE_NAME = 25;
    public static final int SORT_DOWN_COMMITTEE_NAME = 26;
    
    /************************************************************/
	public static String ENCODING_TYPE = Call.ATTACHMENT_ENCAPSULATION_FORMAT_MIME;
 
    private GrantWebService(){}
    
    private static ApplicantWebServicesPortType retrival_binding;
    private static ApplicantWebServicesPortType submission_binding;
    private static GrantWebService GrantWebService;
    
    /**
     * Initializes Binding Object that connects to Grants.gov web-services by Setting KeyStore, Trust Store Parameters. 
     * @param session
     * @param Sys_Prop
     * @param SYSTEM_PARTITION_ID
     */
    public static GrantWebService init // this is the getInstance()
    (
        HttpSession 		session,
        System_Properties 	Sys_Prop, 
        int					SYSTEM_PARTITION_ID
    )
	{
    	if(GrantWebService == null)
    	{
    		GrantWebService = new GrantWebService();
    	}
		try
		{
			Sys_Prop.LoadSSLProperties(SYSTEM_PARTITION_ID);
			String httpsprotocols 	= Sys_Prop.getValueString(SYSTEM_PARTITION_ID,"system.httpsprotocols");
    		if(httpsprotocols != null && httpsprotocols.trim().length() > 0 )
    		{
    			System.setProperty("https.protocols", httpsprotocols ); //Value should be "TLSv1.1,TLSv1.2" for SHA-2 Cert
    		}
    		
			Security.addProvider ( new com.sun.net.ssl.internal.ssl.Provider () ) ;
			


			String ws_grants_gov_soapport_URL 	= Sys_Prop.getValueString(SYSTEM_PARTITION_ID,"system.ws_grants_gov_soapport_URL"); // /grantsws-applicant/services/v2/ApplicantWebServicesSoapPort
			String ws_grants_gov_wsdl_URL 		= Sys_Prop.getValueString(SYSTEM_PARTITION_ID,"system.ws_grants_gov_wsdl_URL"); // /grantsws-applicant/wsdl/ApplicantWebServices-V2.0.wsdl
			
			if(ws_grants_gov_soapport_URL == null || (ws_grants_gov_soapport_URL != null && ws_grants_gov_soapport_URL.trim().length() == 0))
			{
				throw new Exception("Please add ws_grants_gov_soapport_URL property to sa.properties as \nsystem.ws_grants_gov_soapport_URL_1=/grantsws-applicant/services/v2/ApplicantWebServicesSoapPort");
			}
			if(ws_grants_gov_wsdl_URL == null || (ws_grants_gov_wsdl_URL != null && ws_grants_gov_wsdl_URL.trim().length() == 0))
			{
				throw new Exception("Please add ws_grants_gov_wsdl_URL property to sa.properties as \nsystem.ws_grants_gov_wsdl_URL_1=/grantsws-applicant/wsdl/ApplicantWebServices-V2.0.wsdl");				
			}
			ws_grants_gov_soapport_URL 	= ws_grants_gov_soapport_URL.trim();
			ws_grants_gov_wsdl_URL 		= ws_grants_gov_wsdl_URL.trim();
			QName serviceName = new QName("http://apply.grants.gov/services/ApplicantWebServices-V2.0", "ApplicantWebServices-V2.0");
			
			if ( retrival_binding == null )
			{
				String ws_grants_gov_retrival_URL = Sys_Prop.getValueString(SYSTEM_PARTITION_ID,"system.ws_grants_gov_retrieval_URL").trim();
				//System.out.println("retrieval wsdl url  == " +  ws_grants_gov_retrival_URL + ws_grants_gov_wsdl_URL );
				java.net.URL sourcepoint = new URL ( ws_grants_gov_retrival_URL + ws_grants_gov_wsdl_URL) ;
				
				ApplicantWebServicesV20 service = new ApplicantWebServicesV20(sourcepoint, serviceName );
				//log.debug( "service URL: " + service.getWSDLDocumentLocation() );
				retrival_binding = service.getApplicantWebServicesSoapPort(true);
	            BindingProvider bp = ( BindingProvider ) retrival_binding;
	            bp.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,  ws_grants_gov_retrival_URL + ws_grants_gov_soapport_URL);
				bp.getRequestContext().put( JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192 ); // enables streaming
				GrantWebService.setRetrival_binding(retrival_binding);
			}
	
			if ( submission_binding == null )
			{
				String ws_grants_gov_submission_URL = Sys_Prop.getValueString(SYSTEM_PARTITION_ID,"system.ws_grants_gov_submission_URL").trim();
				//System.out.println("submission wsdl url  == " +  ws_grants_gov_submission_URL + ws_grants_gov_wsdl_URL );
				
				java.net.URL endpoint = new URL ( ws_grants_gov_submission_URL + ws_grants_gov_wsdl_URL ) ;
			 
				ApplicantWebServicesV20 service = new ApplicantWebServicesV20(endpoint, serviceName);
				//log.debug( "service URL: " + service.getWSDLDocumentLocation() );
				submission_binding = service.getApplicantWebServicesSoapPort(true); // Enable MTOM;
				BindingProvider bp = ( BindingProvider ) submission_binding;
				bp.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ws_grants_gov_submission_URL + ws_grants_gov_soapport_URL);
				bp.getRequestContext().put( JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192 );// enables streaming
				GrantWebService.setSubmission_binding(submission_binding);					
			}
        }
        catch (Exception ex)
        {        	 
			if(ex.getMessage() != null && ex.getMessage().indexOf("(404)Not Found")  != -1)
			{
				if (session!=null)
				{
					session.setAttribute("sys.ERROR",
						"Unable to initialize Grants.gov web services connection because incorrect system properties or network failure. Please contact your system administrator.");
				}
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
			else if(ex.getMessage() != null 
					&& (ex.getMessage().indexOf("java.net.SocketException: Connection reset") != -1 
								|| ex.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
			{
				if (session!=null)
				{
					session.setAttribute("sys.ERROR","Unable to initialize Grants.gov web services connection" +
							" because the Grants.gov server is down. Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator."); 
				}
			}
			else
			{
				if (session!=null)
				{
					String errorMessage = ex.getMessage();
					errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 				
					session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
						" continue to receive this message, please contact your system administrator.");
				}
			}
			System_Logger.error("GrantWebService.java", ex, ex);
			  
			ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }

		return GrantWebService;

    }

    public static ApplicantWebServicesPortType  getRetrival_binding()
	{
		return retrival_binding;
	}

	public static ApplicantWebServicesPortType  getSubmission_binding()
	{
		return submission_binding;
	}

	public void setRetrival_binding( ApplicantWebServicesPortType  retrivalBinding)
	{
		retrival_binding = retrivalBinding;
	}

	public void setSubmission_binding(ApplicantWebServicesPortType submissionBinding)
	{
		submission_binding = submissionBinding;
	}

	/** gets Opportunities posted on Grants.gov with search parameters. 
     * @param CFDA_NUMBER
     * @param OPPORTUNITY_ID
     * @param COMPETITION_ID
     * @return OpportunityInformationType Object which has schema url for opportunity & other information like competion id, cfda number, opp number, opp name, Agency information
     * @throws Exception
     */
    @Deprecated
    public List<OpportunityInfo> GetOpportunitiesList
    (
		HttpSession session,
        String CFDA_NUMBER,
        String OPPORTUNITY_ID,
        String COMPETITION_ID
    ) throws Exception
    { 
		System_Logger.debug("GrantWebService.java", " CFDA_NUMBER 	: "+CFDA_NUMBER);
		System_Logger.debug("GrantWebService.java", " OPPORTUNITY_ID : "+OPPORTUNITY_ID);
		System_Logger.debug("GrantWebService.java", " COMPETITION_ID 	: "+COMPETITION_ID);
		List<OpportunityInfo> opInfoList = null;
		try
		{
			GetOpportunitiesExpandedRequest request = new GetOpportunitiesExpandedRequest();
			if(CFDA_NUMBER != null && CFDA_NUMBER.trim().length() > 0)
			{
				request.setCFDANumber(CFDA_NUMBER);
			}
			
			if(COMPETITION_ID != null && COMPETITION_ID.trim().length() > 0)
			{
				request.setCompetitionID(COMPETITION_ID);
			}
			if(OPPORTUNITY_ID != null && OPPORTUNITY_ID.trim().length() > 0)
			{
				request.setFundingOpportunityNumber(OPPORTUNITY_ID != null ? OPPORTUNITY_ID.toUpperCase(): OPPORTUNITY_ID);
			}
			GetOpportunitiesExpandedResponse response = retrival_binding.getOpportunitiesExpanded(request);
			if(response != null)
			{
				opInfoList = response.getOpportunityInfo();			
			}
		}
		catch(ErrorMessage e)
		{ 
			if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Unable to Retrieve Opportunities because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
			else if(e.getMessage() != null && (e.getMessage().indexOf("java.net.SocketException: Connection reset") != -1 || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
			{
				if (session!=null)
				{
					session.setAttribute("sys.ERROR","Unable to Retrieve Opportunities" +
							" because the Grants.gov server is down. Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator."); 
				}
			}
			else
			{
				if (session!=null)
				{
					String errorMessage = e.getMessage();
					errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 	
					session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
						" continue to receive this message, please contact your system administrator.");
				}
			}
			System_Logger.error("GrantWebService.java", e, e);
			throw e;	
		}
		catch (Exception e) 
		{
			if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				if (session!=null)
				{
					session.setAttribute("sys.ERROR",
					"Unable to Retrieve Opportunities because incorrect system properties or network failure. Please contact your system administrator.");
					//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
				}
			}
			else if(e.getMessage() != null && (e.getMessage().indexOf("java.net.SocketException: Connection reset") != -1 || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
			{
				if (session!=null)
				{
					session.setAttribute("sys.ERROR","Unable to Retrieve Opportunities" +
							" because the Grants.gov server is down. Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator."); 
				}
			}
			else
			{
				if (session!=null)
				{
					String errorMessage = e.getMessage();
					errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 	
					session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
						" continue to receive this message, please contact your system administrator.");
				}
			}
			System_Logger.error("GrantWebService.java", e, e);
			throw e;	
		}
		return opInfoList;
    }

    /** gets Opportunities posted on Grants.gov with search parameters. 
     * @param PAKAGE_ID 
     * @param OPPORTUNITY_ID 
     * @param CFDA_NUMBER 
     * @param COMPETITION_ID
     * @return OpportunityDetailType Object which has schema url for opportunity & other information like competion id, opp number, opp name, Agency information ( removed cfda number ) this is from release 16.3
     * @throws Exception
     */
    public List<OpportunityDetails> GetOpportunityList
    (
		HttpSession session,
        String PACKAGE_ID,
        String OPPORTUNITY_ID,
        String CFDA_NUMBER,
        String COMPETITION_ID
    ) throws Exception
    { 
		System_Logger.debug("GrantWebService.java", " PACKAGE_ID 	: "+PACKAGE_ID);
		List<OpportunityDetails> opDetailList = null;
		try
		{
			GetOpportunityListRequest request = new GetOpportunityListRequest();
			if(PACKAGE_ID != null && PACKAGE_ID.trim().length() > 0)
			{
				request.setPackageID(PACKAGE_ID != null ? PACKAGE_ID.toUpperCase(): PACKAGE_ID);
			}
			else
			{
				OpportunityFilter filter = new OpportunityFilter();
				boolean add = false;
				if(OPPORTUNITY_ID != null && OPPORTUNITY_ID.trim().length() > 0)
				{
					filter.setFundingOpportunityNumber(OPPORTUNITY_ID != null ? OPPORTUNITY_ID.toUpperCase(): OPPORTUNITY_ID);
					add = true;
				}
				if(CFDA_NUMBER != null && CFDA_NUMBER.trim().length() > 0)
				{
					filter.setCFDANumber(CFDA_NUMBER);
					add = true;
				}
				if(COMPETITION_ID != null && COMPETITION_ID.trim().length() > 0)
				{
					filter.setCompetitionID(COMPETITION_ID);
				}
				
				if(add == true)
				{
					request.setOpportunityFilter(filter);
				}
			}
			GetOpportunityListResponse response = retrival_binding.getOpportunityList(request);
			if(response != null)
			{
				opDetailList = response.getOpportunityDetails();		
			}
		}
		catch(ErrorMessage e)
		{ 
			if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Unable to Retrieve Opportunities because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
			else if(e.getMessage() != null && (e.getMessage().indexOf("java.net.SocketException: Connection reset") != -1 || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
			{
				if (session!=null)
				{
					session.setAttribute("sys.ERROR","Unable to Retrieve Opportunities" +
							" because the Grants.gov server is down. Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator."); 
				}
			}
			else
			{
				if (session!=null)
				{
					String errorMessage = e.getMessage();
					errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 	
					session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
						" continue to receive this message, please contact your system administrator.");
				}
			}
			System_Logger.error("GrantWebService.java", e, e);
			throw e;	
		}
		catch (Exception e) 
		{
			if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				if (session!=null)
				{
					session.setAttribute("sys.ERROR",
					"Unable to Retrieve Opportunities because incorrect system properties or network failure. Please contact your system administrator.");
					//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
				}
			}
			else if(e.getMessage() != null && (e.getMessage().indexOf("java.net.SocketException: Connection reset") != -1 || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
			{
				if (session!=null)
				{
					session.setAttribute("sys.ERROR","Unable to Retrieve Opportunities" +
							" because the Grants.gov server is down. Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator."); 
				}
			}
			else
			{
				if (session!=null)
				{
					String errorMessage = e.getMessage();
					errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 	
					session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
						" continue to receive this message, please contact your system administrator.");
				}
			}
			System_Logger.error("GrantWebService.java", e, e);
			throw e;	
		}
		return opDetailList;
    }

    @Deprecated
    public GetApplicationStatusDetailResponse getApplicationStatusDetail
    (
		HttpSession session,
        String trackingNumber
    ) throws Exception
    {
    	GetApplicationStatusDetailResponse getApplicationStatusDetailResponse = null;
    	String errorMessage = "";
    	try
    	{
    		GetApplicationStatusDetailRequest request = new GetApplicationStatusDetailRequest();
    		request.setGrantsGovTrackingNumber(trackingNumber.trim());
    	
    		getApplicationStatusDetailResponse = submission_binding.getApplicationStatusDetail(request);
    	} 
    	catch (ErrorMessage e)
    	{
    		if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Unable to get application status details because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
			else if(e.getMessage() != null && (e.getMessage().indexOf("java.net.SocketException: Connection reset") != -1 || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
    		{
    			session.setAttribute("sys.ERROR","Grants.gov server is down.  Please try again in a few minutes.  If you continue to receive this message, please contact your system administrator."); 
    		}
			else
			{
				errorMessage = e.getMessage();
				errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 
				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator.");
			}
    		throw e;
    	}
    	catch (Exception e)
    	{

    		if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Unable to get application status details because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
			else if(e.getMessage() != null && (e.getMessage().indexOf("java.net.SocketException: Connection reset") != -1 || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
    		{
    			session.setAttribute("sys.ERROR","Grants.gov server is down.  Please try again in a few minutes.  If you continue to receive this message, please contact your system administrator."); 
    		}
			else
			{
				errorMessage = e.getMessage();
				errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 
				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator.");
			}
    		throw e;    	
    	}
    	return getApplicationStatusDetailResponse;
    }    
    
    public GetApplicationInfoResponse getApplicationInfo
    (
		HttpSession session,
        String trackingNumber
    ) throws Exception
    {
    	String errorMessage = "";
    	GetApplicationInfoResponse applicationInfoResponse  = null;
    	try
    	{
    		GetApplicationInfoRequest request = new GetApplicationInfoRequest();
    		request.setGrantsGovTrackingNumber(trackingNumber.trim());
    	
    		applicationInfoResponse  = submission_binding.getApplicationInfo(request);
    		if(applicationInfoResponse != null)
    		{
    			System_Logger.debug("GrantWebService.java", "***************Grants.gov Status Info for tracking number : \""+trackingNumber+"\" **************************");
    			System_Logger.debug("GrantWebService.java", "StatusDetail : "+applicationInfoResponse.getStatusDetail());
    			System_Logger.debug("GrantWebService.java", "AgencyNotes : "+applicationInfoResponse.getAgencyNotes());
    		}    		
    	} 
    	catch (ErrorMessage e)
    	{
    		if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Unable to get application status information because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
    		else if(e.getMessage() != null && (e.getMessage().indexOf("java.net.SocketException: Connection reset") != -1 || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
    		{
    			session.setAttribute("sys.ERROR","Unable to get application status information because the Grants.gov server is down.  Please try again in a few minutes.  If you continue to receive this message, please contact your system administrator."); 
    		}
			else
			{
				errorMessage = e.getMessage();
				errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 
				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator.");
			}
    		throw e;
    	}
    	catch (Exception e)
    	{

    		if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Unable to get application status information because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
    		else if(e.getMessage() != null && (e.getMessage().indexOf("java.net.SocketException: Connection reset") != -1 || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
    		{
    			session.setAttribute("sys.ERROR","Unable to get application status information because the Grants.gov server is down.  Please try again in a few minutes.  If you continue to receive this message, please contact your system administrator."); 
    		}
			else
			{
				errorMessage = e.getMessage();
				errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 
				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator.");
			}
    		throw e;    	
    	}
    	return applicationInfoResponse ;
    }    

    @Deprecated
    private List<ApplicationInfo> processResponse
    (
            GetApplicationListResponse response
    ) throws Exception 
    {
        List<ApplicationInfo> list = new ArrayList<ApplicationInfo>();
         
         return processResponse(response, list); 
    }

    @Deprecated
    private List<ApplicationInfo> processResponse
    (
            GetApplicationListResponse response,
            List<ApplicationInfo> list
    ) throws Exception 
    { 
    	if(list != null && response != null)
    	{
    		try
    		{
    			List<ApplicationInfo> appInfoList = response.getApplicationInfo();

    			if( appInfoList!=null )
    			{
    				list.addAll(appInfoList); 
    			}
    		}
    		catch(Exception e)
    		{
    			throw e;
    		}
    	}
         return list; 
    }
    
    private List<SubmissionDetails> processResponse
    (
    		GetSubmissionListResponse response
    ) throws Exception 
    {
        List<SubmissionDetails> list = new ArrayList<SubmissionDetails>();
         
        return processResponse(response, list); 
    }
    
    private List<SubmissionDetails> processResponse
    (
            GetSubmissionListResponse response,
            List<SubmissionDetails> list
    ) throws Exception 
    { 
    	if(list != null && response != null)
    	{
    		try
    		{
    			List<SubmissionDetails> subDetailsList = response.getSubmissionDetails();

    			if( subDetailsList != null )
    			{
    				list.addAll(subDetailsList); 
    			}
    		}
    		catch(Exception e)
    		{
    			throw e;
    		}
    	}
        
    	return list; 
    }

    @Deprecated
    public List<ApplicationInfo>  getApplicationList
	(
			HttpSession session,
	        String TrackingNumber
	) throws RemoteException , Exception 
	{
    	ArrayList<String> TrackingNumberList = new ArrayList<String>();
    	TrackingNumberList.add(TrackingNumber);
    	try
    	{
    		return getApplicationList(session, null, null, null,
    				TrackingNumberList, null);
    	}
    	finally
    	{
    		TrackingNumberList = null;
    	}
	}

    /** returns list of opportunities and its status processed by Grants.gov depending on search parameters
	 * @param CFDANumber
	 * @param Status
	 * @param OpportunityID
	 * @param TrackingNumberList
	 * @return GetApplicationListResponse Object
	 * @throws RemoteException
	 * @throws Exception
	 */
    @Deprecated
	public List<ApplicationInfo>  getApplicationList
	(
			HttpSession session,
	        String CFDANumber,
	        String Status ,
	        String OpportunityID,
	        ArrayList<String> TrackingNumberList
	) throws RemoteException , Exception 
	{
		return getApplicationList(session, CFDANumber, Status, OpportunityID,
				TrackingNumberList, null);
	}

	/** returns list of opportunities and its status processed by Grants.gov depending on search parameters
     * @param CFDANumber
     * @param Status
     * @param OpportunityID
     * @param TrackingNumberList
     * @param StatusList 
     * @return GetApplicationListResponse Object
     * @throws RemoteException
     * @throws Exception
     */
    @Deprecated
    public List<ApplicationInfo>  getApplicationList
    (
			HttpSession session,
            String CFDANumber,
            String Status ,
            String OpportunityID,
            ArrayList<String> TrackingNumberList, 
            ArrayList<String> StatusList
    ) throws RemoteException , Exception 
    {
    	
    	//submission_binding.setTimeout(120000);
    	/*System_Logger.info("GrantWebService.java", "CFDANumber --- "+CFDANumber);
    	System_Logger.info("GrantWebService.java", "Status --- "+Status);
    	System_Logger.info("GrantWebService.java", "OpportunityID --- "+OpportunityID);
    	System_Logger.info("GrantWebService.java", "TrackingNumberList --- "+TrackingNumberList);
    	System_Logger.info("GrantWebService.java", "StatusList --- "+StatusList);
    	 */
        //DecimalFormat df = new DecimalFormat("00.000");
    	String errorMessage = "";
        try
        { 
            GetApplicationListRequest appRequest = new GetApplicationListRequest(); 
            GetApplicationListResponse appResponse = null;
            
            {
                
                if(CFDANumber!=null && CFDANumber.trim().length()>0) 
                {
                	ApplicationFilter filter = new  ApplicationFilter();
                    filter.setFilter(ApplicationFilter.CFDANumber);
                    filter.setFilterValue(CFDANumber.trim());
                    appRequest.getApplicationFilter().add(filter);
                }
                
                if(Status!=null && Status.trim().length()>0) 
                {
                    ApplicationFilter filter = new  ApplicationFilter();
                    filter.setFilter(ApplicationFilter.Status);
                    filter.setFilterValue(Status.trim());
                    appRequest.getApplicationFilter().add(filter);
                }
                
                if(OpportunityID!=null && OpportunityID.trim().length()>0) 
                { 
                	ApplicationFilter filter = new  ApplicationFilter();
                    filter.setFilter(ApplicationFilter.OpportunityID);
                    filter.setFilterValue(OpportunityID.trim());
                    appRequest.getApplicationFilter().add(filter);                    
                }
                 
                if(TrackingNumberList!=null && TrackingNumberList.size() == 1)
                {
                	String TrackingNumber = (String) TrackingNumberList.get(0);
                	if(TrackingNumber!=null && TrackingNumber.length()>0) 
                    {
                    	ApplicationFilter filter = new  ApplicationFilter();
                        filter.setFilter(ApplicationFilter.GrantsGovTrackingNumber);
                        filter.setFilterValue(TrackingNumber.trim());
                        appRequest.getApplicationFilter().add(filter);    
                    }
                }
                
                if(StatusList!=null && StatusList.size()== 1)
                {
                	String Status_str = (String) StatusList.get(0);
                	if(Status_str!=null && Status_str.length()>0) 
                    {
                    	ApplicationFilter filter = new  ApplicationFilter();
                        filter.setFilter(ApplicationFilter.Status);
                        filter.setFilterValue(Status_str.trim());
                        appRequest.getApplicationFilter().add(filter);    
                    }                	
                }                
            }
            List<ApplicationInfo> appInfoList = null;

            if(appRequest != null) 
            {
            	appResponse = submission_binding.getApplicationList(appRequest);
            	appInfoList = processResponse(appResponse);
            }
            else
            {
            	appInfoList = new ArrayList<ApplicationInfo>();        	   
            }

            if(TrackingNumberList != null && TrackingNumberList.size()  > 1)
            {
            	for (Iterator<String> iterator = TrackingNumberList.iterator(); iterator.hasNext();) 
            	{
                	String TrackingNumber = (String) iterator.next();
                	if(TrackingNumber!=null && TrackingNumber.length()>0) 
                    {
                		appRequest = new GetApplicationListRequest(); 
                    	ApplicationFilter filter = new  ApplicationFilter();
                        filter.setFilter(ApplicationFilter.GrantsGovTrackingNumber);
                        filter.setFilterValue(TrackingNumber.trim());
                        appRequest.getApplicationFilter().add(filter);    
                        GetApplicationListResponse  response = submission_binding.getApplicationList(appRequest);
                        if(response != null)
                        {
                        	processResponse(response, appInfoList);
                        }
                    }
					
				}
            }
            
            if(StatusList!=null && StatusList.size() >= 1)
            {
            	for (Iterator<String> iterator = StatusList.iterator(); iterator.hasNext();) 
            	{
                	String Status_str = (String) iterator.next();
                	if(Status_str!=null && Status_str.length()>0) 
                	{
                		appRequest = new GetApplicationListRequest(); 
                    	ApplicationFilter filter = new  ApplicationFilter();
                        filter.setFilter(ApplicationFilter.Status);
                        filter.setFilterValue(Status_str.trim());
                        appRequest.getApplicationFilter().add(filter);    
                        GetApplicationListResponse  response = submission_binding.getApplicationList(appRequest);
                        if(response != null)
                        {
                        	processResponse(response, appInfoList);
                        }
                    }    					
				}
            }
         //   System_Logger.warn("GrantWebService.java", "getApplicationList List... "+appInfoList);
           
            return  appInfoList;
        }
        catch (ErrorMessage e)
        { 
     		if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Unable to get application status information because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
    		else if(e.getMessage() != null && (e.getMessage().equals("java.net.SocketException: Connection reset") || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
			{
				session.setAttribute("sys.ERROR","Unable to get application status information because the Grants.gov server is down.  Please try again in a few minutes.  If you continue to receive this message, please contact your system administrator."); 
			}
			else
			{
				errorMessage = e.getMessage();
				errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 
				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator.");
			}
			System_Logger.error("GrantWebService.java", e, e);
			throw e;
        }
        catch (Exception  e)
        {
        	if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Unable to get application status information because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
    		else if(e.getMessage() != null && (e.getMessage().equals("java.net.SocketException: Connection reset") || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
			{
				session.setAttribute("sys.ERROR","Unable to get application status information because the Grants.gov server is down.  Please try again in a few minutes.  If you continue to receive this message, please contact your system administrator."); 
			}
			else
			{
				errorMessage = e.getMessage();
				errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 
				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator.");
			}
			System_Logger.error("GrantWebService.java", e, e);
			throw e;        
        }
    }       

    @Deprecated
    public  List<ApplicationInfo> getApplicationList
    (
    		HttpSession session, 
            ArrayList<String> StatusList
    ) throws RemoteException , Exception 
    {
    	//submission_binding.setTimeout(120000);        
        try
        { 
        	List<ApplicationInfo> opList = new ArrayList<ApplicationInfo>();
            if(StatusList!=null && StatusList.size() >= 1)
            {
            	for (Iterator<String> iterator = StatusList.iterator(); iterator.hasNext();) 
            	{
                	String Status_str = (String) iterator.next();
                	if(Status_str!=null && Status_str.length()>0) 
                	{
                		GetApplicationListRequest appRequest = new GetApplicationListRequest(); 
                    	ApplicationFilter filter = new  ApplicationFilter();
                        filter.setFilter(ApplicationFilter.Status);
                        filter.setFilterValue(Status_str.trim());
                        appRequest.getApplicationFilter().add(filter);    
                        GetApplicationListResponse  response = submission_binding.getApplicationList(appRequest);
                        if(response != null && response.getApplicationInfo() != null)
                        {
                        	opList.addAll(response.getApplicationInfo());
                        }
                    }    					
				}            	
            }
            
            return  opList;
        }
        catch (ErrorMessage e1)
        {
        	System_Logger.error("GrantWebService.java", e1, e1);
            throw e1;
        }
        catch (Exception  e)
        {
        	System_Logger.error("GrantWebService.java", e, e);
            throw e;
        }
    }     
    

    public List<SubmissionDetails>  getSubmissionList
	(
			HttpSession session,
			String PACKAGE_ID,
	        String TrackingNumber
	) throws RemoteException , Exception 
	{
    	ArrayList<String> TrackingNumberList = new ArrayList<String>();
    	TrackingNumberList.add(TrackingNumber);
    	try
    	{
    		return getSubmissionList(session, PACKAGE_ID, TrackingNumberList, null);
    	}
    	finally
    	{
    		TrackingNumberList = null;
    	}
	}
    
    
    /** returns list of opportunities and its status processed by Grants.gov depending on search parameters
     * @param PACKAGE_ID
     * @param TrackingNumberList
     * @param StatusList 
     * @return SubmissionDetails Object
     * @throws RemoteException
     * @throws Exception
     */
    public List<SubmissionDetails> getSubmissionList
    (
			HttpSession session,
            String PACKAGE_ID,
            ArrayList<String> TrackingNumberList, 
            ArrayList<String> StatusList
    ) throws RemoteException , Exception 
    {
    	
    	//submission_binding.setTimeout(120000);
    	/*System_Logger.info("GrantWebService.java", "CFDANumber --- "+CFDANumber);
    	System_Logger.info("GrantWebService.java", "Status --- "+Status);
    	System_Logger.info("GrantWebService.java", "OpportunityID --- "+OpportunityID);
    	System_Logger.info("GrantWebService.java", "TrackingNumberList --- "+TrackingNumberList);
    	System_Logger.info("GrantWebService.java", "StatusList --- "+StatusList);
    	 */
        //DecimalFormat df = new DecimalFormat("00.000");
    	String errorMessage = "";
        try
        { 
            GetSubmissionListRequest subRequest = new GetSubmissionListRequest(); 
            GetSubmissionListResponse subResponse = null;
            
            {
                
                if( PACKAGE_ID != null && PACKAGE_ID.trim().length() > 0 ) 
                {  
                    SubmissionFilter filter = new SubmissionFilter();
                    filter.setType(SubmissionFilterType.PACKAGE_ID);
                    filter.setValue(PACKAGE_ID.trim());
                    subRequest.getSubmissionFilter().add(filter);
                }
                 
                if( TrackingNumberList != null && TrackingNumberList.size() > 0 )
                {
                	String TrackingNumber = (String) TrackingNumberList.get(0);
                	if( TrackingNumber != null && TrackingNumber.length() > 0 ) 
                    {
                        SubmissionFilter filter = new SubmissionFilter();
                        filter.setType(SubmissionFilterType.GRANTS_GOV_TRACKING_NUMBER);
                        filter.setValue(TrackingNumber.trim());
                        subRequest.getSubmissionFilter().add(filter);   
                    }
                }
                
                if( StatusList != null && StatusList.size() > 0 )
                {
                	String Status_str = (String) StatusList.get(0);
                	if( Status_str != null && Status_str.length() >0 ) 
                    {
                        SubmissionFilter filter = new SubmissionFilter();
                        filter.setType(SubmissionFilterType.STATUS);
                        filter.setValue(Status_str.trim());
                        subRequest.getSubmissionFilter().add(filter);   
                    }                	
                }                
            }
            List<SubmissionDetails> subDetailsList = null;

            if( subRequest != null ) 
            {
            	subResponse = submission_binding.getSubmissionList(subRequest);
            	subDetailsList = processResponse(subResponse);
            }

            if(TrackingNumberList != null && TrackingNumberList.size()  > 1)
            {
            	for (Iterator<String> iterator = TrackingNumberList.iterator(); iterator.hasNext();) 
            	{
                	String TrackingNumber = (String) iterator.next();
                	if( TrackingNumber != null && TrackingNumber.length() > 0 ) 
                    {
                		subRequest = new GetSubmissionListRequest(); 
                        SubmissionFilter filter = new SubmissionFilter();
                        filter.setType(SubmissionFilterType.GRANTS_GOV_TRACKING_NUMBER);
                        filter.setValue(TrackingNumber.trim());
                        subRequest.getSubmissionFilter().add(filter);   
                        GetSubmissionListResponse response = submission_binding.getSubmissionList(subRequest);
                        if(response != null)
                        {
                        	processResponse(response, subDetailsList);
                        }
                    }
					
				}
            }
            
            if(StatusList!=null && StatusList.size() >= 1)
            {
            	for (Iterator<String> iterator = StatusList.iterator(); iterator.hasNext();) 
            	{
                	String Status_str = (String) iterator.next();
                	if(Status_str!=null && Status_str.length()>0) 
                	{
                		subRequest = new GetSubmissionListRequest(); 
                        SubmissionFilter filter = new SubmissionFilter();
                        filter.setType(SubmissionFilterType.STATUS);
                        filter.setValue(Status_str.trim());
                        subRequest.getSubmissionFilter().add(filter);     
                        GetSubmissionListResponse response = submission_binding.getSubmissionList(subRequest);
                        if(response != null)
                        {
                        	processResponse(response, subDetailsList);
                        }
                    }    					
				}
            }
         //   System_Logger.warn("GrantWebService.java", "getApplicationList List... "+appInfoList);
           
            return  subDetailsList;
        }
        catch (ErrorMessage e)
        { 
     		if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Unable to get submission status information because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
    		else if(e.getMessage() != null && (e.getMessage().equals("java.net.SocketException: Connection reset") || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
			{
				session.setAttribute("sys.ERROR","Unable to get submission status information because the Grants.gov server is down.  Please try again in a few minutes.  If you continue to receive this message, please contact your system administrator."); 
			}
			else
			{
				errorMessage = e.getMessage();
				errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 
				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator.");
			}
			System_Logger.error("GrantWebService.java", e, e);
			throw e;
        }
        catch (Exception  e)
        {
        	if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Unable to get submission status information because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
    		else if(e.getMessage() != null && (e.getMessage().equals("java.net.SocketException: Connection reset") || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
			{
				session.setAttribute("sys.ERROR","Unable to get submission status information because the Grants.gov server is down.  Please try again in a few minutes.  If you continue to receive this message, please contact your system administrator."); 
			}
			else
			{
				errorMessage = e.getMessage();
				errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 
				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator.");
			}
			System_Logger.error("GrantWebService.java", e, e);
			throw e;        
        }
    }        
	
    /** Prepared Grant Package XML is submitted to Grants.gov  , adds Attachment Objects to  Grants.gov Submission XML
     * @param attachments
     * @param xml
     * @return
     * @throws Exception
     */
    public SubmitApplicationResponse submitApplication
    (
		HttpSession 							session,
        HashMap<Integer, Project_Attachment>    attachments,
        String         							xml
    ) throws Exception
    {
		SubmitApplicationRequest submitRequest = new SubmitApplicationRequest();
		SubmitApplicationResponse submitResponse = null;
		/*long submissionSize = 0; 
		try
		{
			String grantXmlPath = session.getAttribute("SYS_ROOT_DIR")+ "Documents" + File.separator + "User" + session.getAttribute("Sys.User_ID") + File.separator ;
			File grantXmlDir = new File(grantXmlPath);
			if(grantXmlDir != null && !grantXmlDir.exists())
			{
				grantXmlDir.mkdirs();
			}
			
			Utilities.WriteFile( xml, grantXmlPath +"grant.xml" );
			File grantXml = new File(grantXmlPath);
			if(grantXml != null && grantXml.exists())
			{
				submissionSize = grantXml.length();
			}			
		} 
		catch (Exception e1)
		{
			 e1.printStackTrace();
		}
		 */
		String errorMessage = "";
		try
		{
			//submissionSize += 
			addAttachments(attachments, submitRequest );
			submitRequest.setGrantApplicationXML(xml);
			
//			System_Logger.info("GrantWebService.java", " SubmissionSize for Grant Application : "+submissionSize);
//			System_Logger.info("GrantWebService.java", " SubmissionSize for Grant Application in MB : "+(double)submissionSize/(1024*1024));
			submitResponse = submission_binding.submitApplication(submitRequest);
		} 
		catch (ErrorMessage e)
		{
			if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Your proposal has not been submitted to Grants.gov because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
			else if(e.getMessage() != null && (e.getMessage().equals("java.net.SocketException: Connection reset") || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
			{
				session.setAttribute("sys.ERROR","Your proposal has not been submitted to Grants.gov because the Grants.gov server is down. Please try again in a few minutes. If you continue to receive this message, please contact your system administrator."); 
			}
			else
			{
				errorMessage = e.getMessage();
				errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 
				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator.");
			}
			throw e;
		}
		catch (Exception e)
		{
			
			if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
			{
				session.setAttribute("sys.ERROR",
				"Your proposal has not been submitted to Grants.gov because incorrect system properties or network failure. Please contact your system administrator.");
				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
			}
			else if(e.getMessage() != null && (e.getMessage().equals("java.net.SocketException: Connection reset") || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
			{
				session.setAttribute("sys.ERROR","Your proposal has not been submitted to Grants.gov because the Grants.gov server is down. Please try again in a few minutes. If you continue to receive this message, please contact your system administrator."); 
			}
			else
			{
				errorMessage = e.getMessage();

				//errorMessage = "javax.xml.ws.soap.SOAPFaultException: Unable to accept application submission: Temporary Directory for storing submissions at \"/Products_Grants_gov/conf/ReceiptTempDirectory/GRANT00571088\" does not exist.::Temporary Directory for storing sumissions at \"/Products_Grants_gov/conf/ReceiptTempDirectory/GRANT00571088\" does not exist.";

				errorMessage = Utilities.encodeQuotes(errorMessage);

				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
					" continue to receive this message, please contact your system administrator.");
			}
			throw e;
		}
		return submitResponse;
    }
    
    private long addAttachments
    (
        HashMap<Integer,Project_Attachment> attachments,
        SubmitApplicationRequest  submitRequest
    ) throws Exception
	{
		if (attachments == null || attachments.isEmpty()) {
			//no attachments to process
			return 0L;
		}
		long attachSize = 0L;
		Iterator<Project_Attachment> filesToAttach = attachments.values().iterator();
		while (filesToAttach.hasNext())
		{
			Project_Attachment attachment = (Project_Attachment) filesToAttach.next();
			attach(submitRequest, attachment);
			attachSize += attachment.getAttachSize();
		}
		return attachSize;
	}


    private void attach
    (
        SubmitApplicationRequest submitRequest,
		Project_Attachment attachBean
    ) throws Exception
	{
		/*AttachmentPart attachmentPart = new AttachmentPart();
		DataHandler attachmentFile = new DataHandler(attachBean.getByteArrayDS());

		 
		stub.(Call.ATTACHMENT_ENCAPSULATION_FORMAT, ENCODING_TYPE);
		attachmentPart.setDataHandler(attachmentFile);
		
		attachmentPart.setContentId(attachBean.getCONTENT_ID());
		attachmentPart.setContentType("application/octet-stream");
		stub.addAttachment(attachmentPart);
		*/
		Attachment attachment = new Attachment();
		// Create Data Handler for each file.
		attachment.setFileDataHandler(new DataHandler(attachBean.getByteArrayDS()));
		// Assign the CID
		attachment.setFileContentId(attachBean.getCONTENT_ID());
		
		// Add the Attachment to the List of Attachments to be streamed.
		submitRequest.getAttachment().add( attachment);
	}
    
    public static  void Reset_Bindings
    (
        System_Properties Sys_Prop,
        int	SYSTEM_PARTITION_ID
    )
    {
    	retrival_binding 	= null;
    	submission_binding 	= null;   
    	GrantWebService 	= null;
    }
    
   public File GetApplicationZip
   (
	   HttpSession 							session,
	   String TRACKING_NUMBER,
	   String submissionZipPath
   	) throws Exception
   {
 	   GetApplicationZipRequest request = new GetApplicationZipRequest();
 	   request.setGrantsGovTrackingNumber(TRACKING_NUMBER);
 	   
 	  
 	   
 	   String errorMessage = "";
 		try
 		{
 			GetApplicationZipResponse  response =  submission_binding.getApplicationZip(request);
 			if(response != null )
 			{
 				DataHandler  datahandler = response.getFileDataHandler();
 				if(datahandler != null && submissionZipPath != null)
 				{			
 					File f = new File( submissionZipPath);
 					com.sun.xml.internal.ws.developer.StreamingDataHandler dh = null;
 					try
 					{
 						dh = (StreamingDataHandler) ( datahandler );

 						dh.moveTo( f );
 					}
 					finally
 					{
 						dh.close();
 					}
 				}
 			}
 		} 
 		catch (ErrorMessage e)
 		{
 			if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
 			{
 				session.setAttribute("sys.ERROR",
 				"Your proposal has not been submitted to Grants.gov because incorrect system properties or network failure. Please contact your system administrator.");
 				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
 			}
 			else if(e.getMessage() != null && (e.getMessage().equals("java.net.SocketException: Connection reset") || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
 			{
 				session.setAttribute("sys.ERROR","Not able to download application zip because the Grants.gov server is down. Please try again in a few minutes. If you continue to receive this message, please contact your system administrator."); 
 			}
 			else
 			{
 				errorMessage = e.getMessage();
 				if(errorMessage != null)
 				{
 					errorMessage = errorMessage.replaceAll("\"", "\\\"").trim(); 
 				}
 				else
 				{
 					errorMessage = e.toString();
 				}
 				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
 					" continue to receive this message, please contact your system administrator.");
 			}
 			throw e;
 		}
 		catch (Exception e)
 		{
 			
 			if(e.getMessage() != null && e.getMessage().indexOf("(404)Not Found")  != -1)
 			{
 				session.setAttribute("sys.ERROR",
 				"Not able to download application zip because incorrect system properties or network failure. Please contact your system administrator.");
 				//"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.&lt;/p&gt;&lt;p&gt;If the server does not wish to make this information available to the client, the status code 403 (Forbidden) can be used instead. The 410 (Gone) status code SHOULD be used if the server knows, through some internally configurable mechanism, that an old resource is permanently unavailable and has no forwarding address.";
 			}
 			else if(e.getMessage() != null && (e.getMessage().equals("java.net.SocketException: Connection reset") || e.getMessage().indexOf("java.net.ConnectException: Connection timed out: connect") != -1))
 			{
 				session.setAttribute("sys.ERROR","Your proposal has not been submitted to Grants.gov because the Grants.gov server is down. Please try again in a few minutes. If you continue to receive this message, please contact your system administrator."); 
 			}
 			else
 			{
 				errorMessage = e.getMessage();

 				//errorMessage = "javax.xml.ws.soap.SOAPFaultException: Unable to accept application submission: Temporary Directory for storing submissions at \"/Products_Grants_gov/conf/ReceiptTempDirectory/GRANT00571088\" does not exist.::Temporary Directory for storing sumissions at \"/Products_Grants_gov/conf/ReceiptTempDirectory/GRANT00571088\" does not exist.";
 				if(errorMessage != null)
 				{
 					errorMessage = Utilities.encodeQuotes(errorMessage);
 				}

 				session.setAttribute("sys.ERROR",errorMessage+". Please try again in a few minutes. If you" +
 																	" continue to receive this message, please contact your system administrator.");
 			}
 			throw e;
 		}
 	   return null;
 	   
    }
}//end class

