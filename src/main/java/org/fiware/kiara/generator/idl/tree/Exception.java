package org.fiware.kiara.generator.idl.tree;

import java.security.MessageDigest;

import com.eprosima.idl.parser.typecode.StringTypeCode;
import com.eprosima.idl.parser.typecode.TypeCode;

public class Exception extends com.eprosima.idl.parser.tree.Exception {
	
	private StringTypeCode name_tc = null;
	private String exception_name = null;
	
	public Exception(String scopeFile, boolean isInScope, String scope, String name) {
		super(scopeFile, isInScope, scope, name);
		this.name_tc = new StringTypeCode(TypeCode.KIND_STRING, String.valueOf(name.length()));
		this.exception_name = name;
	}
	
	public String getMd5() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            if(md != null) {
                byte[] md5 = md.digest(getScopedname().getBytes());
                int length = md5.length;
                return String.format("0x%02X%02X%02X%02X", md5[length - 4], md5[length - 3], md5[length - 2], md5[length - 1]);
            }
        } catch(java.lang.Exception ex) {
            System.out.println("ERROR<Operation::getMd5>: " + ex.getMessage());
        }
        
        return null;
    }
	
	public TypeCode getTypecode() {
		return this.name_tc;
	}
	
	/*public int getMaxsize() {
		return this.exception_name.length();
	}
	
	public TypeCode getStType() {
		return this.name_tc;
	}
	
	public boolean isPrimitive() {
        return true;
    }
		
	public boolean isIsType_d(){
		return this.name_tc.isIsType_d();
	}
	
	public int getSize() {
		return this.exception_name.length();
	}*/
		

}
