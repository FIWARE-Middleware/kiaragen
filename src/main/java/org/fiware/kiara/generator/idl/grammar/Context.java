/* KIARA - Middleware for efficient and QoS/Security-aware invocation of services and exchange of messages
 *
 * Copyright (C) 2014 Proyectos y Sistemas de Mantenimiento S.L. (eProsima)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fiware.kiara.generator.idl.grammar;

import java.util.ArrayList;
import java.util.Stack;

import com.eprosima.idl.parser.tree.Definition;
import com.eprosima.idl.parser.tree.Interface;
import com.eprosima.idl.parser.typecode.EnumTypeCode;
import com.eprosima.idl.parser.typecode.StructTypeCode;
import com.eprosima.idl.parser.typecode.TypeCode;
import com.eprosima.idl.parser.typecode.UnionMember;
import com.eprosima.idl.parser.typecode.UnionTypeCode;

/**
*
* @author Rafael Lara <rafaellara@eprosima.com>
*/
public class Context extends com.eprosima.idl.context.Context
{
    // TODO Remove middleware parameter. It is temporal while cdr and rest don't have async functions.
    public Context(String filename, String file, ArrayList includePaths, boolean subscribercode, boolean publishercode,
            String appProduct, String pck)
    {
        super(filename, file, includePaths);
        
        
        m_subscribercode = subscribercode;
        m_publishercode = publishercode;
        m_randomGenNames = new Stack<String>();

        // TODO Remove
        m_appProduct = appProduct;
        m_package = pck;
    }
    
    public void setTypelimitation(String lt)
    {
        m_typelimitation = lt;
    }

    public String getTypelimitation()
    {
        return m_typelimitation;
    }
    
    /*!
     * @brief This function adds a global typecode to the context.
     */
    public void addTypeCode(String name, TypeCode typecode)
    {
        super.addTypeCode(name, typecode);
        
        // TODO: Exception.
        if (m_firstStructure == null && typecode.getKind() == TypeCode.KIND_STRUCT && isInScopedFile()) {
        	m_firstStructure = name;
        }
    }

    public boolean isClient()
    {
        return m_subscribercode;
    }
    
    public boolean isServer()
    {
        return m_publishercode;
    }

    // TODO Remove
    public String getProduct()
    {
        return m_appProduct;
    }
    
    public boolean isAnyCdr()
    {
        return true;
    }

    public boolean isCdr()
    {
        return true;
    }
    public boolean isKey()
    {
    	return true;
    }
    public boolean isFastrpcProduct()
    {
        return false;
    }

    public boolean isDds()
    {
        return false;
    }
    
    // TODO Para stringtemplate TopicsPlugin de nuestros tipos DDS.
    public String getNewRandomName()
    {
        String name = "type_" + ++m_randomGenName;
        m_randomGenNames.push(name);
        return name;
    }

    public String getNewLoopVarName()
    {
        m_loopVarName = 'a';
        return Character.toString(m_loopVarName);
    }

    public String getNextLoopVarName()
    {
        return Character.toString(++m_loopVarName);
    }

    // TODO Para stringtemplate TopicsPlugin de nuestros tipos DDS.
    public String getLastRandomName()
    {
        return m_randomGenNames.pop();
    }

    private String m_typelimitation = null;
    
    //! Cache the first interface.
    private Interface m_firstinterface = null;
    //! Cache the first exception.
    private com.eprosima.idl.parser.tree.Exception m_firstexception = null;

    // TODO Lleva la cuenta de generaci�n de nuevos nombres.
    private int m_randomGenName = 0;
    private Stack<String> m_randomGenNames = null;
    // TODO Lleva la cuenta del nombre de variables para bucles anidados.
    private char m_loopVarName = 'a';
    
    // Stores if the user will generate the client source.
    private boolean m_subscribercode = true;
    // Stores if the user will generate the server source.
    private boolean m_publishercode = true;

    private String m_appProduct = null;
    
    private String m_firstStructure = null;
    
    private Interface m_current_ifz = null;
    
    private StructTypeCode m_current_st = null;
    
    private UnionTypeCode m_current_union = null;
    private UnionMember m_current_union_member = null;
    private int m_current_union_member_index = 0;
    private EnumTypeCode m_current_enum = null;
    
    
    private String m_package;

	public String getM_firstStructure() {
		return m_firstStructure;
	}

	public void setM_firstStructure(String m_firstStructure) {
		this.m_firstStructure = m_firstStructure;
	}

	public Interface getCurrentIfz() {
		return m_current_ifz;
	}

	public void setCurrentIfz(Interface m_current_ifz) {
		this.m_current_ifz = m_current_ifz;
	}

	public StructTypeCode getCurrentSt() {
		return m_current_st;
	}

	public void setCurrentSt(StructTypeCode m_current_st) {
		this.m_current_st = m_current_st;
	}
	
	public void setCurrentUnion(UnionTypeCode m_current_union) {
		this.m_current_union = m_current_union;
	}
	
	public UnionTypeCode getCurrentUnion() {
		return this.m_current_union;
	}
	
	public void setCurrentUnionMember(UnionMember m_current_union_member) {
		this.m_current_union_member = m_current_union_member;
	}
	
	public UnionMember getCurrentUnionMember() {
		return this.m_current_union_member;
	}
	
	public int getCurrentUnionMemberIndex() {
		return this.m_current_union_member_index;
	}
	
	public EnumTypeCode getCurrentEnum() {
		return this.m_current_enum;
	}
	
	public void setCurrentEnum(EnumTypeCode enumTc) {
		this.m_current_enum = enumTc;
	}
	
	public String getJavaPackage() {
		return this.m_package;
	}
	
}
