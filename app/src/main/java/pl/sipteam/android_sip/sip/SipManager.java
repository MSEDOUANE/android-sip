package pl.sipteam.android_sip.sip;

import android.javax.sip.DialogTerminatedEvent;
import android.javax.sip.IOExceptionEvent;
import android.javax.sip.InvalidArgumentException;
import android.javax.sip.ListeningPoint;
import android.javax.sip.ObjectInUseException;
import android.javax.sip.PeerUnavailableException;
import android.javax.sip.RequestEvent;
import android.javax.sip.ResponseEvent;
import android.javax.sip.ServerTransaction;
import android.javax.sip.SipException;
import android.javax.sip.SipFactory;
import android.javax.sip.SipListener;
import android.javax.sip.SipProvider;
import android.javax.sip.SipStack;
import android.javax.sip.TimeoutEvent;
import android.javax.sip.TransactionTerminatedEvent;
import android.javax.sip.TransportNotSupportedException;
import android.javax.sip.address.Address;
import android.javax.sip.address.AddressFactory;
import android.javax.sip.address.SipURI;
import android.javax.sip.header.CSeqHeader;
import android.javax.sip.header.CallIdHeader;
import android.javax.sip.header.ContactHeader;
import android.javax.sip.header.ContentTypeHeader;
import android.javax.sip.header.FromHeader;
import android.javax.sip.header.HeaderFactory;
import android.javax.sip.header.MaxForwardsHeader;
import android.javax.sip.header.ToHeader;
import android.javax.sip.header.ViaHeader;
import android.javax.sip.message.MessageFactory;
import android.javax.sip.message.Request;
import android.javax.sip.message.Response;

import org.joda.time.DateTime;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.TooManyListenersException;

import de.greenrobot.event.EventBus;
import pl.sipteam.android_sip.event.SipErrorEvent;
import pl.sipteam.android_sip.event.SipMessageEvent;
import pl.sipteam.android_sip.event.SipStatusEvent;
import pl.sipteam.android_sip.model.SipMessageItem;
import pl.sipteam.android_sip.model.SipMessageType;
import pl.sipteam.android_sip.utils.CustomLogger;

public class SipManager implements SipListener {

    private static SipManager instance = null;

    private EventBus bus;

    private String username;

    private SipStack sipStack;

    private SipFactory sipFactory;

    private AddressFactory addressFactory;

    private HeaderFactory headerFactory;

    private MessageFactory messageFactory;

    private SipProvider sipProvider;

    private String ip;

    public static SipManager getInstance(String username, int port) throws PeerUnavailableException, TransportNotSupportedException,
            InvalidArgumentException, ObjectInUseException, TooManyListenersException {

        if (instance == null) {
            instance = new SipManager(username, port);
        }
        return instance;
    }

    /** Here we initialize the SIP stack. */
    private SipManager(String username, int port) throws PeerUnavailableException, TransportNotSupportedException,
                                                                    InvalidArgumentException, ObjectInUseException, TooManyListenersException {
        bus = EventBus.getDefault();
        ip = getLocalIpAddress(true);
        setUsername(username);
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("android.gov.nist");
        Properties properties = new Properties();
        properties.put("android.gov.nist.javax.sip.STACK_LOGGER", CustomLogger.class.getName());
        properties.setProperty("android.javax.sip.STACK_NAME", "android_sip");
        properties.setProperty("android.javax.sip.IP_ADDRESS", ip);

        //DEBUGGING: Information will go to files
        //textclient.log and textclientdebug.log
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "textclient.txt");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "textclientdebug.log");

        sipStack = sipFactory.createSipStack(properties);
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();

        ListeningPoint tcp = sipStack.createListeningPoint(ip, port, "tcp");
        ListeningPoint udp = sipStack.createListeningPoint(ip, port, "udp");

        sipProvider = sipStack.createSipProvider(tcp);
        sipProvider.addSipListener(this);
        sipProvider = sipStack.createSipProvider(udp);
        sipProvider.addSipListener(this);
    }

    /**
     * This method uses the SIP stack to send a message.
     */
    public void sendMessage(String to, String message) throws ParseException, InvalidArgumentException, SipException {

        SipURI from = addressFactory.createSipURI(getUsername(), getHost()
                + ":" + getPort());
        Address fromNameAddress = addressFactory.createAddress(from);
        fromNameAddress.setDisplayName(getUsername());
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
                "textclientv1.0");

        try {
            String username = to.substring(to.indexOf(":") + 1, to.indexOf("@"));
            String address = to.substring(to.indexOf("@") + 1);

            SipURI toAddress = addressFactory.createSipURI(username, address);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(username);
            ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

            SipURI requestURI = addressFactory.createSipURI(username, address);
            requestURI.setTransportParam("udp");

            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = headerFactory.createViaHeader(getHost(),
                    getPort(), "udp", "branch1");
            viaHeaders.add(viaHeader);

            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.MESSAGE);

            MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

            Request request = messageFactory.createRequest(requestURI,
                    Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);

            SipURI contactURI = addressFactory.createSipURI(getUsername(), getHost());
            contactURI.setPort(getPort());
            Address contactAddress = addressFactory.createAddress(contactURI);
            contactAddress.setDisplayName(getUsername());
            ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
            request.setContent(message, contentTypeHeader);

            sipProvider.sendRequest(request);
            SipMessageItem messageItem = new SipMessageItem(getUsername(),
                    getLocalIpAddress(true),
                    message,
                    new DateTime().getMillis(),
                    SipMessageType.OUTCOMING_MESSAGE);
            bus.post(new SipMessageEvent(messageItem));
        } catch (Exception e) {
            String err;
            if (e instanceof IndexOutOfBoundsException) {
                err = "Wrong address";
            } else {
                err = e.getMessage();
            }
            bus.post(new SipErrorEvent(err));
        }

    }

    /** This method is called by the SIP stack when a response arrives. */
    public void processResponse(ResponseEvent evt) {
        Response response = evt.getResponse();
        int status = response.getStatusCode();

        if ((status >= 200) && (status < 300)) { //Success!
            bus.post(new SipStatusEvent("Sent"));
            return;
        }

        bus.post(new SipErrorEvent("Previous message not sent: " + status));
    }

    /**
     * This method is called by the SIP stack when a new request arrives.
     */
    public void processRequest(RequestEvent evt) {
        Request req = evt.getRequest();

        String method = req.getMethod();
        if (!method.equals("MESSAGE")) { //bad request type.
            bus.post(new SipErrorEvent("Bad request type: " + method));
            return;
        }

        FromHeader from = (FromHeader) req.getHeader("From");
        String address = from.getAddress().toString();
        String name = "Anonymous";
        try {
            name = address.substring(1, address.indexOf("<") - 2);
        } catch (IndexOutOfBoundsException e) {}

        SipMessageItem messageItem = new SipMessageItem(
                name,
                address,
                new String(req.getRawContent()),
                new DateTime().getMillis(),
                SipMessageType.INCOMING_MESSAGE
        );
        bus.post(new SipMessageEvent(messageItem));
        Response response = null;
        try { //Reply with OK
            response = messageFactory.createResponse(200, req);
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            toHeader.setTag("888"); //This is mandatory as per the spec.
            ServerTransaction st = sipProvider.getNewServerTransaction(req);
            st.sendResponse(response);
        } catch (Throwable e) {
            e.printStackTrace();
            bus.post(new SipErrorEvent("Can't send OK reply."));
        }
    }

    /**
     * This method is called by the SIP stack when there's no answer
     * to a message. Note that this is treated differently from an error
     * message.
     */
    public void processTimeout(TimeoutEvent evt) {
        bus.post(new SipErrorEvent("Previous message not sent: " + "timeout"));
    }

    /**
     * This method is called by the SIP stack when there's an asynchronous
     * message transmission error.
     */
    public void processIOException(IOExceptionEvent evt) {
        bus.post(new SipErrorEvent("Previous message not sent: " + "I/O Exception"));
    }

    /**
     * This method is called by the SIP stack when a dialog (session) ends.
     */
    public void processDialogTerminated(DialogTerminatedEvent evt) {
    }

    /**
     * This method is called by the SIP stack when a transaction ends.
     */
    public void processTransactionTerminated(TransactionTerminatedEvent evt) {
    }

    public String getHost() {
        int port = sipProvider.getListeningPoint().getPort();
        String host = sipStack.getIPAddress();
        return host;
    }

    public int getPort() {
        int port = sipProvider.getListeningPoint().getPort();
        return port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String newUsername) {
        username = newUsername;
    }

    public String getLocalIpAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
}
