Author: Robin Zhang Tao

License: Apache License 2.0
http://www.apache.org/licenses/LICENSE-2.0.html

Build:
 Please run: ant build
 The distional jar file is: dist/dtd2xsd.jar

Rquired libs:
 xercesImpl.jar xml-apis.jar log4j-1.2.13.jar

Usage:
  java com.skywing.dtd2xsd.DTD2XSDWriter <-n tartget_namespace> <-t xsd_type=regex_pattern> dtd_file <xsd_file>
  
Examples:
1.PAP
 java com.skywing.dtd2xsd.DTD2XSDWriter -t dateTime=.*time.* pap_dtd/pap_1.0.dtd pap_dtd/pap_1.0.xsd

2.MLP
 java com.skywing.dtd2xsd.DTD2XSDWriter mlp_dtd/MLP_ALL.DTD mlp_dtd/MLP_ALL.xsd

