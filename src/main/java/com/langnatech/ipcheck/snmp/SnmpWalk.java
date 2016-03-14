package com.langnatech.ipcheck.snmp;

import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpWalk {

	public static final int DEFAULT_VERSION = SnmpConstants.version2c;
	public static final String DEFAULT_PROTOCOL = "udp";
	public static final int DEFAULT_PORT = 161;
	public static final long DEFAULT_TIMEOUT = 3 * 1000L;
	public static final int DEFAULT_RETRY = 3;

	/**
	 * 创建对象communityTarget
	 *
	 * @param targetAddress
	 * @param community
	 * @param version
	 * @param timeOut
	 * @param retry
	 * @return CommunityTarget
	 */
	public static CommunityTarget createDefault(String ip, String community) {
		Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + ip
				+ "/" + DEFAULT_PORT);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		target.setAddress(address);
		target.setVersion(DEFAULT_VERSION);
		target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
		target.setRetries(DEFAULT_RETRY);
		return target;
	}

	/**
	 * @param ip
	 * @param community
	 * @param oid
	 */
	@SuppressWarnings("unchecked")
	public static void snmpWalk(String ip, String community, String targetOid) {

		CommunityTarget target = SnmpWalk.createDefault(ip, community);
		@SuppressWarnings("rawtypes")
		TransportMapping transport = null;
		Snmp snmp = null;
		try {
			transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(transport);
			transport.listen();

			PDU pdu = new PDU();
			OID targetOID = new OID(targetOid);
			pdu.add(new VariableBinding(targetOID));

			boolean finished = false;
			System.out.println("----&gt; demo start &lt;----");
			while (!finished) {
				VariableBinding vb = null;
				ResponseEvent respEvent = snmp.getNext(pdu, target);
				PDU response = respEvent.getResponse();
				if (null == response) {
					finished = true;
					break;
				} else {
					vb = response.get(0);
				}
				finished = checkWalkFinished(targetOID, pdu, vb);
				if (!finished) {
					System.out.println(vb.getOid() + " = " + vb.getVariable());
					pdu.setRequestID(new Integer32(0));
					pdu.set(0, vb);
				} else {
					System.out.println("SNMP walk OID has finished.");
					snmp.close();
				}
			}
			System.out.println("----&gt; demo end &lt;----");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SNMP walk Exception: " + e);
		} finally {
			if (snmp != null) {
				try {
					snmp.close();
				} catch (IOException ex1) {
					snmp = null;
				}
			}
		}

	}

	/**
	 * 1)responsePDU == null&lt;br&gt;
	 * 2)responsePDU.getErrorStatus() != 0&lt;br&gt;
	 * 3)responsePDU.get(0).getOid() == null&lt;br&gt;
	 * 4)responsePDU.get(0).getOid().size() &lt; targetOID.size()&lt;br&gt;
	 * 5)targetOID.leftMostCompare(targetOID.size(),responsePDU.get(0).getOid())
	 * !=0&lt;br&gt;
	 * 6)Null.isExceptionSyntax(responsePDU.get(0).getVariable().getSyntax())&lt;br&gt;
	 * 7)responsePDU.get(0).getOid().compareTo(targetOID) &lt;= 0&lt;br&gt;
	 *
	 * @param resquestPDU
	 * @param targetOID
	 * @param responsePDU
	 * @param vb
	 * @return
	 */
	private static boolean checkWalkFinished(OID targetOID, PDU pdu,
			VariableBinding vb) {
		boolean finished = false;
		if (pdu.getErrorStatus() != 0) {
			System.out.println("[true] responsePDU.getErrorStatus() != 0 ");
			System.out.println(pdu.getErrorStatusText());
			finished = true;
		} else if (vb.getOid() == null) {
			System.out.println("[true] vb.getOid() == null");
			finished = true;
		} else if (vb.getOid().size() <targetOID.size()) {
			System.out.println("[true] vb.getOid().size() &lt; targetOID.size()");
			finished = true;
		} else if (targetOID.leftMostCompare(targetOID.size(), vb.getOid()) != 0) {
			System.out.println("[true] targetOID.leftMostCompare() != 0");
			finished = true;
		} else if (Null.isExceptionSyntax(vb.getVariable().getSyntax())) {
			System.out
					.println("[true] Null.isExceptionSyntax(vb.getVariable().getSyntax())");
			finished = true;
		} else if (vb.getOid().compareTo(targetOID) <= 0) {
			System.out.println("[true] Variable received is not "
					+ "lexicographic successor of requested " + "one:");
			System.out.println(vb.toString() + " &lt;= " + targetOID);
			finished = true;
		}
		return finished;

	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		String ip = "192.168.8.254";
		String community = "public";
		// 1.3.6.1.2.1.2.2.1.2
		String targetOid = ".1.3.6.1.2.1.1";
		SnmpWalk.snmpWalk(ip, community, targetOid);

	}

}