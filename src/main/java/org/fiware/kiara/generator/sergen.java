/* KIARA - Middleware for efficient and QoS/Security-aware invocation of services and exchange of messages
 *
 * Copyright (C) 2015 German Research Center for Artificial Intelligence (DFKI)
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
package org.fiware.kiara.generator;

import com.eprosima.log.ColorMessage;
import org.antlr.stringtemplate.*;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

/**
 * @author Dmitri Rubinstein {@literal <dmitri.rubinstein@dfki.de>}
 */
public class sergen {

    private static class TemplateErrorListener implements StringTemplateErrorListener {

        @Override
        public void error(String arg0, Throwable arg1) {
            System.out.println(ColorMessage.error() + arg0);
            arg1.printStackTrace();
        }

        @Override
        public void warning(String arg0) {
            System.out.println(ColorMessage.warning() + arg0);
        }
    }

    public static void main(String[] args) {

        StringTemplateGroupLoader loader = new CommonGroupLoader("org/fiware/kiara/generator/idl/templates", new TemplateErrorListener());
        StringTemplateGroup.registerGroupLoader(loader);

        StringTemplateGroup group = null;

        //group = StringTemplateGroup.loadGroup("Common", DefaultTemplateLexer.class, group);
        //group = StringTemplateGroup.loadGroup("JavaTypes", DefaultTemplateLexer.class, group);
        group = StringTemplateGroup.loadGroup("KIARASerialization", DefaultTemplateLexer.class, group);

        StringTemplate st = group.getInstanceOf("main");

        st.setAttribute("idlTypes", new String[]{
            "float32", "float64",
            "char", "byte",
            "i16", "ui16",
            "i32", "ui32",
            "i64", "ui64",
            "string", "boolean"});
        String result = st.toString();
        System.err.println(result);
    }
}
