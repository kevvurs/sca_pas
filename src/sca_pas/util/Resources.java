package sca_pas.util;

public class Resources {
	public static final String pageLink = "http://sca-corp.com/pasassessments/index.cfm?id=7&janowCentral=bbb.results&page=";
	public static final String entryLink = "http://sca-corp.com/pasassessments/index.cfm?id=7&janowCentral=ccc.parcel&parcel=";
	public static final String requestTemplate = "parcelID=";
	public static final int pageLimit = 280;
	public static final String token = "href";
	public static final String flag1 = "textStyle3";
	public static final String flag2 = "textStyle4";
	public static final String utfcrap = "&nbsp;";
	public static final String formLink = "http://sca-corp.com/pasassessments/index.cfm?id=7&janowCentral=bbb.results";
	public static final String[] universalTemplate = { "http://sca-corp.com/pasassessments/index.cfm?id=7&janowCentral=bbb.results&page=",
			"&ownername=&muniNumber=all&legalAddress=&ParcelID=0&SBLSection=&SBLSubSec=&SBLBlock=&SBLLot=&SBLSubLot=&SBLSuffix="};
	public static final String[] targets = {"ParcelID", "Market Value","Type","Name","Address"};
	@Deprecated
	public static final char[] decNums = {'0','1','2','3','4','5','6','7','8','9'};
}
